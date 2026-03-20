package com.example.mapper;

import com.example.entity.Course;
import com.example.entity.Student;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CourseMapper {
    void insert(Course course);


    @Select("select course.*, college.name as collegeName, teacher.name as teacherName, clazz.name as className from course " +
            "left join college on course.college_id=college.id " +
            "left join teacher on course.teacher_id=teacher.id " +
            "left join clazz on course.class_id=clazz.id ")
    List<Course> selectAll();

    @Select("select course.*, college.name as collegeName, clazz.name as className from course " +
            " left join college on course.college_id=college.id " +
            " left join teacher on course.teacher_id=college.id " +
            " left join clazz on course.class_id=clazz.id " +
            "where course.name like  concat('%',#{name},'%')")
    List<Course> selectByName(String name);

    void updateById(Course course);

    @Delete("delete from course where id=#{id}")
    void deleteById(Integer id);
    //依据教师id搜索相关课程
    @Select("select course.*, college.name as collegeName, teacher.name as teacherName, clazz.name as className from course " +
            " left join college on course.college_id=college.id " +
            " left join teacher on course.teacher_id=teacher.id " +
            " left join clazz on course.class_id=clazz.id " +
            "where course.name like  concat('%',#{name},'%') and teacher.id=#{teacherId}")
    List<Course> selectByNameAndTeacherId(@Param("name") String name,@Param("teacherId") Integer teacherId);

    @Select("select course.*, college.name as collegeName, teacher.name as teacherName, clazz.name as className from course " +
            "left join college on course.college_id=college.id " +
            "left join teacher on course.teacher_id=teacher.id " +
            "left join clazz on course.class_id=clazz.id " +
            "where teacher.id=#{teacherId}")
    List<Course> selectAllByTeacherId(Integer teacherId);

    @Select("select course.*, teacher.name as teacherName, college.name as collegeName, clazz.name as className from course " +
            "left join teacher on course.teacher_id=teacher.id " +
            "left join college on course.college_id=college.id " +
            "left join clazz on course.class_id=clazz.id " +
            "where course.id=#{id}")
    Course selectById(Integer courseId);


    void batchInsert(List<Course> courses);

    /**
     * 根据教师姓名查询课程
     */
    @Select("SELECT c.*, t.name as teacherName, col.name as collegeName, clz.name as className " +
            "FROM course c " +
            "LEFT JOIN teacher t ON c.teacher_id = t.id " +
            "LEFT JOIN college col ON c.college_id = col.id " +
            "LEFT JOIN clazz clz ON c.class_id = clz.id " +
            "WHERE t.name = #{teacherName}")
    List<Course> selectByTeacherName(@Param("teacherName") String teacherName);

    /**
     * 根据学生ID查询已选课程（通过 choice 选课表）
     */
    @Select("SELECT c.*, t.name as teacherName, col.name as collegeName, clz.name as className " +
            "FROM choice ch " +
            "JOIN course c ON ch.course_id = c.id " +
            "LEFT JOIN teacher t ON c.teacher_id = t.id " +
            "LEFT JOIN college col ON c.college_id = col.id " +
            "LEFT JOIN clazz clz ON c.class_id = clz.id " +
            "WHERE ch.student_id = #{studentId}")
    List<Course> selectAllByStudentId(@Param("studentId") Integer studentId);

    /**
     * 根据学生ID和课程名称查询已选课程
     */
    @Select("SELECT c.*, t.name as teacherName, col.name as collegeName, clz.name as className " +
            "FROM choice ch " +
            "JOIN course c ON ch.course_id = c.id " +
            "LEFT JOIN teacher t ON c.teacher_id = t.id " +
            "LEFT JOIN college col ON c.college_id = col.id " +
            "LEFT JOIN clazz clz ON c.class_id = clz.id " +
            "WHERE ch.student_id = #{studentId} AND c.name LIKE CONCAT('%', #{name}, '%')")
    List<Course> selectByNameAndStudentId(@Param("name") String name, @Param("studentId") Integer studentId);
}
