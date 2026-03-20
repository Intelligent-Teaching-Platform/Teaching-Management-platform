-- 为实验任务增加“最后提交或修改时间”字段
-- 说明：
-- 1) 前端“实验作业”列表将展示 work.student_last_submit_time
-- 2) 该字段仅在学生分段保存实验（lab=2，phase=1/2/3 更新 tip1/2/3）时由后端写入
-- 3) 请先备份数据库，再执行

ALTER TABLE `work`
  ADD COLUMN `student_last_submit_time` datetime NULL COMMENT '学生最后一次分段提交或修改时间' AFTER `task_id`;

