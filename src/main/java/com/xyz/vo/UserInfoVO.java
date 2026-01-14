package com.xyz.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息VO（不包含密码）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoVO {
    private Long id;
    private String accountNum;
    private String email;
    private String phone;
    private String nickname;
    private String gender;
    private String image;
    private String bio; // 个人简介
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
