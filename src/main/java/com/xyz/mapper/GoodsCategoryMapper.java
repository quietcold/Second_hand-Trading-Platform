package com.xyz.mapper;

import com.xyz.entity.GoodsCategory;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 商品分类Mapper接口
 */
@Mapper
public interface GoodsCategoryMapper {

    /**
     * 插入新分类
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO goods_category(name, code, display_order, status, create_time, update_time) " +
            "VALUES(#{name}, #{code}, #{displayOrder}, #{status}, #{createTime}, #{updateTime})")
    int insert(GoodsCategory category);

    /**
     * 根据ID查询分类
     */
    @Select("SELECT * FROM goods_category WHERE id = #{id}")
    GoodsCategory findById(Long id);

    /**
     * 根据code查询分类
     */
    @Select("SELECT * FROM goods_category WHERE code = #{code}")
    GoodsCategory findByCode(String code);

    /**
     * 查询所有分类（管理员）
     */
    @Select("SELECT * FROM goods_category ORDER BY display_order ASC, create_time ASC")
    List<GoodsCategory> findAll();

    /**
     * 查询所有上架的分类（用户端）
     */
    @Select("SELECT * FROM goods_category WHERE status = 1 ORDER BY display_order ASC, create_time ASC")
    List<GoodsCategory> findAllActive();

    /**
     * 更新分类
     */
    @Update("UPDATE goods_category SET name = #{name}, code = #{code}, display_order = #{displayOrder}, " +
            "status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int update(GoodsCategory category);

    /**
     * 删除分类
     */
    @Delete("DELETE FROM goods_category WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 检查是否有商品使用该分类
     */
    @Select("SELECT COUNT(*) FROM goods WHERE category_id = #{categoryId}")
    int countGoodsByCategory(Long categoryId);
}
