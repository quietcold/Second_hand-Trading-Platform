package com.xyz.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Admin {
    private Long id;
    private String username; // 管理员账号
    private String password; // 密码
    private String realName; // 真实姓名
    private String phone; // 联系电话
    private String email; // 邮箱
    private Integer status; // 状态：0-禁用，1-启用
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    private LocalDateTime lastLoginTime; // 最后登录时间
}
