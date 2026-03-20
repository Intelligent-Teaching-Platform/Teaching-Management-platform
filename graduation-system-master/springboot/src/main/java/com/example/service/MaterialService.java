package com.example.service;

import cn.hutool.core.io.FileUtil;
import com.example.entity.Material;
import com.example.mapper.MaterialMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 课程资料业务层
 */
@Service
public class MaterialService {
    private static final String FILE_DIR = System.getProperty("user.dir") + "/files/";

    @Resource
    private MaterialMapper materialMapper;

    @Value("${fileBaseUrl:http://localhost:9090}")
    private String fileBaseUrl;

    public void add(Material material) {
        materialMapper.insert(material);
    }

    public void updateByID(Material material) {
        materialMapper.updateById(material);
    }

    public void deleteById(Integer id) {
        materialMapper.deleteById(id);
    }

    public List<Material> selectAll() {
        return materialMapper.selectAll();
    }

    public List<Material> selectByCourseId(Integer courseId) {
        if (courseId == null) return new ArrayList<>();
        return materialMapper.selectByCourseId(courseId);
    }

    public Material selectById(Integer id) {
        return materialMapper.selectById(id);
    }

    /**
     * 教师上传课程资料：保存文件到磁盘并写入资料记录。folderId 可为 null（根目录）。
     */
    public List<Material> uploadFiles(MultipartFile[] files, Integer courseId, Integer teacherId, String teacherName, Integer folderId) throws IOException {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("请选择文件");
        }
        if (!FileUtil.isDirectory(FILE_DIR)) {
            FileUtil.mkdir(FILE_DIR);
        }
        List<Material> list = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) continue;
            String fileName = System.currentTimeMillis() + "-" + originalFilename;
            String realPath = FILE_DIR + fileName;
            FileUtil.writeBytes(file.getBytes(), realPath);
            Material m = new Material();
            m.setName(originalFilename);
            m.setTeacherId(teacherId);
            m.setTeacherName(teacherName != null ? teacherName : "");
            m.setCourseId(courseId);
            m.setFolderId(folderId);
            m.setPath(fileName);
            m.setSize(file.getSize());
            m.setCreateTime(LocalDateTime.now());
            materialMapper.insert(m);
            list.add(m);
        }
        return list;
    }

    /**
     * 根据资料 id 下载文件（流式输出）
     */
    public void downloadFile(Integer id, HttpServletResponse response) throws IOException {
        Material m = materialMapper.selectById(id);
        if (m == null || m.getPath() == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String fileName = m.getPath();
        String realPath = FILE_DIR + fileName;
        if (!FileUtil.exist(realPath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String displayName = m.getName() != null ? m.getName() : fileName;
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(displayName, StandardCharsets.UTF_8));
        byte[] bytes = FileUtil.readBytes(realPath);
        ServletOutputStream os = response.getOutputStream();
        os.write(bytes);
        os.flush();
        os.close();
    }
}
