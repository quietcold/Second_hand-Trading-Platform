package com.xyz.service;

import com.xyz.dto.CategoryDTO;
import com.xyz.dto.CategoryOrderDTO;
import com.xyz.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {
    
    /**
     * 创建分类（管理员）
     */
    CategoryVO createCategory(CategoryDTO categoryDTO);
    
    /**
     * 更新分类（管理员）
     */
    CategoryVO updateCategory(Long id, CategoryDTO categoryDTO);
    
    /**
     * 删除分类（管理员）
     */
    void deleteCategory(Long id);
    
    /**
     * 获取所有分类列表（管理员）
     */
    List<CategoryVO> getAllCategories();

    /**
     * 获取所有上架的分类列表（用户端）
     */
    List<CategoryVO> getActiveCategories();
    
    /**
     * 根据ID获取分类详情
     */
    CategoryVO getCategoryById(Long id);

    /**
     * 批量更新分类顺序（拖拽排序）
     */
    void updateCategoryOrder(List<CategoryOrderDTO> orders);
}
