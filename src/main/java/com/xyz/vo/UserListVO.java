package com.xyz.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户列表VO（管理员查看）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserListVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String accountNum;
    private String nickname;
    private String email;
    private String phone;
    private String image;
    private Integer status; // 0-禁用，1-启用
    private LocalDateTime createTime;
    
    /**
     * 创建时间戳（用于游标分页，不返回给前端）
     */
    @JsonIgnore
    private Long createTimeTimestamp;
}
