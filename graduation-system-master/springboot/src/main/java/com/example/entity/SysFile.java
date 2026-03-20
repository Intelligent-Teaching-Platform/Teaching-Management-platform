package com.example.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

/**
 * 教学文件实体
 * 复用项目原有实体规范（如MyBatis-Plus注解）
 */
@Data
@TableName("sys_file")
public class SysFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String fileName; // 文件名
    private String filePath; // 存储路径/访问链接
    private Long fileSize; // 文件大小
    private String fileType; // 文件类型
    private Long teacherId; // 上传教师ID
    private Long courseId; // 关联课程ID（可选）

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableLogic // 逻辑删除（复用项目原有逻辑删除配置）
    private Integer isDeleted;
}