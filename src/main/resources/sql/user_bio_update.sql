-- 为用户表添加个人简介字段
ALTER TABLE user ADD COLUMN bio VARCHAR(200) COMMENT '个人简介' AFTER image;
