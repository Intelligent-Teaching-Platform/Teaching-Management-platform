package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Clazz;
import com.example.entity.SignIn;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface SignInMapper extends BaseMapper<SignIn> {
    @Select("SELECT * FROM sign_in WHERE course_id = #{courseId} AND end_time > NOW() ORDER BY end_time DESC LIMIT 1")
    SignIn selectActiveByCourseId(@Param("courseId") Integer courseId);

    @Select("SELECT DATE_FORMAT(sr.sign_time, '%Y-%m-%d') AS date, COUNT(*) AS count " +
            "FROM sign_in_record sr JOIN sign_in si ON sr.sign_in_id = si.id " +
            "WHERE si.course_id = #{courseId} AND sr.status = 'SUCCESS' " +
            "GROUP BY DATE_FORMAT(sr.sign_time, '%Y-%m-%d') " +
            "ORDER BY date")
    List<Map<String, Object>> selectSignInStats(@Param("courseId") Integer courseId);

    @Insert("INSERT INTO sign_in (course_id, teacher_id, latitude, longitude, distance, start_time, end_time, snum) " +
            "VALUES (#{courseId}, #{teacherId},  #{latitude}, #{longitude}, #{distance}, #{startTime}, #{endTime}, #{snum})")
    int insert(SignIn signIn);

    @Select("SELECT * FROM sign_in WHERE course_id = #{courseId} AND end_time > #{now} LIMIT 1")
    SignIn selectThis(@Param("courseId") Integer courseId, @Param("now") Date now);

    @Update("UPDATE sign_in SET num = num + 1 WHERE id = #{signInId}")
    void updateSignInNum(@Param("signInId") Integer signInId);

    @Select("SELECT num FROM sign_in ORDER BY id DESC LIMIT 1")
    Integer selectNum();

    @Select("SELECT snum FROM sign_in ORDER BY id DESC LIMIT 1")
    Integer selectSnum();

    @Select("SELECT * FROM sign_in WHERE id = #{id}")
    SignIn selectById(@Param("id") Integer id);

    @Select("SELECT * FROM sign_in WHERE id = #{id}")
    List<SignIn> selectByID(@Param("id") Integer id);

    @Select("SELECT * FROM sign_in WHERE course_id = #{courseId}")
    List<SignIn> selectByCourseId(Integer courseId);

    @Select("select * from sign_in")
    List<SignIn> selectAll();

}
