package com.example.controller;

import com.example.common.Result;
import com.example.dto.*;
import com.example.entity.Clazz;
import com.example.entity.Course;
import com.example.entity.SignIn;
import com.example.service.CourseService;
import com.example.service.SignInService;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.example.exception.GlobalExceptionHandler.log;

@RestController
@RequestMapping("/signIn")
@RequiredArgsConstructor
public class SignInController {
    private final SignInService signInService;
    private final CourseService courseService;

    @GetMapping("selectByName")
    public Result selectByName(@RequestParam String name) {
        Course course = courseService.selectByName(name);
        return Result.success(course);
    }

    @PostMapping("/create")
    public Result createSignIn(@RequestBody SignInCreateDTO dto) {
        log.info("收到签到请求: {}", dto);
        if(dto.getTeacherId() == null || dto.getCourseId() == null) {
            log.error("缺少必要参数: teacherId={}, courseId={}",
                    dto.getTeacherId(), dto.getCourseId());
            return Result.error("缺少教师ID或课程ID");
        }
        return Result.success(signInService.createSignIn(dto));
    }

    @PostMapping("/record")
    public Result recordSignIn(@RequestBody SignInRecordDTO dto) {
        signInService.recordSignIn(dto);
        return Result.success();
    }

    @GetMapping("/selectAll")
    public Result selectAll(@RequestParam(required = false) Integer courseId) {
        return Result.success(signInService.selectAll(courseId));
    }

    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam Integer courseId,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "5") Integer pageSize) {
        PageInfo<SignIn> pageInfo = signInService.selectPage(courseId, pageNum, pageSize);
        return Result.success(pageInfo);
    }

}