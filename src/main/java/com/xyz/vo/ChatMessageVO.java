package com.xyz.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天消息信息")
public class ChatMessageVO {
    
    @Schema(description = "消息ID")
    private Long id;
    
    @Schema(description = "会话ID")
    private String sessionId;
    
    @Schema(description = "发送者ID")
    private Long senderId;
    
    @Schema(description = "发送者用户名")
    private String senderUsername;
    
    @Schema(description = "发送者头像")
    private String senderAvatar;
    
    @Schema(description = "接收者ID")
    private Long receiverId;
    
    @Schema(description = "消息类型: 1文本, 2图片, 3商品卡片")
    private Integer messageType;
    
    @Schema(description = "消息内容")
    private String content;
    
    @Schema(description = "是否已读: 0未读, 1已读")
    private Integer isRead;
    
    @Schema(description = "是否已撤回: 0未撤回, 1已撤回")
    private Integer isRecalled;
    
    @Schema(description = "发送时间戳")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Long sendTime;
}
