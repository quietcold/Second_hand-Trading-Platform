package com.xyz.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分类VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryVO {
    private Long id;
    private String name;
    private String code;
    private Integer displayOrder; // 展示顺序
    private Integer status; // 状态：1-上架，0-下架
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
