package com.xyz.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户视图对象（不包含密码等敏感信息）
 */
@Builder
@Data
public class UserEnterVO {
    private Long id;
    private String token;

}