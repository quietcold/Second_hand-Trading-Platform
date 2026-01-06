package com.xyz.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private String accountNum;
    private String password;
    private String email;
    private String phone;
    private String nickname;
    private String gender;
    private String avatar;
    private Integer status; // 0-禁用，1-启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}