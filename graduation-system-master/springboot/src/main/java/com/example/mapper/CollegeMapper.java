package com.example.mapper;

import com.example.entity.College;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CollegeMapper {
    void insert(College college);


    @Select("select c.id, c.name, c.content, c.score, IFNULL(count(s.id), 0) as num " +
            "from college c " +
            "left join student s on s.college_id = c.id " +
            "group by c.id, c.name, c.content, c.score")
    List<College> selectAll();

    @Select("select c.id, c.name, c.content, c.score, IFNULL(count(s.id), 0) as num " +
            "from college c " +
            "left join student s on s.college_id = c.id " +
            "where c.name like concat('%',#{name},'%') " +
            "group by c.id, c.name, c.content, c.score")
    List<College> selectByName(String name);

    void updateById(College college);

    @Delete("delete from college where id=#{id}")
    void deleteById(Integer id);


}

