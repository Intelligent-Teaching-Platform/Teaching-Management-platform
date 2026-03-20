package com.example.mapper;

import com.example.entity.Notice;
import com.example.entity.Score;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ScoreMapper {

    int insert(Score score);

    void updateById(Score score);

    void deleteById(Integer id);

    @Select("select * from `score` where id = #{id}")
    Score selectById(Integer id);

    @Select("select * from `score` where student_id = #{studentId} and paper_id = #{paperId} limit 1")
    Score selectByStudentIdAndPaperId(@Param("studentId") Integer studentId, @Param("paperId") Integer paperId);

    List<Score> selectAll(Score score);

}

