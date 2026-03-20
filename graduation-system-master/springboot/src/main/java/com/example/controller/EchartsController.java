package com.example.controller;

import com.example.common.Result;
import com.example.entity.Student;
import com.example.entity.Echarts;
import com.example.service.EchartsService;
import com.example.service.StudentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 专业信息模块前端请求接口入口
 */
@RestController
@RequestMapping("/echarts")
public class EchartsController {
    @Resource
    private StudentService studentService;
    @Resource
    private EchartsService echartsService;

    @GetMapping("/bie")
    public Result bie() {
        // 查询所有学生
        List<Student> list = studentService.FindAll();

        // 按照分数进行分组统计
        Map<String, Long> scoreGroups = list.stream()
                .filter(x -> x.getScore() != null) // 过滤掉分数为null的学生
                .collect(Collectors.groupingBy(
                        student -> {
                            int score = student.getScore();
                            // 按照分数段进行分组
                            if (score < 60) return "0-59分";
                            else if (score < 70) return "60-69分";
                            else if (score < 80) return "70-79分";
                            else if (score < 90) return "80-89分";
                            else return "90-100分";
                        },
                        Collectors.counting()
                ));

        // 转换为ECharts需要的格式
        List<Map<String, Object>> chartData = scoreGroups.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("value", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        // 返回统计结果
        return Result.success(chartData);
    }
    @GetMapping("/selectAll")
        public Result selectAll() {
        List<Echarts> list = echartsService.selectAll();
        return Result.success(list);
        }


}