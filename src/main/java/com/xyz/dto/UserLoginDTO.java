package com.xyz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户登录请求DTO
 */
@Data
@Schema(description = "用户登录请求参数")
public class UserLoginDTO {
    @Schema(description = "账号", example = "2637",requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountNum;
    @Schema(description = "密码", example = "1234",requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}