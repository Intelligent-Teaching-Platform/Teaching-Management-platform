package com.example.mapper;

import com.example.entity.Teacher;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TeacherMapper {

    void insert(Teacher teacher);
    @Select("select * from teacher where username=#{username}")
    Teacher selectByUsername(String username);


    @Select("select teacher.*, speciality.name specialityName, college.name as collegeName from teacher " +
            "left join speciality on teacher.speciality_id=speciality.id " +
            "left join college on speciality.college_id = college.id")
    List<Teacher> selectAll();

    @Select("select teacher.*, speciality.name as specialityName, college.name as collegeName from teacher " +
            "left join speciality on teacher.speciality_id = speciality.id " +
            "left join college on speciality.college_id = college.id " +
            "where name like  concat('%',#{name},'%')")
    List<Teacher> selectByName(String name);

    void updateById(Teacher teacher);

    @Delete("delete from teacher where id=#{id}")
    void deleteById(Integer id);

    @Select("SELECT teacher.*, speciality.name AS specialityName, college.name as collegeName FROM teacher " +
            "LEFT JOIN speciality ON teacher.speciality_id = speciality.id " +
            "left join college on speciality.college_id = college.id " +
            "WHERE teacher.id = #{id}")
    Teacher selectById(Integer id);

    void batchInsert(List<Teacher> teachers);
}

