package com.xyz.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodsDTO {
    private Long ownerId; //卖家id
    private Integer goodsType; //1-售卖商品,2-租赁商品(必须)
    private String description;//商品描述(必须)
    private List<String> imageUrls;//图片集合(可选)
    private Long categoryId; //商品分类id(可选)
    private Integer conditionLevel; //1-全新，2-几乎全新，3-轻微使用痕迹，4-明显使用痕迹

    private BigDecimal sellPrice;
    private BigDecimal rentPrice;//租赁价格(只设置每天价格，其它的用户自己描述)

    private Integer status; //1-上架(待售卖和待租赁)，2-已售出，3-租期中
}
