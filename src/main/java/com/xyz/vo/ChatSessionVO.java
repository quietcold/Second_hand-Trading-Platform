package com.xyz.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天会话VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天会话信息")
public class ChatSessionVO {
    
    @Schema(description = "会话ID")
    private String sessionId;
    
    @Schema(description = "对方用户ID")
    private Long otherUserId;
    
    @Schema(description = "对方用户名")
    private String otherUsername;
    
    @Schema(description = "对方头像")
    private String otherAvatar;
    
    @Schema(description = "关联商品ID")
    private Long goodsId;
    
    @Schema(description = "商品标题")
    private String goodsTitle;
    
    @Schema(description = "商品封面")
    private String goodsCover;
    
    @Schema(description = "商品类型: 1-售卖商品, 2-租赁商品")
    private Integer goodsType;
    
    @Schema(description = "商品价格")
    private Double goodsPrice;
    
    @Schema(description = "最后一条消息")
    private String lastMessage;
    
    @Schema(description = "最后消息时间戳")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Long lastMessageTime;
    
    @Schema(description = "未读消息数")
    private Integer unreadCount;
    
    @Schema(description = "创建时间戳")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Long createTime;
}
