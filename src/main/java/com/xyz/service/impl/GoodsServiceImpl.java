package com.xyz.service.impl;

import com.xyz.constant.GoodsStatusConstant;
import com.xyz.constant.MessageConstant;
import com.xyz.constant.RedisConstant;
import com.xyz.constant.GoodsConditionConstant;
import com.xyz.dto.GoodsDTO;
import com.xyz.dto.GoodsQueryDTO;
import com.xyz.entity.Goods;
import com.xyz.entity.GoodsFavorite;
import com.xyz.exception.GoodsInRentException;
import com.xyz.exception.GoodsNotFoundException;
import com.xyz.mapper.GoodsMapper;
import com.xyz.mapper.GoodsQueryMapper;
import com.xyz.service.CollectNumSyncService;
import com.xyz.service.GoodsService;
import com.xyz.util.BaseContext;
import com.xyz.util.CollectNumCacheUtil;
import com.xyz.vo.GoodsCardVO;
import com.xyz.vo.GoodsDetailVO;
import com.xyz.vo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsQueryServiceImpl goodsQueryServiceImpl;

    @Autowired
    private GoodsQueryMapper goodsQueryMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CollectNumSyncService collectNumSyncService;
    
    @Autowired
    private CollectNumCacheUtil collectNumCacheUtil;

    @Override
    @Transactional
    public void releaseGoods(GoodsDTO goodsDTO) {
        // 从上下文获取当前用户ID
        Long currentUserId = BaseContext.getCurrentId();
        
        // 验证图片列表不为空（至少一张）
        List<String> imageUrls = goodsDTO.getImageUrls();
        if (imageUrls == null || imageUrls.isEmpty()) {
            throw new RuntimeException("请至少上传一张图片噢");
        }
        
        Goods goods = new Goods();
        BeanUtils.copyProperties(goodsDTO, goods);
        goods.setCollectNum(0);
        goods.setOwnerId(currentUserId);
        // 商品状态由后端统一设置为上架
        goods.setStatus(GoodsStatusConstant.ON_SALE);
        
        // 设置封面图（取第一张）
        goods.setCoverUrl(imageUrls.get(0));
        
        goodsMapper.insertGoods(goods);
        
        // 添加到分类ZSet缓存（只有当分类ID不为空且ZSet已存在时才添加）
        if (goodsDTO.getCategoryId() != null) {
            String catKey = buildCategoryKey(goodsDTO.getCategoryId());
            if (Boolean.TRUE.equals(redisTemplate.hasKey(catKey))) {
                redisTemplate.opsForZSet().add(catKey, goods.getId(), System.currentTimeMillis());
            }
        }
        
        // 添加到用户商品ZSet缓存（只有当ZSet已存在时才添加）
        String ownerKey = buildOwnerKey(goods.getOwnerId());
        if (Boolean.TRUE.equals(redisTemplate.hasKey(ownerKey))) {
            redisTemplate.opsForZSet().add(ownerKey, goods.getId(), System.currentTimeMillis());
        }
        
        // 添加到所有商品ZSet缓存（只有当ZSet已存在时才添加）
        String allGoodsKey = RedisConstant.GOODS_ALL_IDS_KEY + RedisConstant.GOODS_ALL_IDS_SUFFIX;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(allGoodsKey))) {
            redisTemplate.opsForZSet().add(allGoodsKey, goods.getId(), System.currentTimeMillis());
        }
    }


    //TODO:这个搜索功能考虑直接放到GoodsQueryImpl中
    @Override
    public PageResult<GoodsCardVO> searchGoods(String keyword, Long cursor, int size) {
        long cursorTime = (cursor == null || cursor <= 0) ? System.currentTimeMillis() : cursor;
        
        // 1. 全文索引只查ID（轻量级查询）
        List<Long> ids = goodsMapper.searchGoodsIds(keyword, cursorTime, size + 1);
        
        if (ids == null || ids.isEmpty()) {
            return PageResult.empty();
        }

        //TODO:考虑怎么复用GoodsQueryImpl中的getGoodsCardsFromCacheOrDB方法
        // 2. 根据ID从Redis缓存批量获取商品卡片，未命中的再查MySQL（已包含时间戳）
        List<GoodsCardVO> list = getGoodsCardsFromCacheOrDB(ids);
        
        return buildPageResult(list, size);
    }


    //TODO：getGoodsCardsFromCacheOrDB这个方法在GoodsQueryImpl中已经有了，考虑复用
    /**
     * 从缓存或数据库获取商品卡片列表，缓存未命中的再查MySQL
     * @param ids 商品ID列表
     * @return 商品卡片列表，保持原始ids顺序
     */
    private List<GoodsCardVO> getGoodsCardsFromCacheOrDB(List<Long> ids) {
        // 1. 构建缓存 keys
        List<String> keys = ids.stream()
                .map(id -> RedisConstant.GOODS_CARD_KEY + id)
                .collect(Collectors.toList());
        
        // 2. 批量从缓存获取
        List<Object> cached = redisTemplate.opsForValue().multiGet(keys);
        
        // 3. 找出缓存未命中的 ids
        List<Long> missIds = new ArrayList<>();
        Map<Long, GoodsCardVO> resultMap = new HashMap<>();
        
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            Object value = (cached != null && i < cached.size()) ? cached.get(i) : null;
            if (value != null) {
                resultMap.put(id, (GoodsCardVO) value);
            } else {
                missIds.add(id);
            }
        }
        
        // 4. 缓存未命中的，从 MySQL 查询并存入缓存
        if (!missIds.isEmpty()) {
            List<GoodsCardVO> fromDB = goodsQueryMapper.getGoodsCardsByIds(missIds);
            for (GoodsCardVO vo : fromDB) {
                resultMap.put(vo.getId(), vo);
                // 存入缓存（点赞数可能过时，后续会从点赞数缓存重新获取最新值）
                String key = RedisConstant.GOODS_CARD_KEY + vo.getId();
                long ttl = RedisConstant.GOODS_CARD_TTL + new Random().nextInt(5);
                redisTemplate.opsForValue().set(key, vo, ttl, TimeUnit.MINUTES);
            }
        }
        
        // 5. 批量获取点赞数并赋值给商品卡片
        List<GoodsCardVO> result = ids.stream()
                .map(resultMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        // 批量设置点赞数
        batchSetCollectNum(result);
        
        return result;
    }
    
    /**
     * 为商品详情设置最新的收藏数
     * @param detail 商品详情对象
     */
    private void setLatestCollectNum(GoodsDetailVO detail) {
        if (detail == null) {
            return;
        }
        
        Integer collectNum = collectNumCacheUtil.getCollectNum(detail.getId());
        if (collectNum != null) {
            detail.setCollectNum(collectNum);
        }
        // 如果获取失败，保持原有值
    }
    
    /**
     * 处理商品成色显示逻辑
     * 如果成色是明显使用痕迹，则不显示成色信息
     * @param detail 商品详情对象
     */
    private void processConditionLevel(GoodsDetailVO detail) {
        if (detail == null) {
            return;
        }
        
        // 如果成色是明显使用痕迹，则设置为null，前端不显示
        if (detail.getConditionLevel() != null && 
            detail.getConditionLevel().equals(GoodsConditionConstant.OBVIOUS_WEAR)) {
            detail.setConditionLevel(null);
            log.debug("商品成色为明显使用痕迹，已隐藏成色信息: goodsId={}", detail.getId());
        }
    }
    
    /**
     * 批量设置商品卡片的点赞数（复用GoodsQueryServiceImpl的逻辑）
     * @param cards 商品卡片列表
     */
    private void batchSetCollectNum(List<GoodsCardVO> cards) {
        if (cards == null || cards.isEmpty()) {
            return;
        }
        
        // 提取商品ID列表
        List<Long> goodsIds = cards.stream()
                .map(GoodsCardVO::getId)
                .collect(Collectors.toList());
        
        // 批量获取收藏数
        Map<Long, Integer> collectNumMap = collectNumCacheUtil.batchGetCollectNum(goodsIds);
        
        // 将收藏数赋值给商品卡片
        for (GoodsCardVO card : cards) {
            Integer collectNum = collectNumMap.get(card.getId());
            card.setCollectNum(collectNum != null ? collectNum : 0);
        }
    }
    


    //TODO：这个方法在GoodsQueryImpl中已经有了，考虑复用
    /**
     * 构建分页结果
     */
    private PageResult<GoodsCardVO> buildPageResult(List<GoodsCardVO> list, int size) {
        if (list == null || list.isEmpty()) {
            return PageResult.empty();
        }
        
        boolean hasMore = list.size() > size;
        if (hasMore) {
            list = list.subList(0, size); // 移除多查的那一条
        }
        
        // 下一页游标 = 本页最后一条的时间戳
        Long nextCursor = list.isEmpty() ? null : list.get(list.size() - 1).getUpdateTimestamp();
        
        return PageResult.of(list, nextCursor, hasMore);
    }

    /**
     * 构建分类ZSet Key
     */
    private String buildCategoryKey(long categoryId) {
        return RedisConstant.GOODS_CAT_IDS_KEY + categoryId + RedisConstant.GOODS_CAT_IDS_SUFFIX;
    }

    /**
     * 构建用户商品ZSet Key
     */
    private String buildOwnerKey(long ownerId) {
        return RedisConstant.GOODS_OWNER_IDS_KEY + ownerId + RedisConstant.GOODS_OWNER_IDS_SUFFIX;
    }

    /**
     * 构建用户下架商品ZSet Key
     */
    private String buildOfflineKey(long ownerId) {
        return RedisConstant.GOODS_OFFLINE_IDS_KEY + ownerId + RedisConstant.GOODS_OFFLINE_IDS_SUFFIX;
    }

    @Override
    @Transactional
    public void offlineGoods(Long goodsId, Long ownerId) {
        // 1. 查询商品当前状态
        Integer currentStatus = goodsMapper.getGoodsStatus(goodsId, ownerId);
        if (currentStatus == null) {
            throw new GoodsNotFoundException(MessageConstant.GOODS_NOT_FOUND_OR_NO_PERMISSION);
        }
        
        // 2. 判断是否在租借中
        if (currentStatus == GoodsStatusConstant.RENTING) {
            throw new GoodsInRentException(MessageConstant.GOODS_IN_RENT_CANNOT_OFFLINE);
        }
        
        // 3. 将状态更新为已下架
        goodsMapper.updateGoodsStatus(goodsId, ownerId, GoodsStatusConstant.OFF_SHELF);
        
        // 4. 从上架商品 ZSet 中移除
        removeGoodsFromZSet(goodsId);
        
        // 5. 添加到下架商品 ZSet 中
        addGoodsToOfflineZSet(goodsId, ownerId);
        
        // 6. 清除商品缓存
        clearGoodsCache(goodsId);
    }

    @Override
    public GoodsDetailVO getGoodsDetailById(Long id) {
        String cacheKey = RedisConstant.GOODS_DETAIL_KEY + id;
        
        // 1. 先查缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        GoodsDetailVO detail;
        
        if (cached != null) {
            detail = (GoodsDetailVO) cached;
        } else {
            // 2. 缓存未命中，查数据库
            detail = goodsMapper.getGoodsDetailById(id);
            if (detail == null) {
                return null;
            }
            
            // 3. 存入缓存，设置过期时间（基础TTL + 随机浮动，防止缓存雪崩）
            long ttl = RedisConstant.GOODS_DETAIL_TTL + new Random().nextInt((int) RedisConstant.GOODS_DETAIL_TTL_RANDOM);
            redisTemplate.opsForValue().set(cacheKey, detail, ttl, TimeUnit.MINUTES);
        }
        
        // 4. 从收藏数缓存中获取最新的收藏数
        setLatestCollectNum(detail);
        
        // 5. 处理成色显示逻辑：如果是明显使用痕迹，则不显示成色
        processConditionLevel(detail);
        
        return detail;
    }

    @Override
    @Transactional
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
        
        // 4. 清除商品缓存
        clearGoodsCache(goodsId);
        
        // 5. 更新 ZSet 中的 score（时间戳）
        updateGoodsScoreInZSet(goodsId);
    }

    @Override
    @Transactional
    public void onlineGoods(Long goodsId, Long ownerId) {
        // 重新上架：将状态改为上架
        int rows = goodsMapper.updateGoodsStatus(goodsId, ownerId, GoodsStatusConstant.ON_SALE);
        if (rows == 0) {
            throw new GoodsNotFoundException(MessageConstant.GOODS_NOT_FOUND_OR_NO_PERMISSION);
        }
        
        // 从下架商品 ZSet 中移除
        removeGoodsFromOfflineZSet(goodsId, ownerId);
        
        // 添加到上架商品 ZSet 中
        updateGoodsScoreInZSet(goodsId);
        
        // 清除商品缓存
        clearGoodsCache(goodsId);
    }

    @Override
    @Transactional
    public void deleteGoods(Long goodsId, Long ownerId) {
        // 软删除：将状态改为用户删除
        int rows = goodsMapper.updateGoodsStatus(goodsId, ownerId, GoodsStatusConstant.USER_DELETED);
        if (rows == 0) {
            throw new GoodsNotFoundException(MessageConstant.GOODS_NOT_FOUND_OR_NO_PERMISSION);
        }
        
        // 从ZSet中移除
        removeGoodsFromZSet(goodsId);
        
        // 清除商品缓存
        clearGoodsCache(goodsId);
    }

    @Override
    @Transactional
    public void markAsSold(Long goodsId, Long ownerId) {
        // 标记为已售出
        int rows = goodsMapper.updateGoodsStatus(goodsId, ownerId, GoodsStatusConstant.SOLD);
        if (rows == 0) {
            throw new GoodsNotFoundException(MessageConstant.GOODS_NOT_FOUND_OR_NO_PERMISSION);
        }
        
        // 从ZSet中移除（已售出不再展示）
        removeGoodsFromZSet(goodsId);
        
        // 清除商品缓存
        clearGoodsCache(goodsId);
    }

    @Override
    @Transactional
    public void markAsRenting(Long goodsId, Long ownerId) {
        // 标记为租借中
        int rows = goodsMapper.updateGoodsStatus(goodsId, ownerId, GoodsStatusConstant.RENTING);
        if (rows == 0) {
            throw new GoodsNotFoundException(MessageConstant.GOODS_NOT_FOUND_OR_NO_PERMISSION);
        }
        
        // 从ZSet中移除（租借中不再展示）
        removeGoodsFromZSet(goodsId);
        
        // 清除商品缓存
        clearGoodsCache(goodsId);
    }
    
    /**
     * 清除商品缓存（只清除详情缓存，卡片缓存保留）
     */
    private void clearGoodsCache(Long goodsId) {
        redisTemplate.delete(RedisConstant.GOODS_DETAIL_KEY + goodsId);
        // 不再清除商品卡片缓存，因为点赞数已经单独缓存
        // redisTemplate.delete(RedisConstant.GOODS_CARD_KEY + goodsId);
    }
    
    /**
     * 从zSet中移除商品（分类缓存 + 用户缓存 + 全局缓存）
     */
    private void removeGoodsFromZSet(Long goodsId) {
        Map<String, Object> info = goodsMapper.getGoodsCategoryAndTimeById(goodsId);
        if (info != null) {
            // 从分类ZSet移除（categoryId 可能为 null）
            Object categoryIdObj = info.get("categoryId");
            if (categoryIdObj != null) {
                String catKey = buildCategoryKey(((Number) categoryIdObj).longValue());
                redisTemplate.opsForZSet().remove(catKey, goodsId);
            }
            
            // 从用户ZSet移除
            String ownerKey = buildOwnerKey(((Number) info.get("ownerId")).longValue());
            redisTemplate.opsForZSet().remove(ownerKey, goodsId);
            
            // 从全局所有商品ZSet移除
            String allGoodsKey = RedisConstant.GOODS_ALL_IDS_KEY + RedisConstant.GOODS_ALL_IDS_SUFFIX;
            redisTemplate.opsForZSet().remove(allGoodsKey, goodsId);
        }
    }
    
    /**
     * 添加/更新商品在ZSet中的score（分类缓存 + 用户缓存 + 全局缓存）
     * ZSet的add是幂等的：不存在则添加，存在则更新score
     */
    private void updateGoodsScoreInZSet(Long goodsId) {
        Map<String, Object> info = goodsMapper.getGoodsCategoryAndTimeById(goodsId);
        if (info != null) {
            long timestamp = ((Number) info.get("updateTime")).longValue();
            
            // 更新分类ZSet（categoryId 可能为 null）
            Object categoryIdObj = info.get("categoryId");
            if (categoryIdObj != null) {
                String catKey = buildCategoryKey(((Number) categoryIdObj).longValue());
                if (Boolean.TRUE.equals(redisTemplate.hasKey(catKey))) {
                    redisTemplate.opsForZSet().add(catKey, goodsId, timestamp);
                }
            }
            
            // 更新用户ZSet
            String ownerKey = buildOwnerKey(((Number) info.get("ownerId")).longValue());
            if (Boolean.TRUE.equals(redisTemplate.hasKey(ownerKey))) {
                redisTemplate.opsForZSet().add(ownerKey, goodsId, timestamp);
            }
            
            // 更新全局所有商品ZSet
            String allGoodsKey = RedisConstant.GOODS_ALL_IDS_KEY + RedisConstant.GOODS_ALL_IDS_SUFFIX;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(allGoodsKey))) {
                redisTemplate.opsForZSet().add(allGoodsKey, goodsId, timestamp);
            }
        }
    }

    
    // ==================== 收藏相关方法 ====================
    
    @Override
    @Transactional
    public boolean toggleFavorite(Long userId, Long goodsId) {
        log.info("切换收藏状态: userId={}, goodsId={}", userId, goodsId);

        // 检查商品是否存在
        Goods goods = goodsMapper.getGoodsById(goodsId);
        if (goods == null) {
            throw new GoodsNotFoundException("商品不存在");
        }

        // 检查是否已收藏
        int count = goodsMapper.checkFavorite(userId, goodsId);
        
        if (count > 0) {
            // 已收藏，取消收藏
            goodsMapper.deleteFavorite(userId, goodsId);
            // 异步更新商品收藏数（-1）
            updateGoodsCollectNumAsync(goodsId, -1);
            // 从 Redis ZSet 中移除
            removeFromFavoriteZSet(userId, goodsId);
            log.info("取消收藏成功: userId={}, goodsId={}", userId, goodsId);
            return false;
        } else {
            // 未收藏，添加收藏
            GoodsFavorite favorite = GoodsFavorite.builder()
                    .userId(userId)
                    .goodsId(goodsId)
                    .createTime(LocalDateTime.now())
                    .build();
            goodsMapper.insertFavorite(favorite);
            // 异步更新商品收藏数（+1）
            updateGoodsCollectNumAsync(goodsId, 1);
            // 添加到 Redis ZSet
            addToFavoriteZSet(userId, goodsId);
            log.info("收藏成功: userId={}, goodsId={}", userId, goodsId);
            return true;
        }
    }

    @Override
    public boolean isFavorite(Long userId, Long goodsId) {
        int count = goodsMapper.checkFavorite(userId, goodsId);
        return count > 0;
    }
    
    /**
     * 异步更新商品收藏数（只更新缓存，数据库异步同步）
     * @param goodsId 商品ID
     * @param delta 变化量（+1表示添加收藏，-1表示取消收藏）
     */
    private void updateGoodsCollectNumAsync(Long goodsId, Integer delta) {
        try {
            // 只更新缓存中的点赞数
            updateCollectNumCache(goodsId, delta);
            
            // 标记需要异步同步到数据库
            collectNumSyncService.markForSync(goodsId);
            
            log.info("异步更新商品收藏数缓存: goodsId={}, delta={}", goodsId, delta);
        } catch (Exception e) {
            log.error("异步更新商品收藏数失败: goodsId={}, error={}", goodsId, e.getMessage());
            // 异步更新失败时，降级为同步更新
            updateGoodsCollectNumSync(goodsId, delta);
        }
    }
    
    /**
     * 同步更新商品收藏数（兼容旧逻辑，用于降级）
     * @param goodsId 商品ID
     * @param delta 变化量
     */
    private void updateGoodsCollectNumSync(Long goodsId, Integer delta) {
        try {
            // 更新商品表的收藏数字段（增量更新）
            goodsMapper.updateCollectNum(goodsId, delta);
            log.info("同步更新商品收藏数: goodsId={}, delta={}", goodsId, delta);
            
            // 更新点赞数缓存
            updateCollectNumCache(goodsId, delta);
        } catch (Exception e) {
            log.error("同步更新商品收藏数失败: goodsId={}, error={}", goodsId, e.getMessage());
        }
    }
    
    /**
     * 更新点赞数缓存
     * @param goodsId 商品ID
     * @param delta 变化量
     */
    private void updateCollectNumCache(Long goodsId, Integer delta) {
        collectNumCacheUtil.updateCollectNumCache(goodsId, delta);
    }
    
    /**
     * 构建用户收藏列表ZSet Key
     */
    private String buildFavoriteZSetKey(Long userId) {
        return RedisConstant.FAVORITE_USER_IDS_KEY + userId + RedisConstant.FAVORITE_USER_IDS_SUFFIX;
    }
    
    /**
     * 添加到收藏 ZSet（只有当ZSet已存在时才添加）
     */
    private void addToFavoriteZSet(Long userId, Long goodsId) {
        String key = buildFavoriteZSetKey(userId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForZSet().add(key, goodsId, System.currentTimeMillis());
        }
    }
    
    /**
     * 从收藏 ZSet 中移除
     */
    private void removeFromFavoriteZSet(Long userId, Long goodsId) {
        String key = buildFavoriteZSetKey(userId);
        redisTemplate.opsForZSet().remove(key, goodsId);
    }




    // ==================== 管理员功能 ====================

    @Override
    @Transactional
    public void violationOfflineByAdmin(Long goodsId, String reason) {
        // 查询商品状态（不需要owner_id）
        Integer currentStatus = goodsMapper.getGoodsStatusByAdmin(goodsId);
        if (currentStatus == null) {
            throw new GoodsNotFoundException(MessageConstant.GOODS_NOT_FOUND);
        }

        // 如果已经是系统屏蔽状态，直接返回
        if (currentStatus == GoodsStatusConstant.SYSTEM_BLOCKED) {
            throw new RuntimeException("商品已被违规下架");
        }

        // 更新为系统屏蔽状态
        goodsMapper.updateGoodsStatusByAdmin(goodsId, GoodsStatusConstant.SYSTEM_BLOCKED);

        // 从 ZSet 中移除
        removeGoodsFromZSet(goodsId);

        // 清除商品缓存
        clearGoodsCache(goodsId);

        // TODO: 记录违规原因（可以后续扩展，存入日志表）
        if (reason != null && !reason.trim().isEmpty()) {
            // log.warn("管理员违规下架商品: goodsId={}, reason={}", goodsId, reason);
        }
    }

    @Override
    @Transactional
    public void restoreGoodsByAdmin(Long goodsId) {
        // 查询商品状态
        Integer currentStatus = goodsMapper.getGoodsStatusByAdmin(goodsId);
        if (currentStatus == null) {
            throw new GoodsNotFoundException(MessageConstant.GOODS_NOT_FOUND);
        }

        // 只能恢复系统屏蔽的商品
        if (currentStatus != GoodsStatusConstant.SYSTEM_BLOCKED) {
            throw new RuntimeException("只能恢复违规下架的商品");
        }

        // 恢复为上架状态
        goodsMapper.updateGoodsStatusByAdmin(goodsId, GoodsStatusConstant.ON_SALE);

        // 添加到 ZSet
        updateGoodsScoreInZSet(goodsId);
    }

    @Override
    public PageResult<GoodsCardVO> queryGoodsByConditions(GoodsQueryDTO query) {
        // 设置默认值
        if (query.getCursor() == null || query.getCursor() <= 0) {
            query.setCursor(System.currentTimeMillis());
        }
        if (query.getSize() == null || query.getSize() <= 0) {
            query.setSize(10);
        }

        // 查询 size+1 条，用于判断是否还有更多数据
        int originalSize = query.getSize();
        query.setSize(originalSize + 1);

        // 执行查询
        List<GoodsCardVO> list = goodsMapper.queryGoodsByConditions(query);

        // 恢复原始 size
        query.setSize(originalSize);

        return buildPageResult(list, originalSize);
    }

    /**
     * 将商品添加到下架商品ZSet缓存中
     */
    private void addGoodsToOfflineZSet(Long goodsId, Long ownerId) {
        String offlineKey = buildOfflineKey(ownerId);
        // 只有当ZSet已存在时才添加
        if (Boolean.TRUE.equals(redisTemplate.hasKey(offlineKey))) {
            redisTemplate.opsForZSet().add(offlineKey, goodsId, System.currentTimeMillis());
        }
    }

    /**
     * 从下架商品ZSet缓存中移除商品
     */
    private void removeGoodsFromOfflineZSet(Long goodsId, Long ownerId) {
        String offlineKey = buildOfflineKey(ownerId);
        redisTemplate.opsForZSet().remove(offlineKey, goodsId);
    }
}
