package com.xyz.mapper;

import com.xyz.vo.GoodsCardVO;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 商品收藏 Mapper 接口（仅保留查询相关方法）
 */
@Mapper
public interface GoodsQueryMapper {

    /**
     * 查询某商品的收藏数
     */
    @Select("SELECT COUNT(*) FROM goods_favorite WHERE goods_id = #{goodsId}")
    int countByGoodsId(Long goodsId);


    /**
     * 根据用户ID查询所有收藏的商品ID和收藏时间戳（用于初始化ZSet缓存）
     */
    @Select("SELECT goods_id AS goodsId, UNIX_TIMESTAMP(create_time) * 1000 AS createTime " +
            "FROM goods_favorite WHERE user_id = #{userId}")
    List<Map<String, Object>> getFavoriteIdsWithTimeByUserId(Long userId);

    /**
     * 根据收藏ID删除（用于管理员操作等）
     */
    @Delete("DELETE FROM goods_favorite WHERE id = #{id}")
    int deleteById(Long id);

    // ==================== 商品查询相关方法 ====================

    /**
     * 根据分类ID查询所有商品的ID和更新时间戳（用于初始化ZSet缓存）
     */
    @Select("SELECT id, UNIX_TIMESTAMP(update_time) * 1000 AS updateTime FROM goods " +
            "WHERE category_id = #{categoryId} AND status = 1")
    List<Map<String, Object>> getGoodsIdsWithTimeByCategoryId(long categoryId);

    /**
     * 根据用户ID查询所有商品的ID和更新时间戳
     */
    @Select("SELECT id, UNIX_TIMESTAMP(update_time) * 1000 AS updateTime FROM goods " +
            "WHERE owner_id = #{ownerId}")
    List<Map<String, Object>> getGoodsIdsWithTimeByOwnerId(long ownerId);


    /**
     * 游标分页查询用户自己的收藏列表（返回商品卡片）
     * 使用XML实现，需要连表查询商品和卖家信息
     */
    List<GoodsCardVO> getFavoriteGoodsByUserId(@Param("userId") Long userId,
                                               @Param("cursor") Long cursor,
                                               @Param("size") Integer size);

    /** 游标分页查询商品列表（按分类） */
    List<GoodsCardVO> getGoodsPageByCategoryId(@Param("categoryId") long categoryId,
                                               @Param("cursor") long cursor,
                                               @Param("size") int size);

    /** 游标分页查询用户发布的商品列表 */
    List<GoodsCardVO> getGoodsPageByOwnerId(@Param("ownerId") long ownerId,
                                            @Param("cursor") long cursor,
                                            @Param("size") int size);

    /** 根据ID列表批量查询商品卡片信息 */
    List<GoodsCardVO> getGoodsCardsByIds(@Param("ids") List<Long> ids);
}
