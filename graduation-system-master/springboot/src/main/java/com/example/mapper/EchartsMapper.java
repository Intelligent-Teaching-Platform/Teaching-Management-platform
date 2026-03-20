package com.example.mapper;

import com.example.entity.Echarts;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface EchartsMapper {
    void insert(Echarts echarts);


    @Select("select * from echarts")
    List<Echarts> selectAll();


    void updateById(Echarts echarts);

    @Delete("delete from tj where id=#{id}")
    void deleteById(Integer id);


}

