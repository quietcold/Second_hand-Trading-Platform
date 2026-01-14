package com.xyz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {
    private String name; // 分类名称
    private String code; // 分类编码
    private Integer displayOrder; // 展示顺序
    private Integer status; // 状态：1-上架，0-下架
}
