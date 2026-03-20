package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Account;
import com.example.entity.Notice;
import com.example.entity.Question;
import com.example.mapper.NoticeMapper;
import com.example.mapper.QuestionMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告信息业务层处理
 */
@Service
public class QuestionService {

    @Resource
    private QuestionMapper questionMapper;

    public void add(Question question) {


        questionMapper.insert(question);
    }

    public void updateById(Question question) {
        questionMapper.updateById(question);
    }

    public void deleteById(Integer id) {
        questionMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            questionMapper.deleteById(id);
        }
    }

    public Question selectById(Integer id) {
        return questionMapper.selectById(id);
    }

    public List<Question> selectAll(Question question) {
        return questionMapper.selectAll(question);
    }

    public PageInfo<Question> selectPage(Question question, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Question> list = questionMapper.selectAll(question);
        return PageInfo.of(list);
    }

}
