package com.example.service;

import com.example.entity.Echarts;
import com.example.mapper.EchartsMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告信息业务层处理
 */
@Service
public class EchartsService {
    @Resource
    private EchartsMapper echartsMapper;

//    public void add(Echarts echarts) {
//        echarts.setTime(DateUtil.now());
//        echartsMapper.insert(echarts);
//    }
//
//
//    public void updateByID(Echarts echarts) {
//        echartsMapper.updateById(echarts);
//    }
//
//    public void deleteById(Integer id) {
//        echartsMapper.deleteById(id);
//    }


    public List<Echarts> selectAll() {
        return echartsMapper.selectAll();
    }
}
