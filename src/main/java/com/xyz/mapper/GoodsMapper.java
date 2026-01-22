package com.xyz.mapper;

import com.xyz.entity.Goods;
import com.xyz.entity.GoodsFavorite;
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
     * 更新商品收藏数（增量更新）
     */
    @Update("UPDATE goods SET collect_num = collect_num + #{delta} WHERE id = #{id}")
    int updateCollectNum(@Param("id") Long id, @Param("delta") Integer delta);

    /**
     * 根据商品ID查询点赞数
     */
    @Select("SELECT collect_num FROM goods WHERE id = #{id}")
    Integer getCollectNumById(@Param("id") Long id);

    // ==================== 收藏相关 ====================

    /**
     * 添加收藏
     */
    @Insert("INSERT INTO goods_favorite(user_id, goods_id, create_time) " +
            "VALUES(#{userId}, #{goodsId}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertFavorite(GoodsFavorite goodsFavorite);

    /**
     * 取消收藏
     */
    @Delete("DELETE FROM goods_favorite WHERE user_id = #{userId} AND goods_id = #{goodsId}")
    int deleteFavorite(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    /**
     * 判断用户是否已收藏某商品
     */
    @Select("SELECT COUNT(*) FROM goods_favorite WHERE user_id = #{userId} AND goods_id = #{goodsId}")
    int checkFavorite(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    /**
     * 根据ID查询商品
     */
    @Select("SELECT * FROM goods WHERE id = #{id}")
    @Results(id = "goodsResultMap", value = {
            @Result(property = "imageUrls", column = "image_urls", 
                    typeHandler = com.xyz.handler.ListStringTypeHandler.class)
    })
    Goods getGoodsById(Long id);

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
     * 根据商品ID查询商品的分类ID、用户ID和更新时间戳
     */
    @Select("SELECT category_id AS categoryId, owner_id AS ownerId, " +
            "UNIX_TIMESTAMP(update_time) * 1000 AS updateTime FROM goods WHERE id = #{goodsId}")
    Map<String, Object> getGoodsCategoryAndTimeById(Long goodsId);

    // ========== 以下方法SQL较长，实现在 GoodsMapper.xml 中 ==========

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
