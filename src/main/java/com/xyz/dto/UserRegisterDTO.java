package com.xyz.dto;

import lombok.Data;

/**
 * 用户注册请求DTO
 */
@Data
public class UserRegisterDTO {
    private String accountNum;
    private String password;
    private String email;
    private String phone;
    private String nickname;
}