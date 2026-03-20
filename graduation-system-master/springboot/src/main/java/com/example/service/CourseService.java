package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.entity.*;
import com.example.mapper.*;
import com.example.exception.CustomException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程信息业务层处理
 */
@Service
public class CourseService {
    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private CollegeMapper collegeMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private ChoiceMapper choiceMapper;



    /**
     * 根据教师姓名查询课程
     */
    public List<Course> selectByTeacherName(String teacherName) {
        // 1. 验证教师姓名不能为空
        if (ObjectUtil.isEmpty(teacherName)) {
//            throw new CustomException("");
        }

        // 2. 根据教师姓名查询课程
        return courseMapper.selectByTeacherName(teacherName);
    }
    /**
     * 新增课程
     */
    @Transactional
    public void add(Course course) {
        // 1. 初始化已选人数为 0
        course.setAlreadyNum(0);
        // 2. 保存课程信息
        courseMapper.insert(course);

        // 3. 如果选择了班级，自动为班级中的学生选课
        if (course.getClassId() != null) {
            // 3.1 查询班级中的所有学生
            List<Student> students = studentMapper.selectByClassId(course.getClassId());
            for (Student student : students) {
                // 3.2 创建选课记录
                Choice choice = new Choice();
                choice.setCourseId(course.getId());
                choice.setStudentId(student.getId());
                choiceMapper.insert(choice);

                // 3.3 更新课程的已选人数
                course.setAlreadyNum(course.getAlreadyNum() + 1);
            }
            // 4. 更新课程的已选人数到数据库
            courseMapper.updateById(course);
        }
    }

    /**
     * 分页查询课程
     */
    public PageInfo<Course> selectPage(Course course, Integer pageNum, Integer pageSize) {
        List<Course> list;
        PageHelper.startPage(pageNum, pageSize);

        if (ObjectUtil.isNotEmpty(course.getTeacherId())) {
            // 教师登录，只查自己教的课
            Integer teacherId = course.getTeacherId();
            if (ObjectUtil.isNotEmpty(course.getName())) {
                list = courseMapper.selectByNameAndTeacherId(course.getName(), teacherId);
            } else {
                list = courseMapper.selectAllByTeacherId(teacherId);
            }
        } else if (ObjectUtil.isNotEmpty(course.getStudentId())) {
            // 学生登录，只查已选课程（选课表 choice）
            Integer studentId = course.getStudentId();
            if (ObjectUtil.isNotEmpty(course.getName())) {
                list = courseMapper.selectByNameAndStudentId(course.getName(), studentId);
            } else {
                list = courseMapper.selectAllByStudentId(studentId);
            }
        } else {
            // 管理员，查全部课程
            if (ObjectUtil.isNotEmpty(course.getName())) {
                list = courseMapper.selectByName(course.getName());
            } else {
                list = courseMapper.selectAll();
            }
        }

        return PageInfo.of(list);
    }

    /**
     * 更新课程信息
     */
    public void updateByID(Course course) {
        courseMapper.updateById(course);
    }

    /**
     * 删除课程
     */
    @Transactional
    public void deleteById(Integer id) {
        // 1. 查询课程信息
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new CustomException("课程不存在", "题型名称不能重复");
        }

        // 2. 删除课程与班级的关联关系
        if (course.getClassId() != null) {
            // 2.1 查询班级中的所有学生
            List<Student> students = studentMapper.selectByClassId(course.getClassId());
            for (Student student : students) {
                // 2.2 删除学生的选课记录
                choiceMapper.deleteByCourseIdAndStudentId(course.getId(), student.getId());
            }
        }

        // 3. 删除课程
        courseMapper.deleteById(id);
    }

    /**
     * 查询所有课程
     */
    public List<Course> selectAll() {
        return courseMapper.selectAll();
    }


    @Transactional
    public void batchInsert(List<Course> courses) {
        // 逐条插入以保证获取ID
        for (Course course : courses) {
            courseMapper.insert(course); // 使用单条插入

            if (course.getClassId() != null && course.getClassId() > 0) {
                List<Student> students = studentMapper.selectByClassId(course.getClassId());

                List<Choice> choices = students.stream()
                        .map(student -> {
                            Choice choice = new Choice();
                            choice.setCourseId(course.getId()); // 这里能获取到正确的ID
                            choice.setStudentId(student.getId());
                            choice.setName(course.getName());
                            choice.setTeacherId(course.getTeacherId());
                            choice.setCollegeId(course.getCollegeId());
                            choice.setClassId(course.getClassId());
                            return choice;
                        })
                        .collect(Collectors.toList());

                if (!choices.isEmpty()) {
                    choiceMapper.batchInsert(choices);
                    course.setAlreadyNum(choices.size());
                    courseMapper.updateById(course);
                }
            }
        }
    }
    public Course selectByName(String name) {

        // 2. 真实查询
        List<Course> courses = courseMapper.selectByName(name);

        return courses.isEmpty() ? null : courses.get(0);
    }


}
