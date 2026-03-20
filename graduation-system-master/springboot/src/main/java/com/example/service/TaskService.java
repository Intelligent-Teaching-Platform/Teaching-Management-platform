package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Task;
import com.example.mapper.TaskMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告信息业务层处理
 */
@Service
public class TaskService {
    @Resource
    private TaskMapper taskMapper;

    public void add(Task task) {
        // 插入 task 表
        taskMapper.insert(task);

        // 获取插入后的 task id
        Integer taskId = task.getId();
        // 获取 class_id
        Integer classId = task.getClassId();
        // 获取 task 的 name、content、lab 和 teacher_id
        String name = task.getName();
        String content = task.getContent();
        Integer lab = task.getLab();        // 获取 lab 字段
        Integer teacherId = task.getTeacherId();  // 获取 teacher_id 字段
        Integer courseId = task.getCourseId();    // 获取 course_id

        // 根据 class_id 查询 student 表中的 id
        List<Integer> studentIds = taskMapper.getStudentIdsByClassId(classId);

        // 将对应的 student_id、task_id、name、content、lab 和 teacher_id 插入到 work 表
        for (Integer studentId : studentIds) {
            taskMapper.insertIntoWork(taskId, studentId, name, content, lab, teacherId, courseId);
        }
    }

    public PageInfo<Task> selectPage(Task task, Integer pageNum, Integer pageSize) {
        List<Task> list;
        PageHelper.startPage(pageNum, pageSize);
        if (ObjectUtil.isNotEmpty(task.getTeacherId())) {
            // 教师登录，只查自己发布的任务
            Integer courseId = task.getCourseId();
            if (courseId != null) {
                if (ObjectUtil.isNotEmpty(task.getName())) {
                    list = taskMapper.selectByTeacherIdAndCourseIdAndName(task.getTeacherId(), courseId, task.getName());
                } else {
                    list = taskMapper.selectByTeacherIdAndCourseId(task.getTeacherId(), courseId);
                }
            } else {
                if (ObjectUtil.isNotEmpty(task.getName())) {
                    list = taskMapper.selectByTeacherIdAndName(task.getTeacherId(), task.getName());
                } else {
                    list = taskMapper.selectByTeacherId(task.getTeacherId());
                }
            }
        } else {
            // 管理员或其他角色，查所有任务
            if (ObjectUtil.isNotEmpty(task.getName())) {
                list = taskMapper.selectByName(task.getName());
            } else {
                list = taskMapper.selectAll();
            }
        }
        return PageInfo.of(list);
    }

    public void updateByID(Task task) {
        // 更新 task 表
        taskMapper.updateById(task);

        // 获取 task id
        Integer taskId = task.getId();
        // 获取 class_id
        Integer classId = task.getClassId();
        // 获取 task 的 name、content、lab 和 teacher_id
        String name = task.getName();
        String content = task.getContent();
        Integer lab = task.getLab();        // 获取 lab 字段
        Integer teacherId = task.getTeacherId();  // 获取 teacher_id 字段
        Integer courseId = task.getCourseId();

        // 根据 class_id 查询 student 表中的 id
        List<Integer> studentIds = taskMapper.getStudentIdsByClassId(classId);

        // 更新 work 表中与 task_id 对应的所有字段
        for (Integer studentId : studentIds) {
            taskMapper.updateWorkAllFields(taskId, studentId, name, content, lab, teacherId, courseId);
        }
    }

    public void deleteById(Integer id) {
        // 删除 task 表中的数据
        taskMapper.deleteById(id);

        // 删除 work 表中与 task_id 对应的所有数据
        taskMapper.deleteWorkByTaskId(id);
    }

    public List<Task> selectAll() {
        return taskMapper.selectAll();
    }
}