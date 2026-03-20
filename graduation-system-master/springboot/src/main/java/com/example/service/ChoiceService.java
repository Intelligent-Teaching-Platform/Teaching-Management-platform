package com.example.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Choice;
import com.example.entity.Course;
import com.example.entity.Student;
import com.example.exception.CustomException;
import com.example.mapper.ChoiceMapper;
import com.example.mapper.CourseMapper;
import com.example.mapper.StudentMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 选课信息业务层处理
 */
@Service
public class ChoiceService {
    @Resource
    private ChoiceMapper choiceMapper;
    @Resource
    private CourseMapper courseMapper;
    @Resource
    private StudentMapper studentMapper;

    public void add(Course course) {
        //先判断课程有没有满员
        if(course.getNum().equals(course.getAlreadyNum())){
            throw new CustomException("课程已满员", "题型名称不能重复");
        }
        //判断学生是否已选该课程
        List<Choice> list =choiceMapper.selectByCourseIdAndStudentId(course.getId(), course.getStudentId());
        if (CollectionUtil.isNotEmpty(list)){
            throw new CustomException("您已选过该门课，请勿重复选课", "题型名称不能重复");
        }
        //往选课信息表里插入一条选课记录
        Choice choice =new Choice();
        choice.setName(course.getName());
        choice.setTeacherId(course.getTeacherId());
        choice.setStudentId(course.getStudentId());
        choice.setCourseId(course.getId());
        choiceMapper.insert(choice);
        //更新课程已选人数+1
        course.setAlreadyNum(course.getAlreadyNum()+1);
        courseMapper.updateById(course);
        //更新学分增加
        Student student = studentMapper.selectById(course.getStudentId());
        student.setScore(student.getScore()+course.getScore());
        studentMapper.updateById(student);

    }

    public PageInfo<Choice> selectPage(Choice choice,Integer pageNum, Integer pageSize) {
        List<Choice> list;
        PageHelper.startPage(pageNum, pageSize);
        if(ObjectUtil.isNotEmpty(choice.getStudentId())) {
            //说明是学生登录进行分页查询
            if(ObjectUtil.isNotEmpty(choice.getName())) {
                list = choiceMapper.selectByNameAndStudentId(choice.getName(),choice.getStudentId());
            }
            else{
                list = choiceMapper.selectAllByStudentId(choice.getStudentId());
            }
        } else if(ObjectUtil.isNotEmpty(choice.getTeacherId())){
            //说明是老师登录进行分页查询
            if(ObjectUtil.isNotEmpty(choice.getName())) {
                list = choiceMapper.selectByNameAndTeacherId(choice.getName(),choice.getTeacherId());
            }
            else{
                list = choiceMapper.selectAllByTeacherId(choice.getTeacherId());
            }
        }
        else {//说明是管理员登录进行分页查询
            if(ObjectUtil.isNotEmpty(choice.getName())) {
                list = choiceMapper.selectByName(choice.getName());
            }
            else{
                list =choiceMapper.selectAll();
            }
        }
        return PageInfo.of(list);
    }

    public void updateByID(Choice choice) {
        choiceMapper.updateById(choice);
    }

    public void deleteById(Integer id) {
        Choice choice = choiceMapper.selectById(id);
        choiceMapper.deleteById(id);
        //对应课程信息内的已选人数-1
        Course course = courseMapper.selectById(choice.getCourseId());
        course.setAlreadyNum(course.getAlreadyNum()-1);
        courseMapper.updateById(course);
        //对应学生信息内的学分-1
        Course dbCourse = courseMapper.selectById(choice.getCourseId());
        Student student = studentMapper.selectById(choice.getStudentId());
        student.setScore(student.getScore() - dbCourse.getScore());
        studentMapper.updateById(student);
    }


    public List<Choice> selectAll() {
        return choiceMapper.selectAll();
    }


    public List<Choice> selectStudentsByCourseId(Integer courseId) {
        // 参数校验
        if (courseId == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }

        // 调用Mapper查询
        return choiceMapper.selectByCourseId(courseId);
    }


}
