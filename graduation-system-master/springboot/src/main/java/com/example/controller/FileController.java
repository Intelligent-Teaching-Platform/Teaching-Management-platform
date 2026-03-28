package com.example.controller;

import cn.hutool.core.io.FileUtil;
import com.example.common.Result;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.File;

/**
 * 文件相关操作接口
 */
@RestController
@RequestMapping("/files")
public class FileController {

    /** 可选：通过配置项指定上传目录，未配置则使用 <user.dir>/files */
    @Value("${fileUploadPath:}")
    private String fileUploadPath;

    private String getStorageDir() {
        if (fileUploadPath != null && !fileUploadPath.trim().isEmpty()) {
            return fileUploadPath.trim();
        }
        return System.getProperty("user.dir") + File.separator + "files";
    }

    private String decodeFileName(String fileName) {
        if (fileName == null) return "";
        try {
            return URLDecoder.decode(fileName, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return fileName;
        }
    }

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("上传失败：文件为空");
        }

        String original = file.getOriginalFilename();
        String suffix = "";
        if (original != null) {
            int idx = original.lastIndexOf(".");
            if (idx >= 0 && idx < original.length() - 1) {
                suffix = original.substring(idx);
            }
        }

        // 定义文件的唯一标识
        String fileName = System.currentTimeMillis() + suffix;
        String storageDir = getStorageDir();
        // 拼接完整的文件存储路径
        String realFilePath = storageDir + File.separator + fileName;
        // 拼接完整的文件存储路径
        try {
            if (!FileUtil.isDirectory(storageDir)) {
                FileUtil.mkdir(storageDir);
            }
            FileUtil.writeBytes(file.getBytes(), realFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败：" + e.getMessage());
        }
        // 返回相对路径，前端用 config.json 的 serverUrl 拼接，本地/部署切换时预览与上传一致
        String url = "/files/view/" + fileName;
        return Result.success(url);
    }

    /**
     * 文件下载
     */
    @GetMapping("/download/{fileName:.+}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        String decodedName = decodeFileName(fileName);
        // 设置下载文件http响应头
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(decodedName, StandardCharsets.UTF_8));
        // 拼接完整的文件存储路径
        String realFilePath = getStorageDir() + File.separator + decodedName;
        try {
            if (!FileUtil.exist(realFilePath)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            // 通过文件的存储路径拿到文件字节数组
            byte[] bytes = FileUtil.readBytes(realFilePath);
            ServletOutputStream os = response.getOutputStream();
            // 将文件字节数组写出到文件流
            os.write(bytes);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 文件预览（图片等资源展示）
     */
    @GetMapping("/view/{fileName:.+}")
    public void view(@PathVariable String fileName, HttpServletResponse response) {
        String decodedName = decodeFileName(fileName);
        String realFilePath = getStorageDir() + File.separator + decodedName;
        try {
            if (!FileUtil.exist(realFilePath)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            String mimeType = FileUtil.getMimeType(realFilePath);
            response.setContentType(mimeType != null ? mimeType : "application/octet-stream");
            byte[] bytes = FileUtil.readBytes(realFilePath);
            ServletOutputStream os = response.getOutputStream();
            os.write(bytes);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
