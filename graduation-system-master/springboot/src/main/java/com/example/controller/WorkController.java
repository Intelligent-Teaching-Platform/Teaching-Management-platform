package com.example.controller;

import com.example.common.Result;
import com.example.entity.Work;
import com.example.service.ExperimentReportService;
import com.example.service.WorkService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 专业信息模块前端请求接口入口
 */
@RestController
@RequestMapping("/work")
public class WorkController {
    @Resource
    private WorkService workService;
    @Resource
    private ExperimentReportService experimentReportService;
    /**
     * 增加
     */
    @PostMapping("/add")
    public Result add(@RequestBody Work work) {

        workService.add(work);
        return Result.success();
    }
    /**
     * 更新
     */
    @PutMapping("/update")
    public Result update(@RequestBody Work work) {
        workService.updateByID(work);
        return Result.success();
    }
    @PutMapping("/updateone")
    public Result updateone(@RequestBody Work work) {
        workService.updateByIDone(work);
        return Result.success();
    }

    /**
     * 分页查询接口
     */
    @GetMapping("/selectPage")
    public Result selectPage(Work work,
                             @RequestParam(required = false) Integer courseId,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "5") Integer pageSize)
    {
        PageInfo<Work> pageInfo = workService.selectPage(work, pageNum, pageSize, courseId);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectPageone")
    public Result selectPageoen(Work work,
                             @RequestParam(required = false) Integer courseId,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "5") Integer pageSize)
    {
        PageInfo<Work> pageInfo = workService.selectPageone(work, pageNum, pageSize, courseId);
        return Result.success(pageInfo);
    }
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        workService.deleteById(id);
        return Result.success();
    }
    /*
     * 查询所有
     */

    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Work> list = workService.selectAll();
        return Result.success(list);
    }
    @GetMapping("/selectAllone")
    public Result selectAllone() {
        List<Work> list = workService.selectAllone();
        return Result.success(list);
    }

    /**
     * 教师审核实验作业时查重提示：返回最大相似度及对应学生
     */
    @GetMapping("/experimentSimilarity/{id}")
    public Result experimentSimilarity(@PathVariable Integer id) {
        return Result.success(workService.experimentSimilarityHint(id));
    }

    /**
     * 学生保存实验作业分段（lab=2）：phase=1 题目，2 任务要求，3 实验报告主体
     */
    @PutMapping("/studentStage")
    public Result studentStage(@RequestParam Integer phase, @RequestBody Work work) {
        if (work.getStudentId() == null) {
            return Result.error("缺少学生信息");
        }
        workService.submitStudentStage(work.getStudentId(), phase, work);
        return Result.success();
    }

    /**
     * 教师查看某实验任务下，各分段完成人数及学生姓名
     */
    @GetMapping("/stageStats")
    public Result stageStats(@RequestParam Integer taskId) {
        Map<String, Object> stats = workService.stageStatsByTaskId(taskId);
        return Result.success(stats);
    }

    /**
     * 生成并下载实验报告 Word 文档（实验任务 lab=2 提交后可用）
     */
    @GetMapping("/report/{id}")
    public void downloadReport(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        try {
            byte[] docx = experimentReportService.generateReportDocx(id);
            String filename = URLEncoder.encode("实验报告.docx", StandardCharsets.UTF_8).replace("+", "%20");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
            response.getOutputStream().write(docx);
            response.getOutputStream().flush();
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"code\":\"500\",\"msg\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }
}