package com.example.mapper;

import com.example.entity.Speciality;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SpecialityMapper {
    void insert(Speciality speciality);


    @Select("select speciality.*, college.name as collegeName from speciality left join college on speciality.college_id=college.id")
    List<Speciality> selectAll();

    @Select("select speciality.*, college.name as collegeName from speciality" +
            " left join college on speciality.college_id=college.id " +
            "where speciality.name like  concat('%',#{name},'%')")
    List<Speciality> selectByName(String name);

    List<Speciality> selectByCollegeId(@Param("collegeId") Integer collegeId, @Param("name") String name);

    void updateById(Speciality speciality);

    @Delete("delete from speciality where id=#{id}")
    void deleteById(Integer id);

    @Delete("delete from speciality where college_id=#{collegeId}")
    void deleteByCollegeId(Integer collegeId);


}

