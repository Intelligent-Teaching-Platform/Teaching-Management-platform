package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.SignInRecord;
import com.example.exception.ServiceException;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SignInRecordMapper extends BaseMapper<SignInRecord> {
    @Select("SELECT sr.*, s.name AS student_name, c.name AS course_name " +
            "FROM sign_in_record sr " +
            "JOIN sign_in si ON sr.sign_in_id = si.id " +
            "JOIN student s ON sr.student_id = s.id " +
            "JOIN course c ON si.course_id = c.id " +
            "WHERE si.course_id = #{courseId} " +
            "ORDER BY sr.sign_time DESC")
    List<SignInRecord> selectByCourseId(@Param("courseId") Integer courseId);

    @Select("SELECT COUNT(*) FROM sign_in_record WHERE token = #{token} LIMIT 1")
    boolean existsByToken(@Param("token") String token);

    @Select("SELECT 1 FROM sign_in_record WHERE sign_in_id = #{signInId} AND student_id = #{studentId} LIMIT 1")
    Integer existsRecord(@Param("studentId") Integer studentId,@Param("signInId") Integer signInId);

    int insert(SignInRecord record);


}