package com.example.mapper;

import com.example.entity.Work;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface WorkMapper {
    void insert(Work work);


    @Select("select w.*, t.name as teacherName, s.name as studentName, " +
            "s.code as studentCode, c.name as className, co.name as courseName, " +
            "ta.place as place, ta.experiment_time as experimentTime, " +
            "ta.experiment_purpose as experimentPurpose, ta.experiment_environment as experimentEnvironment, " +
            "ta.experiment_content as experimentContent, " +
            "ta.q1 as q1, ta.q2 as q2, ta.q3 as q3, ta.q4 as q4, ta.q5 as q5, " +
            "ta.q6 as q6, ta.q7 as q7, ta.q8 as q8, ta.q9 as q9, " +
            "w.student_last_submit_time as studentLastSubmitTime " +
            "from work w " +
            "left join teacher t on w.teacher_id = t.id " +
            "left join student s on w.student_id = s.id " +
            "left join clazz c on s.class_id = c.id " +
            "left join course co on w.course_id = co.id " +
            "left join task ta on w.task_id = ta.id")
    List<Work> selectAll();

    @Select("select w.*, s.name as studentName, t.name as teacherName, " +
            "s.code as studentCode, c.name as className, co.name as courseName, " +
            "ta.place as place, ta.experiment_time as experimentTime, " +
            "ta.experiment_purpose as experimentPurpose, ta.experiment_environment as experimentEnvironment, " +
            "ta.experiment_content as experimentContent, " +
            "ta.q1 as q1, ta.q2 as q2, ta.q3 as q3, ta.q4 as q4, ta.q5 as q5, " +
            "ta.q6 as q6, ta.q7 as q7, ta.q8 as q8, ta.q9 as q9, " +
            "w.student_last_submit_time as studentLastSubmitTime " +
            "from work w " +
            "left join teacher t on w.teacher_id = t.id " +
            "left join student s on w.student_id = s.id " +
            "left join clazz c on s.class_id = c.id " +
            "left join course co on w.course_id = co.id " +
            "left join task ta on w.task_id = ta.id " +
            "where w.name like concat('%',#{name},'%')")
    List<Work> selectByName(String name);

    void updateById(Work work);

    /**
     * 仅用于"学生提交/修改"（课后作业 lab=1）更新 student_last_submit_time，
     * 避免教师审核/评分触发更新时间误导。
     */
    void updateByIdOne(Work work);

    @Delete("delete from work where id=#{id}")
    void deleteById(Integer id);

    //依据教师id搜索相关课程
    @Select("select w.*, t.name as teacherName, s.name as studentName, " +
            "s.code as studentCode, c.name as className, co.name as courseName, " +
            "ta.place as place, ta.experiment_time as experimentTime, " +
            "ta.experiment_purpose as experimentPurpose, ta.experiment_environment as experimentEnvironment, " +
            "ta.experiment_content as experimentContent, " +
            "ta.q1 as q1, ta.q2 as q2, ta.q3 as q3, ta.q4 as q4, ta.q5 as q5, " +
            "ta.q6 as q6, ta.q7 as q7, ta.q8 as q8, ta.q9 as q9, " +
            "w.student_last_submit_time as studentLastSubmitTime " +
            "from work w " +
            "left join student s on w.student_id = s.id " +
            "left join teacher t on w.teacher_id = t.id " +
            "left join clazz c on s.class_id = c.id " +
            "left join course co on w.course_id = co.id " +
            "left join task ta on w.task_id = ta.id " +
            "where s.name like concat('%',#{name},'%') and t.id = #{teacherId}")
    List<Work> selectByNameAndTeacherId(@Param("name") String name, @Param("teacherId") Integer teacherId);


    @Select("select w.*, t.name as teacherName, s.name as studentName, " +
            "s.code as studentCode, c.name as className, co.name as courseName, " +
            "ta.place as place, ta.experiment_time as experimentTime, " +
            "ta.experiment_purpose as experimentPurpose, ta.experiment_environment as experimentEnvironment, " +
            "ta.experiment_content as experimentContent, " +
            "ta.q1 as q1, ta.q2 as q2, ta.q3 as q3, ta.q4 as q4, ta.q5 as q5, " +
            "ta.q6 as q6, ta.q7 as q7, ta.q8 as q8, ta.q9 as q9, " +
            "w.student_last_submit_time as studentLastSubmitTime " +
            "from work w " +
            "left join teacher t on w.teacher_id = t.id " +
            "left join student s on w.student_id = s.id " +
            "left join clazz c on s.class_id = c.id " +
            "left join course co on w.course_id = co.id " +
            "left join task ta on w.task_id = ta.id " +
            "where t.id = #{teacherId}")
    List<Work> selectAllByTeacherId(Integer teacherId);
    //依据学生id搜索相关课程
    @Select("select w.*, t.name as teacherName, s.name as studentName, " +
            "s.code as studentCode, c.name as className, co.name as courseName, " +
            "ta.place as place, ta.experiment_time as experimentTime, " +
            "ta.experiment_purpose as experimentPurpose, ta.experiment_environment as experimentEnvironment, " +
            "ta.experiment_content as experimentContent, " +
            "ta.q1 as q1, ta.q2 as q2, ta.q3 as q3, ta.q4 as q4, ta.q5 as q5, " +
            "ta.q6 as q6, ta.q7 as q7, ta.q8 as q8, ta.q9 as q9, " +
            "w.student_last_submit_time as studentLastSubmitTime " +
            "from work w " +
            "left join student s on w.student_id = s.id " +
            "left join teacher t on w.teacher_id = t.id " +
            "left join clazz c on s.class_id = c.id " +
            "left join course co on w.course_id = co.id " +
            "left join task ta on w.task_id = ta.id " +
            "where s.name like concat('%',#{name},'%') and s.id = #{studentId}")
    List<Work> selectByNameAndStudentId(@Param("name") String name, @Param("studentId") Integer studentId);

    @Select("select w.*, t.name as teacherName, s.name as studentName, " +
            "s.code as studentCode, c.name as className, co.name as courseName, " +
            "ta.place as place, ta.experiment_time as experimentTime, " +
            "ta.experiment_purpose as experimentPurpose, ta.experiment_environment as experimentEnvironment, " +
            "ta.experiment_content as experimentContent, " +
            "ta.q1 as q1, ta.q2 as q2, ta.q3 as q3, ta.q4 as q4, ta.q5 as q5, " +
            "ta.q6 as q6, ta.q7 as q7, ta.q8 as q8, ta.q9 as q9, " +
            "w.student_last_submit_time as studentLastSubmitTime " +
            "from work w " +
            "left join teacher t on w.teacher_id = t.id " +
            "left join student s on w.student_id = s.id " +
            "left join clazz c on s.class_id = c.id " +
            "left join course co on w.course_id = co.id " +
            "left join task ta on w.task_id = ta.id " +
            "where s.id = #{studentId}")
    List<Work> selectAllByStudentId(Integer studentId);

    /**
     * 按课程过滤：教师视角（通过 task -> course 关联）
     */
    @Select("select w.*, t.name as teacherName, s.name as studentName, " +
            "s.code as studentCode, c.name as className, co.name as courseName, " +
            "ta.place as place, ta.experiment_time as experimentTime, " +
            "ta.experiment_purpose as experimentPurpose, ta.experiment_environment as experimentEnvironment, " +
            "ta.experiment_content as experimentContent, " +
            "ta.q1 as q1, ta.q2 as q2, ta.q3 as q3, ta.q4 as q4, ta.q5 as q5, " +
            "ta.q6 as q6, ta.q7 as q7, ta.q8 as q8, ta.q9 as q9, " +
            "w.student_last_submit_time as studentLastSubmitTime " +
            "from work w " +
            "left join teacher t on w.teacher_id = t.id " +
            "left join student s on w.student_id = s.id " +
            "left join clazz c on s.class_id = c.id " +
            "left join course co on w.course_id = co.id " +
            "left join task ta on w.task_id = ta.id " +
            "where t.id = #{teacherId} and w.course_id = #{courseId}")
    List<Work> selectByCourseAndTeacher(@Param("courseId") Integer courseId, @Param("teacherId") Integer teacherId);

    /**
     * 教师端：仅取实验作业（lab=2），并限制课程/教师范围。
     * 用于"实验作业相似度预警"看板实时计算。
     */
    @Select("select w.*, t.name as teacherName, s.name as studentName, " +
            "s.code as studentCode, c.name as className, co.name as courseName, " +
            "ta.place as place, ta.experiment_time as experimentTime, " +
            "ta.experiment_purpose as experimentPurpose, ta.experiment_environment as experimentEnvironment, " +
            "ta.experiment_content as experimentContent, " +
            "ta.q1 as q1, ta.q2 as q2, ta.q3 as q3, ta.q4 as q4, ta.q5 as q5, " +
            "ta.q6 as q6, ta.q7 as q7, ta.q8 as q8, ta.q9 as q9, " +
            "w.student_last_submit_time as studentLastSubmitTime " +
            "from work w " +
            "left join teacher t on w.teacher_id = t.id " +
            "left join student s on w.student_id = s.id " +
            "left join clazz c on s.class_id = c.id " +
            "left join course co on w.course_id = co.id " +
            "left join task ta on w.task_id = ta.id " +
            "where t.id = #{teacherId} and w.course_id = #{courseId} and w.lab = 2 " +
            "order by w.id desc")
    List<Work> selectLab2ByCourseAndTeacher(@Param("courseId") Integer courseId, @Param("teacherId") Integer teacherId);

    /**
     * 按课程过滤：学生视角（通过 task -> course 关联）
     */
    @Select("select w.*, t.name as teacherName, s.name as studentName, " +
            "s.code as studentCode, c.name as className, co.name as courseName, " +
            "ta.place as place, ta.experiment_time as experimentTime, " +
            "ta.experiment_purpose as experimentPurpose, ta.experiment_environment as experimentEnvironment, " +
            "ta.experiment_content as experimentContent, " +
            "ta.q1 as q1, ta.q2 as q2, ta.q3 as q3, ta.q4 as q4, ta.q5 as q5, " +
            "ta.q6 as q6, ta.q7 as q7, ta.q8 as q8, ta.q9 as q9, " +
            "w.student_last_submit_time as studentLastSubmitTime " +
            "from work w " +
            "left join teacher t on w.teacher_id = t.id " +
            "left join student s on w.student_id = s.id " +
            "left join clazz c on s.class_id = c.id " +
            "left join course co on w.course_id = co.id " +
            "left join task ta on w.task_id = ta.id " +
            "where s.id = #{studentId} and w.course_id = #{courseId}")
    List<Work> selectByCourseAndStudent(@Param("courseId") Integer courseId, @Param("studentId") Integer studentId);

    /**
     * 按课程过滤：管理员视角（通过 task -> course 关联）
     */
    @Select("select w.*, t.name as teacherName, s.name as studentName, " +
            "s.code as studentCode, c.name as className, co.name as courseName, " +
            "ta.place as place, ta.experiment_time as experimentTime, " +
            "ta.experiment_purpose as experimentPurpose, ta.experiment_environment as experimentEnvironment, " +
            "ta.experiment_content as experimentContent, " +
            "ta.q1 as q1, ta.q2 as q2, ta.q3 as q3, ta.q4 as q4, ta.q5 as q5, " +
            "ta.q6 as q6, ta.q7 as q7, ta.q8 as q8, ta.q9 as q9, " +
            "w.student_last_submit_time as studentLastSubmitTime " +
            "from work w " +
            "left join teacher t on w.teacher_id = t.id " +
            "left join student s on w.student_id = s.id " +
            "left join clazz c on s.class_id = c.id " +
            "left join course co on w.course_id = co.id " +
            "left join task ta on w.task_id = ta.id " +
            "where w.course_id = #{courseId}")
    List<Work> selectByCourse(@Param("courseId") Integer courseId);

    @Select("select w.*, t.name as teacherName, s.name as studentName, " +
            "s.code as studentCode, c.name as className, co.name as courseName, " +
            "ta.place as place, ta.experiment_time as experimentTime, " +
            "ta.experiment_purpose as experimentPurpose, ta.experiment_environment as experimentEnvironment, " +
            "ta.experiment_content as experimentContent, " +
            "ta.q1 as q1, ta.q2 as q2, ta.q3 as q3, ta.q4 as q4, ta.q5 as q5, " +
            "ta.q6 as q6, ta.q7 as q7, ta.q8 as q8, ta.q9 as q9, " +
            "w.student_last_submit_time as studentLastSubmitTime " +
            "from work w " +
            "left join teacher t on w.teacher_id = t.id " +
            "left join student s on w.student_id = s.id " +
            "left join clazz c on s.class_id = c.id " +
            "left join course co on w.course_id = co.id " +
            "left join task ta on w.task_id = ta.id " +
            "where w.id = #{id}")
    Work selectById(Integer id);

    /** 审核查重：同一实验任务(task_id)下，排除自身，取其它学生实验内容（tip2）+总结（tip3） */
    @Select("select work.id, work.student_id as studentId, student.name as studentName, work.tip2 as tip2, work.tip3 as tip3 from work " +
            "left join student on work.student_id = student.id " +
            "where work.task_id = #{taskId} and work.id <> #{excludeId} " +
            "and work.state = '审核通过' and work.tip2 is not null and TRIM(work.tip2) <> ''")
    List<Work> selectOthersByTaskIdForSimilarity(@Param("taskId") Integer taskId, @Param("excludeId") Integer excludeId);

    /** 审核查重兜底：若 task_id 为空，则按(teacher_id + name + lab=2)找同题其它提交 */
    @Select("select work.id, work.student_id as studentId, student.name as studentName, work.tip2 as tip2, work.tip3 as tip3 from work " +
            "left join student on work.student_id = student.id " +
            "where work.teacher_id = #{teacherId} and work.lab = 2 and work.name = #{name} and work.id <> #{excludeId} " +
            "and work.state = '审核通过' and work.tip2 is not null and TRIM(work.tip2) <> ''")
    List<Work> selectOthersByTeacherAndNameForSimilarity(@Param("teacherId") Integer teacherId, @Param("name") String name, @Param("excludeId") Integer excludeId);

    @Select("select w.*, s.name as studentName from work w " +
            "left join student s on w.student_id = s.id " +
            "where w.task_id = #{taskId} and w.lab = 2")
    List<Work> selectByTaskIdForStageStats(@Param("taskId") Integer taskId);

    /** 阶段一：实验环境（tip1） */
    @Update("UPDATE work SET tip1 = #{env}, " +
            "student_last_submit_time = NOW(), " +
            "submit_phase = CASE WHEN IFNULL(submit_phase, 0) < 1 THEN 1 ELSE submit_phase END " +
            "WHERE id = #{id} AND student_id = #{studentId} AND lab = 2")
    int updateStudentStage1(@Param("id") Integer id, @Param("studentId") Integer studentId, @Param("env") String env);

    /** 阶段二：实验内容与步骤（tip2），必须已完成 tip1 */
    @Update("UPDATE work SET tip2 = #{steps}, " +
            "student_last_submit_time = NOW(), " +
            "submit_phase = CASE WHEN IFNULL(submit_phase, 0) < 2 THEN 2 ELSE GREATEST(IFNULL(submit_phase, 0), 2) END " +
            "WHERE id = #{id} AND student_id = #{studentId} AND lab = 2 " +
            "AND tip1 IS NOT NULL AND TRIM(tip1) <> ''")
    int updateStudentStage2(@Param("id") Integer id, @Param("studentId") Integer studentId, @Param("steps") String steps);

    /** 阶段三：实验总结与心得（tip3），必须已完成 tip2 */
    @Update("UPDATE work SET tip3 = #{summary}, " +
            "student_last_submit_time = NOW(), " +
            "submit_phase = 3, state = NULL " +
            "WHERE id = #{id} AND student_id = #{studentId} AND lab = 2 " +
            "AND ((tip2 IS NOT NULL AND TRIM(tip2) <> '') OR IFNULL(submit_phase, 0) >= 3)")
    int updateStudentStage3(@Param("id") Integer id, @Param("studentId") Integer studentId,
                            @Param("summary") String summary);
}

