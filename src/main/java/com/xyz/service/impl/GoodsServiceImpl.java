package com.xyz.service.impl;

import com.xyz.constant.GoodsStatusConstant;
import com.xyz.constant.MessageConstant;
import com.xyz.constant.RedisConstant;
import com.xyz.dto.GoodsDTO;
import com.xyz.entity.Goods;
import com.xyz.exception.GoodsInRentException;
import com.xyz.exception.GoodsNotFoundException;
import com.xyz.mapper.GoodsMapper;
import com.xyz.service.GoodsService;
import com.xyz.util.BaseContext;
import com.xyz.vo.GoodsCardVO;
import com.xyz.vo.GoodsDetailVO;
import com.xyz.vo.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public void releaseGoods(GoodsDTO goodsDTO) {
        // 从上下文获取当前用户ID
        Long currentUserId = BaseContext.getCurrentId();
        
        Goods goods = new Goods();
        BeanUtils.copyProperties(goodsDTO, goods);
        goods.setCollectNum(0);
        goods.setOwnerId(currentUserId);
        // 商品状态由后端统一设置为上架
        goods.setStatus(GoodsStatusConstant.ON_SALE);
        
        // 设置封面图（取第一张）
        List<String> imageUrls = goodsDTO.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            goods.setCoverUrl(imageUrls.get(0));
        }
        
        goodsMapper.insertGoods(goods);
        
        // 添加到分类ZSet缓存（只有当ZSet已存在时才添加）
        String catKey = buildCategoryKey(goodsDTO.getCategoryId());
        if (Boolean.TRUE.equals(redisTemplate.hasKey(catKey))) {
            redisTemplate.opsForZSet().add(catKey, goods.getId(), System.currentTimeMillis());
        }
        
        // 添加到用户商品ZSet缓存（只有当ZSet已存在时才添加）
        String ownerKey = buildOwnerKey(goods.getOwnerId());
        if (Boolean.TRUE.equals(redisTemplate.hasKey(ownerKey))) {
            redisTemplate.opsForZSet().add(ownerKey, goods.getId(), System.currentTimeMillis());
        }
    }

    @Override
    public PageResult<GoodsCardVO> getGoodsPageByCategoryId(long categoryId, Long cursor, int size) {
        // 首次请求或cursor为null，使用当前时间
        long cursorTime = (cursor == null || cursor <= 0) ? System.currentTimeMillis() : cursor;
        
        String zsetKey = buildCategoryKey(categoryId);
        
        // 尝试从 Redis ZSet 获取ID列表
        List<Long> ids = getIdsFromZSet(zsetKey, cursorTime, size + 1);
        
        List<GoodsCardVO> list;
        if (ids != null && !ids.isEmpty()) {
            // ZSet命中，先从缓存批量获取，未命中的再查MySQL
            list = getGoodsCardsFromCacheOrDB(ids);
        } else {
            // ZSet未命中，直接查MySQL并重建缓存
            list = goodsMapper.getGoodsPageByCategoryId(categoryId, cursorTime, size + 1);
            rebuildCategoryZSet(categoryId);
            // 将查询结果存入卡片缓存
            cacheGoodsCards(list);
        }
        
        return buildPageResult(list, size);
    }

    @Override
    public PageResult<GoodsCardVO> getGoodsPageByOwnerId(long ownerId, Long cursor, int size) {
        long cursorTime = (cursor == null || cursor <= 0) ? System.currentTimeMillis() : cursor;
        
        String zsetKey = buildOwnerKey(ownerId);
        
        // 尝试从 Redis ZSet 获取ID列表
        List<Long> ids = getIdsFromZSet(zsetKey, cursorTime, size + 1);
        
        List<GoodsCardVO> list;
        if (ids != null && !ids.isEmpty()) {
            // ZSet命中，先从缓存批量获取，未命中的再查MySQL
            list = getGoodsCardsFromCacheOrDB(ids);
        } else {
            // ZSet未命中，直接查MySQL并重建缓存
            list = goodsMapper.getGoodsPageByOwnerId(ownerId, cursorTime, size + 1);
            rebuildOwnerZSet(ownerId);
            // 将查询结果存入卡片缓存
            cacheGoodsCards(list);
        }
        
        return buildPageResult(list, size);
    }


    @Override
    public PageResult<GoodsCardVO> searchGoods(String keyword, Long cursor, int size) {
        long cursorTime = (cursor == null || cursor <= 0) ? System.currentTimeMillis() : cursor;
        
        // 1. 全文索引只查ID（轻量级查询）
        List<Long> ids = goodsMapper.searchGoodsIds(keyword, cursorTime, size + 1);
        
        if (ids == null || ids.isEmpty()) {
            return PageResult.empty();
        }
        
        // 2. 根据ID从Redis缓存批量获取商品卡片，未命中的再查MySQL（已包含时间戳）
        List<GoodsCardVO> list = getGoodsCardsFromCacheOrDB(ids);
        
        return buildPageResult(list, size);
    }

    /**
     * 从ZSet中获取商品ID列表
     * @param key ZSet的key
     * @param cursor 游标时间戳
     * @param count 获取数量
     * @return ID列表，如果ZSet不存在返回null
     */
    private List<Long> getIdsFromZSet(String key, long cursor, int count) {
         //❌ 危险写法
         //(!redisTemplate.hasKey(key)) { ... }
         //如果 hasKey 返回 null，!null 会抛 NullPointerException
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            return null;
        }
        // reverseRangeByScore: 按score从大到小排序，范围是[min, max]
        // 这里查询 score < cursor 的数据，按降序取count条
        Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, cursor - 1, 0, count);
        
        if (tuples == null || tuples.isEmpty()) {
            return List.of();
        }

        // ❌ 可能报错
        // (Long) t.getValue()
        // Redis返回的可能是Integer，直接转Long会ClassCastException
        //
        // ✅ 安全写法
        // ((Number) t.getValue()).longValue()
        // Number是Integer和Long的共同父类，再调longValue()转换
        return tuples.stream()
                .map(t -> ((Number) t.getValue()).longValue())
                .collect(Collectors.toList());
    }

    /**
     * 重建分类ZSet缓存
     */
    private void rebuildCategoryZSet(long categoryId) {
        String key = buildCategoryKey(categoryId);
        List<Map<String, Object>> data = goodsMapper.getGoodsIdsWithTimeByCategoryId(categoryId);
        if (data == null || data.isEmpty()) {
            return;
        }
        //TypedTuple.of(value, score)是 Spring Data Redis 提供的静态工厂方法，用于创建 ZSet 元素。
        //(Long) m.get("id")   // 如果 MySQL 返回的是 Integer，会抛 ClassCastException
        //((Number) m.get("id")).longValue()  // Number 是 Integer 和 Long 的公共父类
        Set<ZSetOperations.TypedTuple<Object>> tuples = data.stream()
                .map(m -> ZSetOperations.TypedTuple.of(
                        (Object) ((Number) m.get("id")).longValue(),
                        ((Number) m.get("updateTime")).doubleValue()//score必须是 Double类型
                ))
                .collect(Collectors.toSet());
        
        redisTemplate.opsForZSet().add(key, tuples);
        redisTemplate.expire(key, RedisConstant.GOODS_IDS_TTL, TimeUnit.MINUTES);
    }

    /**
     * 重建用户商品ZSet缓存
     */
    private void rebuildOwnerZSet(long ownerId) {
        String key = buildOwnerKey(ownerId);
        List<Map<String, Object>> data = goodsMapper.getGoodsIdsWithTimeByOwnerId(ownerId);
        if (data == null || data.isEmpty()) {
            return;
        }
        Set<ZSetOperations.TypedTuple<Object>> tuples = data.stream()
                .map(m -> ZSetOperations.TypedTuple.of(
                        (Object) ((Number) m.get("id")).longValue(),
                        ((Number) m.get("updateTime")).doubleValue()
                ))
                .collect(Collectors.toSet());
        
        redisTemplate.opsForZSet().add(key, tuples);
        redisTemplate.expire(key, RedisConstant.GOODS_IDS_TTL, TimeUnit.MINUTES);
    }

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
            List<GoodsCardVO> fromDB = goodsMapper.getGoodsCardsByIds(missIds);
            for (GoodsCardVO vo : fromDB) {
                resultMap.put(vo.getId(), vo);
                // 存入缓存
                String key = RedisConstant.GOODS_CARD_KEY + vo.getId();
                long ttl = RedisConstant.GOODS_CARD_TTL + new Random().nextInt(5);
                redisTemplate.opsForValue().set(key, vo, ttl, TimeUnit.MINUTES);
            }
        }
        
        // 5. 按原始ids顺序返回
        return ids.stream()
                .map(resultMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * 批量将商品卡片存入缓存
     */
    private void cacheGoodsCards(List<GoodsCardVO> cards) {
        if (cards == null || cards.isEmpty()) {
            return;
        }
        for (GoodsCardVO vo : cards) {
            String key = RedisConstant.GOODS_CARD_KEY + vo.getId();
            long ttl = RedisConstant.GOODS_CARD_TTL + new Random().nextInt(5);
            redisTemplate.opsForValue().set(key, vo, ttl, TimeUnit.MINUTES);
        }
    }

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
        
        // 4. 从 ZSet 中移除
        removeGoodsFromZSet(goodsId);
        
        // 5. 清除商品缓存
        clearGoodsCache(goodsId);
    }

    @Override
    public GoodsDetailVO getGoodsDetailById(Long id) {
        String cacheKey = RedisConstant.GOODS_DETAIL_KEY + id;
        
        // 1. 先查缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return (GoodsDetailVO) cached;
        }
        
        // 2. 缓存未命中，查数据库
        GoodsDetailVO detail = goodsMapper.getGoodsDetailById(id);
        if (detail == null) {
            return null;
        }
        
        // 3. 存入缓存，设置过期时间（基础TTL + 随机浮动，防止缓存雪崩）
        long ttl = RedisConstant.GOODS_DETAIL_TTL + new Random().nextInt((int) RedisConstant.GOODS_DETAIL_TTL_RANDOM);
        redisTemplate.opsForValue().set(cacheKey, detail, ttl, TimeUnit.MINUTES);
        
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
        
        // 添加到 ZSet
        updateGoodsScoreInZSet(goodsId);
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
     * 清除商品缓存（详情缓存 + 卡片缓存）
     */
    private void clearGoodsCache(Long goodsId) {
        redisTemplate.delete(RedisConstant.GOODS_DETAIL_KEY + goodsId);
        redisTemplate.delete(RedisConstant.GOODS_CARD_KEY + goodsId);
    }
    
    /**
     * 从zSet中移除商品（分类缓存 + 用户缓存）
     */
    private void removeGoodsFromZSet(Long goodsId) {
        Map<String, Object> info = goodsMapper.getGoodsCategoryAndTimeById(goodsId);
        if (info != null) {
            // 从分类ZSet移除
            String catKey = buildCategoryKey(((Number) info.get("categoryId")).longValue());
            redisTemplate.opsForZSet().remove(catKey, goodsId);
            
            // 从用户ZSet移除
            String ownerKey = buildOwnerKey(((Number) info.get("ownerId")).longValue());
            redisTemplate.opsForZSet().remove(ownerKey, goodsId);
        }
    }
    
    /**
     * 添加/更新商品在ZSet中的score（分类缓存 + 用户缓存）
     * ZSet的add是幂等的：不存在则添加，存在则更新score
     */
    private void updateGoodsScoreInZSet(Long goodsId) {
        Map<String, Object> info = goodsMapper.getGoodsCategoryAndTimeById(goodsId);
        if (info != null) {
            long timestamp = ((Number) info.get("updateTime")).longValue();
            
            // 更新分类ZSet
            String catKey = buildCategoryKey(((Number) info.get("categoryId")).longValue());
            if (Boolean.TRUE.equals(redisTemplate.hasKey(catKey))) {
                redisTemplate.opsForZSet().add(catKey, goodsId, timestamp);
            }
            
            // 更新用户ZSet
            String ownerKey = buildOwnerKey(((Number) info.get("ownerId")).longValue());
            if (Boolean.TRUE.equals(redisTemplate.hasKey(ownerKey))) {
                redisTemplate.opsForZSet().add(ownerKey, goodsId, timestamp);
            }
        }
    }
}
