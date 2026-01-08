package com.xyz.service;

import com.xyz.dto.GoodsDTO;

import com.xyz.vo.GoodsCardVO;
import com.xyz.vo.GoodsDetailVO;

import java.util.List;

public interface GoodsService {
    void releaseGoods(GoodsDTO goodsDTO);

    List<GoodsCardVO> getGoodsListByCategoryId(long categoryId);

    List<GoodsCardVO> getGoodsListByOwnerId(long ownerId);

    void offlineGoods(Long goodsId, Long ownerId);

    GoodsDetailVO getGoodsDetailById(Long id);

    void updateGoods(Long goodsId, GoodsDTO goodsDTO, Long ownerId);

    void onlineGoods(Long goodsId, Long ownerId);

    void deleteGoods(Long goodsId, Long ownerId);
}
