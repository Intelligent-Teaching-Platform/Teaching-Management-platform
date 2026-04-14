package com.example.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface DashboardMapper {

    // ---------- Admin (mainscreen) ----------
    @Select("select count(*) from course")
    Integer countCourses();

    @Select("select count(*) from teacher")
    Integer countTeachers();

    @Select("select count(*) from student")
    Integer countStudents();

    @Select("select count(*) from clazz")
    Integer countClazz();

    @Select("""
            select ifnull(nullif(w.state,''),'未提交') as name, count(*) as value
            from work w
            group by ifnull(nullif(w.state,''),'未提交')
            """)
    List<Map<String, Object>> workStateStatsAll();

    @Select("""
            select
              s.id as studentId,
              s.name as studentName,
              s.code as code,
              sum(case when w.scontent is not null and w.scontent <> '' then 1 else 0 end) as completedCount,
              count(w.id) as totalCount,
              round(
                if(count(w.id)=0, 0,
                   100 * sum(case when w.scontent is not null and w.scontent <> '' then 1 else 0 end) / count(w.id)
                )
              , 2) as completionRate
            from student s
            left join work w on w.student_id = s.id
            group by s.id, s.name, s.code
            order by completionRate desc
            limit 10
            """)
    List<Map<String, Object>> topStudentCompletionAll();

    @Select("""
            select coalesce(c.term, c.time, '未知') as label, count(*) as value
            from course c
            group by coalesce(c.term, c.time, '未知')
            order by label
            """)
    List<Map<String, Object>> courseCountByTermAll();

    @Select("""
            select
              c.id as courseId,
              c.name as courseName,
              t.name as teacherName,
              cl.name as clazzName,
              c.time as time,
              c.location as location,
              (select count(*) from choice ch where ch.course_id = c.id) as studentCount,
              (select count(*) from task tk where tk.course_id = c.id) as taskCount
            from course c
            left join teacher t on c.teacher_id = t.id
            left join clazz cl on c.class_id = cl.id
            order by c.id desc
            limit 6
            """)
    List<Map<String, Object>> latestCourseCards();

    @Select("""
            select ifnull(nullif(title,''), '未知') as name, count(*) as value
            from teacher
            group by ifnull(nullif(title,''), '未知')
            """)
    List<Map<String, Object>> teacherTitleStats();

    @Select("""
            select
              w.id as id,
              w.name as workName,
              ifnull(nullif(w.state,''),'未提交') as state,
              w.lab as lab,
              w.score as score,
              s.name as studentName,
              t.name as teacherName
            from work w
            left join student s on w.student_id = s.id
            left join teacher t on w.teacher_id = t.id
            order by w.id desc
            limit 10
            """)
    List<Map<String, Object>> recentWorkList();

    // ---------- Teacher (screen) ----------
    @Select("select count(*) from course where teacher_id = #{teacherId}")
    Integer teacherCourseCount(@Param("teacherId") Integer teacherId);

    @Select("""
            select count(distinct ch.student_id)
            from choice ch
            join course c on ch.course_id = c.id
            where c.teacher_id = #{teacherId}
            """)
    Integer teacherStudentDistinctCount(@Param("teacherId") Integer teacherId);

    @Select("select count(distinct class_id) from course where teacher_id = #{teacherId} and class_id is not null")
    Integer teacherClazzDistinctCount(@Param("teacherId") Integer teacherId);

    @Select("""
            select coalesce(c.term, c.time, '未知') as label, count(*) as value
            from course c
            where c.teacher_id = #{teacherId}
            group by coalesce(c.term, c.time, '未知')
            order by label
            """)
    List<Map<String, Object>> courseCountByTermTeacher(@Param("teacherId") Integer teacherId);

    @Select("""
            select
              c.id as courseId,
              c.name as courseName,
              cl.name as clazzName,
              c.time as time,
              c.location as location,
              (select count(*) from choice ch where ch.course_id = c.id) as studentCount,
              (select count(*) from task tk where tk.course_id = c.id) as taskCount
            from course c
            left join clazz cl on c.class_id = cl.id
            where c.teacher_id = #{teacherId}
            order by c.id desc
            """)
    List<Map<String, Object>> teacherCourseCards(@Param("teacherId") Integer teacherId);

    @Select("select id, teacher_id as teacherId, class_id as classId from course where id = #{courseId}")
    Map<String, Object> courseTeacherAndClazz(@Param("courseId") Integer courseId);

    @Select("""
            select ifnull(nullif(w.state,''),'未提交') as name, count(*) as value
            from work w
            join task tk on w.task_id = tk.id
            where w.teacher_id = #{teacherId}
              and tk.class_id = #{classId}
            group by ifnull(nullif(w.state,''),'未提交')
            """)
    List<Map<String, Object>> workStateStatsByTeacherAndClass(@Param("teacherId") Integer teacherId,
                                                              @Param("classId") Integer classId);

    /**
     * 教师端：作业状态分布（按课程）
     * 统计“学生人数”，并按任务类型（课后/实验）+ 是否提交（未提交/已提交）拆分。
     *
     * - 课后任务（lab=1）：已提交 => scontent 非空
     * - 实验任务（lab=2）：已提交 => tip3 非空（完成阶段三）
     */
    @Select("""
            select
              /* 仅统计“确实存在 work 记录”的学生：没有任务时四项应为 0 */
              coalesce((
                select sum(case when af.after_flag = 0 then 1 else 0 end)
                from (
                  select w.student_id as studentId,
                         max(case when w.scontent is not null and trim(w.scontent) <> '' then 1 else 0 end) as after_flag
                  from work w
                  where w.teacher_id = #{teacherId}
                    and w.course_id = #{courseId}
                    and w.lab = 1
                  group by w.student_id
                ) af
                join student s on s.id = af.studentId
                where s.class_id = #{classId}
              ), 0) as after_unsubmitted,

              coalesce((
                select sum(case when af.after_flag = 1 then 1 else 0 end)
                from (
                  select w.student_id as studentId,
                         max(case when w.scontent is not null and trim(w.scontent) <> '' then 1 else 0 end) as after_flag
                  from work w
                  where w.teacher_id = #{teacherId}
                    and w.course_id = #{courseId}
                    and w.lab = 1
                  group by w.student_id
                ) af
                join student s on s.id = af.studentId
                where s.class_id = #{classId}
              ), 0) as after_submitted,

              coalesce((
                select sum(case when ef.exp_flag = 0 then 1 else 0 end)
                from (
                  select w.student_id as studentId,
                         max(case when w.tip3 is not null and trim(w.tip3) <> '' then 1 else 0 end) as exp_flag
                  from work w
                  where w.teacher_id = #{teacherId}
                    and w.course_id = #{courseId}
                    and w.lab = 2
                  group by w.student_id
                ) ef
                join student s on s.id = ef.studentId
                where s.class_id = #{classId}
              ), 0) as exp_unsubmitted,

              coalesce((
                select sum(case when ef.exp_flag = 1 then 1 else 0 end)
                from (
                  select w.student_id as studentId,
                         max(case when w.tip3 is not null and trim(w.tip3) <> '' then 1 else 0 end) as exp_flag
                  from work w
                  where w.teacher_id = #{teacherId}
                    and w.course_id = #{courseId}
                    and w.lab = 2
                  group by w.student_id
                ) ef
                join student s on s.id = ef.studentId
                where s.class_id = #{classId}
              ), 0) as exp_submitted
            """)
    Map<String, Object> workSubmitStateStatsByTeacherAndCourse(@Param("teacherId") Integer teacherId,
                                                                 @Param("courseId") Integer courseId,
                                                                 @Param("classId") Integer classId);

    @Select("""
            select
              s.id as studentId,
              s.name as studentName,
              s.code as code,
              sum(case when w.scontent is not null and w.scontent <> '' then 1 else 0 end) as completedCount,
              count(w.id) as totalCount,
              round(
                if(count(w.id)=0, 0,
                   100 * sum(case when w.scontent is not null and w.scontent <> '' then 1 else 0 end) / count(w.id)
                )
              , 2) as completionRate
            from work w
            join task tk on w.task_id = tk.id
            join student s on w.student_id = s.id
            where w.teacher_id = #{teacherId}
              and tk.class_id = #{classId}
            group by s.id, s.name, s.code
            order by completionRate desc
            limit 10
            """)
    List<Map<String, Object>> topStudentCompletionByTeacherAndClass(@Param("teacherId") Integer teacherId,
                                                                    @Param("classId") Integer classId);

    /**
     * 教师端：学生作业完成率 Top10（强绑定 courseId）
     * 新口径：同时统计课后作业（lab=1）+ 实验任务（lab=2）。
     * - 课后任务：scontent 非空视为已完成
     * - 实验任务：tip3 非空视为阶段三已完成
     */
    @Select("""
            select
              s.id as studentId,
              s.name as studentName,
              s.code as code,
              sum(
                case
                  when w.lab = 1 and w.scontent is not null and trim(w.scontent) <> '' then 1
                  when w.lab = 2 and w.tip3 is not null and trim(w.tip3) <> '' then 1
                  else 0
                end
              ) as completedCount,
              count(w.id) as totalCount,
              round(
                if(count(w.id)=0, 0,
                   100 * sum(
                     case
                       when w.lab = 1 and w.scontent is not null and trim(w.scontent) <> '' then 1
                       when w.lab = 2 and w.tip3 is not null and trim(w.tip3) <> '' then 1
                       else 0
                     end
                   ) / count(w.id)
                )
              , 2) as completionRate
            from work w
            join student s on w.student_id = s.id
            where w.teacher_id = #{teacherId}
              and w.course_id = #{courseId}
              and w.lab in (1, 2)
            group by s.id, s.name, s.code
            order by completionRate desc
            limit 10
            """)
    List<Map<String, Object>> topStudentCompletionByTeacherAndCourse(@Param("teacherId") Integer teacherId,
                                                                        @Param("courseId") Integer courseId);

    @Select("""
            select
              sum(case when completionRate < 80 then 1 else 0 end) as lowRateCount,
              count(*) as totalStudents
            from (
              select
                s.id as studentId,
                round(
                  if(count(w.id)=0, 0,
                     100 * sum(case when w.scontent is not null and w.scontent <> '' then 1 else 0 end) / count(w.id)
                  )
                , 2) as completionRate
              from work w
              join task tk on w.task_id = tk.id
              join student s on w.student_id = s.id
              where w.teacher_id = #{teacherId}
                and tk.class_id = #{classId}
              group by s.id
            ) t
            """)
    Map<String, Object> lowCompletionSummaryByTeacherAndClass(@Param("teacherId") Integer teacherId,
                                                              @Param("classId") Integer classId);

    @Select("""
            select
              case
                when sc.score < 60 then '0-59'
                when sc.score < 70 then '60-69'
                when sc.score < 80 then '70-79'
                when sc.score < 90 then '80-89'
                else '90-100'
              end as name,
              count(*) as value
            from score sc
            where sc.course_id = #{courseId}
              and sc.score is not null
            group by
              case
                when sc.score < 60 then '0-59'
                when sc.score < 70 then '60-69'
                when sc.score < 80 then '70-79'
                when sc.score < 90 then '80-89'
                else '90-100'
              end
            order by name
            """)
    List<Map<String, Object>> scoreSegmentStatsByCourse(@Param("courseId") Integer courseId);

    @Select("""
            select
              sc.id as id,
              sc.name as paperName,
              sc.score as score,
              sc.status as status,
              st.name as studentName
            from score sc
            left join student st on sc.student_id = st.id
            where sc.teacher_id = #{teacherId}
              and (#{courseId} is null or sc.course_id = #{courseId})
            order by sc.id desc
            limit 10
            """)
    List<Map<String, Object>> recentScoreList(@Param("teacherId") Integer teacherId,
                                              @Param("courseId") Integer courseId);

    // ---------- Radar helpers ----------
    @Select("select max(id) from course")
    Integer latestCourseId();

    @Select("""
            select s.id as studentId, s.name as studentName, s.code as code
            from choice ch
            join student s on ch.student_id = s.id
            where ch.course_id = #{courseId}
            """)
    List<Map<String, Object>> studentsByCourse(@Param("courseId") Integer courseId);

    @Select("select count(*) from sign_in where course_id = #{courseId}")
    Integer signInTotalByCourse(@Param("courseId") Integer courseId);

    @Select("""
            select sr.student_id as studentId, count(*) as attendCount
            from sign_in_record sr
            join sign_in si on sr.sign_in_id = si.id
            where si.course_id = #{courseId}
              and sr.status = 'SUCCESS'
            group by sr.student_id
            """)
    List<Map<String, Object>> signInAttendByCourse(@Param("courseId") Integer courseId);

    /**
     * 教师端：当前课程“签到情况”（饼图用）
     * - 已签到：在当前进行中的签到（end_time > now，取最新的一次；否则取最后一次）中存在 SUCCESS 记录
     * - 未签到：同一课程选修学生中，不存在对应 SUCCESS 记录
     * - 同时返回姓名列表（用于饼图右侧展示）
     */
    @Select("""
            select
              /* 已签到人数 */
              (
                select count(distinct s.id)
                from choice ch
                join student s on ch.student_id = s.id
                join sign_in_record sr on sr.student_id = s.id
                where ch.course_id = #{courseId}
                  and sr.status = 'SUCCESS'
                  and sr.sign_in_id = coalesce(
                    (select id from sign_in where course_id = #{courseId} and end_time > NOW() order by end_time desc limit 1),
                    (select id from sign_in where course_id = #{courseId} order by id desc limit 1)
                  )
              ) as signedCount,
              /* 未签到人数 */
              (
                select count(distinct s.id)
                from choice ch
                join student s on ch.student_id = s.id
                left join sign_in_record sr on sr.student_id = s.id
                  and sr.status = 'SUCCESS'
                  and sr.sign_in_id = coalesce(
                    (select id from sign_in where course_id = #{courseId} and end_time > NOW() order by end_time desc limit 1),
                    (select id from sign_in where course_id = #{courseId} order by id desc limit 1)
                  )
                where ch.course_id = #{courseId}
                  and sr.id is null
              ) as unsignedCount,
              /* 已签到学生姓名 */
              (
                select group_concat(distinct s.name order by s.name separator ',')
                from choice ch
                join student s on ch.student_id = s.id
                join sign_in_record sr on sr.student_id = s.id
                where ch.course_id = #{courseId}
                  and sr.status = 'SUCCESS'
                  and sr.sign_in_id = coalesce(
                    (select id from sign_in where course_id = #{courseId} and end_time > NOW() order by end_time desc limit 1),
                    (select id from sign_in where course_id = #{courseId} order by id desc limit 1)
                  )
              ) as signedNames,
              /* 未签到学生姓名 */
              (
                select group_concat(distinct s.name order by s.name separator ',')
                from choice ch
                join student s on ch.student_id = s.id
                left join sign_in_record sr on sr.student_id = s.id
                  and sr.status = 'SUCCESS'
                  and sr.sign_in_id = coalesce(
                    (select id from sign_in where course_id = #{courseId} and end_time > NOW() order by end_time desc limit 1),
                    (select id from sign_in where course_id = #{courseId} order by id desc limit 1)
                  )
                where ch.course_id = #{courseId}
                  and sr.id is null
              ) as unsignedNames
            """)
    Map<String, Object> signInSignedUnsignedByCourse(@Param("courseId") Integer courseId);

    @Select("""
            select w.student_id as studentId, avg(ifnull(w.score, 0)) as avgScore
            from work w
            join task tk on w.task_id = tk.id
            where w.teacher_id = #{teacherId}
              and tk.class_id = #{classId}
            group by w.student_id
            """)
    List<Map<String, Object>> workAvgScoreByTeacherAndClass(@Param("teacherId") Integer teacherId,
                                                            @Param("classId") Integer classId);

    @Select("""
            select student_id as studentId, avg(ifnull(score, 0)) as avgScore
            from score
            where course_id = #{courseId}
            group by student_id
            """)
    List<Map<String, Object>> examAvgScoreByCourse(@Param("courseId") Integer courseId);
}

