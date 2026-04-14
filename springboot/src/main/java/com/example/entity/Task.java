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

    /** 实验内容列表，JSON格式存储（如：[{"title":"实验目的","content":"..."},{"title":"实验原理","content":"..."}]） */
    private String experimentContents;

    /** 实验步骤列表，JSON格式存储（如：[{"step":1,"title":"步骤一","content":"..."},{...}]） */
    private String experimentSteps;

    /** Word模板：上机地点 */
    private String place;
    /** Word模板：上机时间 */
    private String experimentTime;
    /** Word模板：上机内容 */
    private String experimentContent;
    /** Word模板：实验目的及要求 */
    private String experimentPurpose;
    /** Word模板：实验环境及要求 */
    private String experimentEnvironment;
    /** Word模板：实验题目1 */
    private String q1;
    /** Word模板：实验题目2 */
    private String q2;
    /** Word模板：实验题目3 */
    private String q3;
    /** Word模板：实验题目4 */
    private String q4;
    /** Word模板：实验题目5 */
    private String q5;
    /** Word模板：实验题目6 */
    private String q6;
    /** Word模板：实验题目7 */
    private String q7;
    /** Word模板：实验题目8 */
    private String q8;
    /** Word模板：实验题目9 */
    private String q9;

    public String getExperimentContents() {
        return experimentContents;
    }

    public void setExperimentContents(String experimentContents) {
        this.experimentContents = experimentContents;
    }

    public String getExperimentSteps() {
        return experimentSteps;
    }

    public void setExperimentSteps(String experimentSteps) {
        this.experimentSteps = experimentSteps;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getExperimentTime() {
        return experimentTime;
    }

    public void setExperimentTime(String experimentTime) {
        this.experimentTime = experimentTime;
    }

    public String getExperimentContent() {
        return experimentContent;
    }

    public void setExperimentContent(String experimentContent) {
        this.experimentContent = experimentContent;
    }

    public String getExperimentPurpose() {
        return experimentPurpose;
    }

    public void setExperimentPurpose(String experimentPurpose) {
        this.experimentPurpose = experimentPurpose;
    }

    public String getExperimentEnvironment() {
        return experimentEnvironment;
    }

    public void setExperimentEnvironment(String experimentEnvironment) {
        this.experimentEnvironment = experimentEnvironment;
    }

    public String getQ1() {
        return q1;
    }

    public void setQ1(String q1) {
        this.q1 = q1;
    }

    public String getQ2() {
        return q2;
    }

    public void setQ2(String q2) {
        this.q2 = q2;
    }

    public String getQ3() {
        return q3;
    }

    public void setQ3(String q3) {
        this.q3 = q3;
    }

    public String getQ4() {
        return q4;
    }

    public void setQ4(String q4) {
        this.q4 = q4;
    }

    public String getQ5() {
        return q5;
    }

    public void setQ5(String q5) {
        this.q5 = q5;
    }

    public String getQ6() {
        return q6;
    }

    public void setQ6(String q6) {
        this.q6 = q6;
    }

    public String getQ7() {
        return q7;
    }

    public void setQ7(String q7) {
        this.q7 = q7;
    }

    public String getQ8() {
        return q8;
    }

    public void setQ8(String q8) {
        this.q8 = q8;
    }

    public String getQ9() {
        return q9;
    }

    public void setQ9(String q9) {
        this.q9 = q9;
    }
}
