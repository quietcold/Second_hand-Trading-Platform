package com.xyz.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员登录响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginVO {
    private Long id; // 管理员ID
    private String username; // 管理员账号
    private String realName; // 真实姓名
    private String token; // JWT Token
}
