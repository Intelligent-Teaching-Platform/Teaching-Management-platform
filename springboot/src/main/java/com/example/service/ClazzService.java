package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Clazz;
import com.example.mapper.ClazzMapper;
import com.example.mapper.StudentMapper;
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
public class ClazzService {
    @Resource
    private ClazzMapper clazzMapper;

    @Resource
    private StudentMapper studentMapper;

    public void add(Clazz clazz) {
        clazzMapper.insert(clazz);
    }

    public PageInfo<Clazz> selectPage(Clazz clazz,Integer pageNum, Integer pageSize) {
        List<Clazz> list;
        PageHelper.startPage(pageNum, pageSize);
        if(ObjectUtil.isNotEmpty(clazz.getName())) {
            list = clazzMapper.selectByName(clazz.getName());
        }
        else{
        list =clazzMapper.selectAll();
        }
        // 填充每个班级的学生人数
        for (Clazz c : list) {
            int count = studentMapper.countByClassId(c.getId());
            c.setStudentNum(count);
        }
        return PageInfo.of(list);
    }

    public void updateByID(Clazz clazz) {
        clazzMapper.updateById(clazz);
    }

    public void deleteById(Integer id) {
        clazzMapper.deleteById(id);
    }


    public List<Clazz> selectAll() {
        return clazzMapper.selectAll();
    }

    /**
     * 班级名称 -> 班级ID
     * 用于导入课程等场景：当 Excel 里填的是班级名称而非ID时进行转换。
     */
    public Map<String, Integer> getNameIdMap() {
        List<Clazz> classList = clazzMapper.selectAll();
        return classList.stream()
                .collect(Collectors.toMap(
                        c -> {
                            String n = c.getName();
                            if (n == null) return "";
                            // 去掉全角空格与所有空白字符，避免 Excel/数据库存在不可见差异
                            return n.replace('\u3000', ' ').replaceAll("\\s+", "").trim();
                        },
                        Clazz::getId,
                        // 相同名称取第一个（避免 toMap 抛异常）
                        (a, b) -> a
                ));
    }


}
