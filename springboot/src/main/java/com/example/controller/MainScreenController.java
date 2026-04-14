package com.example.controller;

import com.example.common.Result;
import com.example.service.DashboardService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员统计大屏接口（对应图片：/mainscreen/*）
 */
@RestController
@RequestMapping("/mainscreen")
public class MainScreenController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping("/left1")
    public Result left1() {
        return Result.success(dashboardService.mainLeft1());
    }

    @GetMapping("/left2")
    public Result left2() {
        return Result.success(dashboardService.mainLeft2());
    }

    @GetMapping("/left3")
    public Result left3() {
        return Result.success(dashboardService.mainLeft3());
    }

    @GetMapping("/mid1")
    public Result mid1() {
        return Result.success(dashboardService.mainMid1());
    }

    @GetMapping("/mid2")
    public Result mid2() {
        return Result.success(dashboardService.mainMid2());
    }

    @GetMapping("/mid3")
    public Result mid3(@RequestParam(required = false) Integer courseId) {
        return Result.success(dashboardService.mainMid3(courseId));
    }

    @GetMapping("/right1")
    public Result right1() {
        return Result.success(dashboardService.mainRight1());
    }

    @GetMapping("/right2")
    public Result right2() {
        return Result.success(dashboardService.mainRight2());
    }

    @GetMapping("/right3")
    public Result right3() {
        return Result.success(dashboardService.mainRight3());
    }
}

