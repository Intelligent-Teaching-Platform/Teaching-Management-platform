package com.example.mapper;

import com.example.entity.Material;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MaterialMapper {
    void insert(Material material);

    @Delete("delete from material where id=#{id}")
    void deleteById(Integer id);

    void updateById(Material material);

    @Select("select * from material")
    List<Material> selectAll();

    @Select("select * from material where course_id = #{courseId} order by create_time desc")
    List<Material> selectByCourseId(@Param("courseId") Integer courseId);

    @Select("select * from material where id = #{id}")
    Material selectById(Integer id);




//    @Select("select material.*, teacher.name as teacherName, student.name as studentName from material " +
//            "left join teacher on material.teacher_id = teacher.id " +
//            "left join student on material.student_id = student.id ")
//    List<Material> selectAll();
//
//    @Select("select material.*, student.name as studentName,teacher.name as teacherName from material " +
//            "left join teacher on material.teacher_id=teacher.id " +
//            "left join student on material.student_id=student.id " +
//            "where material.name like  concat('%',#{name},'%')")
//    List<Material> selectByName(String name);
//
//    void updateById(Material material);
//
//    @Delete("delete from material where id=#{id}")
//    void deleteById(Integer id);
//
//    //依据教师id搜索相关课程
//    @Select("select material.*, teacher.name as teacherName, student.name as studentName from material " +
//            " left join student on material.student_id=student.id " +
//            " left join teacher on material.teacher_id=teacher.id " +
//            "where student.name like  concat('%',#{name},'%') and teacher.id=#{teacherId}")
//    List<Material> selectByNameAndTeacherId(@Param("name") String name, @Param("teacherId") Integer teacherId);
//
//
//    @Select("select material.*, teacher.name as teacherName, student.name as studentName from material " +
//            "left join teacher on material.teacher_id = teacher.id " +
//            "left join student on material.student_id = student.id " +
//            "where teacher.id = #{teacherId}")
//    List<Material> selectAllByTeacherId(Integer teacherId);
//    //依据学生id搜索相关课程
//    @Select("select material.*, teacher.name as teacherName, student.name as studentName from material " +
//            "left join student on material.student_id=student.id " +
//            "left join teacher on material.teacher_id=teacher.id " +
//            "where student.name like  concat('%',#{name},'%') and student.id=#{studentId}")
//    List<Material> selectByNameAndStudentId(@Param("name") String name, @Param("studentId") Integer studentId);
//
//    @Select("select material.*, teacher.name as teacherName, student.name as studentName from material " +
//            "left join teacher on material.teacher_id = teacher.id " +
//            "left join student on material.student_id = student.id " +
//            "where student.id = #{studentId}")
//    List<Material> selectAllByStudentId(Integer studentId);

}

