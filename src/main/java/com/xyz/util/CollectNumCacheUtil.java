package com.xyz.util;

import com.xyz.constant.RedisConstant;
import com.xyz.mapper.GoodsQueryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 收藏数缓存工具类
 */
@Slf4j
@Component
public class CollectNumCacheUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private GoodsQueryMapper goodsQueryMapper;

    /**
     * 获取单个商品的收藏数
     * @param goodsId 商品ID
     * @return 收藏数，获取失败返回null
     */
    public Integer getCollectNum(Long goodsId) {
        try {
            String collectKey = RedisConstant.GOODS_COLLECT_KEY + goodsId;
            Object collectNumObj = redisTemplate.opsForValue().get(collectKey);
            
            if (collectNumObj != null) {
                return (Integer) collectNumObj;
            } else {
                // 缓存未命中，从数据库查询并缓存
                Map<Long, Integer> dbResult = goodsQueryMapper.getCollectNumsByIds(Arrays.asList(goodsId));
                Integer collectNum = dbResult.get(goodsId);
                if (collectNum != null) {
                    redisTemplate.opsForValue().set(collectKey, collectNum, 
                            RedisConstant.GOODS_COLLECT_TTL, TimeUnit.MINUTES);
                    return collectNum;
                }
            }
        } catch (Exception e) {
            log.error("获取商品收藏数失败: goodsId={}, error={}", goodsId, e.getMessage());
        }
        return null;
    }

    /**
     * 批量获取商品收藏数
     * @param goodsIds 商品ID列表
     * @return Map<商品ID, 收藏数>
     */
    public Map<Long, Integer> batchGetCollectNum(List<Long> goodsIds) {
        Map<Long, Integer> result = new HashMap<>();
        
        if (goodsIds == null || goodsIds.isEmpty()) {
            return result;
        }
        
        try {
            // 1. 构建缓存keys
            List<String> collectKeys = new ArrayList<>();
            for (Long goodsId : goodsIds) {
                collectKeys.add(RedisConstant.GOODS_COLLECT_KEY + goodsId);
            }
            
            // 2. 批量从缓存获取
            List<Object> collectNums = redisTemplate.opsForValue().multiGet(collectKeys);
            
            // 3. 找出缓存未命中的商品ID
            List<Long> missIds = new ArrayList<>();
            
            for (int i = 0; i < goodsIds.size(); i++) {
                Long goodsId = goodsIds.get(i);
                Object collectNum = (collectNums != null && i < collectNums.size()) ? collectNums.get(i) : null;
                if (collectNum != null) {
                    result.put(goodsId, (Integer) collectNum);
                } else {
                    missIds.add(goodsId);
                }
            }
            
            // 4. 缓存未命中的，从数据库查询并存入缓存
            if (!missIds.isEmpty()) {
                Map<Long, Integer> dbCollectNums = goodsQueryMapper.getCollectNumsByIds(missIds);
                for (Map.Entry<Long, Integer> entry : dbCollectNums.entrySet()) {
                    Long goodsId = entry.getKey();
                    Integer collectNum = entry.getValue();
                    result.put(goodsId, collectNum);
                    
                    // 存入缓存
                    String collectKey = RedisConstant.GOODS_COLLECT_KEY + goodsId;
                    redisTemplate.opsForValue().set(collectKey, collectNum, 
                            RedisConstant.GOODS_COLLECT_TTL, TimeUnit.MINUTES);
                }
            }
            
        } catch (Exception e) {
            log.error("批量获取商品收藏数失败: goodsIds={}, error={}", goodsIds, e.getMessage());
        }
        
        return result;
    }

    /**
     * 更新收藏数缓存
     * @param goodsId 商品ID
     * @param delta 变化量
     */
    public void updateCollectNumCache(Long goodsId, Integer delta) {
        try {
            String collectKey = RedisConstant.GOODS_COLLECT_KEY + goodsId;
            
            // 尝试增量更新缓存
            Long newCollectNum = redisTemplate.opsForValue().increment(collectKey, delta);
            
            // 如果缓存不存在（increment返回的是delta值），从数据库查询并设置
            if (newCollectNum.equals(delta.longValue())) {
                Map<Long, Integer> dbResult = goodsQueryMapper.getCollectNumsByIds(Arrays.asList(goodsId));
                Integer dbCollectNum = dbResult.get(goodsId);
                if (dbCollectNum != null) {
                    redisTemplate.opsForValue().set(collectKey, dbCollectNum, 
                            RedisConstant.GOODS_COLLECT_TTL, TimeUnit.MINUTES);
                }
            } else {
                // 设置过期时间（increment不会自动设置过期时间）
                redisTemplate.expire(collectKey, RedisConstant.GOODS_COLLECT_TTL, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.error("更新收藏数缓存失败: goodsId={}, delta={}, error={}", goodsId, delta, e.getMessage());
            // 删除缓存，下次查询时重新从数据库获取
            String collectKey = RedisConstant.GOODS_COLLECT_KEY + goodsId;
            redisTemplate.delete(collectKey);
        }
    }
}