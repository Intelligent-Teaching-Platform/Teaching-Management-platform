-- 为实验作业/作业增加“教师评价”字段
-- MySQL
ALTER TABLE `work`
  ADD COLUMN `teacher_comment` VARCHAR(2000) NULL COMMENT '教师评价' AFTER `amendment`;

