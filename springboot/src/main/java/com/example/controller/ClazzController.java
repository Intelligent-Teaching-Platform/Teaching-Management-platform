package com.example.controller;

import com.example.common.Result;
import com.example.entity.Clazz;
import com.example.service.ClazzService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学院模块前端请求接口入口
 */
@RestController
@RequestMapping("/clazz")
public class ClazzController {
    @Resource
    private ClazzService clazzService;
    /**
     * 增加
     */
    @PostMapping("/add")
    public Result add(@RequestBody Clazz clazz) {

        clazzService.add(clazz);
        return Result.success();
    }
/**
 * 更新
 */
    @PutMapping("/update")
    public Result update(@RequestBody Clazz clazz) {
        clazzService.updateByID(clazz);
        return Result.success();
    }

    /**
     * 分页查询接口
     */
    @GetMapping("/selectPage")
    public Result selectPage(Clazz clazz,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "5") Integer pageSize)
    {
        PageInfo<Clazz> pageInfo = clazzService.selectPage(clazz,pageNum,pageSize);
        return Result.success(pageInfo);
    }
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        clazzService.deleteById(id);
        return Result.success();
    }
    /*
     * 查询所有
     */

    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Clazz> list = clazzService.selectAll();
        return Result.success(list);
    }

}