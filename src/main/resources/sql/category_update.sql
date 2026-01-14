-- 为商品分类表添加展示顺序和状态字段
ALTER TABLE goods_category 
ADD COLUMN display_order INT DEFAULT 999 COMMENT '展示顺序，数字越小越靠前' AFTER code,
ADD COLUMN status INT DEFAULT 1 COMMENT '状态：1-上架，0-下架' AFTER display_order;

-- 为现有数据设置默认展示顺序（按创建时间顺序）
UPDATE goods_category SET display_order = id WHERE display_order IS NULL;
