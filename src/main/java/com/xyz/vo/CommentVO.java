package com.xyz.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 评论视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO {

    private Long commentId; //评论ID
    private Long userId; //评论用户ID
    private String content; //评论内容
    private Integer likeCount; //点赞数
    private Long createTimestamp; //创建时间戳（毫秒）
    private Integer status; //评论状态：1-正常，2-已删除，3-违规屏蔽
    
    @JsonIgnore  // 不返回给前端，只用于内部计算
    private LocalDateTime createTime;

    private Integer replyCount; //回复数量（仅顶层评论有此字段）
    private CommentVO latestReply; //最新一条回复（仅顶层评论有此字段）

    private Long replyToUserId; //被回复的用户ID（如果是回复评论）
    private String replyToUserNickname; //被回复的用户昵称（如果是回复评论）

    private Boolean hasLiked; //当前用户是否已点赞

    private String userNickname; //评论用户昵称
    private String userAvatar; //评论用户头像
    private Boolean isSeller; //是否为卖家
    
    // 自定义 setter，设置 createTime 时同时计算 createTimestamp
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        if (createTime != null) {
            this.createTimestamp = createTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
    }
}
