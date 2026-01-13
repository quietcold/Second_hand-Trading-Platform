package com.xyz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送消息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发送消息参数")
public class ChatMessageDTO {
    
    @Schema(description = "会话ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;
    
    @Schema(description = "接收者用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;
    
    @Schema(description = "消息类型: 1文本, 2图片, 3商品卡片", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "消息类型不能为空")
    private Integer messageType;
    
    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "消息内容不能为空")
    private String content;
    
    @Schema(description = "关联商品ID (商品卡片类型时必填)")
    private Long goodsId;
}
