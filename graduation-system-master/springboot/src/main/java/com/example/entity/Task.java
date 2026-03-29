package com.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** 忽略 JSON 中列表关联带来的多余字段（如仅有 getter 的扩展字段），避免反序列化异常 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Task {
    private Integer id;
    private String name;
    private String content;
    /** 封面图（文件上传后返回的 URL） */
    private String cover;
    private Integer classId;
    private Integer courseId; // 课程ID（用于隔离跨课程数据）
    private Integer lab;
    private Integer teacherId;
    private String className;  // 班级名称（关联查询）
    /** 多个班级ID，用逗号分隔（如 "1,2,3"），用于多班级发放 */
    private String classIds;
    /** 多个班级名称，用逗号分隔 */
    private String classNames;

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassIds() {
        return classIds;
    }

    public void setClassIds(String classIds) {
        this.classIds = classIds;
    }

    public String getClassNames() {
        return classNames;
    }

    public void setClassNames(String classNames) {
        this.classNames = classNames;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Integer teacherId) {
        this.teacherId = teacherId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public Integer getLab() {
        return lab;
    }

    public void setLab(Integer lab) {
        this.lab = lab;
    }
}
