package com.example.controller;

import com.example.common.Result;
import com.example.service.DashboardService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 教师统计看板接口（对应图片：/screen/*）
 */
@RestController
@RequestMapping("/screen")
public class ScreenController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping("/left1")
    public Result left1(@RequestParam Integer teacherId) {
        return Result.success(dashboardService.screenLeft1(teacherId));
    }

    @GetMapping("/left2")
    public Result left2(@RequestParam Integer teacherId, @RequestParam Integer courseId) {
        return Result.success(dashboardService.screenLeft2(teacherId, courseId));
    }

    @GetMapping("/left3")
    public Result left3(@RequestParam Integer teacherId, @RequestParam Integer courseId) {
        return Result.success(dashboardService.screenLeft3(teacherId, courseId));
    }

    @GetMapping("/mid1")
    public Result mid1(@RequestParam Integer teacherId) {
        return Result.success(dashboardService.screenMid1(teacherId));
    }

    @GetMapping("/mid2")
    public Result mid2(@RequestParam Integer teacherId) {
        return Result.success(dashboardService.screenMid2(teacherId));
    }

    @GetMapping("/mid3")
    public Result mid3(@RequestParam Integer teacherId, @RequestParam Integer courseId) {
        return Result.success(dashboardService.screenMid3(teacherId, courseId));
    }

    @GetMapping("/right1")
    public Result right1(@RequestParam Integer courseId) {
        return Result.success(dashboardService.screenRight1(courseId));
    }

    @GetMapping("/right2")
    public Result right2(@RequestParam Integer teacherId, @RequestParam Integer courseId) {
        return Result.success(dashboardService.screenRight2(teacherId, courseId));
    }

    @GetMapping("/right3")
    public Result right3(@RequestParam Integer teacherId, @RequestParam(required = false) Integer courseId) {
        return Result.success(dashboardService.screenRight3(teacherId, courseId));
    }
}

