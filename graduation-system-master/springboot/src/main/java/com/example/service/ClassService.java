package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Class;
import com.example.mapper.ClassMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学院信息业务层处理
 */
@Service
public class ClassService {
    @Resource
    private ClassMapper ClassMapper;

    public void add(Class clazz) {
        ClassMapper.insert(clazz);
    }

//    public PageInfo<Class> selectPage(Class Class,Integer pageNum, Integer pageSize) {
//        List<Class> list;
//        PageHelper.startPage(pageNum, pageSize);
//        if(ObjectUtil.isNotEmpty(Class.getName())) {
//            list = ClassMapper.selectByName(Class.getName());
//        }
//        else{
//            list =ClassMapper.selectAll();
//        }
//        return PageInfo.of(list);
//    }
//
//    public void updateByID(Class Class) {
//        ClassMapper.updateById(Class);
//    }
//
//    public void deleteById(Integer id) {
//        ClassMapper.deleteById(id);
//    }


    public List<Class> selectAll() {
        return ClassMapper.selectAll();
    }
}

