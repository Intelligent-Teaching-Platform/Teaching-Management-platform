package com.example.controller;

import com.example.common.Result;
import com.example.entity.Choice;
import com.example.entity.Course;
import com.example.entity.Student;
import com.example.service.ChoiceService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 选课信息前端请求接口入口
 */
@RestController
@RequestMapping("/choice")
public class ChoiceController {
    @Resource
    private ChoiceService choiceService;
    /**
     * 增加
     */
    @PostMapping("/add")
    public Result add(@RequestBody Course course) {

        choiceService.add(course);
        return Result.success();
    }

    @GetMapping("/selectAllChoice")
    public Result selectAllChoice(@RequestParam Integer courseId) {
        if (courseId == null) {
            return Result.error("课程ID不能为空");
        }

        try {
            List<Choice> choices = choiceService.selectStudentsByCourseId(courseId);

            // 返回成功结果
            return Result.success(choices);
        } catch (Exception e) {
            return Result.error("查询学生失败");
        }
    }

    /**
     * 更新
     */
    @PutMapping("/update")
    public Result update(@RequestBody Choice choice) {
        choiceService.updateByID(choice);
        return Result.success();
    }

    /**
     * 分页查询接口
     */
    @GetMapping("/selectPage")
    public Result selectPage(Choice choice,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "5") Integer pageSize)
    {
        PageInfo<Choice> pageInfo = choiceService.selectPage(choice,pageNum,pageSize);
        return Result.success(pageInfo);
    }
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        choiceService.deleteById(id);
        return Result.success();
    }
    /*
     * 查询所有
     */

    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Choice> list = choiceService.selectAll();
        return Result.success(list);
    }


}
