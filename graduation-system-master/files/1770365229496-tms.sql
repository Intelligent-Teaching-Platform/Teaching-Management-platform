/*
 Navicat Premium Data Transfer

 Source Server         : data
 Source Server Type    : MySQL
 Source Server Version : 90200
 Source Host           : localhost:3306
 Source Schema         : tms

 Target Server Type    : MySQL
 Target Server Version : 90200
 File Encoding         : 65001

 Date: 05/02/2026 22:09:39
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity
-- ----------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '课程名称',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '活动内容',
  `score` int NULL DEFAULT NULL COMMENT '活动学分',
  `teacher_id` int NULL DEFAULT NULL COMMENT '教师ID',
  `student_id` int NULL DEFAULT NULL COMMENT '学生ID',
  `num` int NULL DEFAULT NULL COMMENT '开班人数',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '上课时间',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '上课地点',
  `already_num` int NULL DEFAULT 0 COMMENT '已选人数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程活动信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of activity
-- ----------------------------

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', 'admin', '管理员', 'http://localhost:9090/files/download/1747067092328-1746275454280-微信图片_20231208005201.jpg', 'ADMIN');
INSERT INTO `admin` VALUES (12, 'admin1', 'admin', 'a', NULL, 'ADMIN');

-- ----------------------------
-- Table structure for asign
-- ----------------------------
DROP TABLE IF EXISTS `asign`;
CREATE TABLE `asign`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `c-name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '课程名',
  `t-id` int NULL DEFAULT NULL COMMENT '老师id',
  `s-id` int NULL DEFAULT NULL COMMENT '学生id',
  `sum-num` int NULL DEFAULT NULL COMMENT '学生总人数',
  `num` int NULL DEFAULT NULL COMMENT '已签到人数',
  `t-name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发布教师名',
  `sorce` float NULL DEFAULT NULL COMMENT '签到学分',
  `s-name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学生名',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '签到时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程活动签到表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of asign
-- ----------------------------

-- ----------------------------
-- Table structure for choice
-- ----------------------------
DROP TABLE IF EXISTS `choice`;
CREATE TABLE `choice`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '课程名称',
  `teacher_id` int NULL DEFAULT NULL COMMENT '授课教师ID',
  `college_id` int NULL DEFAULT NULL COMMENT '学院ID',
  `course_id` int NULL DEFAULT NULL COMMENT '课程ID',
  `class_id` int NULL DEFAULT NULL COMMENT '班级id',
  `student_id` int NULL DEFAULT NULL COMMENT '学生id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 202 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of choice
-- ----------------------------
INSERT INTO `choice` VALUES (37, 'java开发', 48, 10, 21, 1, 12);
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
INSERT INTO `choice` VALUES (159, 'web开发', 62, 10, 21, 1, 65);
INSERT INTO `choice` VALUES (197, NULL, NULL, NULL, 80, NULL, 65);
INSERT INTO `choice` VALUES (198, NULL, NULL, NULL, 80, NULL, 66);
INSERT INTO `choice` VALUES (199, NULL, NULL, NULL, 80, NULL, 67);
INSERT INTO `choice` VALUES (200, NULL, NULL, NULL, 80, NULL, 68);
INSERT INTO `choice` VALUES (201, NULL, NULL, NULL, 80, NULL, 69);

-- ----------------------------
-- Table structure for clazz
-- ----------------------------
DROP TABLE IF EXISTS `clazz`;
CREATE TABLE `clazz`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
  `teacher_id` int NULL DEFAULT NULL,
  `college_id` int NULL DEFAULT NULL,
  `create_time` char(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of clazz
-- ----------------------------
INSERT INTO `clazz` VALUES (6, '计科2025', NULL, NULL, '2025-05-09');
INSERT INTO `clazz` VALUES (8, '软工2025', NULL, NULL, '2025-05-08');

-- ----------------------------
-- Table structure for college
-- ----------------------------
DROP TABLE IF EXISTS `college`;
CREATE TABLE `college`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学院名称',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '学院描述',
  `score` int NULL DEFAULT NULL COMMENT '最低学分',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 133 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学院信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of college
-- ----------------------------
INSERT INTO `college` VALUES (10, '信息科学与工程学院', '信息科学技术', 100);
INSERT INTO `college` VALUES (126, '文法学院', '文法学院', 100);

-- ----------------------------
-- Table structure for course
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID\r\n',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '课程名称',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '课程介绍',
  `score` int NULL DEFAULT NULL COMMENT '课程学分',
  `teacher_id` int NULL DEFAULT NULL COMMENT '授课教师',
  `num` int NULL DEFAULT NULL COMMENT '开班人数',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '上课时间',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '上课地点',
  `college_id` int NULL DEFAULT NULL COMMENT '所属学院',
  `already_num` int NULL DEFAULT 5 COMMENT '已选人数',
  `class_id` int NULL DEFAULT NULL COMMENT '班级id',
  `term` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学期·',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 81 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of course
-- ----------------------------
INSERT INTO `course` VALUES (21, 'java开发', 'java开发', 10, 48, 111, '11', '11', 10, 5, 1, '2024-2025上学期');
INSERT INTO `course` VALUES (80, 'Python', 'Python学习', 11, 48, 11, '10:30', '101', 10, 5, 6, NULL);

-- ----------------------------
-- Table structure for experiment_monitoring
-- ----------------------------
DROP TABLE IF EXISTS `experiment_monitoring`;
CREATE TABLE `experiment_monitoring`  (
  `id` int NOT NULL COMMENT '主键id',
  `student_id` int NULL DEFAULT NULL COMMENT '学号',
  `course_id` int NULL DEFAULT NULL COMMENT '课程编号',
  ` data_sequence` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL COMMENT '数据上传顺序',
  `first_error` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL COMMENT '第一次错误',
  `data` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL COMMENT '原始记录',
  ` completion_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = '实验监测表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of experiment_monitoring
-- ----------------------------

-- ----------------------------
-- Table structure for material
-- ----------------------------
DROP TABLE IF EXISTS `material`;
CREATE TABLE `material`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '资料名称',
  `teacher_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '教师名称',
  `create_time` datetime(6) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `size` double NULL DEFAULT NULL COMMENT '大小',
  `course_id` int NULL DEFAULT NULL COMMENT '课程号',
  `teacher_id` int NULL DEFAULT NULL COMMENT '教师ID',
  `path` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '文件路径',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of material
-- ----------------------------
INSERT INTO `material` VALUES (7, 'Java学习资料', '张', '2025-05-12 09:36:24.654000', 5, NULL, NULL, 'https://www.bilibili.com/video/BV17F411T7Ao/?spm_id_from=333.337.search-card.all.click&vd_source=73c3164bae744dcb6c7ebc0b2b568a3e');
INSERT INTO `material` VALUES (13, 'Python', '张老师', '2025-05-13 10:22:36.867000', 11, NULL, NULL, 'https://www.bilibili.com/video/BV1rpWjevEip/?spm_id_from=333.337.search-card.all.click&vd_source=2f9a96986a88688a69ddbbc6a885b4fc');

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '公告内容',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发布时间\r\n',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告信息\r\n' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notice
-- ----------------------------
INSERT INTO `notice` VALUES (2, '关于举办学术讲座的通知', '为拓宽学术视野，营造浓厚学术氛围，学院特邀[知名专家，开展主题为如何更好的学习的学术讲座，现将有关事项通知如下：​\n时间：2025.5.15 上午10：30 \n地点：一号教学楼 101\n讲座内容：本次讲座将围绕如何养成更好的学习方法，深入探讨相关领域的前沿动态与学术热点，为师生带来一场学术盛宴。​\n欢迎广大师生积极参加，提前 10 分钟入场就座，保持会场秩序。​\n信息科学与工程学院\n2025.5.1', '2025-01-06 17:34:40');
INSERT INTO `notice` VALUES (3, '校园文化节活动报名通知​', '亲爱的同学们：​\n为丰富校园文化生活，展现我院学子风采，学院将于 5.5-5.10举办校园文化节活动。现面向全院同学征集活动参与者，具体事项通知如下：​\n活动内容：本次文化节涵盖文艺表演、书画展览、创意比赛等，形式多样，精彩纷呈。​\n报名方式：即日起至 5.3，可通过线上或线下方式报名。线上报名请填写在线报名表；线下报名请前往行政楼102领取报名表。​\n奖励设置：各活动项目将评选出一、二、三等奖及优秀奖，获奖者将获得荣誉证书及精美奖品。​\n期待同学们踊跃报名，积极参与，共同打造一场难忘的文化盛宴！​\n信息科学与工程学院\n4.25', '2025-01-06 17:34:47');
INSERT INTO `notice` VALUES (4, '学分下发通知', '学分已经下发请各位同学查看学分是否属实', '2025-01-06 19:13:54');

-- ----------------------------
-- Table structure for preview_records
-- ----------------------------
DROP TABLE IF EXISTS `preview_records`;
CREATE TABLE `preview_records`  (
  `id` int NOT NULL COMMENT '主键Id',
  `student_id` int NULL DEFAULT NULL COMMENT '学号',
  `course_id` int NULL DEFAULT NULL COMMENT '课程编号',
  `count` int NULL DEFAULT NULL COMMENT '预习次数',
  `sorce` double NULL DEFAULT NULL COMMENT '得分',
  `time` int NULL DEFAULT NULL COMMENT '预习时长',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = '预习监测表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of preview_records
-- ----------------------------

-- ----------------------------
-- Table structure for question
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目名称',
  `course_id` int NULL DEFAULT NULL COMMENT '课程ID',
  `teacher_id` int NULL DEFAULT NULL COMMENT '教师ID',
  `type_id` int NULL DEFAULT NULL COMMENT '题型ID',
  `option_a` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '选项A',
  `option_b` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '选项B',
  `option_c` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '选项C',
  `option_d` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '选项D',
  `answer` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目答案',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题目信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of question
-- ----------------------------
INSERT INTO `question` VALUES (2, '111', 21, 48, 1, '这是A选项', '这是B选项', '这是C选项', '这是D选项', 'A');
INSERT INTO `question` VALUES (3, '1111', 21, 48, 1, '1111', '2222', '3333', '4444', 'A');
INSERT INTO `question` VALUES (4, NULL, 21, 48, 1, '11111', '2222', '3333', '4444', 'A');
INSERT INTO `question` VALUES (5, '2222', 21, 48, 2, NULL, NULL, NULL, NULL, '正确');
INSERT INTO `question` VALUES (6, 'wodqwdqojodjsa', 21, 48, 1, 'dsafaio', 'sdasaf', 'dasfqdsaddsad', 'csaasca', 'C');

-- ----------------------------
-- Table structure for question_type
-- ----------------------------
DROP TABLE IF EXISTS `question_type`;
CREATE TABLE `question_type`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题型名称',
  `score` int NULL DEFAULT NULL COMMENT '题型分数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题型信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of question_type
-- ----------------------------
INSERT INTO `question_type` VALUES (1, '单选题', 10);
INSERT INTO `question_type` VALUES (2, '判断题', 5);

-- ----------------------------
-- Table structure for score
-- ----------------------------
DROP TABLE IF EXISTS `score`;
CREATE TABLE `score`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '试卷名称',
  `course_id` int NULL DEFAULT NULL COMMENT '课程ID',
  `teacher_id` int NULL DEFAULT NULL COMMENT '教师ID',
  `student_id` int NULL DEFAULT NULL COMMENT '学生ID',
  `paper_id` int NULL DEFAULT NULL COMMENT '试卷ID',
  `score` int NULL DEFAULT NULL COMMENT '成绩得分',
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态',
  `answer` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '提交结果',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '成绩信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of score
-- ----------------------------
INSERT INTO `score` VALUES (1, 'dhao', 21, 48, NULL, 7, 0, '已阅卷', '[{\"typeName\":\"单选题\",\"questionId\":6,\"score\":10,\"answer\":\"C\",\"newAnswer\":\"B\"}]');
INSERT INTO `score` VALUES (2, 'dhao', 21, 48, NULL, 7, NULL, '待阅卷', '[{\"typeName\":\"单选题\",\"questionId\":6,\"score\":10,\"answer\":\"C\",\"newAnswer\":\"C\"}]');
INSERT INTO `score` VALUES (3, 'dhao', 21, 48, NULL, 7, NULL, '待阅卷', '[{\"typeName\":\"单选题\",\"questionId\":6,\"score\":10,\"answer\":\"C\",\"newAnswer\":\"C\"}]');
INSERT INTO `score` VALUES (4, 'Java测试1', 21, 48, NULL, 6, NULL, '待阅卷', '[{\"typeName\":\"单选题\",\"questionId\":2,\"score\":10,\"answer\":\"A\",\"newAnswer\":\"A\"},{\"typeName\":\"判断题\",\"questionId\":5,\"score\":5,\"answer\":\"正确\",\"newAnswer\":\"正确\"}]');
INSERT INTO `score` VALUES (5, 'Java测试1', 21, 48, NULL, 6, NULL, '待阅卷', '[{\"typeName\":\"单选题\",\"questionId\":2,\"score\":10,\"answer\":\"A\",\"newAnswer\":\"A\"},{\"typeName\":\"判断题\",\"questionId\":5,\"score\":5,\"answer\":\"正确\",\"newAnswer\":\"正确\"}]');
INSERT INTO `score` VALUES (6, 'Java测试1', 21, 48, NULL, 6, NULL, '待阅卷', '[{\"typeName\":\"单选题\",\"questionId\":2,\"score\":10,\"answer\":\"A\",\"newAnswer\":\"C\"},{\"typeName\":\"判断题\",\"questionId\":5,\"score\":5,\"answer\":\"正确\",\"newAnswer\":\"错误\"}]');
INSERT INTO `score` VALUES (7, 'Java测试1', 21, 48, NULL, 6, NULL, '待阅卷', '[{\"typeName\":\"单选题\",\"questionId\":2,\"score\":10,\"answer\":\"A\",\"newAnswer\":\"D\"},{\"typeName\":\"判断题\",\"questionId\":5,\"score\":5,\"answer\":\"正确\",\"newAnswer\":\"正确\"}]');
INSERT INTO `score` VALUES (8, 'Java测试1', 21, 48, NULL, 6, NULL, '待阅卷', '[{\"typeName\":\"单选题\",\"questionId\":2,\"score\":10,\"answer\":\"A\"},{\"typeName\":\"判断题\",\"questionId\":5,\"score\":5,\"answer\":\"正确\"}]');
INSERT INTO `score` VALUES (9, 'dhao', 21, 48, NULL, 7, NULL, '待阅卷', '[{\"typeName\":\"单选题\",\"questionId\":6,\"score\":10,\"answer\":\"C\",\"newAnswer\":\"A\"}]');
INSERT INTO `score` VALUES (10, 'dhao', 21, 48, NULL, 7, NULL, '待阅卷', '[{\"typeName\":\"单选题\",\"questionId\":6,\"score\":10,\"answer\":\"C\",\"newAnswer\":\"B\"}]');
INSERT INTO `score` VALUES (11, 'dhao', 21, 48, NULL, 7, NULL, '待阅卷', '[{\"typeName\":\"单选题\",\"questionId\":6,\"score\":10,\"answer\":\"C\",\"newAnswer\":\"C\"}]');
INSERT INTO `score` VALUES (12, 'dhao', 21, 48, NULL, 7, NULL, '待阅卷', '[{\"typeName\":\"单选题\",\"questionId\":6,\"score\":10,\"answer\":\"C\",\"newAnswer\":\"C\"}]');
INSERT INTO `score` VALUES (13, 'dhao', 21, 48, NULL, 7, NULL, '待阅卷', '[{\"typeName\":\"单选题\",\"questionId\":6,\"score\":10,\"answer\":\"C\",\"newAnswer\":\"C\"}]');

-- ----------------------------
-- Table structure for sign
-- ----------------------------
DROP TABLE IF EXISTS `sign`;
CREATE TABLE `sign`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '签到ID',
  `snum` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学号',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学生姓名',
  `student_id` int NULL DEFAULT NULL COMMENT '学生ID',
  `teacher_id` int NULL DEFAULT NULL COMMENT '老师ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sign
-- ----------------------------

-- ----------------------------
-- Table structure for sign_in
-- ----------------------------
DROP TABLE IF EXISTS `sign_in`;
CREATE TABLE `sign_in`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `course_id` int NULL DEFAULT NULL COMMENT '课程ID',
  `teacher_id` int NULL DEFAULT NULL COMMENT '教师ID',
  `token` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签到令牌',
  `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '签到纬度',
  `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '签到经度',
  `distance` int NULL DEFAULT NULL COMMENT '允许签到距离(米)',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `num` int NULL DEFAULT 0 COMMENT '签到人数',
  `snum` int NULL DEFAULT NULL COMMENT '应签人数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_token`(`token` ASC) USING BTREE,
  INDEX `idx_course`(`course_id` ASC) USING BTREE,
  INDEX `idx_teacher`(`teacher_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '签到表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sign_in
-- ----------------------------
INSERT INTO `sign_in` VALUES (22, 21, 48, NULL, 39.9042000, 116.4074000, 100, '2025-05-13 03:24:15', '2025-05-13 03:54:15', '2025-05-13 03:24:15', 1, 2);
INSERT INTO `sign_in` VALUES (23, 21, 48, NULL, 39.9042000, 116.4074000, 100, '2025-05-13 13:00:53', '2025-05-13 13:30:53', '2025-05-13 13:00:52', 2, 2);
INSERT INTO `sign_in` VALUES (24, 21, 48, NULL, 39.9042000, 116.4074000, 100, '2025-05-13 14:09:46', '2025-05-13 14:39:46', '2025-05-13 14:09:45', 3, 2);
INSERT INTO `sign_in` VALUES (25, 21, 48, NULL, 39.9042000, 116.4074000, 100, '2025-05-13 14:30:57', '2025-05-13 15:00:57', '2025-05-13 14:30:56', 0, 5);
INSERT INTO `sign_in` VALUES (26, 21, 48, NULL, 39.9042000, 116.4074000, 100, '2025-05-13 15:06:18', '2025-05-13 15:36:18', '2025-05-13 15:06:18', 1, 2);
INSERT INTO `sign_in` VALUES (27, 21, 48, NULL, 39.9042000, 116.4074000, 100, '2025-05-13 15:34:21', '2025-05-13 16:04:21', '2025-05-13 15:34:21', 0, 5);
INSERT INTO `sign_in` VALUES (28, 21, 48, NULL, 39.9042000, 116.4074000, 100, '2025-05-13 17:30:42', '2025-05-13 18:00:42', '2025-05-13 17:30:42', 5, 5);
INSERT INTO `sign_in` VALUES (35, 21, 48, NULL, 39.9042000, 116.4074000, 100, '2025-05-13 18:22:25', '2025-05-13 18:25:25', '2025-05-13 18:22:24', 1, 5);
INSERT INTO `sign_in` VALUES (36, 21, 48, NULL, 39.9042000, 116.4074000, 100, '2025-06-18 14:54:16', '2025-06-18 15:24:16', '2025-06-18 14:54:16', 0, 5);
INSERT INTO `sign_in` VALUES (37, 80, 48, NULL, 39.9042000, 116.4074000, 100, '2025-06-18 16:06:53', '2025-06-18 16:36:53', '2025-06-18 16:06:53', 0, 5);
INSERT INTO `sign_in` VALUES (38, 80, 48, NULL, 39.9042000, 116.4074000, 100, '2025-06-18 16:13:26', '2025-06-18 16:43:26', '2025-06-18 16:13:25', 0, 5);
INSERT INTO `sign_in` VALUES (39, 21, 48, NULL, 39.9042000, 116.4074000, 100, '2025-06-18 16:18:32', '2025-06-18 16:48:32', '2025-06-18 16:18:31', 0, 5);

-- ----------------------------
-- Table structure for sign_in_record
-- ----------------------------
DROP TABLE IF EXISTS `sign_in_record`;
CREATE TABLE `sign_in_record`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `sign_in_id` int NOT NULL COMMENT '签到ID',
  `student_id` int NOT NULL COMMENT '学生ID',
  `token` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签到令牌',
  `latitude` decimal(10, 7) NOT NULL COMMENT '签到纬度',
  `longitude` decimal(10, 7) NOT NULL COMMENT '签到经度',
  `distance` int NOT NULL COMMENT '与签到点距离(米)',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态(SUCCESS/FAIL)',
  `sign_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '签到时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sign_in`(`sign_in_id` ASC) USING BTREE,
  INDEX `idx_student`(`student_id` ASC) USING BTREE,
  INDEX `idx_token`(`token` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '签到记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sign_in_record
-- ----------------------------
INSERT INTO `sign_in_record` VALUES (1, 22, 12, NULL, 39.9042000, 116.4074000, 0, 'SUCCESS', '2025-05-13 03:32:33');
INSERT INTO `sign_in_record` VALUES (2, 23, 12, NULL, 39.9042000, 116.4074000, 0, 'SUCCESS', '2025-05-13 13:02:27');
INSERT INTO `sign_in_record` VALUES (3, 23, 12, NULL, 39.9042000, 116.4074000, 0, 'SUCCESS', '2025-05-13 13:02:29');
INSERT INTO `sign_in_record` VALUES (4, 24, 65, NULL, 39.9042000, 116.4074000, 0, 'SUCCESS', '2025-05-13 14:17:12');
INSERT INTO `sign_in_record` VALUES (5, 24, 65, NULL, 39.9042000, 116.4074000, 0, 'SUCCESS', '2025-05-13 14:17:30');
INSERT INTO `sign_in_record` VALUES (6, 24, 65, NULL, 39.9042000, 116.4074000, 0, 'SUCCESS', '2025-05-13 14:23:13');
INSERT INTO `sign_in_record` VALUES (7, 26, 65, NULL, 39.9042000, 116.4074000, 0, 'SUCCESS', '2025-05-13 15:35:53');
INSERT INTO `sign_in_record` VALUES (8, 28, 65, NULL, 39.9042000, 116.4074000, 0, 'SUCCESS', '2025-05-13 17:30:55');
INSERT INTO `sign_in_record` VALUES (9, 28, 65, NULL, 39.9042000, 116.4074000, 0, 'SUCCESS', '2025-05-13 17:53:52');
INSERT INTO `sign_in_record` VALUES (10, 28, 65, NULL, 39.9042000, 116.4074000, 0, 'SUCCESS', '2025-05-13 17:53:58');
INSERT INTO `sign_in_record` VALUES (11, 28, 65, NULL, 39.9042000, 116.4074000, 0, 'SUCCESS', '2025-05-13 17:54:47');
INSERT INTO `sign_in_record` VALUES (12, 28, 65, NULL, 39.9042000, 116.4074000, 0, 'SUCCESS', '2025-05-13 17:55:32');
INSERT INTO `sign_in_record` VALUES (13, 29, 65, NULL, 39.9042000, 116.4074000, 0, 'SUCCESS', '2025-05-13 18:01:10');
INSERT INTO `sign_in_record` VALUES (14, 29, 65, NULL, 39.9042000, 116.4074000, 0, 'SUCCESS', '2025-05-13 18:01:20');
INSERT INTO `sign_in_record` VALUES (15, 29, 65, NULL, 39.9042000, 116.4074000, 0, 'SUCCESS', '2025-05-13 18:11:32');
INSERT INTO `sign_in_record` VALUES (16, 33, 65, NULL, 39.9042000, 116.4074000, 0, 'SUCCESS', '2025-05-13 18:16:40');
INSERT INTO `sign_in_record` VALUES (17, 35, 65, NULL, 39.9042000, 116.4074000, 0, 'SUCCESS', '2025-05-13 18:23:16');

-- ----------------------------
-- Table structure for speciality
-- ----------------------------
DROP TABLE IF EXISTS `speciality`;
CREATE TABLE `speciality`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业名称\r\n',
  `college_id` int NULL DEFAULT NULL COMMENT '学院ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '专业信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of speciality
-- ----------------------------
INSERT INTO `speciality` VALUES (6, '计算机科学与技术', 10);
INSERT INTO `speciality` VALUES (8, '软工', 10);

-- ----------------------------
-- Table structure for student
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码\r\n',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '姓名\r\n',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色',
  `sex` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别',
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学号',
  `college_id` int NULL DEFAULT NULL COMMENT '学院ID',
  `score` int NULL DEFAULT NULL COMMENT '学分\r\n',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `class_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 75 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of student
-- ----------------------------
INSERT INTO `student` VALUES (65, '123456', '123456', 'zhangsan', 'STUDENT', '男', '2025', 10, 0, NULL, 6);
INSERT INTO `student` VALUES (66, '2025111', '123456', '张三', 'STUDENT', '男', '2025111', 10, 0, NULL, 6);
INSERT INTO `student` VALUES (67, '2025222', '123456', '李四', 'STUDENT', '男', '2025222', 10, 0, NULL, 6);
INSERT INTO `student` VALUES (68, '2025333', '123456', '王五', 'STUDENT', '男', '2025333', 10, 0, NULL, 6);
INSERT INTO `student` VALUES (69, '2025444', '123456', '赵六', 'STUDENT', '男', '2025444', 10, 0, NULL, 6);
INSERT INTO `student` VALUES (74, 'lisi', '123456', 'lisi', 'STUDENT', '男', '2025', 10, 0, NULL, 8);

-- ----------------------------
-- Table structure for student_course
-- ----------------------------
DROP TABLE IF EXISTS `student_course`;
CREATE TABLE `student_course`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `student_id` int NOT NULL COMMENT '学生ID',
  `course_id` int NOT NULL COMMENT '课程ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_course`(`student_id` ASC, `course_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生课程关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of student_course
-- ----------------------------
INSERT INTO `student_course` VALUES (1, 123, 1, '2025-04-15 16:31:24');

-- ----------------------------
-- Table structure for task
-- ----------------------------
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '任务名称',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '任务内容',
  `class_id` int NULL DEFAULT NULL COMMENT '任务班级',
  `lab` int NULL DEFAULT NULL COMMENT '任务类型，作业1，实验2',
  `teacher_id` int NULL DEFAULT NULL COMMENT '教师ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of task
-- ----------------------------
INSERT INTO `task` VALUES (31, 'MIPS指令系统和流水线冲突', '1.	了解和熟悉MIPS指令集； 2.	熟练掌握模拟器MIPSsim的操作和使用方法； 3.	熟悉MIPS指令系统及其特点，加深对MIPS指令操作语义的理解； 4.	熟悉MIPS体系结构； 5.	加深理解MIPS计算机流水线基本概念的理解； 6.	理解MIPS结构如何用5段流水线来实现，理解各段的功能和基本操作； 7.	加深对于流水线冲突的理解，以及解决流水线冲突的方法，掌握如何应用定向技术来减少数据冲突引起的停顿。', 6, 2, 48);
INSERT INTO `task` VALUES (32, 'MIPS指令系统和流水线冲突', '1.	了解和熟悉MIPS指令集； 2.	熟练掌握模拟器MIPSsim的操作和使用方法； 3.	熟悉MIPS指令系统及其特点，加深对MIPS指令操作语义的理解； 4.	熟悉MIPS体系结构； 5.	加深理解MIPS计算机流水线基本概念的理解； 6.	理解MIPS结构如何用5段流水线来实现，理解各段的功能和基本操作； 7.	加深对于流水线冲突的理解，以及解决流水线冲突的方法，掌握如何应用定向技术来减少数据冲突引起的停顿。', 6, 1, 48);

-- ----------------------------
-- Table structure for teacher
-- ----------------------------
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '姓名',
  `sex` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '职称',
  `speciality_id` int NULL DEFAULT NULL COMMENT '专业id',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像\r\n',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 80 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '教师信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of teacher
-- ----------------------------
INSERT INTO `teacher` VALUES (48, 'zhang', '123456', '张老师', '男', '讲师', 6, 'TEACHER', NULL);
INSERT INTO `teacher` VALUES (61, 'wang', '123456', '王老师', '男', '教授', 6, 'TEACHER', 'http://localhost:9090/files/download/1747067110068-1745847686130-微信图片_20231208005154.jpg');
INSERT INTO `teacher` VALUES (62, 'li', '123456', '李老师', '女', '副教授', 6, 'TEACHER', 'http://localhost:9090/files/download/1747067121944-1746275454280-微信图片_20231208005201.jpg');
INSERT INTO `teacher` VALUES (78, '111111', '123456', '王雨薇', '男', '教授', 6, 'TEACHER', NULL);
INSERT INTO `teacher` VALUES (79, '222222', '123456', '李彤', '女', '副教授', 6, 'TEACHER', NULL);

-- ----------------------------
-- Table structure for test_paper
-- ----------------------------
DROP TABLE IF EXISTS `test_paper`;
CREATE TABLE `test_paper`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '试卷名称',
  `course_id` int NULL DEFAULT NULL COMMENT '课程ID',
  `teacher_id` int NULL DEFAULT NULL COMMENT '教师ID',
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '类型',
  `start` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '开始时间',
  `end` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '结束时间',
  `time` int NULL DEFAULT NULL COMMENT '考试时长',
  `question_ids` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目IDs',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '试卷信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of test_paper
-- ----------------------------
INSERT INTO `test_paper` VALUES (6, 'Java测试1', 21, 48, '手动选题', '2025-05-30', '2025-06-01', 100, '[2,5]');
INSERT INTO `test_paper` VALUES (7, 'dhao', 21, 48, '手动选题', '2025-05-30', '2025-05-31', 100, '[6]');

-- ----------------------------
-- Table structure for work
-- ----------------------------
DROP TABLE IF EXISTS `work`;
CREATE TABLE `work`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '作业题目',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '作业内容',
  `file` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '作业文件',
  `scontent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '学生作业',
  `score` int NULL DEFAULT NULL COMMENT '作业学分',
  `teacher_id` int NULL DEFAULT NULL COMMENT '教师ID',
  `student_id` int NULL DEFAULT NULL COMMENT '学生ID',
  `state` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核状态',
  `amendment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '修改意见',
  `lab` int NULL DEFAULT NULL COMMENT '任务类型 1作业，2实验',
  `tip1` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '补充信息1',
  `tip2` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '补充信息2',
  `tip3` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '补充信息3',
  `task_id` int NULL DEFAULT NULL COMMENT '任务ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 205 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生任务记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of work
-- ----------------------------
INSERT INTO `work` VALUES (184, 'MIPS指令系统和流水线冲突', '1.	了解和熟悉MIPS指令集； 2.	熟练掌握模拟器MIPSsim的操作和使用方法； 3.	熟悉MIPS指令系统及其特点，加深对MIPS指令操作语义的理解； 4.	熟悉MIPS体系结构； 5.	加深理解MIPS计算机流水线基本概念的理解； 6.	理解MIPS结构如何用5段流水线来实现，理解各段的功能和基本操作； 7.	加深对于流水线冲突的理解，以及解决流水线冲突的方法，掌握如何应用定向技术来减少数据冲突引起的停顿。', NULL, '1.	了解和熟悉MIPS指令集；\n2.	熟练掌握模拟器MIPSsim的操作和使用方法；\n3.	熟悉MIPS指令系统及其特点，加深对MIPS指令操作语义的理解；\n4.	熟悉MIPS体系结构；\n5.	加深理解MIPS计算机流水线基本概念的理解；\n6.	理解MIPS结构如何用5段流水线来实现，理解各段的功能和基本操作；\n7.	加深对于流水线冲突的理解，以及解决流水线冲突的方法，掌握如何应用定向技术来减少数据冲突引起的停顿。\n', 100, 48, 65, '审核通过', '无', 2, '实验平台采用指令级和流水线操作级模拟器MIPSsim。MIPSsim是一款在Windows环境中实现的MIPS虚拟处理器，它实现的是MIPS64指令系统的子集。其由7个窗口、7个一级主菜单构成。可以仿真以MIPS64指令集编制的程序运行与调试。该模拟器使用方法参考文档“MIPSsim 使用手册.doc”。', '阅读MIPSsim模拟器的使用方法，了解MIPSsim的指令系统和汇编语言。\n1. 启动MIPSsim（双击MIPSsim.exe）。\n2. 选择“配置”→“流水方式”选项，使模拟器工作在非流水方式下。\n3．选择“文件” →“载入程序”选项，加载样例程序alltest.s（在模拟器所在文件夹下的“样例程序”文件夹中），然后查看“代码”窗口，查看程序所在的位置。（起始地址为0x00000000）。\n4. 然后分别以单步执行一条指令、执行多条指令、连续执行、设置断点等的方式运行程序，观察程序的执行情况，观察CPU中寄存器和存储器的内容的变化。\n5．查看“寄存器”窗口PC寄存器的值：[PC]=0x  00000000    。\n6. 执行load和store指令,步骤如下：\n（1）	单步执行一条指令（F7）\n（2）	下一条指令地址为0x 00000004  ，是一条  有       (有，无)符号载入\n             字节    （字节，半字，字）指令。\n（3）	单步执行一条指令（F7）\n（4）	查看R1的值，[R1] =0x FFFFFFFFFFFFFF80             。\n（5）	下一条指令地址为0x  000000008  ，是一条    有     (有，无)符号载入\n              字   （字节，半字，字）指令。\n（6）	单步执行一条指令。\n（7）	查看R1的值，[R1] =0x  0000000000000080  。\n（8）	下一条指令地址为 0x 0000000C       ，是一条  无     (有，无)符号载入\n             字节     （字节，半字，字）指令。\n（9）	单步执行一条指令。\n（10）	查看R1的值，[R1] =0x 0000000000000080    。\n（11）	单步执行一条指令。\n（12）	下一条指令地址为0x 00000014             ，是一条保存  字节   （字节，半字，字）指令。\n（13）	单步执行一条指令（F7）\n（14）	查看内存BUFFER处字的值，值为0x 00000080          。\n7. 执行算术运算类指令。步骤如下：\n（1）	双击“寄存器”窗口中的R1，将值修改为2。\n（2）	双击“寄存器”窗口中的R2，将值修改为3。\n（3）', '实验通过编写和执行MIPS汇编程序，将课堂上学到的计算机体系结构和流水线执行的理论知识与实际操作相结合，加深了我对这些概念的理解。\n分析了结构冲突对 CPU 性能的影响，解决的办法是，结构冲突出现的频率较高，导致的停顿使得CPU流水的效率下降，可以通过分别设独立的指令存储器和数据存储器的方法，或者仍只是一个存储器，但采用两个分离的Cache。\n通过实训，我体会到了书本上的理论知识，如何运用于实践中。原先学的时候还感叹资料太难懂，此刻想来有些并不难，关键在于理解。在此次实践中，我关联了我其他方面的潜力，提高了我的综合素质，提出了独立思考问题的关键在自己动手操作潜力。我信这次宝贵的经验，会成为我今后成功的基石。读万卷书不如行万里路，行万里路不如阅人无数，其实在实操中也是我增加了更多的与团队合作的能力，现在我能做的就是吸取更多专业知识，提高自己的综合素质\n', 31);
INSERT INTO `work` VALUES (185, 'MIPS指令系统和流水线冲突', '1.	了解和熟悉MIPS指令集； 2.	熟练掌握模拟器MIPSsim的操作和使用方法； 3.	熟悉MIPS指令系统及其特点，加深对MIPS指令操作语义的理解； 4.	熟悉MIPS体系结构； 5.	加深理解MIPS计算机流水线基本概念的理解； 6.	理解MIPS结构如何用5段流水线来实现，理解各段的功能和基本操作； 7.	加深对于流水线冲突的理解，以及解决流水线冲突的方法，掌握如何应用定向技术来减少数据冲突引起的停顿。', NULL, NULL, NULL, 48, 66, NULL, NULL, 2, NULL, NULL, NULL, 31);
INSERT INTO `work` VALUES (186, 'MIPS指令系统和流水线冲突', '1.	了解和熟悉MIPS指令集； 2.	熟练掌握模拟器MIPSsim的操作和使用方法； 3.	熟悉MIPS指令系统及其特点，加深对MIPS指令操作语义的理解； 4.	熟悉MIPS体系结构； 5.	加深理解MIPS计算机流水线基本概念的理解； 6.	理解MIPS结构如何用5段流水线来实现，理解各段的功能和基本操作； 7.	加深对于流水线冲突的理解，以及解决流水线冲突的方法，掌握如何应用定向技术来减少数据冲突引起的停顿。', NULL, NULL, NULL, 48, 67, NULL, NULL, 2, NULL, NULL, NULL, 31);
INSERT INTO `work` VALUES (187, 'MIPS指令系统和流水线冲突', '1.	了解和熟悉MIPS指令集； 2.	熟练掌握模拟器MIPSsim的操作和使用方法； 3.	熟悉MIPS指令系统及其特点，加深对MIPS指令操作语义的理解； 4.	熟悉MIPS体系结构； 5.	加深理解MIPS计算机流水线基本概念的理解； 6.	理解MIPS结构如何用5段流水线来实现，理解各段的功能和基本操作； 7.	加深对于流水线冲突的理解，以及解决流水线冲突的方法，掌握如何应用定向技术来减少数据冲突引起的停顿。', NULL, NULL, NULL, 48, 68, NULL, NULL, 2, NULL, NULL, NULL, 31);
INSERT INTO `work` VALUES (188, 'MIPS指令系统和流水线冲突', '1.	了解和熟悉MIPS指令集； 2.	熟练掌握模拟器MIPSsim的操作和使用方法； 3.	熟悉MIPS指令系统及其特点，加深对MIPS指令操作语义的理解； 4.	熟悉MIPS体系结构； 5.	加深理解MIPS计算机流水线基本概念的理解； 6.	理解MIPS结构如何用5段流水线来实现，理解各段的功能和基本操作； 7.	加深对于流水线冲突的理解，以及解决流水线冲突的方法，掌握如何应用定向技术来减少数据冲突引起的停顿。', NULL, NULL, NULL, 48, 69, NULL, NULL, 2, NULL, NULL, NULL, 31);
INSERT INTO `work` VALUES (189, 'MIPS指令系统和流水线冲突', '1.	了解和熟悉MIPS指令集； 2.	熟练掌握模拟器MIPSsim的操作和使用方法； 3.	熟悉MIPS指令系统及其特点，加深对MIPS指令操作语义的理解； 4.	熟悉MIPS体系结构； 5.	加深理解MIPS计算机流水线基本概念的理解； 6.	理解MIPS结构如何用5段流水线来实现，理解各段的功能和基本操作； 7.	加深对于流水线冲突的理解，以及解决流水线冲突的方法，掌握如何应用定向技术来减少数据冲突引起的停顿。', NULL, '实验通过编写和执行MIPS汇编程序，将课堂上学到的计算机体系结构和流水线执行的理论知识与实际操作相结合，加深了我对这些概念的理解。\n分析了结构冲突对 CPU 性能的影响，解决的办法是，结构冲突出现的频率较高，导致的停顿使得CPU流水的效率下降，可以通过分别设独立的指令存储器和数据存储器的方法，或者仍只是一个存储器，但采用两个分离的Cache。\n通过实训，我体会到了书本上的理论知识，如何运用于实践中。原先学的时候还感叹资料太难懂，此刻想来有些并不难，关键在于理解。在此次实践中，我关联了我其他方面的潜力，提高了我的综合素质，提出了独立思考问题的关键在自己动手操作潜力。我信这次宝贵的经验，会成为我今后成功的基石。读万卷书不如行万里路，行万里路不如阅人无数，其实在实操中也是我增加了更多的与团队合作的能力，现在我能做的就是吸取更多专业知识，提高自己的综合素质\n', 100, 48, 65, '审核通过', '无', 1, '0.00%', NULL, NULL, 32);
INSERT INTO `work` VALUES (190, 'MIPS指令系统和流水线冲突', '1.	了解和熟悉MIPS指令集； 2.	熟练掌握模拟器MIPSsim的操作和使用方法； 3.	熟悉MIPS指令系统及其特点，加深对MIPS指令操作语义的理解； 4.	熟悉MIPS体系结构； 5.	加深理解MIPS计算机流水线基本概念的理解； 6.	理解MIPS结构如何用5段流水线来实现，理解各段的功能和基本操作； 7.	加深对于流水线冲突的理解，以及解决流水线冲突的方法，掌握如何应用定向技术来减少数据冲突引起的停顿。', NULL, '实验通过编写和执行MIPS汇编程序，将课堂上学到的计算机体系结构和流水线执行的理论知识与实际操作相结合，加深了我对这些概念的理解。\n分析了结构冲突对 CPU 性能的影响，解决的办法是，结构冲突出现的频率较高，导致的停顿使得CPU流水的效率下降，可以通过分别设独立的指令存储器和数据存储器的方法，或者仍只是一个存储器，但采用两个分离的Cache。\n通过实训，我体会到了书本上的理论知识，如何运用于实践中。原先学的时候还感叹资料太难懂，此刻想来有些并不难，关键在于理解。在此次实践中，我关联了我其他方面的潜力，提高了我的综合素质，提出了独立思考问题的关键在自己动手操作潜力。我信这次宝贵的经验，会成为我今后成功的基石。读万卷书不如行万里路，行万里路不如阅人无数，其实在实操中也是我增加了更多的与团队合作的能力，现在我能做的就是吸取更多专业知识，提高自己的综合素质\n', NULL, 48, 66, '', NULL, 1, '100.00%', NULL, NULL, 32);
INSERT INTO `work` VALUES (191, 'MIPS指令系统和流水线冲突', '1.	了解和熟悉MIPS指令集； 2.	熟练掌握模拟器MIPSsim的操作和使用方法； 3.	熟悉MIPS指令系统及其特点，加深对MIPS指令操作语义的理解； 4.	熟悉MIPS体系结构； 5.	加深理解MIPS计算机流水线基本概念的理解； 6.	理解MIPS结构如何用5段流水线来实现，理解各段的功能和基本操作； 7.	加深对于流水线冲突的理解，以及解决流水线冲突的方法，掌握如何应用定向技术来减少数据冲突引起的停顿。', NULL, NULL, NULL, 48, 67, NULL, NULL, 1, NULL, NULL, NULL, 32);
INSERT INTO `work` VALUES (192, 'MIPS指令系统和流水线冲突', '1.	了解和熟悉MIPS指令集； 2.	熟练掌握模拟器MIPSsim的操作和使用方法； 3.	熟悉MIPS指令系统及其特点，加深对MIPS指令操作语义的理解； 4.	熟悉MIPS体系结构； 5.	加深理解MIPS计算机流水线基本概念的理解； 6.	理解MIPS结构如何用5段流水线来实现，理解各段的功能和基本操作； 7.	加深对于流水线冲突的理解，以及解决流水线冲突的方法，掌握如何应用定向技术来减少数据冲突引起的停顿。', NULL, NULL, NULL, 48, 68, NULL, NULL, 1, NULL, NULL, NULL, 32);
INSERT INTO `work` VALUES (193, 'MIPS指令系统和流水线冲突', '1.	了解和熟悉MIPS指令集； 2.	熟练掌握模拟器MIPSsim的操作和使用方法； 3.	熟悉MIPS指令系统及其特点，加深对MIPS指令操作语义的理解； 4.	熟悉MIPS体系结构； 5.	加深理解MIPS计算机流水线基本概念的理解； 6.	理解MIPS结构如何用5段流水线来实现，理解各段的功能和基本操作； 7.	加深对于流水线冲突的理解，以及解决流水线冲突的方法，掌握如何应用定向技术来减少数据冲突引起的停顿。', NULL, NULL, NULL, 48, 69, NULL, NULL, 1, NULL, NULL, NULL, 32);

SET FOREIGN_KEY_CHECKS = 1;
