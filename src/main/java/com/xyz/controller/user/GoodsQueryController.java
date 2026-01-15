package com.xyz.controller.user;

import com.xyz.service.GoodsQueryService;
import com.xyz.util.BaseContext;
import com.xyz.vo.GoodsCardVO;
import com.xyz.vo.PageResult;
import com.xyz.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商品查询控制器（收藏、发布、分类查询）
 */
@Slf4j
@RestController
@RequestMapping("/user/goods-query")
@Tag(name = "商品查询")
public class GoodsQueryController {

    @Autowired
    private GoodsQueryService goodsQueryService;

    /**
     * 分页获取分类下的商品列表（无限滚动）
     */
    @GetMapping("/category/page")
    @Operation(summary = "分页获取分类下的商品列表")
    public Result<PageResult<GoodsCardVO>> getGoodsPageByCategory(
            @Parameter(description = "分类ID") @RequestParam Long categoryId,
            @Parameter(description = "游标（时间戳）") @RequestParam(required = false) Long cursor,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        PageResult<GoodsCardVO> page = goodsQueryService.getGoodsPageByCategoryId(categoryId, cursor, size);
        return Result.success(page);
    }

    /**
     * 获取我的收藏列表（游标分页）
     */
    @GetMapping("/my/favorite")
    @Operation(summary = "获取我的收藏列表")
    public Result<PageResult<GoodsCardVO>> getMyFavorites(
            @Parameter(description = "游标（首次请求不传或传null）") @RequestParam(required = false) Long cursor,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        try {
            Long userId = BaseContext.getCurrentId();
            PageResult<GoodsCardVO> pageResult = goodsQueryService.getFavoriteGoods(userId, cursor, size);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取收藏列表失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询他人发布的所有商品
     */
    @GetMapping("/published/{userId}")
    @Operation(summary = "查询用户发布的商品")
    public Result<PageResult<GoodsCardVO>> getUserPublishedGoods(
            @Parameter(description = "目标用户ID") @PathVariable Long userId,
            @Parameter(description = "游标（首次请求不传或传null）") @RequestParam(required = false) Long cursor,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        try {
            PageResult<GoodsCardVO> pageResult = goodsQueryService.getUserPublishedGoods(userId, cursor, size);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("查询用户发布的商品失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询我发布的所有商品
     */
    @GetMapping("/published/my")
    @Operation(summary = "我的发布")
    public Result<PageResult<GoodsCardVO>> getMyPublishedGoods(
            @Parameter(description = "游标（首次请求不传或传null）") @RequestParam(required = false) Long cursor,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        try {
            Long userId = BaseContext.getCurrentId();
            PageResult<GoodsCardVO> pageResult = goodsQueryService.getMyPublishedGoods(userId, cursor, size);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("查询我发布的商品失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
