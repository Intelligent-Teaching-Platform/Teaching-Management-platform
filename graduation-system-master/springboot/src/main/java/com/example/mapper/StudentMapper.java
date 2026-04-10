package com.example.mapper;

import com.example.entity.Choice;
import com.example.entity.College;
import com.example.entity.Student;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StudentMapper {

    void insert(Student student);
    @Select("select * from student where username=#{username}")
    Student selectByUsername(String username);

    /** 登录专用：如果数据库 role 为 NULL，自动降级为 STUDENT */
    Student selectByUsernameForLogin(String username);


    @Select("select student.*, college.name as collegeName from student " +
            "left join college on student.college_id = college.id")
    List<Student> selectAll();

    @Select("select student.*,college.name as collegeName from student " +
            "left join college on student.college_id = college.id " +
            "where student.name like  concat('%',#{name},'%')")
    List<Student> selectByName(String name);

    void updateById(Student student);

    @Delete("delete from student where id=#{id}")
    void deleteById(Integer id);

    @Select("select student.*,college.name as collegeName from student " +
            "left join college on student.college_id = college.id " +
            "where student.id = #{id}")
    Student selectById(Integer id);

    List<Student> selectByClassId(Integer classId );

    void batchInsert(List<Student> students);

    @Select("select s.id, s.name, s.username, s.code from student s join choice on s.id = choice.student_id where choice.course_id = #{courseId}")
    List<Student> selectByCourseId(Integer courseId);

    @Select("select * from student")
    List<Student> FindAll();

    @Select("select count(*) from student where class_id = #{classId}")
    int countByClassId(Integer classId);

    @Select("select count(*) from student where speciality_id = #{specialityId}")
    int countBySpecialityId(Integer specialityId);
}

