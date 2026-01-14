package com.xyz.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户详细信息VO（管理员查看）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailVO {
    private Long id;
    private String accountNum;
    private String nickname;
    private String email;
    private String phone;
    private String gender;
    private String image;
    private String bio;
    private Integer status; // 0-禁用，1-启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
