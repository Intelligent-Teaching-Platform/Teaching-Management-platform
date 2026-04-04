package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.entity.College;
import com.example.mapper.CollegeMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学院信息业务层处理
 */
@Service
public class CollegeService {
    @Resource
    private CollegeMapper collegeMapper;

    public void add(College college) {
        collegeMapper.insert(college);
    }

    public PageInfo<College> selectPage(College college,Integer pageNum, Integer pageSize) {
        List<College> list;
        PageHelper.startPage(pageNum, pageSize);
        if(ObjectUtil.isNotEmpty(college.getName())) {
            list = collegeMapper.selectByName(college.getName());
        }
        else{
        list =collegeMapper.selectAll();
        }
        return PageInfo.of(list);
    }

    public void updateByID(College college) {
        collegeMapper.updateById(college);
    }

    public void deleteById(Integer id) {
        collegeMapper.deleteById(id);
    }


    public List<College> selectAll() {
        return collegeMapper.selectAll();
    }

    public College selectById(Integer id) {
        if (ObjectUtil.isEmpty(id)) {
            return null;
        }
        return collegeMapper.selectById(id);
    }

    public Map<String, Integer> getNameIdMap() {
        List<College> collegeList = collegeMapper.selectAll();
        return collegeList.stream()
                .collect(Collectors.toMap(
                        College::getName,  // 键：学院名称
                        College::getId,   // 值：学院ID
                        // 同名学院取第一个，避免 toMap 抛异常导致导入失败
                        (a, b) -> a
                ));
    }
}
