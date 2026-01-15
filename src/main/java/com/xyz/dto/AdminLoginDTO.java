package com.xyz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员登录请求DTO
 */
@Data
@Schema(description = "管理员登录请求参数")
public class AdminLoginDTO {
    @Schema(description = "管理员账号", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;
    
    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
