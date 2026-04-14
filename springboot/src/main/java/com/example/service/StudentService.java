package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Account;
import com.example.entity.Choice;
import com.example.entity.Student;
import com.example.exception.CustomException;
import com.example.mapper.StudentMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 学生信息业务层处理
 */
@Service
public class StudentService {
    @Resource
    private StudentMapper studentMapper;

    public void add(Student student) {
        //  做一些逻辑处理
        Student dbStudent = studentMapper.selectByUsername(student.getUsername());
        if(ObjectUtil.isNotEmpty(dbStudent)) {
            throw new CustomException("用户名已存在", "题型名称不能重复");
        }
        if(ObjectUtil.isEmpty(student.getPassword())) {
            student.setPassword("123456");
        }if (ObjectUtil.isEmpty(student.getName())) {
            student.setName(student.getUsername());
        }
        student.setRole("STUDENT");
        student.setScore(0);
        studentMapper.insert(student);
    }

    public PageInfo<Student> selectPage(Student student,Integer pageNum, Integer pageSize) {
        List<Student> list;
        PageHelper.startPage(pageNum, pageSize);
        if(ObjectUtil.isNotEmpty(student.getName())) {
            list = studentMapper.selectByName(student.getName());
        }
        else{
        list =studentMapper.selectAll();
        }
        return PageInfo.of(list);
    }

    public void updateByID(Student student) {
        studentMapper.updateById(student);
    }

    public void deleteById(Integer id) {
        studentMapper.deleteById(id);
    }

    /**
     * 登录
     */
    public Student login(Account account) {
        Student dbStudent = studentMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbStudent)) {
            throw new CustomException("用户不存在", "题型名称不能重复");
        }
        if (!account.getPassword().equals(dbStudent.getPassword())) {
            throw new CustomException("账号或密码错误", "题型名称不能重复");
        }
        if (ObjectUtil.isEmpty(dbStudent.getRole())) {
            dbStudent.setRole("STUDENT");
        }
        return dbStudent;
    }

    /**
     *
     *注册
     *
     */
    public void register(Account account) {
        Student student = new Student();
        student.setUsername(account.getUsername());
        student.setPassword(account.getPassword());
        add(student);
    }

    public void updatePassword(Account account) {
        Student dbStudent = studentMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbStudent)) {
            throw new CustomException("用户不存在", "题型名称不能重复");
        }
        if (!account.getPassword().equals(dbStudent.getPassword())) {
            throw new CustomException("原密码错误", "题型名称不能重复");
        }
        dbStudent.setPassword(account.getNewPassword());
        studentMapper.updateById(dbStudent);
    }

    public Student selectById(Integer id) {
        return studentMapper.selectById(id);
    }

    public void resetPassword(Integer id) {
        Student student = studentMapper.selectById(id);
        if (ObjectUtil.isNull(student)) {
            throw new CustomException("学生不存在", null);
        }
        student.setPassword("123456");
        studentMapper.updateById(student);
    }

    @Transactional  //添加事务管理

    public void batchInsert(List<Student> students) {
        studentMapper.batchInsert(students);
    }

    public List <Student> selectByCourseId(Integer courseId) {
        return studentMapper.selectByCourseId(courseId);
    }


    public List<Student> selectAll() {
        return studentMapper.selectAll();
    }

    public List<Student> FindAll() {
        return studentMapper.FindAll();
    }

}
