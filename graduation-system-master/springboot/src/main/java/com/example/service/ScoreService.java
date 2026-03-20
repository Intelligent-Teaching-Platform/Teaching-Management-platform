package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.example.entity.*;
import com.example.exception.CustomException;

import com.example.mapper.NoticeMapper;
import com.example.mapper.QuestionMapper;
import com.example.mapper.ScoreMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


/**
 * 公告信息业务层处理
 */
@Service
public class ScoreService {

    @Resource
    private ScoreMapper scoreMapper;

    @Resource
    private QuestionMapper questionMapper;

    public Score add(TestPaper testPaper) {
        // 防重复提交：同一学生同一试卷只允许提交一次
        if (testPaper == null || testPaper.getStudentId() == null || testPaper.getId() == null) {
            throw new CustomException("提交信息不完整（studentId/paperId 为空）", null);
        }
        Score existed = scoreMapper.selectByStudentIdAndPaperId(testPaper.getStudentId(), testPaper.getId());
        if (existed != null) {
            throw new CustomException("该试卷已提交，不能重复提交", null);
        }

        // 封装一下用户提交的试卷信息
        List<Answer> list = new ArrayList<>();
        int objectiveTotalCount = 0;
        int objectiveCorrectCount = 0;
        for (Question question : testPaper.getQuestions()) {
            Answer answer = new Answer();
            answer.setTypeName(question.getTypeName());
            answer.setScore(question.getTypeScore());
            answer.setQuestionId(question.getId());
            answer.setNewAnswer(question.getNewAnswer());
            answer.setAnswer(question.getAnswer());
            list.add(answer);

            // 客观题自动批改：单选题/判断题
            if ("单选题".equals(question.getTypeName()) || "判断题".equals(question.getTypeName())) {
                String studentAnswer = question.getNewAnswer();
                boolean answered = studentAnswer != null && !studentAnswer.trim().isEmpty();
                objectiveTotalCount++;
                if (answered && studentAnswer.equals(question.getAnswer())) {
                    objectiveCorrectCount++;
                }
            }
        }

        Score score = new Score();

        score.setTeacherId(testPaper.getTeacherId());
        score.setCourseId(testPaper.getCourseId());
        score.setName(testPaper.getName());
        score.setPaperId(testPaper.getId());
        score.setStatus("已阅卷");
        score.setStudentId(testPaper.getStudentId());
        score.setAnswer(JSONUtil.toJsonStr(list));

        // 按客观题正确率计算总分：满分 100，保留一位小数
        double total;
        if (objectiveTotalCount <= 0) {
            total = 0d;
        } else {
            double raw = 100d * objectiveCorrectCount / objectiveTotalCount;
            total = BigDecimal.valueOf(raw).setScale(1, RoundingMode.HALF_UP).doubleValue();
        }
        score.setScore(total);

        scoreMapper.insert(score);
        return score;
    }

    public void updateById(Score score) {
        List<Answer> answerData = score.getAnswerData();
        double total = 0d;
        for (Answer answer : answerData) {
            if (ObjectUtil.isNotEmpty(answer.getResult())) {
                total += answer.getResult();
            }
        }
        score.setScore(total);
        score.setAnswer(JSONUtil.toJsonStr(answerData));
        score.setStatus("已阅卷");
        scoreMapper.updateById(score);
    }

    public void deleteById(Integer id) {
        scoreMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            scoreMapper.deleteById(id);
        }
    }

    public Score selectById(Integer id) {
        List<Score> scores = scoreMapper.selectAll(new Score());
        Score score = scores.stream().filter(x -> x.getId().equals(id)).findFirst().get();
        String answer = score.getAnswer();
        List<Answer> list = JSONUtil.toList(answer, Answer.class);

        List<Question> questions = new ArrayList<>();
        for (Answer ans : list) {
            Question question = questionMapper.selectById(ans.getQuestionId());
            if (ObjectUtil.isNotEmpty(question)) {
                if ("多选题".equals(ans.getTypeName())) {
                    String newAnswer = ans.getNewAnswer(); // A,B,C
                    List<String> checkList = Arrays.asList(newAnswer.split(","));
                } else {
                    question.setNewAnswer(ans.getNewAnswer());
                }
                questions.add(question);
            }
        }
        score.setQuestions(questions);
        return score;
    }

    public List<Score> selectAll(Score score) {
        return scoreMapper.selectAll(score);
    }

    public PageInfo<Score> selectPage(Score score, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Score> list = scoreMapper.selectAll(score);
        return PageInfo.of(list);
    }

    public List<Answer> selectAnswer(Integer id) {
        Score score = scoreMapper.selectById(id);
        List<Answer> list = JSONUtil.toList(score.getAnswer(), Answer.class);
        // 客观题自动阅卷功能
        for (Answer answer : list) {
            Question question = questionMapper.selectById(answer.getQuestionId());
            answer.setQuestionName(question.getName());
            // 客观题自动打分
            if (!"简答题".equals(answer.getTypeName())) {
                if (!"多选题".equals(answer.getTypeName())) {
                    if (answer.getAnswer().equals(answer.getNewAnswer())) {
                        answer.setResult(answer.getScore());
                    } else {
                        answer.setResult(0);
                    }
                } else {
                    // 多选题 只要选项都对就行 顺序没有关系：  A,B,C 和 B,A,C 是一样的
                    List<String> standardList = Arrays.asList(answer.getAnswer().split(",")); // [A, B, C]
                    List<String> studentList = Arrays.asList(answer.getNewAnswer().split(",")); // [B, A, C]
                    for (String s : standardList) {
                        if (!studentList.contains(s)) {
                            answer.setResult(0);
                            break;
                        }
                    }
                    if (ObjectUtil.isEmpty(answer.getResult())) {
                        if (studentList.size() == standardList.size()) {
                            answer.setResult(answer.getScore());
                        } else {
                            answer.setResult(0);
                        }
                    }
                }
            }
        }
        return list;
    }



}
