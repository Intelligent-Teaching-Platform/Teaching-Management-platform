/*
 Navicat Premium Data Transfer

 Source Server         : 118.31.64.90
 Source Server Type    : MySQL
 Source Server Version : 50744
 Source Host           : 118.31.64.90:3306
 Source Schema         : tms

 Target Server Type    : MySQL
 Target Server Version : 50744
 File Encoding         : 65001

 Date: 07/05/2025 19:58:15
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity
-- ----------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '课程名称',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '活动内容',
  `score` int(11) NULL DEFAULT NULL COMMENT '活动学分',
  `teacher_id` int(11) NULL DEFAULT NULL COMMENT '教师ID',
  `student_id` int(11) NULL DEFAULT NULL COMMENT '学生ID',
  `num` int(11) NULL DEFAULT NULL COMMENT '开班人数',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '上课时间',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '上课地点',
  `already_num` int(11) NULL DEFAULT 0 COMMENT '已选人数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程活动信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of activity
-- ----------------------------

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', 'admin', '管理员', 'http://localhost:9090/files/download/1735900907017-avatar.png', 'ADMIN');

-- ----------------------------
-- Table structure for asign
-- ----------------------------
DROP TABLE IF EXISTS `asign`;
CREATE TABLE `asign`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `c-name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '课程名',
  `t-id` int(11) NULL DEFAULT NULL COMMENT '老师id',
  `s-id` int(11) NULL DEFAULT NULL COMMENT '学生id',
  `sum-num` int(11) NULL DEFAULT NULL COMMENT '学生总人数',
  `num` int(11) NULL DEFAULT NULL COMMENT '已签到人数',
  `t-name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发布教师名',
  `sorce` float NULL DEFAULT NULL COMMENT '签到学分',
  `s-name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学生名',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '签到时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程活动签到表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of asign
-- ----------------------------

-- ----------------------------
-- Table structure for choice
-- ----------------------------
DROP TABLE IF EXISTS `choice`;
CREATE TABLE `choice`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '课程名称',
  `teacher_id` int(11) NULL DEFAULT NULL COMMENT '授课教师ID',
  `college_id` int(11) NULL DEFAULT NULL COMMENT '学院ID',
  `course_id` int(11) NULL DEFAULT NULL COMMENT '课程ID',
  `class_id` int(11) NULL DEFAULT NULL COMMENT '班级id',
  `student_id` int(11) NULL DEFAULT NULL COMMENT '学生id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 160 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '选课信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of choice
-- ----------------------------
INSERT INTO `choice` VALUES (37, '软件工程', 48, 10, 1, 1, 12);
INSERT INTO `choice` VALUES (114, 'java开发', 49, 10, 59, 1, 58);
INSERT INTO `choice` VALUES (115, 'java开发', 49, 10, 59, 1, 59);
INSERT INTO `choice` VALUES (118, 'web开发', 50, 10, 60, 1, 58);
INSERT INTO `choice` VALUES (119, 'web开发', 50, 10, 60, 1, 59);
INSERT INTO `choice` VALUES (148, 'java开发', 61, 10, 70, 1, 12);
INSERT INTO `choice` VALUES (149, 'java开发', 61, 10, 70, 1, 15);
INSERT INTO `choice` VALUES (150, 'java开发', 61, 10, 70, 1, 60);
INSERT INTO `choice` VALUES (151, 'java开发', 61, 10, 70, 1, 61);
INSERT INTO `choice` VALUES (152, 'java开发', 61, 10, 70, 1, 62);
INSERT INTO `choice` VALUES (153, 'java开发', 61, 10, 70, 1, 63);
INSERT INTO `choice` VALUES (154, 'web开发', 62, 10, 71, 1, 12);
INSERT INTO `choice` VALUES (155, 'web开发', 62, 10, 71, 1, 15);
INSERT INTO `choice` VALUES (156, 'web开发', 62, 10, 71, 1, 60);
INSERT INTO `choice` VALUES (157, 'web开发', 62, 10, 71, 1, 61);
INSERT INTO `choice` VALUES (158, 'web开发', 62, 10, 71, 1, 62);
INSERT INTO `choice` VALUES (159, 'web开发', 62, 10, 71, 1, 63);

-- ----------------------------
-- Table structure for class
-- ----------------------------
DROP TABLE IF EXISTS `class`;
CREATE TABLE `class`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `teacher_id` int(11) NULL DEFAULT NULL,
  `college_id` int(11) NULL DEFAULT NULL,
  `create_time` char(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of class
-- ----------------------------
INSERT INTO `class` VALUES (1, '软工1101', NULL, 10, '2025-04-09 15:18:31');
INSERT INTO `class` VALUES (4, '软工1102', NULL, 10, '2025-04-09 15:19:43');

-- ----------------------------
-- Table structure for college
-- ----------------------------
DROP TABLE IF EXISTS `college`;
CREATE TABLE `college`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学院名称',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '学院描述',
  `score` int(11) NULL DEFAULT NULL COMMENT '最低学分',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 124 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学院信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of college
-- ----------------------------
INSERT INTO `college` VALUES (10, '信息科学与工程学院', '信息科学技术', 100);
INSERT INTO `college` VALUES (123, 'admin', NULL, NULL);

-- ----------------------------
-- Table structure for course
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID\r\n',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '课程名称',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '课程介绍',
  `score` int(11) NULL DEFAULT NULL COMMENT '课程学分',
  `teacher_id` int(11) NULL DEFAULT NULL COMMENT '授课教师',
  `num` int(11) NULL DEFAULT NULL COMMENT '开班人数',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '上课时间',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '上课地点',
  `college_id` int(11) NULL DEFAULT NULL COMMENT '所属学院',
  `already_num` int(11) NULL DEFAULT 5 COMMENT '已选人数',
  `class_id` int(11) NULL DEFAULT NULL COMMENT '班级id',
  `term` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学期·',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 72 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of course
-- ----------------------------
INSERT INTO `course` VALUES (5, 'Java基础', 'Java基础学习提升', 40, 49, 5, '19点30分', '18-210', 10, 1, NULL, '');
INSERT INTO `course` VALUES (21, '111', '11', 11, 48, 111, '11', '11', 10, 2, 1, '');
INSERT INTO `course` VALUES (70, 'java开发', '啦啦啦啦啦3', 3, 61, 123, '8：30-10：00', '信-501', 10, 6, 1, '2024-2025上学期');
INSERT INTO `course` VALUES (71, 'web开发', '啦啦啦啦啦3', 3, 62, 53, '8：30-10：00', '信-205', 10, 6, 1, '2024-2025下学期');

-- ----------------------------
-- Table structure for experiment_monitoring
-- ----------------------------
DROP TABLE IF EXISTS `experiment_monitoring`;
CREATE TABLE `experiment_monitoring`  (
  `id` int(11) NOT NULL COMMENT '主键id',
  `student_id` int(11) NULL DEFAULT NULL COMMENT '学号',
  `course_id` int(11) NULL DEFAULT NULL COMMENT '课程编号',
  ` data_sequence` text CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT '数据上传顺序',
  `first_error` text CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT '第一次错误',
  `data` text CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT '原始记录',
  ` completion_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '实验监测表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of experiment_monitoring
-- ----------------------------

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '公告内容',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发布时间\r\n',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告信息\r\n' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notice
-- ----------------------------
INSERT INTO `notice` VALUES (2, '额温枪而', '恶趣味请问我去恶趣味恶趣味额温枪', '2025-01-06 17:34:40');
INSERT INTO `notice` VALUES (3, '额外企鹅我去额外企鹅', '额外企鹅去玩额温枪恶趣味额温枪', '2025-01-06 17:34:47');
INSERT INTO `notice` VALUES (4, '学分公告', '学分已经下发请各位同学查看学分是否属实', '2025-01-06 19:13:54');
INSERT INTO `notice` VALUES (5, '学院公告', '公告信息大大叔大婶看见撒垃圾堆积立卡', '2025-01-08 19:29:05');

-- ----------------------------
-- Table structure for preview_records
-- ----------------------------
DROP TABLE IF EXISTS `preview_records`;
CREATE TABLE `preview_records`  (
  `id` int(11) NOT NULL COMMENT '主键Id',
  `student_id` int(11) NULL DEFAULT NULL COMMENT '学号',
  `course_id` int(11) NULL DEFAULT NULL COMMENT '课程编号',
  `count` int(11) NULL DEFAULT NULL COMMENT '预习次数',
  `sorce` double NULL DEFAULT NULL COMMENT '得分',
  `time` int(11) NULL DEFAULT NULL COMMENT '预习时长',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '预习监测表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of preview_records
-- ----------------------------

-- ----------------------------
-- Table structure for sorce
-- ----------------------------
DROP TABLE IF EXISTS `sorce`;
CREATE TABLE `sorce`  (
  `id` int(11) NOT NULL COMMENT '主键',
  `student_id` int(11) NULL DEFAULT NULL COMMENT '学号',
  `course_id` int(11) NULL DEFAULT NULL COMMENT '课程号',
  `end_sorce` double NULL DEFAULT NULL COMMENT '最终成绩',
  `sign_sorce` double NULL DEFAULT NULL COMMENT '签到得分',
  `test_sorce` double NULL DEFAULT NULL COMMENT '测试得分',
  `process_sorce` double NULL DEFAULT NULL COMMENT '过程得分',
  `experiment_sorce` double NULL DEFAULT NULL COMMENT '实验得分',
  `term` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '学期',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sorce
-- ----------------------------

-- ----------------------------
-- Table structure for speciality
-- ----------------------------
DROP TABLE IF EXISTS `speciality`;
CREATE TABLE `speciality`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业名称\r\n',
  `college_id` int(11) NULL DEFAULT NULL COMMENT '学院ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '专业信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of speciality
-- ----------------------------
INSERT INTO `speciality` VALUES (6, '计算机科学与技术', 10);

-- ----------------------------
-- Table structure for student
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码\r\n',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '姓名\r\n',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色',
  `sex` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别',
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学号',
  `college_id` int(11) NULL DEFAULT NULL COMMENT '学院ID',
  `score` int(11) NULL DEFAULT NULL COMMENT '学分\r\n',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `class_id` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 64 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of student
-- ----------------------------
INSERT INTO `student` VALUES (12, '123456', '123456', '学生姓名', 'STUDENT', '男', '2025', 10, 70, NULL, 1);
INSERT INTO `student` VALUES (15, '1', '123456', '1', 'STUDENT', '男', '1', 10, 89, NULL, 1);
INSERT INTO `student` VALUES (60, 'user1', NULL, '张三', 'student', '男', '5001', 10, 90, NULL, 1);
INSERT INTO `student` VALUES (61, 'user2', NULL, '李四', 'student', '男', '5002', 10, 45, NULL, 1);
INSERT INTO `student` VALUES (63, 'user2', NULL, '李四', 'student', '男', '5002', 10, 70, NULL, 1);

-- ----------------------------
-- Table structure for student_course
-- ----------------------------
DROP TABLE IF EXISTS `student_course`;
CREATE TABLE `student_course`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `student_id` int(11) NOT NULL COMMENT '学生ID',
  `course_id` int(11) NOT NULL COMMENT '课程ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_course`(`student_id`, `course_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生课程关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of student_course
-- ----------------------------
INSERT INTO `student_course` VALUES (1, 123, 1, '2025-04-15 16:31:24');

-- ----------------------------
-- Table structure for teacher
-- ----------------------------
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '姓名',
  `sex` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '职称',
  `speciality_id` int(11) NULL DEFAULT NULL COMMENT '专业id',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像\r\n',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 63 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '教师信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of teacher
-- ----------------------------
INSERT INTO `teacher` VALUES (48, '123456', '123456', '', '男', '讲师', 6, 'TEACHER', NULL);
INSERT INTO `teacher` VALUES (61, '111111', '123456', '王雨薇', '男', '教授', 6, 'TEACHER', 'http://localhost:9090/files/download/1735900907017-avatar.png');
INSERT INTO `teacher` VALUES (62, '222222', '123456', '李彤', '女', '副教授', 6, 'TEACHER', 'http://localhost:9090/files/download/1735900907017-avatar.png');

-- ----------------------------
-- Table structure for work
-- ----------------------------
DROP TABLE IF EXISTS `work`;
CREATE TABLE `work`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '作业题目',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '作业内容',
  `file` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '作业文件',
  `scontent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '学生作业',
  `score` int(11) NULL DEFAULT NULL COMMENT '作业学分',
  `teacher_id` int(11) NULL DEFAULT NULL COMMENT '教师ID',
  `student_id` int(11) NULL DEFAULT NULL COMMENT '学生ID',
  `state` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核状态',
  `amendment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '修改意见',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程作业信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of work
-- ----------------------------
INSERT INTO `work` VALUES (38, '1', '11', NULL, NULL, 10, 48, 12, '待审核', NULL);

SET FOREIGN_KEY_CHECKS = 1;
