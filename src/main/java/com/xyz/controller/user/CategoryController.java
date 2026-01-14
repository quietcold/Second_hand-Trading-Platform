package com.xyz.controller.user;

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
 * 用户-分类查询控制器
 */
@RestController("userCategoryController")
@RequestMapping("/user/category")
@Tag(name = "用户-分类查询接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取所有上架的分类列表（按展示顺序排列）
     */
    @GetMapping
    @Operation(summary = "获取所有上架的分类列表")
    public Result<List<CategoryVO>> getAllCategories() {
        try {
            List<CategoryVO> categories = categoryService.getActiveCategories();
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
}
