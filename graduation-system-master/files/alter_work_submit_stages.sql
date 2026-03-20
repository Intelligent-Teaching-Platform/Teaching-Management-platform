-- 实验任务分段提交：题目 → 任务要求 → 实验报告内容
-- 执行前请备份数据库。

ALTER TABLE `work`
  ADD COLUMN `student_stage_title` varchar(255) NULL COMMENT '学生阶段一：任务题目' AFTER `task_id`,
  ADD COLUMN `student_stage_requirement` text NULL COMMENT '学生阶段二：任务要求' AFTER `student_stage_title`,
  ADD COLUMN `submit_phase` int NULL DEFAULT 0 COMMENT '0未分段保存 1题目 2要求 3内容已交' AFTER `student_stage_requirement`;
