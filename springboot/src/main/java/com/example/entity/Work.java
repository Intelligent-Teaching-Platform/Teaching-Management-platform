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
    /** 学生学号 */
    private String studentCode;
    /** 班级名称 */
    private String className;
    /** 课程名称 */
    private String courseName;
    /** 上机地点 */
    private String place;
    /** 上机时间（来自task表） */
    private String experimentTime;
    /** 实验目的（来自task表） */
    private String experimentPurpose;
    /** 实验环境（来自task表） */
    private String experimentEnvironment;
    /** 实验要求（来自task表） */
    private String experimentRequirement;
    /** 实验内容及步骤（来自task表） */
    private String experimentContent;
    /** 实验题目1-9（来自task表） */
    private String q1;
    private String q2;
    private String q3;
    private String q4;
    private String q5;
    private String q6;
    private String q7;
    private String q8;
    private String q9;
    private String amendment;
    private String teacherComment; // 教师评价
    private Integer lab;
    private String tip1;
    private String tip2;
    private String tip3;
    private String tip4;
    private String tip5;
    private String tip6;
    private String tip7;
    private String tip8;
    private String tip9;
    /** 学生心得体会 */
    private String experience;
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

    public String getTip4() {
        return tip4;
    }

    public void setTip4(String tip4) {
        this.tip4 = tip4;
    }

    public String getTip5() {
        return tip5;
    }

    public void setTip5(String tip5) {
        this.tip5 = tip5;
    }

    public String getTip6() {
        return tip6;
    }

    public void setTip6(String tip6) {
        this.tip6 = tip6;
    }

    public String getTip7() {
        return tip7;
    }

    public void setTip7(String tip7) {
        this.tip7 = tip7;
    }

    public String getTip8() {
        return tip8;
    }

    public void setTip8(String tip8) {
        this.tip8 = tip8;
    }

    public String getTip9() {
        return tip9;
    }

    public void setTip9(String tip9) {
        this.tip9 = tip9;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
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

    public String getExperimentRequirement() {
        return experimentRequirement;
    }

    public void setExperimentRequirement(String experimentRequirement) {
        this.experimentRequirement = experimentRequirement;
    }

    public String getExperimentContent() {
        return experimentContent;
    }

    public void setExperimentContent(String experimentContent) {
        this.experimentContent = experimentContent;
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

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
