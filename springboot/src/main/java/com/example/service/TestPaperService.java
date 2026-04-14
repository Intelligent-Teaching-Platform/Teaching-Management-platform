package com.example.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.example.entity.*;
import com.example.exception.CustomException;
import com.example.mapper.CourseMapper;
import com.example.mapper.QuestionMapper;
import com.example.mapper.TeacherMapper;
import com.example.mapper.TestPaperMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告信息业务层处理
 */
@Service
public class TestPaperService {

    @Resource
    private TestPaperMapper testPaperMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private TeacherMapper teacherMapper;

    public void add(TestPaper testPaper) {
        if ("手动选题".equals(testPaper.getType())) {
            List<Integer> idList = testPaper.getIdList();
            testPaper.setQuestionIds(JSONUtil.toJsonStr(idList));
        }
        testPaperMapper.insert(testPaper);
    }

    public void updateById(TestPaper testPaper) {
        testPaperMapper.updateById(testPaper);
    }

    public void deleteById(Integer id) {
        testPaperMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            testPaperMapper.deleteById(id);
        }
    }

    public TestPaper selectById(Integer id) {
        TestPaper testPaper = testPaperMapper.selectById(id);
        Course course = courseMapper.selectById(testPaper.getCourseId());
        if (ObjectUtil.isNotEmpty(course)) {
            testPaper.setCourseName(course.getName());
        }
        Teacher teacher = teacherMapper.selectById(testPaper.getTeacherId());
        if (ObjectUtil.isNotEmpty(teacher)) {
            testPaper.setTeacherName(teacher.getName());
        }
        String questionIds = testPaper.getQuestionIds();
        List<Integer> idlist = JSONUtil.toList(questionIds, Integer.class);
        List<Question> questions = new ArrayList<>();
        for (Integer questionId : idlist) {
            Question question = questionMapper.selectById(questionId);
            questions.add(question);
        }
        testPaper.setQuestions(questions);
        return testPaper;
    }

    public List<TestPaper> selectAll(TestPaper testPaper) {
        return testPaperMapper.selectAll(testPaper);
    }

    public PageInfo<TestPaper> selectPage(TestPaper testPaper, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<TestPaper> list = testPaperMapper.selectAll(testPaper);
        return PageInfo.of(list);
    }



}
