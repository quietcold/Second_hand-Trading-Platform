package com.xyz.controller;

import com.xyz.dto.GoodsDTO;
import com.xyz.service.GoodsService;
import com.xyz.util.BaseContext;
import com.xyz.vo.GoodsCardVO;
import com.xyz.vo.GoodsDetailVO;
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
        Long currentUserId = BaseContext.getCurrentId();
        goodsDTO.setOwnerId(currentUserId);
        goodsService.releaseGoods(goodsDTO);
        return Result.success("发布成功");
    }

    @GetMapping("/list")
    @Operation(summary = "根据分类ID获取商品列表")
    public Result<List<GoodsCardVO>> getGoodsListByCategoryId(@RequestParam Long categoryId) {
        List<GoodsCardVO> list = goodsService.getGoodsListByCategoryId(categoryId);
        return Result.success(list);
    }

    @GetMapping("/my")
    @Operation(summary = "获取当前用户自己发布的商品列表")
    public Result<List<GoodsCardVO>> getMyGoodsList() {
        Long currentUserId = BaseContext.getCurrentId();
        List<GoodsCardVO> list = goodsService.getGoodsListByOwnerId(currentUserId);
        return Result.success(list);
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
}
