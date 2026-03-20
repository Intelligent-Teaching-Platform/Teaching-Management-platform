package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Account;
import com.example.entity.Student;
import com.example.entity.Teacher;
import com.example.exception.CustomException;
import com.example.mapper.TeacherMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 教师信息业务层处理
 */
@Service
public class TeacherService {
    @Resource
    private TeacherMapper teacherMapper;

    public void add(Teacher teacher) {
        //  做一些逻辑处理
        Teacher dbTeacher = teacherMapper.selectByUsername(teacher.getUsername());
        if(ObjectUtil.isNotEmpty(dbTeacher)) {
            throw new CustomException("用户名已存在", "题型名称不能重复");
        }
        if(ObjectUtil.isEmpty(teacher.getPassword())) {
            teacher.setPassword("123456");
        }
        teacher.setRole("TEACHER");
        teacherMapper.insert(teacher);
    }

    public PageInfo<Teacher> selectPage(Teacher teacher,Integer pageNum, Integer pageSize) {
        List<Teacher> list;
        PageHelper.startPage(pageNum, pageSize);
        if(ObjectUtil.isNotEmpty(teacher.getName())) {
            list = teacherMapper.selectByName(teacher.getName());
        }
        else{
        list =teacherMapper.selectAll();
        }
        return PageInfo.of(list);
    }

    public void updateByID(Teacher teacher) {
        teacherMapper.updateById(teacher);
    }

    public void deleteById(Integer id) {
        teacherMapper.deleteById(id);
    }

    /**
     * 登录
     */
    public Teacher login(Account account) {
        Teacher dbTeacher = teacherMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbTeacher)) {
            throw new CustomException("用户不存在", "题型名称不能重复");
        }
        if (!account.getPassword().equals(dbTeacher.getPassword())) {
            throw new CustomException("账号或密码错误", "题型名称不能重复");
        }
        return dbTeacher;
    }

    public void updatePassword(Account account) {
        Teacher dbTeacher = teacherMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbTeacher)) {
            throw new CustomException("用户不存在", "题型名称不能重复");
        }
        if (!account.getPassword().equals(dbTeacher.getPassword())) {
            throw new CustomException("原密码错误", "题型名称不能重复");
        }
        dbTeacher.setPassword(account.getNewPassword());
        teacherMapper.updateById(dbTeacher);
    }

    public List<Teacher> selectAll() {
        return teacherMapper.selectAll();
    }

    public Map<String, Integer> getNameIdMap() {
        return teacherMapper.selectAll().stream()
                .collect(Collectors.toMap(Teacher::getName, Teacher::getId));
    }

    @Transactional  //添加事务管理
    public void batchInsert(List<Teacher> teachers) {
        teacherMapper.batchInsert(teachers);
    }

}

