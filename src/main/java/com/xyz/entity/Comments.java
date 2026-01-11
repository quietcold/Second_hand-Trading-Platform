package com.xyz.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评论实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comments {
    private Long id; //评论ID
    private Long goodsId; //商品ID
    private Long userId; //评论用户ID
    private String content; //评论内容
    private Integer likeCount;  //点赞数
    private Integer status; //评论状态：1-正常，2-已删除，3-违规屏蔽

    private Long parentId; //父评论ID（如果是回复评论，则记录被回复的评论ID；顶级评论则为null）
    private Long replyToUserId; //被回复的用户ID（如果是回复评论，则记录被回复的用户ID；顶级评论则为null）

    private LocalDateTime createTime; //创建时间
    private LocalDateTime updateTime; //更新时间
}
