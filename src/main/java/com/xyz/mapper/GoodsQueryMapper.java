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
     * 只返回上架状态的商品
     */
    @Select("SELECT gf.goods_id AS goodsId, UNIX_TIMESTAMP(gf.create_time) * 1000 AS createTime " +
            "FROM goods_favorite gf " +
            "INNER JOIN goods g ON gf.goods_id = g.id " +
            "WHERE gf.user_id = #{userId} AND g.status = 1")
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

    /** 游标分页查询所有上架商品列表（按更新时间倒序） */
    List<GoodsCardVO> getAllGoodsPage(@Param("cursor") long cursor,
                                      @Param("size") int size);

    /**
     * 查询所有上架商品的ID和更新时间戳（用于初始化ZSet缓存）
     */
    @Select("SELECT id, UNIX_TIMESTAMP(update_time) * 1000 AS updateTime FROM goods " +
            "WHERE status = 1")
    List<Map<String, Object>> getAllGoodsIdsWithTime();

    /**
     * 根据用户ID查询所有下架商品的ID和更新时间戳（用于初始化ZSet缓存）
     */
    @Select("SELECT id, UNIX_TIMESTAMP(update_time) * 1000 AS updateTime FROM goods " +
            "WHERE owner_id = #{ownerId} AND status = 4")
    List<Map<String, Object>> getOfflineGoodsIdsWithTimeByOwnerId(long ownerId);

    /** 游标分页查询用户下架的商品列表 */
    List<GoodsCardVO> getOfflineGoodsByOwnerId(@Param("ownerId") long ownerId,
                                               @Param("cursor") long cursor,
                                               @Param("size") int size);

    /**
     * 根据商品ID列表批量查询点赞数
     * @param ids 商品ID列表
     * @return Map<商品ID, 点赞数>
     */
    @Select("<script>" +
            "SELECT id, collect_num FROM goods WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Map<String, Object>> getCollectNumsByIdsRaw(@Param("ids") List<Long> ids);

    /**
     * 根据商品ID列表批量查询点赞数的默认方法
     */
    default Map<Long, Integer> getCollectNumsByIds(List<Long> ids) {
        List<Map<String, Object>> rawResults = getCollectNumsByIdsRaw(ids);
        Map<Long, Integer> result = new java.util.HashMap<>();
        for (Map<String, Object> row : rawResults) {
            Long id = ((Number) row.get("id")).longValue();
            Integer collectNum = ((Number) row.get("collect_num")).intValue();
            result.put(id, collectNum);
        }
        return result;
    }
}
