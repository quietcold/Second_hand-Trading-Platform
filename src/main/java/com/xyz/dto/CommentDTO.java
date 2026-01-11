package com.xyz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    
    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;
    
    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    /**
     * 父评论ID（如果是回复评论，则传此值；顶级评论则为null）
     */
    private Long parentId;
    
    /**
     * 被回复的用户ID（如果是回复评论，则传此值）
     */
    private Long replyToUserId;
}
