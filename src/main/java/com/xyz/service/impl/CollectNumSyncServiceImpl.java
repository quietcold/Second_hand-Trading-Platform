package com.xyz.service.impl;

import com.xyz.constant.RedisConstant;
import com.xyz.mapper.GoodsMapper;
import com.xyz.service.CollectNumSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 收藏数同步服务实现类
 */
@Slf4j
@Service
public class CollectNumSyncServiceImpl implements CollectNumSyncService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private GoodsMapper goodsMapper;
    
    /** 待同步商品ID集合的Redis Key */
    private static final String SYNC_PENDING_KEY = "goods:collect:sync:pending";
    
    /** 同步锁的Redis Key */
    private static final String SYNC_LOCK_KEY = "goods:collect:sync:lock";
    
    /** 同步锁过期时间（秒） */
    private static final int SYNC_LOCK_TTL = 300; // 5分钟

    @Override
    public void markForSync(Long goodsId) {
        try {
            // 将商品ID添加到待同步集合
            redisTemplate.opsForSet().add(SYNC_PENDING_KEY, goodsId);
            // 设置过期时间，防止集合无限增长
            redisTemplate.expire(SYNC_PENDING_KEY, 24, TimeUnit.HOURS);
            log.debug("标记商品收藏数待同步: goodsId={}", goodsId);
        } catch (Exception e) {
            log.error("标记商品收藏数待同步失败: goodsId={}, error={}", goodsId, e.getMessage());
        }
    }

    @Override
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void batchSyncCollectNum() {
        // 尝试获取分布式锁
        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(SYNC_LOCK_KEY, "locked", SYNC_LOCK_TTL, TimeUnit.SECONDS);
        
        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                doSyncCollectNum();
            } finally {
                // 释放锁
                redisTemplate.delete(SYNC_LOCK_KEY);
            }
        } else {
            log.debug("收藏数同步任务已在其他实例执行，跳过本次同步");
        }
    }

    @Override
    @Async("asyncTaskExecutor")
    public void forceSyncCollectNum(Long goodsId) {
        try {
            syncSingleGoodsCollectNum(goodsId);
            log.info("强制同步商品收藏数完成: goodsId={}", goodsId);
        } catch (Exception e) {
            log.error("强制同步商品收藏数失败: goodsId={}, error={}", goodsId, e.getMessage());
        }
    }

    /**
     * 执行收藏数同步
     */
    private void doSyncCollectNum() {
        try {
            // 获取所有待同步的商品ID
            Set<Object> pendingIds = redisTemplate.opsForSet().members(SYNC_PENDING_KEY);
            
            if (pendingIds == null || pendingIds.isEmpty()) {
                log.debug("没有待同步的商品收藏数");
                return;
            }
            
            log.info("开始批量同步商品收藏数，待同步商品数量: {}", pendingIds.size());
            
            int successCount = 0;
            int failCount = 0;
            
            for (Object idObj : pendingIds) {
                try {
                    Long goodsId = ((Number) idObj).longValue();
                    syncSingleGoodsCollectNum(goodsId);
                    
                    // 同步成功后从待同步集合中移除
                    redisTemplate.opsForSet().remove(SYNC_PENDING_KEY, goodsId);
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("同步单个商品收藏数失败: goodsId={}, error={}", idObj, e.getMessage());
                    failCount++;
                }
            }
            
            log.info("批量同步商品收藏数完成，成功: {}, 失败: {}", successCount, failCount);
            
        } catch (Exception e) {
            log.error("批量同步商品收藏数异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 同步单个商品的收藏数
     */
    @Transactional
    private void syncSingleGoodsCollectNum(Long goodsId) {
        // 从Redis获取最新的收藏数
        String collectKey = RedisConstant.GOODS_COLLECT_KEY + goodsId;
        Object collectNumObj = redisTemplate.opsForValue().get(collectKey);
        
        if (collectNumObj == null) {
            log.warn("商品收藏数缓存不存在，跳过同步: goodsId={}", goodsId);
            return;
        }
        
        Integer cacheCollectNum = (Integer) collectNumObj;
        
        // 获取数据库中的收藏数
        Integer dbCollectNum = goodsMapper.getCollectNumById(goodsId);
        if (dbCollectNum == null) {
            log.warn("商品不存在，跳过同步: goodsId={}", goodsId);
            return;
        }
        
        // 如果缓存和数据库的收藏数不一致，则更新数据库
        if (!cacheCollectNum.equals(dbCollectNum)) {
            int delta = cacheCollectNum - dbCollectNum;
            goodsMapper.updateCollectNum(goodsId, delta);
            log.debug("同步商品收藏数: goodsId={}, 数据库: {}, 缓存: {}, 增量: {}", 
                     goodsId, dbCollectNum, cacheCollectNum, delta);
        }
    }
}