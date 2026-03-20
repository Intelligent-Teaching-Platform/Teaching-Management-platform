package com.example.controller;

import com.example.common.Result;
import com.example.entity.College;
import com.example.service.CollegeService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学院模块前端请求接口入口
 */
@RestController
@RequestMapping("/college")
public class CollegeController {
    @Resource
    private CollegeService collegeService;
    /**
     * 增加
     */
    @PostMapping("/add")
    public Result add(@RequestBody College college) {

        collegeService.add(college);
        return Result.success();
    }
/**
 * 更新
 */
    @PutMapping("/update")
    public Result update(@RequestBody College college) {
        collegeService.updateByID(college);
        return Result.success();
    }

    /**
     * 分页查询接口
     */
    @GetMapping("/selectPage")
    public Result selectPage(College college,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "5") Integer pageSize)
    {
        PageInfo<College> pageInfo = collegeService.selectPage(college,pageNum,pageSize);
        return Result.success(pageInfo);
    }
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        collegeService.deleteById(id);
        return Result.success();
    }
    /*
     * 查询所有
     */

    @GetMapping("/selectAll")
    public Result selectAll() {
        List<College> list = collegeService.selectAll();
        return Result.success(list);
    }

}