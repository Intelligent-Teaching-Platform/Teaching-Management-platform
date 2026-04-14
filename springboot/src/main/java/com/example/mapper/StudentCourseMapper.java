package com.example.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.StudentCourse;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface StudentCourseMapper extends BaseMapper<StudentCourse> {
    // 修改方法定义，接受2个参数
    @Select("SELECT COUNT(*) FROM student_course WHERE student_id = #{studentId} AND course_id = #{courseId}")
    int exists(@Param("studentId") Integer studentId, @Param("courseId") Integer courseId);

    // 或者使用MyBatis-Plus的Lambda写法（推荐）
    default boolean existsRelation(Integer studentId, Integer courseId) {
        return this.selectCount(new LambdaQueryWrapper<StudentCourse>()
                .eq(StudentCourse::getStudentId, studentId)
                .eq(StudentCourse::getCourseId, courseId)) > 0;
    }
}