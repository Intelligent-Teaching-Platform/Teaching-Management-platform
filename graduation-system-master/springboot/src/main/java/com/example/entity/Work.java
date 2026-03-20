package com.example.entity;

import java.time.LocalDateTime;

public class Work {
    private Integer id;
    private String name;
    private String content;
    private Integer courseId;
    private String file;
    private Integer score;
    private Integer teacherId;
    private Integer studentId;
    private String scontent;
    private String state;
    private String teacherName;
    private String studentName;
    private String amendment;
    private String teacherComment; // 教师评价
    private Integer lab;
    private String tip1;
    private String tip2;
    private String tip3;
    private Integer taskId;
    /**
     * 学生最后一次分段提交或修改时间（用于展示“最后提交或修改时间”）
     * 只在学生保存实验分段（tip1/2/3）时更新。
     */
    private LocalDateTime studentLastSubmitTime;
    /** 学生分段提交：阶段一「任务题目」 */
    private String studentStageTitle;
    /** 学生分段提交：阶段二「任务要求」理解与填写 */
    private String studentStageRequirement;
    /** 分段进度：0 未开始，1 题目已保存，2 要求已保存，3 实验内容已提交（待审核） */
    private Integer submitPhase;

    public String getStudentStageTitle() {
        return studentStageTitle;
    }

    public void setStudentStageTitle(String studentStageTitle) {
        this.studentStageTitle = studentStageTitle;
    }

    public String getStudentStageRequirement() {
        return studentStageRequirement;
    }

    public void setStudentStageRequirement(String studentStageRequirement) {
        this.studentStageRequirement = studentStageRequirement;
    }

    public Integer getSubmitPhase() {
        return submitPhase;
    }

    public void setSubmitPhase(Integer submitPhase) {
        this.submitPhase = submitPhase;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public LocalDateTime getStudentLastSubmitTime() {
        return studentLastSubmitTime;
    }

    public void setStudentLastSubmitTime(LocalDateTime studentLastSubmitTime) {
        this.studentLastSubmitTime = studentLastSubmitTime;
    }

    public String getTip1() {
        return tip1;
    }

    public void setTip1(String tip1) {
        this.tip1 = tip1;
    }

    public String getTip2() {
        return tip2;
    }

    public void setTip2(String tip2) {
        this.tip2 = tip2;
    }

    public String getTip3() {
        return tip3;
    }

    public void setTip3(String tip3) {
        this.tip3 = tip3;
    }

    public Integer getLab() {
        return lab;
    }

    public void setLab(Integer lab) {
        this.lab = lab;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getAmendment() {
        return amendment;
    }

    public void setAmendment(String amendment) {
        this.amendment = amendment;
    }

    public String getTeacherComment() {
        return teacherComment;
    }

    public void setTeacherComment(String teacherComment) {
        this.teacherComment = teacherComment;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
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

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Integer teacherId) {
        this.teacherId = teacherId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getScontent() {
        return scontent;
    }

    public void setScontent(String scontent) {
        this.scontent = scontent;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
