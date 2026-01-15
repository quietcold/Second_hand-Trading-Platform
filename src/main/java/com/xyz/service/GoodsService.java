package com.xyz.service;

import com.xyz.dto.GoodsDTO;

import com.xyz.dto.GoodsQueryDTO;
import com.xyz.vo.GoodsCardVO;
import com.xyz.vo.GoodsDetailVO;
import com.xyz.vo.PageResult;

import java.util.List;

public interface GoodsService {
    void releaseGoods(GoodsDTO goodsDTO);

    /**
     * 搜索商品（模糊查询）
     */
    PageResult<GoodsCardVO> searchGoods(String keyword, Long cursor, int size);

    void offlineGoods(Long goodsId, Long ownerId);

    GoodsDetailVO getGoodsDetailById(Long id);

    void updateGoods(Long goodsId, GoodsDTO goodsDTO, Long ownerId);

    void onlineGoods(Long goodsId, Long ownerId);

    void deleteGoods(Long goodsId, Long ownerId);

    /**
     * 标记商品为已售出
     */
    void markAsSold(Long goodsId, Long ownerId);

    /**
     * 标记商品为租借中
     */
    void markAsRenting(Long goodsId, Long ownerId);

    /**
     * 管理员违规下架商品（不需要owner_id验证）
     */
    void violationOfflineByAdmin(Long goodsId, String reason);

    /**
     * 管理员恢复违规下架的商品
     */
    void restoreGoodsByAdmin(Long goodsId);

    /**
     * 管理员多条件查询商品
     */
    PageResult<GoodsCardVO> queryGoodsByConditions(GoodsQueryDTO query);

    /**
     * 收藏/取消收藏商品（切换状态）
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @return true-已收藏, false-已取消收藏
     */
    boolean toggleFavorite(Long userId, Long goodsId);

    /**
     * 判断用户是否已收藏某商品
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @return true-已收藏, false-未收藏
     */
    boolean isFavorite(Long userId, Long goodsId);
}
