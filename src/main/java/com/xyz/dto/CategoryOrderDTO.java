package com.xyz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类顺序DTO（用于批量更新顺序）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryOrderDTO {
    private Long id; // 分类ID
    private Integer displayOrder; // 新的展示顺序
}
