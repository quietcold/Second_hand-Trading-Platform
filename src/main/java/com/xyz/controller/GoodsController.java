package com.xyz.controller;

import com.xyz.dto.GoodsDTO;
import com.xyz.service.GoodsService;
import com.xyz.util.BaseContext;
import com.xyz.vo.GoodsCardVO;
import com.xyz.vo.GoodsDetailVO;
import com.xyz.vo.PageResult;
import com.xyz.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/goods")
@Tag(name = "商品相关接口")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @PostMapping
    @Operation(summary = "发布商品")
    public Result releaseGoods(@RequestBody GoodsDTO goodsDTO) {
        long ownerId = BaseContext.getCurrentId();
        goodsService.releaseGoods(ownerId,goodsDTO);
        return Result.success("发布成功");
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取商品列表（无限滚动）")
    public Result<PageResult<GoodsCardVO>> getGoodsPage(
            @RequestParam Long categoryId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<GoodsCardVO> page = goodsService.getGoodsPageByCategoryId(categoryId, cursor, size);
        return Result.success(page);
    }

    @GetMapping("/my/page")
    @Operation(summary = "分页获取当前用户的商品列表（无限滚动）")
    public Result<PageResult<GoodsCardVO>> getMyGoodsPage(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") Integer size) {
        Long currentUserId = BaseContext.getCurrentId();
        PageResult<GoodsCardVO> page = goodsService.getGoodsPageByOwnerId(currentUserId, cursor, size);
        return Result.success(page);
    }



    @GetMapping("/search")
    @Operation(summary = "搜索商品")
    public Result<PageResult<GoodsCardVO>> searchGoods(
            @RequestParam String keyword,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<GoodsCardVO> page = goodsService.searchGoods(keyword, cursor, size);
        return Result.success(page);
    }

    @PutMapping("/{id}/offline")
    @Operation(summary = "下架商品")
    public Result offlineGoods(@PathVariable Long id) {
        Long currentUserId = BaseContext.getCurrentId();
        goodsService.offlineGoods(id, currentUserId);
        return Result.success("下架成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询商品详情")
    public Result<GoodsDetailVO> getGoodsDetail(@PathVariable Long id) {
        GoodsDetailVO detail = goodsService.getGoodsDetailById(id);
        return Result.success(detail);
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑商品")
    public Result updateGoods(
            @PathVariable Long id,
            @RequestBody GoodsDTO goodsDTO) {
        Long currentUserId = BaseContext.getCurrentId();
        goodsService.updateGoods(id, goodsDTO, currentUserId);
        return Result.success("更新成功");
    }

    @PutMapping("/{id}/online")
    @Operation(summary = "重新上架商品")
    public Result onlineGoods(@PathVariable Long id) {
        Long currentUserId = BaseContext.getCurrentId();
        goodsService.onlineGoods(id, currentUserId);
        return Result.success("上架成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品")
    public Result deleteGoods(@PathVariable Long id) {
        Long currentUserId = BaseContext.getCurrentId();
        goodsService.deleteGoods(id, currentUserId);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/sold")
    @Operation(summary = "标记商品为已售出")
    public Result markAsSold(@PathVariable Long id) {
        Long currentUserId = BaseContext.getCurrentId();
        goodsService.markAsSold(id, currentUserId);
        return Result.success("已标记为已售出");
    }

    @PutMapping("/{id}/rent")
    @Operation(summary = "标记商品为租借中")
    public Result markAsRenting(@PathVariable Long id) {
        Long currentUserId = BaseContext.getCurrentId();
        goodsService.markAsRenting(id, currentUserId);
        return Result.success("已标记为租借中");
    }
}
