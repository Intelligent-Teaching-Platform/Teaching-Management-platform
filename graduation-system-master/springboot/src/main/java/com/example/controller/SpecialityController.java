package com.example.controller;

import com.example.common.Result;
import com.example.entity.Speciality;
import com.example.service.SpecialityService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专业信息模块前端请求接口入口
 */
@RestController
@RequestMapping("/speciality")
public class SpecialityController {
    @Resource
    private SpecialityService specialityService;
    /**
     * 增加
     */
    @PostMapping("/add")
    public Result add(@RequestBody Speciality speciality) {

        specialityService.add(speciality);
        return Result.success();
    }
/**
 * 更新
 */
    @PutMapping("/update")
    public Result update(@RequestBody Speciality speciality) {
        specialityService.updateByID(speciality);
        return Result.success();
    }

    /**
     * 分页查询接口
     */
    @GetMapping("/selectPage")
    public Result selectPage(Speciality speciality,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "5") Integer pageSize)
    {
        PageInfo<Speciality> pageInfo = specialityService.selectPage(speciality,pageNum,pageSize);
        return Result.success(pageInfo);
    }
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        specialityService.deleteById(id);
        return Result.success();
    }
    /*
     * 查询所有
     */

    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Speciality> list = specialityService.selectAll();
        return Result.success(list);
    }

}