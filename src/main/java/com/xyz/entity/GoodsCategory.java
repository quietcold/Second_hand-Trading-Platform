package com.xyz.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsCategory {
    private long  id;
    private String name;
    private String code; //AI建议：前端如果想对某个特定分类做特殊UI处理，判断 code 比判断 id (1, 2) 更安全
    private LocalDateTime createTime;
    private LocalDateTime updateTime;


}
