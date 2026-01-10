package com.xyz.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsCardVO {
    private Long id;
    private Long ownerId;
    private String briefDescription; // 截取前25字+...
    private String coverUrl; // 第一张图片
    private Integer goodsType; // 1-售卖商品, 2-租赁商品
    private Integer conditionLevel; // 1-全新, 2-几乎全新, 3-轻微使用痕迹, 4-明显使用痕迹
    private Integer collectNum; // 收藏数
    private Long categoryId; //商品分类id
    private BigDecimal sellPrice;
    private BigDecimal rentPrice;

    // 卖家信息（连表查询）
    private String ownerName;
    private String ownerAvatar;
    
    // 更新时间戳（毫秒），用于游标分页
    private Long updateTimestamp;
}
