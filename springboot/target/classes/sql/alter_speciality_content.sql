-- 专业表增加「专业介绍」字段（执行一次即可）
ALTER TABLE `speciality`
    ADD COLUMN `content` TEXT NULL COMMENT '专业介绍' AFTER `college_id`;
