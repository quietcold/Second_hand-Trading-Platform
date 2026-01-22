package com.xyz.service.impl;

import com.xyz.constant.RedisConstant;
import com.xyz.mapper.GoodsQueryMapper;
import com.xyz.service.GoodsQueryService;
import com.xyz.util.CollectNumCacheUtil;
import com.xyz.vo.GoodsCardVO;
import com.xyz.vo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 商品查询服务实现类（整合收藏、发布查询）
 */
@Slf4j
@Service
public class GoodsQueryServiceImpl implements GoodsQueryService {

    @Autowired
    private GoodsQueryMapper goodsQueryMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private CollectNumCacheUtil collectNumCacheUtil;

    @Override
    public PageResult<GoodsCardVO> getFavoriteGoods(Long userId, Long cursor, Integer size) {
        log.info("查询收藏列表: userId={}, cursor={}, size={}", userId, cursor, size);

        // 设置默认值
        long cursorTime = (cursor == null || cursor <= 0) ? System.currentTimeMillis() : cursor;
        if (size == null || size <= 0) {
            size = 10;
        }

        String zsetKey = buildZSetKey(RedisConstant.FAVORITE_USER_IDS_KEY, userId, RedisConstant.FAVORITE_USER_IDS_SUFFIX);

        // 尝试从 Redis ZSet 获取ID列表
        List<Long> ids = getIdsFromZSet(zsetKey, cursorTime, size + 1);

        List<GoodsCardVO> list;
        if (ids != null && !ids.isEmpty()) {
            // ZSet命中，先从缓存批量获取，未命中的再查MySQL
            list = getGoodsCardsFromCacheOrDB(ids);
        } else {
            // ZSet未命中，直接查MySQL并重建缓存
            list = goodsQueryMapper.getFavoriteGoodsByUserId(userId, cursorTime, size + 1);
            rebuildFavoriteZSet(userId);
            // 将查询结果存入卡片缓存
            cacheGoodsCards(list);
        }

        return buildPageResult(list, size);
    }


    @Override
    public PageResult<GoodsCardVO> getGoodsPageByCategoryId(Long categoryId, Long cursor, Integer size) {
        log.info("查询分类商品列表: categoryId={}, cursor={}, size={}", categoryId, cursor, size);
        
        // 设置默认值
        long cursorTime = (cursor == null || cursor <= 0) ? System.currentTimeMillis() : cursor;
        if (size == null || size <= 0) {
            size = 10;
        }
        
        String zsetKey = buildZSetKey(RedisConstant.GOODS_CAT_IDS_KEY, categoryId, RedisConstant.GOODS_CAT_IDS_SUFFIX);
        
        // 尝试从 Redis ZSet 获取ID列表
        List<Long> ids = getIdsFromZSet(zsetKey, cursorTime, size + 1);
        
        List<GoodsCardVO> list;
        if (ids != null && !ids.isEmpty()) {
            // ZSet命中，先从缓存批量获取，未命中的再查MySQL
            list = getGoodsCardsFromCacheOrDB(ids);
        } else {
            // ZSet未命中，直接查MySQL并重建缓存
            list = goodsQueryMapper.getGoodsPageByCategoryId(categoryId, cursorTime, size + 1);
            rebuildCategoryZSet(categoryId);
            // 将查询结果存入卡片缓存
            cacheGoodsCards(list);
        }
        
        return buildPageResult(list, size);
    }



    @Override
    public PageResult<GoodsCardVO> getUserPublishedGoods(Long targetUserId, Long cursor, Integer size) {
        log.info("查询用户发布的商品: targetUserId={}, cursor={}, size={}", targetUserId, cursor, size);

        // 设置默认值
        long cursorTime = (cursor == null || cursor <= 0) ? System.currentTimeMillis() : cursor;
        if (size == null || size <= 0) {
            size = 10;
        }

        String zsetKey = buildZSetKey(RedisConstant.GOODS_OWNER_IDS_KEY, targetUserId, RedisConstant.GOODS_OWNER_IDS_SUFFIX);

        // 尝试从 Redis ZSet 获取ID列表
        List<Long> ids = getIdsFromZSet(zsetKey, cursorTime, size + 1);

        List<GoodsCardVO> list;
        if (ids != null && !ids.isEmpty()) {
            // ZSet命中，先从缓存批量获取
            list = getGoodsCardsFromCacheOrDB(ids);
        } else {
            // ZSet未命中，直接查MySQL并重建缓存
            list = goodsQueryMapper.getGoodsPageByOwnerId(targetUserId, cursorTime, size + 1);
            rebuildOwnerZSet(targetUserId);
            cacheGoodsCards(list);
        }

        return buildPageResult(list, size);
    }

