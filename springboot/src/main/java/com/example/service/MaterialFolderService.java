package com.example.service;

import com.example.entity.MaterialFolder;
import com.example.mapper.MaterialFolderMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MaterialFolderService {
    @Resource
    private MaterialFolderMapper materialFolderMapper;

    public List<MaterialFolder> listByCourseId(Integer courseId) {
        if (courseId == null) return List.of();
        return materialFolderMapper.selectByCourseId(courseId);
    }

    public MaterialFolder create(String name, Integer courseId, Integer parentId, Integer teacherId) {
        MaterialFolder f = new MaterialFolder();
        f.setName(name);
        f.setCourseId(courseId);
        f.setParentId(parentId);
        f.setTeacherId(teacherId);
        f.setCreateTime(LocalDateTime.now());
        materialFolderMapper.insert(f);
        return f;
    }

    public void deleteById(Integer id) {
        materialFolderMapper.deleteById(id);
    }
}
