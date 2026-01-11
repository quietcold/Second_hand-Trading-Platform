package com.xyz.service;

import com.xyz.dto.CommentDTO;
import com.xyz.vo.CommentVO;
import com.xyz.vo.PageResult;

/**
 * 评论服务接口
 */
public interface CommentService {

    /**
     * 发表评论
     */
    CommentVO addComment(CommentDTO commentDTO);

    /**
     * 删除评论
     */
    void deleteComment(Long commentId);

    /**
     * 点赞/取消点赞评论
     */
    void likeComment(Long commentId);

    /**
     * 获取商品的顶层评论列表（游标分页）
     */
    PageResult<CommentVO> getTopComments(Long goodsId, Long cursor, Integer size);

    /**
     * 获取某条评论的所有回复（游标分页）
     */
    PageResult<CommentVO> getReplies(Long parentId, Long cursor, Integer size);
}
