package com.xyz.mapper;

import com.xyz.entity.Comments;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommentMapper {

    /**
     * 插入评论
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO comments(goods_id, user_id, content, parent_id, reply_to_user_id, like_count, status) " +
            "VALUES(#{goodsId}, #{userId}, #{content}, #{parentId}, #{replyToUserId}, 0, 1)")
    void insertComment(Comments comment);

    /**
     * 根据ID查询评论
     */
    @Select("SELECT * FROM comments WHERE id = #{id} AND status = 1")
    Comments getCommentById(Long id);

    /**
     * 更新评论点赞数
     */
    @Update("UPDATE comments SET like_count = #{likeCount} WHERE id = #{id}")
    int updateLikeCount(@Param("id") Long id, @Param("likeCount") Integer likeCount);

    /**
     * 删除评论（软删除）
     */
    @Update("UPDATE comments SET status = 2 WHERE id = #{id} AND user_id = #{userId}")
    int deleteComment(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 游标分页查询商品的顶层评论（按创建时间倒序）
     */
    @Select("SELECT * FROM comments " +
            "WHERE goods_id = #{goodsId} AND parent_id IS NULL AND status = 1 " +
            "AND UNIX_TIMESTAMP(create_time) * 1000 < #{cursor} " +
            "ORDER BY create_time DESC LIMIT #{size}")
    List<Comments> getTopCommentsByGoodsId(@Param("goodsId") Long goodsId,
                                           @Param("cursor") Long cursor,
                                           @Param("size") Integer size);

    /**
     * 游标分页查询某条评论的所有子评论（按创建时间倒序）
     */
    @Select("SELECT * FROM comments " +
            "WHERE parent_id = #{parentId} AND status = 1 " +
            "AND UNIX_TIMESTAMP(create_time) * 1000 < #{cursor} " +
            "ORDER BY create_time DESC LIMIT #{size}")
    List<Comments> getRepliesByParentId(@Param("parentId") Long parentId,
                                        @Param("cursor") Long cursor,
                                        @Param("size") Integer size);

    /**
     * 批量查询评论的回复数量（SQL 在 CommentMapper.xml 中）
     */
    List<Map<String, Object>> getReplyCountsByParentIds(@Param("parentIds") List<Long> parentIds);

    /**
     * 批量查询每条父评论的最新一条回复（SQL 在 CommentMapper.xml 中）
     */
    List<Comments> getLatestRepliesByParentIds(@Param("parentIds") List<Long> parentIds);

    /**
     * 查询某条父评论的最新一条回复
     */
    @Select("SELECT * FROM comments " +
            "WHERE parent_id = #{parentId} AND status = 1 " +
            "ORDER BY create_time DESC LIMIT 1")
    Comments getLatestReplyByParentId(Long parentId);

    /**
     * 查询某条评论的回复数量
     */
    @Select("SELECT COUNT(*) FROM comments WHERE parent_id = #{parentId} AND status = 1")
    Integer getReplyCountByParentId(Long parentId);

    /**
     * 根据商品ID查询商品的卖家ID
     */
    @Select("SELECT owner_id FROM goods WHERE id = #{goodsId}")
    Long getSellerIdByGoodsId(Long goodsId);

    /**
     * 管理员删除评论（违规屏蔽，状态改为3）
     */
    @Update("UPDATE comments SET status = 3 WHERE id = #{id}")
    int deleteCommentByAdmin(Long id);

    /**
     * 管理员恢复评论（状态改为1）
     */
    @Update("UPDATE comments SET status = 1 WHERE id = #{id}")
    int restoreCommentByAdmin(Long id);

    /**
     * 根据ID查询评论（不限制状态）
     */
    @Select("SELECT * FROM comments WHERE id = #{id}")
    Comments getCommentByIdWithoutStatus(Long id);

    /**
     * 管理员查询商品的顶层评论（所有状态）
     */
    @Select("SELECT * FROM comments " +
            "WHERE goods_id = #{goodsId} AND parent_id IS NULL " +
            "AND UNIX_TIMESTAMP(create_time) * 1000 < #{cursor} " +
            "ORDER BY create_time DESC LIMIT #{size}")
    List<Comments> getTopCommentsByGoodsIdForAdmin(@Param("goodsId") Long goodsId,
                                                    @Param("cursor") Long cursor,
                                                    @Param("size") Integer size);

    /**
     * 管理员查询评论的回复（所有状态）
     */
    @Select("SELECT * FROM comments " +
            "WHERE parent_id = #{parentId} " +
            "AND UNIX_TIMESTAMP(create_time) * 1000 < #{cursor} " +
            "ORDER BY create_time DESC LIMIT #{size}")
    List<Comments> getRepliesByParentIdForAdmin(@Param("parentId") Long parentId,
                                                 @Param("cursor") Long cursor,
                                                 @Param("size") Integer size);
}
