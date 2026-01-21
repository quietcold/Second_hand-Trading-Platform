package com.xyz.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodsDTO {

    @NotNull(message = "商品类型不能为空")
    private Integer goodsType; //1-售卖商品,2-租赁商品(必须)
    @NotNull(message = "商品描述不能为空")
    private String description;//商品描述(必须)
    @NotEmpty(message = "请至少上传一张图片噢")
    private List<String> imageUrls;//图片集合(必须至少一张)
    private Long categoryId; //商品分类id(可选)
    @NotNull(message = "商品成色不能为空")
    private Integer conditionLevel; //1-全新，2-几乎全新，3-轻微使用痕迹，4-明显使用痕迹

    private BigDecimal sellPrice;
    private BigDecimal rentPrice;//租赁价格(只设置每天价格，其它的用户自己描述)

}
