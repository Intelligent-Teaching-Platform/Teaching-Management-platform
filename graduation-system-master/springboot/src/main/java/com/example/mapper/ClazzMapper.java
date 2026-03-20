package com.example.mapper;

import com.example.entity.Clazz;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ClazzMapper {
    void insert(Clazz clazz);


    @Select("select * from clazz")
    List<Clazz> selectAll();

    @Select("select * from clazz where name like  concat('%',#{name},'%')")
    List<Clazz> selectByName(String name);

    void updateById(Clazz clazz);

    @Delete("delete from clazz where id=#{id}")
    void deleteById(Integer id);

    @Select("select * from clazz where id=#{id}")
    Clazz selectById(Integer id);



}

