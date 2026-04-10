package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Task;
import com.example.mapper.TaskMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 公告信息业务层处理
 */
@Service
public class TaskService {
    @Resource
    private TaskMapper taskMapper;

    @Transactional
    public void add(Task task) {
        // 插入 task 表
        taskMapper.insert(task);

        // 获取插入后的 task id
        Integer taskId = task.getId();
        // 获取 task 的 name、content、lab 和 teacher_id
        String name = task.getName();
        String content = task.getContent();
        Integer lab = task.getLab();        // 获取 lab 字段
        Integer teacherId = task.getTeacherId();  // 获取 teacher_id 字段
        Integer courseId = task.getCourseId();    // 获取 course_id

        // 收集所有需要发放的班级ID（支持单班级和多班级）
        Set<Integer> classIdSet = new HashSet<>();
        
        // 1. 处理单个班级ID
        if (task.getClassId() != null) {
            classIdSet.add(task.getClassId());
        }
        
        // 2. 处理多个班级ID（逗号分隔，如 "1,2,3"）
        if (task.getClassIds() != null && !task.getClassIds().trim().isEmpty()) {
            String[] classIdArray = task.getClassIds().split(",");
            for (String idStr : classIdArray) {
                try {
                    classIdSet.add(Integer.parseInt(idStr.trim()));
                } catch (NumberFormatException e) {
                    // 忽略无效的班级ID
                }
            }
        }

        // 3. 查询所有班级下的学生ID，并去重
        Set<Integer> uniqueStudentIds = new HashSet<>();
        for (Integer classId : classIdSet) {
            List<Integer> studentIds = taskMapper.getStudentIdsByClassId(classId);
            uniqueStudentIds.addAll(studentIds);
        }

        // 4. 将对应的 student_id、task_id、name、content、lab 和 teacher_id 插入到 work 表
        for (Integer studentId : uniqueStudentIds) {
            // 检查是否已存在相同的 task_id 和 student_id 记录，避免重复插入
            if (!taskMapper.existsWorkByTaskIdAndStudentId(taskId, studentId)) {
                taskMapper.insertIntoWork(taskId, studentId, name, content, lab, teacherId, courseId);
            }
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
            // 管理员、学生等：若带 courseId 则只查该课程（避免学生看到全校任务）
            if (task.getCourseId() != null) {
                if (ObjectUtil.isNotEmpty(task.getName())) {
                    list = taskMapper.selectByCourseIdAndName(task.getCourseId(), task.getName());
                } else {
                    list = taskMapper.selectByCourseId(task.getCourseId());
                }
            } else if (ObjectUtil.isNotEmpty(task.getName())) {
                list = taskMapper.selectByName(task.getName());
            } else {
                list = taskMapper.selectAll();
            }
        }
        return PageInfo.of(list);
    }

    @Transactional
    public void updateByID(Task task) {
        // 更新 task 表
        taskMapper.updateById(task);

        // 获取 task id
        Integer taskId = task.getId();
        // 获取 task 的 name、content、lab 和 teacher_id
        String name = task.getName();
        String content = task.getContent();
        Integer lab = task.getLab();        // 获取 lab 字段
        Integer teacherId = task.getTeacherId();  // 获取 teacher_id 字段
        Integer courseId = task.getCourseId();

        // 收集所有需要更新的班级ID（支持单班级和多班级）
        Set<Integer> classIdSet = new HashSet<>();
        
        // 1. 处理单个班级ID
        if (task.getClassId() != null) {
            classIdSet.add(task.getClassId());
        }
        
        // 2. 处理多个班级ID（逗号分隔，如 "1,2,3"）
        if (task.getClassIds() != null && !task.getClassIds().trim().isEmpty()) {
            String[] classIdArray = task.getClassIds().split(",");
            for (String idStr : classIdArray) {
                try {
                    classIdSet.add(Integer.parseInt(idStr.trim()));
                } catch (NumberFormatException e) {
                    // 忽略无效的班级ID
                }
            }
        }

        // 3. 查询所有班级下的学生ID，并去重
        Set<Integer> uniqueStudentIds = new HashSet<>();
        for (Integer classId : classIdSet) {
            List<Integer> studentIds = taskMapper.getStudentIdsByClassId(classId);
            uniqueStudentIds.addAll(studentIds);
        }

        // 4. 更新 work 表中与 task_id 对应的所有字段（如果不存在则插入）
        for (Integer studentId : uniqueStudentIds) {
            // 使用 INSERT ... ON DUPLICATE KEY UPDATE 确保新学生也能创建记录
            taskMapper.updateWorkAllFields(taskId, studentId, name, content, lab, teacherId, courseId);
        }
    }

    public void deleteById(Integer id) {
        // 删除 task 表中的数据
        taskMapper.deleteById(id);

        // 删除 work 表中与 task_id 对应的所有数据
        taskMapper.deleteWorkByTaskId(id);
    }

    public List<Task> selectClassesByTeacherAndCourse(Integer teacherId, Integer courseId, String courseName) {
        if (teacherId != null) {
            return taskMapper.selectClassesByTeacherAndCourse(teacherId, courseId, courseName);
        }
        if (courseId != null) {
            return taskMapper.selectClassesByCourseId(courseId);
        }
        return List.of();
    }

    public List<Task> selectAll() {
        return taskMapper.selectAll();
    }

    public Task selectById(Integer id) {
        return taskMapper.selectById(id);
    }
}
