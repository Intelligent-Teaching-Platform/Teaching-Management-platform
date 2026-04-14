-- 任务表增加封面图字段（用于任务卡片封面展示）
ALTER TABLE task
  ADD COLUMN cover varchar(512) DEFAULT NULL COMMENT '任务封面图 URL';

