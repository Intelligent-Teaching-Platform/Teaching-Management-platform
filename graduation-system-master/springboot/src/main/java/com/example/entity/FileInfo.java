package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("file")
public class FileInfo {
    private Integer id;
    private Integer materialId;
    private String fileName;
    private String fileType;
    private Double fileSize;
    private String filePath;
    private Date uploadTime;
    private Integer uploaderId;
}