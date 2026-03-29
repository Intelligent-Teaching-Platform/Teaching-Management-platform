package com.example.controller;

import com.example.common.Result;
import com.example.entity.Task;
import com.example.service.TaskService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专业信息模块前端请求接口入口
 */
@RestController
@RequestMapping("/task")
public class TaskController {
    @Resource
    private TaskService taskService;

    /**
     * 获取当前教师+课程关联的班级列表（用于发布任务时选择班级）
     * @param teacherId 教师ID
     * @param courseId 课程ID（优先使用，更精确）
     * @param courseName 课程名称（兼容旧参数）
     */
    @GetMapping("/selectClasses")
    public Result selectClasses(
            @RequestParam(required = false) Integer teacherId,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String courseName) {
        List<Task> list = taskService.selectClassesByTeacherAndCourse(teacherId, courseId, courseName);
        return Result.success(list);
    }

    /**
     * 增加
     */
    @PostMapping("/add")
    public Result add(@RequestBody Task task) {

        taskService.add(task);
        return Result.success();
    }
    /**
     * 更新
     */
    @PutMapping("/update")
    public Result update(@RequestBody Task task) {
        taskService.updateByID(task);
        return Result.success();
    }

    /**
     * 分页查询接口
     */
    @GetMapping("/selectPage")
    public Result selectPage(Task task,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "5") Integer pageSize)
    {
        PageInfo<Task> pageInfo = taskService.selectPage(task,pageNum,pageSize);
        return Result.success(pageInfo);
    }
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        taskService.deleteById(id);
        return Result.success();
    }
    /*
     * 查询所有
     */

    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Task> list = taskService.selectAll();
        return Result.success(list);
    }

}