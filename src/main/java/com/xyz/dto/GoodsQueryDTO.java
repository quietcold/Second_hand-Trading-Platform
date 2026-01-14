package com.xyz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品多条件查询DTO（管理员使用）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoodsQueryDTO {
    
    /**
     * 商品状态（可选）
     * 1-上架, 2-已售出, 3-租借中, 4-已下架, 5-用户删除, 6-系统屏蔽
     */
    private Integer status;
    
    /**
     * 分类ID（可选）
     */
    private Long categoryId;
    
    /**
     * 发布用户ID（可选）
     */
    private Long ownerId;
    
    /**
     * 商品类型（可选）
     * 1-出售, 2-租赁, 3-出售或租赁
     */
    private Integer goodsType;
    
    /**
     * 关键词搜索（可选）- 模糊搜索标题和描述
     */
    private String keyword;
    
    /**
     * 最低价格（可选）
     */
    private Double minPrice;
    
    /**
     * 最高价格（可选）
     */
    private Double maxPrice;
    
    /**
     * 开始时间（可选）- 查询该时间之后发布的商品
     */
    private Long startTime;
    
    /**
     * 结束时间（可选）- 查询该时间之前发布的商品
     */
    private Long endTime;
    
    /**
     * 游标（用于分页）
     */
    private Long cursor;
    
    /**
     * 每页数量
     */
    private Integer size;
}
