package com.xyz.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Goods {
    private Long id;
    private Long ownerId; //卖家id
    private Integer goodsType; //1-售卖商品,2-租赁商品
    
    private String description;
    private List<String> imageUrls;//商品图片的集合
    private Long categoryId; //商品分类id
    private Integer conditionLevel; //1-全新，2-几乎全新，3-轻微使用痕迹，4-明显使用痕迹
    private Integer collectNum; //收藏该商品的人数
    private Integer status; //1-上架(待售卖和待租赁)，2-已售出，3-租借中，4-已下架，5-用户自己删除，6-违规被下架/系统屏蔽

    private BigDecimal sellPrice;
    private BigDecimal rentPrice;//租赁价格(只设置每天价格，其它的用户自己描述)

    //冒余字段，直接把 images 中的第一张图存到这里作为封面图片
    private String coverUrl;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
