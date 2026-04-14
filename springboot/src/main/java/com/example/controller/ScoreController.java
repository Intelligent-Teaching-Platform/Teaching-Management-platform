package com.example.controller;

import com.example.common.Result;
import com.example.entity.Notice;
import com.example.entity.Score;
import com.example.entity.TestPaper;
import com.example.entity.Answer;
import com.example.service.NoticeService;
import com.example.service.ScoreService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告模块前端请求接口入口
 */
@RestController
@RequestMapping("/score")
public class ScoreController {

    @Resource
    private ScoreService scoreService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody TestPaper testPaper) {
        Score score = scoreService.add(testPaper);
        return Result.success(score);
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody Score score) {
        scoreService.updateById(score);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        scoreService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        scoreService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Score score = scoreService.selectById(id);
        return Result.success(score);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Score score) {
        List<Score> list = scoreService.selectAll(score);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Score score,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Score> pageInfo = scoreService.selectPage(score, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectAnswer/{id}")
    public Result selectAnswer(@PathVariable Integer id) {
        List<Answer> list = scoreService.selectAnswer(id);
        return Result.success(list);
    }

}