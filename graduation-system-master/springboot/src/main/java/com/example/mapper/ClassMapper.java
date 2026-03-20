package com.example.mapper;

import com.example.entity.Class;
import com.example.entity.College;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ClassMapper {
    void insert(Class clazz);

    @Select("select * from clazz where name like  concat('%',#{name},'%')")
    List<Class> selectByName(String name);

    @Select("select * from clazz")
    List<Class> selectAll();

}