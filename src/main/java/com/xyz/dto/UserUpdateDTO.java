package com.xyz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户修改信息DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {
    private String email;
    private String phone;
    private String nickname;
    private String gender;
    private String image;
    private String bio; // 个人简介
}