    @Override
    public PageResult<GoodsCardVO> getMyPublishedGoods(Long userId, Long cursor, Integer size) {
        log.info("查询我发布的商品: userId={}, cursor={}, size={}", userId, cursor, size);
        // 直接复用 getUserPublishedGoods 方法
        return getUserPublishedGoods(userId, cursor, size);
    }

    @Override
    public PageResult<GoodsCardVO> getAllGoodsPage(Long cursor, Integer size) {
        log.info("查询所有商品列表: cursor={}, size={}", cursor, size);
        
        // 设置默认值
        long cursorTime = (cursor == null || cursor <= 0) ? System.currentTimeMillis() : cursor;
        if (size == null || size <= 0) {
            size = 10;
        }
        
        String zsetKey = RedisConstant.GOODS_ALL_IDS_KEY + RedisConstant.GOODS_ALL_IDS_SUFFIX;
        
        // 尝试从 Redis ZSet 获取ID列表
        List<Long> ids = getIdsFromZSet(zsetKey, cursorTime, size + 1);
        
        List<GoodsCardVO> list;
        if (ids != null && !ids.isEmpty()) {
            // ZSet命中，先从缓存批量获取，未命中的再查MySQL
            list = getGoodsCardsFromCacheOrDB(ids);
        } else {
            // ZSet未命中，直接查MySQL并重建缓存
            list = goodsQueryMapper.getAllGoodsPage(cursorTime, size + 1);
            rebuildAllGoodsZSet();
            // 将查询结果存入卡片缓存
            cacheGoodsCards(list);
        }
        
        return buildPageResult(list, size);
    }

    @Override
    public PageResult<GoodsCardVO> getMyOfflineGoods(Long userId, Long cursor, Integer size) {
        log.info("查询我下架的商品: userId={}, cursor={}, size={}", userId, cursor, size);

        // 设置默认值
        long cursorTime = (cursor == null || cursor <= 0) ? System.currentTimeMillis() : cursor;
        if (size == null || size <= 0) {
            size = 10;
        }

        String zsetKey = buildZSetKey(RedisConstant.GOODS_OFFLINE_IDS_KEY, userId, RedisConstant.GOODS_OFFLINE_IDS_SUFFIX);

        // 尝试从 Redis ZSet 获取ID列表
        List<Long> ids = getIdsFromZSet(zsetKey, cursorTime, size + 1);

        List<GoodsCardVO> list;
        if (ids != null && !ids.isEmpty()) {
            // ZSet命中，先从缓存批量获取
            list = getGoodsCardsFromCacheOrDB(ids);
        } else {
            // ZSet未命中，直接查MySQL并重建缓存
            list = goodsQueryMapper.getOfflineGoodsByOwnerId(userId, cursorTime, size + 1);
            rebuildOfflineZSet(userId);
            cacheGoodsCards(list);
        }

        return buildPageResult(list, size);
    }

    
    // ==================== Redis缓存辅助方法 ====================
    
