package com.xyz.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 消息类型: 1文本, 2图片, 3商品卡片
     */
    private Integer messageType;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 关联商品ID (商品卡片类型)
     */
    private Long goodsId;
    
    /**
     * 是否已读: 0未读, 1已读
     */
    private Integer isRead;
    
    /**
     * 是否已撤回: 0未撤回, 1已撤回
     */
    private Integer isRecalled;
    
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
}
