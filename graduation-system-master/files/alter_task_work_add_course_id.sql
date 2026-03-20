-- 为 task/work 增加 course_id：防止跨课程访问任务/作业
-- 强烈建议先备份数据库，再执行。

ALTER TABLE `task`
  ADD COLUMN `course_id` int NULL COMMENT '课程ID（用于任务归属与隔离）' AFTER `teacher_id`;

ALTER TABLE `work`
  ADD COLUMN `course_id` int NULL COMMENT '课程ID（用于作业归属与隔离）' AFTER `task_id`;

-- 可选：给隔离查询加索引（如已存在可跳过）
CREATE INDEX `idx_task_course_id` ON `task`(`course_id`);
CREATE INDEX `idx_work_course_id` ON `work`(`course_id`);

