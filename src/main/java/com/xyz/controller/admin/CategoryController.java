package com.xyz.controller.admin;

import com.xyz.constant.MessageConstant;
import com.xyz.dto.CategoryDTO;
import com.xyz.dto.CategoryOrderDTO;
import com.xyz.service.CategoryService;
import com.xyz.vo.CategoryVO;
import com.xyz.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员-分类管理控制器
 */
@RestController("adminCategoryController")
@RequestMapping("/admin/category")
@Tag(name = "分类管理")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 创建分类
     */
    @PostMapping
    @Operation(summary = "创建分类")
    public Result<CategoryVO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        try {
            CategoryVO categoryVO = categoryService.createCategory(categoryDTO);
            return Result.success(MessageConstant.CATEGORY_CREATE_SUCCESS, categoryVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新分类")
    public Result<CategoryVO> updateCategory(
            @Parameter(description = "分类ID") @PathVariable Long id,
            @RequestBody CategoryDTO categoryDTO) {
        try {
            CategoryVO categoryVO = categoryService.updateCategory(id, categoryDTO);
            return Result.success(MessageConstant.CATEGORY_UPDATE_SUCCESS, categoryVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public Result<String> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return Result.success(MessageConstant.CATEGORY_DELETE_SUCCESS);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有分类列表（按展示顺序排列）
     */
    @GetMapping
    @Operation(summary = "获取所有分类列表")
    public Result<List<CategoryVO>> getAllCategories() {
        try {
            List<CategoryVO> categories = categoryService.getAllCategories();
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取分类详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取分类详情")
    public Result<CategoryVO> getCategoryById(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        try {
            CategoryVO categoryVO = categoryService.getCategoryById(id);
            return Result.success(categoryVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上架分类
     */
    @PutMapping("/{id}/online")
    @Operation(summary = "上架分类")
    public Result<String> onlineCategory(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        try {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setStatus(1);
            categoryService.updateCategory(id, categoryDTO);
            return Result.success(MessageConstant.CATEGORY_STATUS_ONLINE);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 下架分类
     */
    @PutMapping("/{id}/offline")
    @Operation(summary = "下架分类")
    public Result<String> offlineCategory(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        try {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setStatus(0);
            categoryService.updateCategory(id, categoryDTO);
            return Result.success(MessageConstant.CATEGORY_STATUS_OFFLINE);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量更新分类顺序（拖拽排序）
     */
    @PutMapping("/order/batch")
    @Operation(summary = "批量更新分类顺序")
    public Result<String> updateCategoryOrder(@RequestBody List<CategoryOrderDTO> orders) {
        try {
            categoryService.updateCategoryOrder(orders);
            return Result.success("分类顺序更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
