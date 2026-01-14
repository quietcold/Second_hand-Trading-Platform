package com.xyz.service.impl;

import com.xyz.constant.MessageConstant;
import com.xyz.dto.CategoryDTO;
import com.xyz.dto.CategoryOrderDTO;
import com.xyz.entity.GoodsCategory;
import com.xyz.exception.CategoryDeleteException;
import com.xyz.exception.CategoryNotFoundException;
import com.xyz.mapper.GoodsCategoryMapper;
import com.xyz.service.CategoryService;
import com.xyz.vo.CategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private GoodsCategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryVO createCategory(CategoryDTO categoryDTO) {
        // 检查编码是否已存在
        GoodsCategory existCategory = categoryMapper.findByCode(categoryDTO.getCode());
        if (existCategory != null) {
            throw new RuntimeException(MessageConstant.CATEGORY_CODE_EXISTS);
        }

        // 创建分类
        GoodsCategory category = new GoodsCategory();
        BeanUtils.copyProperties(categoryDTO, category);
        
        // 设置默认值
        if (category.getDisplayOrder() == null) {
            category.setDisplayOrder(999); // 默认排在最后
        }
        if (category.getStatus() == null) {
            category.setStatus(1); // 默认上架
        }
        
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());

        categoryMapper.insert(category);

        // 返回VO
        CategoryVO categoryVO = new CategoryVO();
        BeanUtils.copyProperties(category, categoryVO);
        return categoryVO;
    }

    @Override
    @Transactional
    public CategoryVO updateCategory(Long id, CategoryDTO categoryDTO) {
        // 检查分类是否存在
        GoodsCategory category = categoryMapper.findById(id);
        if (category == null) {
            throw new CategoryNotFoundException(MessageConstant.CATEGORY_NOT_FOUND);
        }

        // 如果传入了code，检查新编码是否与其他分类重复
        if (categoryDTO.getCode() != null) {
            GoodsCategory existCategory = categoryMapper.findByCode(categoryDTO.getCode());
            if (existCategory != null && !existCategory.getId().equals(id)) {
                throw new RuntimeException(MessageConstant.CATEGORY_CODE_EXISTS);
            }
        }

        // 只更新非 null 的字段
        if (categoryDTO.getName() != null) {
            category.setName(categoryDTO.getName());
        }
        if (categoryDTO.getCode() != null) {
            category.setCode(categoryDTO.getCode());
        }
        if (categoryDTO.getDisplayOrder() != null) {
            category.setDisplayOrder(categoryDTO.getDisplayOrder());
        }
        if (categoryDTO.getStatus() != null) {
            category.setStatus(categoryDTO.getStatus());
        }
        
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.update(category);

        // 返回VO
        CategoryVO categoryVO = new CategoryVO();
        BeanUtils.copyProperties(category, categoryVO);
        return categoryVO;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        // 检查分类是否存在
        GoodsCategory category = categoryMapper.findById(id);
        if (category == null) {
            throw new CategoryNotFoundException(MessageConstant.CATEGORY_NOT_FOUND);
        }

        // 检查是否有商品使用该分类
        int goodsCount = categoryMapper.countGoodsByCategory(id);
        if (goodsCount > 0) {
            throw new CategoryDeleteException(MessageConstant.CATEGORY_HAS_GOODS);
        }

        // 删除分类
        categoryMapper.deleteById(id);
    }

    @Override
    public List<CategoryVO> getAllCategories() {
        List<GoodsCategory> categories = categoryMapper.findAll();
        return categories.stream()
                .map(category -> {
                    CategoryVO vo = new CategoryVO();
                    BeanUtils.copyProperties(category, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> getActiveCategories() {
        List<GoodsCategory> categories = categoryMapper.findAllActive();
        return categories.stream()
                .map(category -> {
                    CategoryVO vo = new CategoryVO();
                    BeanUtils.copyProperties(category, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public CategoryVO getCategoryById(Long id) {
        GoodsCategory category = categoryMapper.findById(id);
        if (category == null) {
            throw new CategoryNotFoundException(MessageConstant.CATEGORY_NOT_FOUND);
        }

        CategoryVO categoryVO = new CategoryVO();
        BeanUtils.copyProperties(category, categoryVO);
        return categoryVO;
    }

    @Override
    @Transactional
    public void updateCategoryOrder(List<CategoryOrderDTO> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }

        // 批量更新每个分类的顺序
        for (CategoryOrderDTO orderDTO : orders) {
            GoodsCategory category = categoryMapper.findById(orderDTO.getId());
            if (category != null) {
                category.setDisplayOrder(orderDTO.getDisplayOrder());
                category.setUpdateTime(LocalDateTime.now());
                categoryMapper.update(category);
            }
        }
    }
}
