-- score.score 由 int 改为 DECIMAL(5,1)
-- 用于保存客观题正确率换算后的“一位小数”总分
ALTER TABLE `score`
MODIFY `score` DECIMAL(5,1) NULL DEFAULT NULL COMMENT '成绩得分';

