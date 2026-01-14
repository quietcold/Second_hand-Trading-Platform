package com.xyz.controller.admin;

import com.xyz.constant.MessageConstant;
import com.xyz.dto.GoodsQueryDTO;
import com.xyz.service.GoodsService;
import com.xyz.vo.GoodsCardVO;
import com.xyz.vo.GoodsDetailVO;
import com.xyz.vo.PageResult;
import com.xyz.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员-商品管理控制器
 */
@RestController("adminGoodsController")
@RequestMapping("/admin/goods")
@Tag(name = "管理员-商品管理接口")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 分页获取商品列表（按分类）- 复用用户端接口
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取商品列表（按分类）")
    public Result<PageResult<GoodsCardVO>> getGoodsPage(
            @Parameter(description = "分类ID") @RequestParam Long categoryId,
            @Parameter(description = "游标（首次请求不传或传null）") @RequestParam(required = false) Long cursor,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        PageResult<GoodsCardVO> page = goodsService.getGoodsPageByCategoryId(categoryId, cursor, size);
        return Result.success(page);
    }

    /**
     * 查询指定用户的商品列表 - 复用用户端接口
     */
    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "查询指定用户的商品列表")
    public Result<PageResult<GoodsCardVO>> getGoodsByOwner(
            @Parameter(description = "用户ID") @PathVariable Long ownerId,
            @Parameter(description = "游标（首次请求不传或传null）") @RequestParam(required = false) Long cursor,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        PageResult<GoodsCardVO> page = goodsService.getGoodsPageByOwnerId(ownerId, cursor, size);
        return Result.success(page);
    }

    /**
     * 搜索商品 - 复用用户端接口
     */
    @GetMapping("/search")
    @Operation(summary = "搜索商品")
    public Result<PageResult<GoodsCardVO>> searchGoods(
            @Parameter(description = "关键词") @RequestParam String keyword,
            @Parameter(description = "游标（首次请求不传或传null）") @RequestParam(required = false) Long cursor,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        PageResult<GoodsCardVO> page = goodsService.searchGoods(keyword, cursor, size);
        return Result.success(page);
    }

    /**
     * 查询商品详情 - 复用用户端接口
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询商品详情")
    public Result<GoodsDetailVO> getGoodsDetail(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        GoodsDetailVO detail = goodsService.getGoodsDetailById(id);
        return Result.success(detail);
    }

    /**
     * 违规下架商品（管理员强制下架）
     */
    @PutMapping("/{id}/violation-offline")
    @Operation(summary = "违规下架商品")
    public Result violationOffline(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @Parameter(description = "下架原因（可选）") @RequestParam(required = false) String reason) {
        try {
            goodsService.violationOfflineByAdmin(id, reason);
            return Result.success(MessageConstant.GOODS_VIOLATION_OFFLINE_SUCCESS);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 恢复违规下架的商品（管理员操作）
     */
    @PutMapping("/{id}/restore")
    @Operation(summary = "恢复违规下架的商品")
    public Result restoreGoods(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        try {
            goodsService.restoreGoodsByAdmin(id);
            return Result.success(MessageConstant.GOODS_RESTORE_SUCCESS);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 多条件查询商品（管理员）
     */
    @PostMapping("/query")
    @Operation(summary = "多条件查询商品")
    public Result<PageResult<GoodsCardVO>> queryGoods(@RequestBody GoodsQueryDTO query) {
        try {
            PageResult<GoodsCardVO> result = goodsService.queryGoodsByConditions(query);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
