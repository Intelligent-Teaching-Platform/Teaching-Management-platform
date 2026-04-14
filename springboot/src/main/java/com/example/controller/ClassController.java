package com.example.controller;

import com.example.entity.Class;
import com.example.entity.College;
import com.example.service.ClassService;
import com.example.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/class")
public class ClassController {

    @Autowired
    private ClassService classService;


    /*
     * 查询所有
     */

    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Class> list = classService.selectAll();
        return Result.success(list);
    }
}

