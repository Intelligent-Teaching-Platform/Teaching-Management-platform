-- 课程资料文件夹表（教师在某课程下创建的文件夹）
CREATE TABLE IF NOT EXISTS material_folder (
  id int AUTO_INCREMENT PRIMARY KEY,
  name varchar(255) NOT NULL COMMENT '文件夹名称',
  course_id int NOT NULL COMMENT '课程id',
  parent_id int DEFAULT NULL COMMENT '父文件夹id，NULL表示根目录',
  teacher_id int NOT NULL COMMENT '创建教师id',
  create_time datetime DEFAULT NULL COMMENT '创建时间'
) COMMENT '课程资料文件夹';

-- 为 material 表增加 folder_id 列（若已有则跳过）
ALTER TABLE material ADD COLUMN folder_id int DEFAULT NULL COMMENT '所属文件夹id，NULL表示根目录';
