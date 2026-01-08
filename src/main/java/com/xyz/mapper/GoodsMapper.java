package com.xyz.mapper;

import com.xyz.entity.Goods;
import com.xyz.vo.GoodsCardVO;
import com.xyz.vo.GoodsDetailVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

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
     * 根据分类ID查询商品列表（包含卖家信息）
     */
    @Select("SELECT g.id, g.owner_id AS ownerId, g.goods_type AS goodsType, " +
            "CASE WHEN CHAR_LENGTH(g.description) > 25 THEN CONCAT(SUBSTRING(g.description, 1, 25), '...') " +
            "ELSE g.description END AS briefDescription, " +
            "g.cover_url AS coverUrl, g.condition_level AS conditionLevel, g.collect_num AS collectNum, " +
            "g.category_id AS categoryId, g.sell_price AS sellPrice, g.rent_price AS rentPrice, " +
            "u.nickname AS ownerName, u.image AS ownerAvatar " +
            "FROM goods g " +
            "LEFT JOIN user u ON g.owner_id = u.id " +
            "WHERE g.category_id = #{categoryId} AND g.status = 1 " +
            "ORDER BY g.update_time DESC")
    List<GoodsCardVO> getGoodsListByCategoryId(long categoryId);

    /**
     * 根据用户ID查询其发布的商品列表（包含卖家信息）
     */
    @Select("SELECT g.id, g.owner_id AS ownerId, g.goods_type AS goodsType, " +
            "CASE WHEN CHAR_LENGTH(g.description) > 25 THEN CONCAT(SUBSTRING(g.description, 1, 25), '...') " +
            "ELSE g.description END AS briefDescription, " +
            "g.cover_url AS coverUrl, g.condition_level AS conditionLevel, g.collect_num AS collectNum, " +
            "g.category_id AS categoryId, g.sell_price AS sellPrice, g.rent_price AS rentPrice, " +
            "u.nickname AS ownerName, u.image AS ownerAvatar " +
            "FROM goods g " +
            "LEFT JOIN user u ON g.owner_id = u.id " +
            "WHERE g.owner_id = #{ownerId} " +
            "ORDER BY g.update_time DESC")
    List<GoodsCardVO> getGoodsListByOwnerId(long ownerId);

    /**
     * 更新商品状态（下架、已售出、租期中等）
     */
    @Update("UPDATE goods SET status = #{status} WHERE id = #{id} AND owner_id = #{ownerId}")
    int updateGoodsStatus(@Param("id") Long id, @Param("ownerId") Long ownerId, @Param("status") Integer status);

    /**
     * 查询商品状态
     */
    @Select("SELECT status FROM goods WHERE id = #{id} AND owner_id = #{ownerId}")
    Integer getGoodsStatus(@Param("id") Long id, @Param("ownerId") Long ownerId);

    /**
     * 根据ID查询商品详情（包含卖家信息）
     */
    @Select("SELECT g.id, g.owner_id AS ownerId, g.goods_type AS goodsType, g.description, " +
            "g.image_urls AS imageUrls, g.category_id AS categoryId, g.condition_level AS conditionLevel, " +
            "g.collect_num AS collectNum, g.sell_price AS sellPrice, g.rent_price AS rentPrice, " +
            "g.status, g.create_time AS createTime, g.update_time AS updateTime, " +
            "u.nickname AS ownerName, u.image AS ownerAvatar " +
            "FROM goods g " +
            "LEFT JOIN user u ON g.owner_id = u.id " +
            "WHERE g.id = #{id}")
    @Results({
            @Result(property = "imageUrls", column = "imageUrls", 
                    typeHandler = com.xyz.handler.ListStringTypeHandler.class)
    })
    GoodsDetailVO getGoodsDetailById(Long id);

    /**
     * 更新商品信息（权限校验）
     */
    @Update("UPDATE goods SET goods_type = #{goodsType}, description = #{description}, " +
            "image_urls = #{imageUrls, typeHandler=com.xyz.handler.ListStringTypeHandler}, " +
            "category_id = #{categoryId}, condition_level = #{conditionLevel}, " +
            "sell_price = #{sellPrice}, rent_price = #{rentPrice}, cover_url = #{coverUrl} " +
            "WHERE id = #{id} AND owner_id = #{ownerId}")
    int updateGoods(Goods goods);

}
