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
     * 游标分页查询商品列表（用于无限滚动）
     * @param categoryId 分类ID
     * @param cursor 游标（时间戳毫秒），首次传null或不传
     * @param size 每页条数
     */
    PageResult<GoodsCardVO> getGoodsPageByCategoryId(long categoryId, Long cursor, int size);

    /**
     * 游标分页查询用户商品列表
     */
    PageResult<GoodsCardVO> getGoodsPageByOwnerId(long ownerId, Long cursor, int size);

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
}
