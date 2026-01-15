package com.xyz.controller.user;

import com.xyz.dto.GoodsDTO;
import com.xyz.service.GoodsService;
import com.xyz.util.BaseContext;
import com.xyz.vo.GoodsCardVO;
import com.xyz.vo.GoodsDetailVO;
import com.xyz.vo.PageResult;
import com.xyz.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("userGoodsController")
@RequestMapping("/user/goods")
@Tag(name = "商品管理")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @PostMapping
    @Operation(summary = "发布商品")
    public Result releaseGoods(@RequestBody GoodsDTO goodsDTO) {
        goodsService.releaseGoods(goodsDTO);
        return Result.success("发布成功");
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

    // ==================== 收藏相关接口 ====================

    /**
     * 收藏/取消收藏商品（切换状态）
     */
    @PostMapping("/{id}/favorite")
    @Operation(summary = "收藏/取消收藏商品")
    public Result<String> toggleFavorite(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        try {
            Long userId = BaseContext.getCurrentId();
            boolean isFavorited = goodsService.toggleFavorite(userId, id);

            if (isFavorited) {
                return Result.success("收藏成功");
            } else {
                return Result.success("取消收藏成功");
            }
        } catch (Exception e) {
            log.error("切换收藏状态失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 判断用户是否已收藏某商品
     */
    @GetMapping("/{id}/favorite")
    @Operation(summary = "检查是否已收藏")
    public Result<Boolean> checkFavorite(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        try {
            Long userId = BaseContext.getCurrentId();
            boolean isFavorited = goodsService.isFavorite(userId, id);
            return Result.success(isFavorited);
        } catch (Exception e) {
            log.error("检查收藏状态失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
