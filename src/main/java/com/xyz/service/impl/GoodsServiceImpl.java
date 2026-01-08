package com.xyz.service.impl;

import com.xyz.constant.MessageConstant;
import com.xyz.dto.GoodsDTO;
import com.xyz.entity.Goods;
import com.xyz.exception.GoodsInRentException;
import com.xyz.exception.GoodsNotFoundException;
import com.xyz.mapper.GoodsMapper;
import com.xyz.service.GoodsService;
import com.xyz.vo.GoodsCardVO;
import com.xyz.vo.GoodsDetailVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    @Transactional
    @CacheEvict(value = "goodsList", key = "#goodsDTO.categoryId")
    public void releaseGoods(GoodsDTO goodsDTO) {
        Goods goods = new Goods();
        BeanUtils.copyProperties(goodsDTO, goods);
        goods.setCollectNum(0);
        
        // 设置封面图（取第一张）
        List<String> imageUrls = goodsDTO.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            goods.setCoverUrl(imageUrls.get(0));
        }
        
        goodsMapper.insertGoods(goods);
    }

    @Override
    @Cacheable(value = "goodsList", key = "#categoryId", unless = "#result == null || #result.isEmpty()")
    public List<GoodsCardVO> getGoodsListByCategoryId(long categoryId) {
        return goodsMapper.getGoodsListByCategoryId(categoryId);
    }

    @Override
    @Cacheable(value = "userGoodsList", key = "#ownerId", unless = "#result == null || #result.isEmpty()")
    public List<GoodsCardVO> getGoodsListByOwnerId(long ownerId) {
        return goodsMapper.getGoodsListByOwnerId(ownerId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"goodsList", "userGoodsList"}, allEntries = true)
    public void offlineGoods(Long goodsId, Long ownerId) {
        // 1. 查询商品当前状态
        Integer currentStatus = goodsMapper.getGoodsStatus(goodsId, ownerId);
        if (currentStatus == null) {
            throw new GoodsNotFoundException(MessageConstant.GOODS_NOT_FOUND_OR_NO_PERMISSION);
        }
        
        // 2. 判断是否在租借中（status=3）
        if (currentStatus == 3) {
            throw new GoodsInRentException(MessageConstant.GOODS_IN_RENT_CANNOT_OFFLINE);
        }
        
        // 3. 将状态更新为4（已下架）
        goodsMapper.updateGoodsStatus(goodsId, ownerId, 4);
    }

    @Override
    public GoodsDetailVO getGoodsDetailById(Long id) {
        return goodsMapper.getGoodsDetailById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"goodsList", "userGoodsList"}, allEntries = true)
    public void updateGoods(Long goodsId, GoodsDTO goodsDTO, Long ownerId) {
        // 1. 构建更新对象
        Goods goods = new Goods();
        BeanUtils.copyProperties(goodsDTO, goods);
        goods.setId(goodsId);
        goods.setOwnerId(ownerId);
        
        // 2. 更新封面图（取第一张）
        List<String> imageUrls = goodsDTO.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            goods.setCoverUrl(imageUrls.get(0));
        }
        
        // 3. 执行更新（WHERE中包含id和owner_id，权限校验）
        int rows = goodsMapper.updateGoods(goods);
        if (rows == 0) {
            throw new GoodsNotFoundException(MessageConstant.GOODS_UPDATE_FAILED);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"goodsList", "userGoodsList"}, allEntries = true)
    public void onlineGoods(Long goodsId, Long ownerId) {
        // 重新上架：将状态改为1
        int rows = goodsMapper.updateGoodsStatus(goodsId, ownerId, 1);
        if (rows == 0) {
            throw new GoodsNotFoundException(MessageConstant.GOODS_NOT_FOUND_OR_NO_PERMISSION);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"goodsList", "userGoodsList"}, allEntries = true)
    public void deleteGoods(Long goodsId, Long ownerId) {
        // 软删除：将状态改为5（用户删除）
        int rows = goodsMapper.updateGoodsStatus(goodsId, ownerId, 5);
        if (rows == 0) {
            throw new GoodsNotFoundException(MessageConstant.GOODS_NOT_FOUND_OR_NO_PERMISSION);
        }
    }
}
