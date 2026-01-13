package com.xyz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建会话DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建会话参数")
public class ChatSessionDTO {
    
    @Schema(description = "接收者用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;
    
    @Schema(description = "关联商品ID")
    private Long goodsId;
}
