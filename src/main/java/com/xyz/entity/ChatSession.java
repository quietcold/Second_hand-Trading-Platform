package com.xyz.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天会话实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 会话ID (user1_user2格式,小ID在前)
     */
    private String sessionId;
    
    /**
     * 用户1ID
     */
    private Long user1Id;
    
    /**
     * 用户2ID
     */
    private Long user2Id;
    
    /**
     * 关联商品ID
     */
    private Long goodsId;
    
    /**
     * 最后一条消息
     */
    private String lastMessage;
    
    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageTime;
    
    /**
     * 用户1未读数
     */
    private Integer user1Unread;
    
    /**
     * 用户2未读数
     */
    private Integer user2Unread;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * user1是否隐藏会话: 0显示, 1隐藏
     */
    private Integer user1Hide;
    
    /**
     * user2是否隐藏会话: 0显示, 1隐藏
     */
    private Integer user2Hide;
}
