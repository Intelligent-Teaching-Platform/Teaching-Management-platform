package com.example.mapper;

import com.example.entity.Notice;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface NoticeMapper {
    void insert(Notice notice);


    @Select("select * from notice")
    List<Notice> selectAll();

    @Select("select * from notice where title like  concat('%',#{title},'%')")
    List<Notice> selectByTitle(String title);

    void updateById(Notice notice);

    @Delete("delete from notice where id=#{id}")
    void deleteById(Integer id);


}