    /**
     * 从 ZSet 中获取商品ID列表
     * @param key ZSet的key
     * @param cursor 游标时间戳
     * @param count 获取数量
     * @return ID列表，如果ZSet不存在返回null
     */
    private List<Long> getIdsFromZSet(String key, long cursor, int count) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            return null;
        }
        // reverseRangeByScore: 按score从大到小排序，范围是[min, max]
        Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, cursor - 1, 0, count);
        
        if (tuples == null || tuples.isEmpty()) {
            return List.of();
        }
        
        return tuples.stream()
                .map(t -> ((Number) t.getValue()).longValue())
                .collect(Collectors.toList());
    }


    /**
     * 构建ZSet Key的通用方法
     * @param prefix Key前缀
     * @param id ID值
     * @param suffix Key后缀
     * @return 完整的Key
     */
    private String buildZSetKey(String prefix, Long id, String suffix) {
        return prefix + id + suffix;
    }


    /**
     * 重建ZSet缓存的通用方法
     * @param key Redis ZSet的key
     * @param dataSupplier 数据获取函数
     * @param idField ID字段名
     * @param timeField 时间字段名
     * @param ttl 缓存过期时间（分钟）
     */
    private void rebuildZSet(String key, Supplier<List<Map<String, Object>>> dataSupplier,
                             String idField, String timeField, long ttl) {
        List<Map<String, Object>> data = dataSupplier.get();
        if (data == null || data.isEmpty()) {
            return;
        }

//        TypedTuple {
//            Object value;   // ZSet 的 member（成员值），这里是商品ID
//            Double score;   // ZSet 的 score（排序分数），这里是时间戳
//        }
        Set<ZSetOperations.TypedTuple<Object>> tuples = data.stream()
                .map(m -> ZSetOperations.TypedTuple.of(
                        (Object) ((Number) m.get(idField)).longValue(),
                        ((Number) m.get(timeField)).doubleValue()
                ))
                .collect(Collectors.toSet());
        
        redisTemplate.opsForZSet().add(key, tuples);
        redisTemplate.expire(key, ttl, TimeUnit.MINUTES);
    }
    
    /**
     * 重建用户收藏列表ZSet缓存
     */
    private void rebuildFavoriteZSet(Long userId) {
        String key = buildZSetKey(RedisConstant.FAVORITE_USER_IDS_KEY, userId, RedisConstant.FAVORITE_USER_IDS_SUFFIX);
        rebuildZSet(key, () -> goodsQueryMapper.getFavoriteIdsWithTimeByUserId(userId),
                    "goodsId", "createTime", RedisConstant.GOODS_IDS_TTL);
    }
    
    /**
     * 重建分类ZSet缓存
     */
    private void rebuildCategoryZSet(Long categoryId) {
        String key = buildZSetKey(RedisConstant.GOODS_CAT_IDS_KEY, categoryId, RedisConstant.GOODS_CAT_IDS_SUFFIX);
        rebuildZSet(key, () -> goodsQueryMapper.getGoodsIdsWithTimeByCategoryId(categoryId), 
                    "id", "updateTime", RedisConstant.GOODS_IDS_TTL);
    }

    /**
     * 重建用户发布商品ZSet缓存
     */
    private void rebuildOwnerZSet(Long ownerId) {
        String key = buildZSetKey(RedisConstant.GOODS_OWNER_IDS_KEY, ownerId, RedisConstant.GOODS_OWNER_IDS_SUFFIX);
        rebuildZSet(key, () -> goodsQueryMapper.getGoodsIdsWithTimeByOwnerId(ownerId), 
                    "id", "updateTime", RedisConstant.GOODS_IDS_TTL);
    }

    /**
     * 重建所有商品ZSet缓存
     */
    private void rebuildAllGoodsZSet() {
        String key = RedisConstant.GOODS_ALL_IDS_KEY + RedisConstant.GOODS_ALL_IDS_SUFFIX;
        rebuildZSet(key, () -> goodsQueryMapper.getAllGoodsIdsWithTime(),
                    "id", "updateTime", RedisConstant.GOODS_IDS_TTL);
    }

    /**
     * 重建用户下架商品ZSet缓存
     */
    private void rebuildOfflineZSet(Long ownerId) {
        String key = buildZSetKey(RedisConstant.GOODS_OFFLINE_IDS_KEY, ownerId, RedisConstant.GOODS_OFFLINE_IDS_SUFFIX);
        rebuildZSet(key, () -> goodsQueryMapper.getOfflineGoodsIdsWithTimeByOwnerId(ownerId), 
                    "id", "updateTime", RedisConstant.GOODS_IDS_TTL);
    }
    
    /**
     * 从缓存或数据库获取商品卡片列表（复用商品卡片缓存）
     * @param ids 商品ID列表
     * @return 商品卡片列表，保持原始ids顺序
     */
    private List<GoodsCardVO> getGoodsCardsFromCacheOrDB(List<Long> ids) {
        // 1. 构建缓存 keys
        List<String> keys = ids.stream()
                .map(id -> RedisConstant.GOODS_CARD_KEY + id)
                .collect(Collectors.toList());
        
        // 2. 批量从缓存获取商品卡片信息
        List<Object> cached = redisTemplate.opsForValue().multiGet(keys);
        
        // 3. 找出缓存未命中的 ids
        List<Long> missIds = new ArrayList<>();

        //HashMap是无序的，这个Long类型是用来记录GoodsCardVO对应的商品id的，用于在第5步根据ids顺序进行排序再返回
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
     * 批量将商品卡片存入缓存
     */
    private void cacheGoodsCards(List<GoodsCardVO> cards) {
        if (cards == null || cards.isEmpty()) {
            return;
        }
        
        // 批量设置点赞数
        batchSetCollectNum(cards);
        
        // 存入商品卡片缓存（点赞数可能过时，后续会从点赞数缓存重新获取最新值）
        for (GoodsCardVO vo : cards) {
            String key = RedisConstant.GOODS_CARD_KEY + vo.getId();
            long ttl = RedisConstant.GOODS_CARD_TTL + new Random().nextInt(5);
            redisTemplate.opsForValue().set(key, vo, ttl, TimeUnit.MINUTES);
        }
    }
    
    /**
     * 批量设置商品卡片的点赞数
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

}



