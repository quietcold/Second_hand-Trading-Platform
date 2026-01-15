package com.xyz.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 商品收藏实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoodsFavorite {
    private Long id; // 收藏ID
    private Long userId; // 用户ID
    private Long goodsId; // 商品ID
    private LocalDateTime createTime; // 收藏时间
}
