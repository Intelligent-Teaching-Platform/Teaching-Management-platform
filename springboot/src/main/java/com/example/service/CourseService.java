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
import java.util.HashMap;
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
            // 学生：已选课（choice）+ 绑定其所在班级的课程（course.class_id），避免仅有班级课但无选课记录时列表为空
            Integer studentId = course.getStudentId();
            Student stu = studentMapper.selectById(studentId);
            Integer classId = null;
            if (stu != null && stu.getClassId() > 0) {
                classId = stu.getClassId();
            }
            String nameFilter = ObjectUtil.isNotEmpty(course.getName()) ? course.getName() : null;
            list = courseMapper.selectForStudent(studentId, classId, nameFilter);
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
    @Transactional
    public void updateByID(Course course) {
        courseMapper.updateById(course);
        Course fresh = courseMapper.selectById(course.getId());
        if (fresh != null && fresh.getClassId() != null && fresh.getClassId() > 0) {
            backfillChoicesForSingleCourse(fresh);
            refreshCourseAlreadyNum(fresh.getId());
        }
    }

    /**
     * 为单门「已绑定班级」的课程补全班级内学生选课记录（仅插入尚不存在的 choice）
     */
    private int backfillChoicesForSingleCourse(Course course) {
        if (course == null || course.getId() == null || course.getClassId() == null || course.getClassId() <= 0) {
            return 0;
        }
        int inserted = 0;
        List<Student> students = studentMapper.selectByClassId(course.getClassId());
        for (Student student : students) {
            List<Choice> existing = choiceMapper.selectByCourseIdAndStudentId(course.getId(), student.getId());
            if (existing != null && !existing.isEmpty()) {
                continue;
            }
            Choice choice = new Choice();
            choice.setCourseId(course.getId());
            choice.setStudentId(student.getId());
            choice.setName(course.getName());
            choice.setTeacherId(course.getTeacherId());
            choiceMapper.insert(choice);
            inserted++;
        }
        return inserted;
    }

    private void refreshCourseAlreadyNum(Integer courseId) {
        Course db = courseMapper.selectById(courseId);
        if (db == null) {
            return;
        }
        db.setAlreadyNum(choiceMapper.countByCourseId(courseId));
        courseMapper.updateById(db);
    }

    /**
     * 全量补全：所有带班级的课程 ↔ 该班学生选课记录，并校正已选人数
     */
    @Transactional
    public Map<String, Object> backfillAllChoicesByClass() {
        int inserted = 0;
        List<Course> all = courseMapper.selectAll();
        for (Course course : all) {
            if (course.getClassId() == null || course.getClassId() <= 0) {
                continue;
            }
            inserted += backfillChoicesForSingleCourse(course);
            refreshCourseAlreadyNum(course.getId());
        }
        Map<String, Object> ret = new HashMap<>(4);
        ret.put("insertedChoices", inserted);
        ret.put("message", "已为绑定班级的课程补全选课记录，并同步已选人数");
        return ret;
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
