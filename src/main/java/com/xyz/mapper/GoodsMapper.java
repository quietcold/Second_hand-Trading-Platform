package com.xyz.mapper;

import com.xyz.entity.Goods;
import com.xyz.vo.GoodsCardVO;
import com.xyz.vo.GoodsDetailVO;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface GoodsMapper {

    /**
     * 插入商品（时间由数据库自动生成）
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO goods(owner_id, goods_type, description, image_urls, category_id, " +
            "condition_level, collect_num, sell_price, rent_price, status, cover_url) " +
            "VALUES(#{ownerId}, #{goodsType}, #{description}, " +
            "#{imageUrls, typeHandler=com.xyz.handler.ListStringTypeHandler}, " +
            "#{categoryId}, #{conditionLevel}, #{collectNum}, #{sellPrice}, #{rentPrice}, #{status}, #{coverUrl})")
    void insertGoods(Goods goods);

    /**
     * 更新商品状态（下架、已售出、租期中等）
     */
    @Update("UPDATE goods SET status = #{status} WHERE id = #{id} AND owner_id = #{ownerId}")
    int updateGoodsStatus(@Param("id") Long id, @Param("ownerId") Long ownerId, @Param("status") Integer status);

    /**
     * 管理员更新商品状态（不需要owner_id验证）
     */
    @Update("UPDATE goods SET status = #{status} WHERE id = #{id}")
    int updateGoodsStatusByAdmin(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 查询商品状态
     */
    @Select("SELECT status FROM goods WHERE id = #{id} AND owner_id = #{ownerId}")
    Integer getGoodsStatus(@Param("id") Long id, @Param("ownerId") Long ownerId);

    /**
     * 管理员查询商品状态（不需要owner_id）
     */
    @Select("SELECT status FROM goods WHERE id = #{id}")
    Integer getGoodsStatusByAdmin(@Param("id") Long id);

    /**
     * 更新商品信息（权限校验）
     */
    @Update("UPDATE goods SET goods_type = #{goodsType}, description = #{description}, " +
            "image_urls = #{imageUrls, typeHandler=com.xyz.handler.ListStringTypeHandler}, " +
            "category_id = #{categoryId}, condition_level = #{conditionLevel}, " +
            "sell_price = #{sellPrice}, rent_price = #{rentPrice}, cover_url = #{coverUrl} " +
            "WHERE id = #{id} AND owner_id = #{ownerId}")
    int updateGoods(Goods goods);

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
     * 游标分页查询用户商品ID列表
     */
    @Select("SELECT id FROM goods WHERE owner_id = #{ownerId} " +
            "AND UNIX_TIMESTAMP(update_time) * 1000 < #{cursor} " +
            "ORDER BY update_time DESC LIMIT #{size}")
    List<Long> getGoodsIdsByOwnerId(@Param("ownerId") long ownerId,
                                    @Param("cursor") long cursor,
                                    @Param("size") int size);

    /**
     * 根据商品ID查询商品的分类ID、用户ID和更新时间戳
     */
    @Select("SELECT category_id AS categoryId, owner_id AS ownerId, " +
            "UNIX_TIMESTAMP(update_time) * 1000 AS updateTime FROM goods WHERE id = #{goodsId}")
    Map<String, Object> getGoodsCategoryAndTimeById(Long goodsId);

    // ========== 以下方法SQL较长，实现在 GoodsMapper.xml 中 ==========

    /** 游标分页查询商品列表（按分类） */
    List<GoodsCardVO> getGoodsPageByCategoryId(@Param("categoryId") long categoryId,
                                               @Param("cursor") long cursor,
                                               @Param("size") int size);

    /** 游标分页查询用户商品列表 */
    List<GoodsCardVO> getGoodsPageByOwnerId(@Param("ownerId") long ownerId,
                                            @Param("cursor") long cursor,
                                            @Param("size") int size);

    /** 根据ID列表批量查询商品卡片信息 */
    List<GoodsCardVO> getGoodsCardsByIds(@Param("ids") List<Long> ids);

    /** 根据ID查询商品卡片信息 */
    GoodsCardVO getGoodsCardById(Long id);

    /** 搜索商品ID（全文索引，只返回ID） */
    List<Long> searchGoodsIds(@Param("keyword") String keyword,
                              @Param("cursor") long cursor,
                              @Param("size") int size);

    /** 根据ID查询商品详情 */
    GoodsDetailVO getGoodsDetailById(Long id);

    /** 管理员多条件查询商品（动态SQL） */
    List<GoodsCardVO> queryGoodsByConditions(@Param("query") com.xyz.dto.GoodsQueryDTO query);
}
