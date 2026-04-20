-- 任务表：独立「实验要求」字段（与实验目的、实验环境并列）
ALTER TABLE task ADD COLUMN experiment_requirement TEXT NULL COMMENT '实验要求' AFTER experiment_environment;
