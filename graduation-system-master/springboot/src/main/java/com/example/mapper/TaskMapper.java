package com.example.mapper;

import com.example.entity.Task;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface TaskMapper {

    /**
     * 根据教师ID和课程名称获取该教师所有同名课程关联的班级列表
     * 用于发布任务时选择班级
     */
    @Select("<script>" +
            "SELECT DISTINCT clz.id as classId, clz.name as className " +
            "FROM course c " +
            "JOIN course_class cc ON c.id = cc.course_id " +
            "JOIN clazz clz ON cc.class_id = clz.id " +
            "WHERE c.teacher_id = #{teacherId} " +
            "<if test='courseId != null'> AND c.id = #{courseId} </if>" +
            "<if test='courseName != null and courseName != \"\"'> AND c.name = #{courseName} </if>" +
            "ORDER BY clz.id" +
            "</script>")
    List<Task> selectClassesByTeacherAndCourse(@Param("teacherId") Integer teacherId,
                                               @Param("courseId") Integer courseId,
                                               @Param("courseName") String courseName);

    /**
     * 根据课程ID查询关联的所有班级（返回 Task 对象，包含 classId 和 className）
     */
    @Select("SELECT clz.id as classId, clz.name as className " +
            "FROM course_class cc " +
            "JOIN clazz clz ON cc.class_id = clz.id " +
            "WHERE cc.course_id = #{courseId} " +
            "ORDER BY clz.id")
    List<Task> selectClassesByCourseId(@Param("courseId") Integer courseId);

    /**
     * 根据班级ID查询任务（用于课程详情页按当前班级过滤）
     */
    @Select("SELECT t.*, c.name as className FROM task t " +
            "LEFT JOIN clazz c ON t.class_id = c.id " +
            "WHERE t.class_id = #{classId} " +
            "ORDER BY t.id DESC")
    List<Task> selectByClassId(@Param("classId") Integer classId);

    /**
     * 根据课程ID和班级ID查询任务
     */
    @Select("SELECT t.*, c.name as className FROM task t " +
            "LEFT JOIN clazz c ON t.class_id = c.id " +
            "WHERE t.course_id = #{courseId} AND t.class_id = #{classId} " +
            "ORDER BY t.id DESC")
    List<Task> selectByCourseIdAndClassId(@Param("courseId") Integer courseId, @Param("classId") Integer classId);

    @Select("SELECT t.*, c.name as className FROM task t " +
            "LEFT JOIN clazz c ON t.class_id = c.id " +
            "WHERE t.course_id = #{courseId} AND t.class_id = #{classId} AND t.name LIKE CONCAT('%', #{name}, '%') " +
            "ORDER BY t.id DESC")
    List<Task> selectByCourseIdAndClassIdAndName(@Param("courseId") Integer courseId, @Param("classId") Integer classId, @Param("name") String name);

    @Select("SELECT t.*, c.name as className FROM task t " +
            "LEFT JOIN clazz c ON t.class_id = c.id " +
            "WHERE t.teacher_id = #{teacherId} AND t.course_id = #{courseId} AND t.class_id = #{classId} " +
            "ORDER BY t.id DESC")
    List<Task> selectByTeacherIdAndCourseIdAndClassId(@Param("teacherId") Integer teacherId, @Param("courseId") Integer courseId, @Param("classId") Integer classId);

    @Select("SELECT t.*, c.name as className FROM task t " +
            "LEFT JOIN clazz c ON t.class_id = c.id " +
            "WHERE t.teacher_id = #{teacherId} AND t.course_id = #{courseId} AND t.class_id = #{classId} AND t.name LIKE CONCAT('%', #{name}, '%') " +
            "ORDER BY t.id DESC")
    List<Task> selectByTeacherIdAndCourseIdAndClassIdAndName(@Param("teacherId") Integer teacherId, @Param("courseId") Integer courseId, @Param("classId") Integer classId, @Param("name") String name);

    /**
     * 根据课程ID查询任务（包含班级信息）
     */
    @Select("SELECT t.*, c.name as className, " +
            "GROUP_CONCAT(DISTINCT cc2.class_id) as classIds " +
            "FROM task t " +
            "LEFT JOIN clazz c ON t.class_id = c.id " +
            "LEFT JOIN course_class cc2 ON t.course_id = cc2.course_id " +
            "WHERE t.course_id = #{courseId} " +
            "GROUP BY t.id " +
            "ORDER BY t.id DESC")
    List<Task> selectByCourseIdWithClasses(@Param("courseId") Integer courseId);

    @Select("select t.*, c.name as className from task t " +
            "left join clazz c on t.class_id = c.id " +
            "order by t.id desc")
    List<Task> selectAll();

    @Select("select t.*, c.name as className from task t " +
            "left join clazz c on t.class_id = c.id " +
            "where t.name like concat('%',#{name},'%') " +
            "order by t.id desc")
    List<Task> selectByName(String name);

    @Select("select t.*, c.name as className from task t " +
            "left join clazz c on t.class_id = c.id " +
            "where t.teacher_id = #{teacherId} " +
            "order by t.id desc")
    List<Task> selectByTeacherId(@Param("teacherId") Integer teacherId);

    @Select("select t.*, c.name as className from task t " +
            "left join clazz c on t.class_id = c.id " +
            "where t.teacher_id = #{teacherId} and t.name like concat('%',#{name},'%') " +
            "order by t.id desc")
    List<Task> selectByTeacherIdAndName(@Param("teacherId") Integer teacherId, @Param("name") String name);

    @Select("select t.*, c.name as className from task t " +
            "left join clazz c on t.class_id = c.id " +
            "where t.teacher_id = #{teacherId} and t.course_id = #{courseId} " +
            "order by t.id desc")
    List<Task> selectByTeacherIdAndCourseId(@Param("teacherId") Integer teacherId, @Param("courseId") Integer courseId);

    @Select("select t.*, c.name as className from task t " +
            "left join clazz c on t.class_id = c.id " +
            "where t.teacher_id = #{teacherId} and t.course_id = #{courseId} and t.name like concat('%',#{name},'%') " +
            "order by t.id desc")
    List<Task> selectByTeacherIdAndCourseIdAndName(@Param("teacherId") Integer teacherId,
                                                    @Param("courseId") Integer courseId,
                                                    @Param("name") String name);

    /** 按课程查询（学生/管理员在带 courseId 分页时使用） */
    @Select("select t.*, c.name as className from task t " +
            "left join clazz c on t.class_id = c.id " +
            "where t.course_id = #{courseId} " +
            "order by t.id desc")
    List<Task> selectByCourseId(@Param("courseId") Integer courseId);

    @Select("select t.*, c.name as className from task t " +
            "left join clazz c on t.class_id = c.id " +
            "where t.course_id = #{courseId} and t.name like concat('%',#{name},'%') " +
            "order by t.id desc")
    List<Task> selectByCourseIdAndName(@Param("courseId") Integer courseId, @Param("name") String name);

    @Delete("delete from task where id = #{id}")
    void deleteById(Integer id);

    @Insert("INSERT INTO task (name, content, cover, class_id, lab, teacher_id, course_id) " +
            "VALUES (#{name}, #{content}, #{cover}, #{classId}, #{lab}, #{teacherId}, #{courseId})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(Task task);

    @Update("UPDATE task SET " +
            "name = #{name}, " +
            "content = #{content}, " +
            "cover = #{cover}, " +
            "class_id = #{classId}, " +
            "lab = #{lab}, " +
            "teacher_id = #{teacherId}, " +
            "course_id = #{courseId} " +
            "WHERE id = #{id}")
    void updateById(Task task);

    // 根据 class_id 查询 student 表中的 id
    @Select("SELECT id FROM student WHERE class_id = #{classId}")
    List<Integer> getStudentIdsByClassId(Integer classId);

    // 插入 work 表：实验任务（lab=2）时，把老师填写的任务内容回填到 work 的 scontent/阶段展示字段
    // 目的：老师在创建实验任务时填写的“实验目的/要求”无需在评审弹窗逐个学生再填
    @Insert("INSERT INTO work (task_id, student_id, name, content, scontent, student_stage_title, student_stage_requirement, lab, teacher_id, course_id) " +
            "VALUES (" +
            "  #{taskId}, " +
            "  #{studentId}, " +
            "  #{name}, " +
            "  #{content}, " +
            "  CASE WHEN #{lab} = 2 THEN #{content} ELSE NULL END, " +
            "  CASE WHEN #{lab} = 2 THEN #{name} ELSE NULL END, " +
            "  CASE WHEN #{lab} = 2 THEN #{content} ELSE NULL END, " +
            "  #{lab}, " +
            "  #{teacherId}, " +
            "  #{courseId}" +
            ")")
    void insertIntoWork(@Param("taskId") Integer taskId,
                        @Param("studentId") Integer studentId,
                        @Param("name") String name,
                        @Param("content") String content,
                        @Param("lab") Integer lab,      // 修改为 Integer 类型
                        @Param("teacherId") Integer teacherId,
                        @Param("courseId") Integer courseId);

    // 删除 work 表中与 task_id 对应的所有数据
    @Delete("DELETE FROM work WHERE task_id = #{taskId}")
    void deleteWorkByTaskId(Integer taskId);

    // 更新 work 表中与 task_id 对应的所有字段
    @Update("UPDATE work SET " +
            "student_id = #{studentId}, " +
            "name = #{name}, " +
            "content = #{content}, " +
            "scontent = CASE WHEN #{lab} = 2 THEN #{content} ELSE scontent END, " +
            "student_stage_title = CASE WHEN #{lab} = 2 THEN #{name} ELSE NULL END, " +
            "student_stage_requirement = CASE WHEN #{lab} = 2 THEN #{content} ELSE NULL END, " +
            "lab = #{lab}, " +
            "teacher_id = #{teacherId}, " +
            "course_id = #{courseId} " +
            "WHERE task_id = #{taskId}")
    void updateWorkAllFields(@Param("taskId") Integer taskId,
                             @Param("studentId") Integer studentId,
                             @Param("name") String name,
                             @Param("content") String content,
                             @Param("lab") Integer lab,      // 修改为 Integer 类型
                             @Param("teacherId") Integer teacherId,
                             @Param("courseId") Integer courseId);
}