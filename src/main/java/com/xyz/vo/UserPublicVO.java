package com.xyz.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户公开信息VO（不包含隐私信息）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPublicVO {
    private Long id;
    private String nickname;
    private String gender;
    private String image;
    private String bio; // 个人简介
    private LocalDateTime createTime;
}
