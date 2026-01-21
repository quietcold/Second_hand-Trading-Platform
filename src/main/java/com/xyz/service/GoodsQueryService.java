package com.xyz.service;

import com.xyz.vo.GoodsCardVO;
import com.xyz.vo.PageResult;

/**
 * 商品查询服务接口（整合收藏、发布、分类查询）
 */
public interface GoodsQueryService {

    /**
     * 游标分页查询商品列表（按分类）
     * @param categoryId 分类ID
     * @param cursor 游标（时间戳毫秒）
     * @param size 每页数量
     * @return 分页结果
     */
    PageResult<GoodsCardVO> getGoodsPageByCategoryId(Long categoryId, Long cursor, Integer size);

    /**
     * 获取用户的收藏商品列表（游标分页）
     * @param userId 用户ID
     * @param cursor 游标（时间戳毫秒）
     * @param size 每页数量
     * @return 分页结果
     */
    PageResult<GoodsCardVO> getFavoriteGoods(Long userId, Long cursor, Integer size);
    
    /**
     * 查询他人发布的所有商品（游标分页）
     * @param targetUserId 目标用户ID
     * @param cursor 游标（时间戳毫秒）
     * @param size 每页数量
     * @return 分页结果
     */
    PageResult<GoodsCardVO> getUserPublishedGoods(Long targetUserId, Long cursor, Integer size);
    
    /**
     * 查询我发布的所有商品（游标分页）
     * @param userId 当前用户ID
     * @param cursor 游标（时间戳毫秒）
     * @param size 每页数量
     * @return 分页结果
     */
    PageResult<GoodsCardVO> getMyPublishedGoods(Long userId, Long cursor, Integer size);

    /**
     * 游标分页查询所有上架商品列表（按更新时间倒序）
     * @param cursor 游标（时间戳毫秒）
     * @param size 每页数量
     * @return 分页结果
     */
    PageResult<GoodsCardVO> getAllGoodsPage(Long cursor, Integer size);

    /**
     * 查询我下架的商品（游标分页）
     * @param userId 当前用户ID
     * @param cursor 游标（时间戳毫秒）
     * @param size 每页数量
     * @return 分页结果
     */
    PageResult<GoodsCardVO> getMyOfflineGoods(Long userId, Long cursor, Integer size);
}
