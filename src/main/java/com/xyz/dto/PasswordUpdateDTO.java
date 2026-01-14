package com.xyz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改密码DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordUpdateDTO {
    private String oldPassword; // 旧密码
    private String newPassword; // 新密码
}
