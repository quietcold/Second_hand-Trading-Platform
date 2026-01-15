package com.xyz.service.impl;

import com.xyz.constant.MessageConstant;
import com.xyz.constant.RedisConstant;
import com.xyz.dto.CommentDTO;
import com.xyz.entity.Comments;
import com.xyz.entity.User;
import com.xyz.exception.CommentDeleteException;
import com.xyz.exception.CommentNotFoundException;
import com.xyz.mapper.CommentMapper;
import com.xyz.mapper.UserMapper;
import com.xyz.service.CommentService;
import com.xyz.util.BaseContext;
import com.xyz.vo.CommentVO;
import com.xyz.vo.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public CommentVO addComment(CommentDTO commentDTO) {
        Long currentUserId = BaseContext.getCurrentId();
        
        // 构建评论实体
        LocalDateTime now = LocalDateTime.now();
        Comments comment = Comments.builder()
                .goodsId(commentDTO.getGoodsId())
                .userId(currentUserId)
                .content(commentDTO.getContent())
                .parentId(commentDTO.getParentId())
                .replyToUserId(commentDTO.getReplyToUserId())
                .likeCount(0)
                .status(1)
                .createTime(now)
                .updateTime(now)
                .build();
        
        // 插入评论
        commentMapper.insertComment(comment);
        
        // 如果是回复评论，更新父评论的回复计数和最新回复缓存
        if (comment.getParentId() != null) {
            String countKey = RedisConstant.COMMENT_REPLY_COUNT_KEY + comment.getParentId();
            String latestKey = RedisConstant.COMMENT_LATEST_REPLY_KEY + comment.getParentId();
            
            // 增加回复计数
            redisTemplate.opsForValue().increment(countKey);
            redisTemplate.expire(countKey, RedisConstant.COMMENT_COUNT_TTL, TimeUnit.MINUTES);
            
            // 更新最新回复ID
            redisTemplate.opsForValue().set(latestKey, comment.getId().toString());
            redisTemplate.expire(latestKey, RedisConstant.COMMENT_COUNT_TTL, TimeUnit.MINUTES);
        }
        
        // 构建并返回 CommentVO
        return buildCommentVO(comment, commentDTO.getGoodsId());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Long currentUserId = BaseContext.getCurrentId();
        int rows = commentMapper.deleteComment(commentId, currentUserId);
        
        if (rows == 0) {
            throw new CommentDeleteException(MessageConstant.COMMENT_DELETE_FAILED);
        }
        
        // 删除相关 Redis 缓存
        Comments comment = commentMapper.getCommentById(commentId);
        if (comment != null && comment.getParentId() != null) {
            String countKey = RedisConstant.COMMENT_REPLY_COUNT_KEY + comment.getParentId();
            String latestKey = RedisConstant.COMMENT_LATEST_REPLY_KEY + comment.getParentId();
            
            // 清除缓存，让下次查询时重新从数据库获取
            redisTemplate.delete(countKey);
            redisTemplate.delete(latestKey);
        }
    }

    @Override
    @Transactional
    public void likeComment(Long commentId) {
        Long currentUserId = BaseContext.getCurrentId();
        
        String commentLikeKey = RedisConstant.COMMENT_LIKE_KEY + commentId;
        String userLikeKey = RedisConstant.USER_LIKE_COMMENTS_KEY + currentUserId;
        
        // 判断用户是否已点赞
        Boolean isMember = redisTemplate.opsForSet().isMember(commentLikeKey, currentUserId.toString());
        
        if (Boolean.TRUE.equals(isMember)) {
            // 已点赞，则取消点赞
            redisTemplate.opsForSet().remove(commentLikeKey, currentUserId.toString());
            redisTemplate.opsForSet().remove(userLikeKey, commentId.toString());
        } else {
            // 未点赞，则添加点赞
            redisTemplate.opsForSet().add(commentLikeKey, currentUserId.toString());
            redisTemplate.opsForSet().add(userLikeKey, commentId.toString());
        }
        
        // 设置过期时间
        redisTemplate.expire(commentLikeKey, RedisConstant.COMMENT_LIKE_TTL, TimeUnit.MINUTES);
        redisTemplate.expire(userLikeKey, RedisConstant.COMMENT_LIKE_TTL, TimeUnit.MINUTES);
        
        // 异步更新数据库点赞数
        Long likeCount = redisTemplate.opsForSet().size(commentLikeKey);
        commentMapper.updateLikeCount(commentId, likeCount != null ? likeCount.intValue() : 0);
    }

    @Override
    public PageResult<CommentVO> getTopComments(Long goodsId, Long cursor, Integer size) {
        // 设置默认值
        if (cursor == null || cursor == 0) {
            cursor = System.currentTimeMillis();
        }
        if (size == null || size <= 0) {
            size = 10;
        }
        
        // 查询顶层评论（多查一条用于判断是否还有更多）
        List<Comments> comments = commentMapper.getTopCommentsByGoodsId(goodsId, cursor, size + 1);
        
        if (comments.isEmpty()) {
            return PageResult.empty();
        }
        
        // 判断是否还有更多数据
        boolean hasMore = comments.size() > size;
        if (hasMore) {
            comments = comments.subList(0, size);
        }
        
        // 获取下一页游标
        Long nextCursor = hasMore ? getTimestamp(comments.get(comments.size() - 1).getCreateTime()) : null;
        
        // 批量查询回复数量和最新回复
        List<Long> commentIds = comments.stream().map(Comments::getId).collect(Collectors.toList());
        Map<Long, Integer> replyCountMap = getReplyCountMap(commentIds);
        Map<Long, Comments> latestReplyMap = getLatestReplyMap(commentIds);
        
        // 构建 CommentVO 列表
        List<CommentVO> commentVOList = comments.stream()
                .map(comment -> {
                    CommentVO vo = buildCommentVO(comment, goodsId);
                    vo.setReplyCount(replyCountMap.getOrDefault(comment.getId(), 0));
                    
                    // 如果有回复，添加最新一条回复
                    Comments latestReply = latestReplyMap.get(comment.getId());
                    if (latestReply != null) {
                        vo.setLatestReply(buildCommentVO(latestReply, goodsId));
                    }
                    
                    return vo;
                })
                .collect(Collectors.toList());
        
        return PageResult.of(commentVOList, nextCursor, hasMore);
    }

    @Override
    public PageResult<CommentVO> getReplies(Long parentId, Long cursor, Integer size) {
        // 设置默认值
        if (cursor == null || cursor == 0) {
            cursor = System.currentTimeMillis();
        }
        if (size == null || size <= 0) {
            size = 20;
        }
        
        // 查询子评论（多查一条用于判断是否还有更多）
        List<Comments> replies = commentMapper.getRepliesByParentId(parentId, cursor, size + 1);
        
        if (replies.isEmpty()) {
            return PageResult.empty();
        }
        
        // 判断是否还有更多数据
        boolean hasMore = replies.size() > size;
        //比如说本来一次要取10条返回前端，查数据库的时候查了11条(多查一条用于判断是否还有更多)，则截取前10条返回
        if (hasMore) {
            replies = replies.subList(0, size);
        }
        
        // 获取下一页游标
        Long nextCursor = hasMore ? getTimestamp(replies.get(replies.size() - 1).getCreateTime()) : null;
        
        // 获取父评论的商品ID（用于判断是否为卖家）
        Comments parentComment = commentMapper.getCommentById(parentId);
        Long goodsId = parentComment != null ? parentComment.getGoodsId() : null;
        
        // 构建 CommentVO 列表
        List<CommentVO> replyVOList = replies.stream()
                .map(reply -> buildCommentVO(reply, goodsId))
                .collect(Collectors.toList());
        
        return PageResult.of(replyVOList, nextCursor, hasMore);
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId, String reason) {
        // 查询评论是否存在
        Comments comment = commentMapper.getCommentByIdWithoutStatus(commentId);
        if (comment == null) {
            throw new CommentNotFoundException(MessageConstant.COMMENT_NOT_FOUND);
        }
        
        // 管理员删除评论，状态改为3（违规屏蔽）
        int rows = commentMapper.deleteCommentByAdmin(commentId);
        if (rows == 0) {
            throw new CommentDeleteException("删除评论失败");
        }
        
        log.info("管理员删除评论成功: commentId={}, reason={}", commentId, reason);
        
        // 清除相关 Redis 缓存
        clearCommentCache(comment);
    }

    @Override
    @Transactional
    public void restoreCommentByAdmin(Long commentId) {
        // 查询评论是否存在
        Comments comment = commentMapper.getCommentByIdWithoutStatus(commentId);
        if (comment == null) {
            throw new CommentNotFoundException(MessageConstant.COMMENT_NOT_FOUND);
        }
        
        // 管理员恢复评论，状态改为1（正常）
        int rows = commentMapper.restoreCommentByAdmin(commentId);
        if (rows == 0) {
            throw new CommentDeleteException("恢复评论失败");
        }
        
        log.info("管理员恢复评论成功: commentId={}", commentId);
        
        // 清除相关 Redis 缓存，让下次查询时重新从数据库获取
        clearCommentCache(comment);
    }

    /**
     * 清除评论相关的 Redis 缓存
     */
    private void clearCommentCache(Comments comment) {
        if (comment != null && comment.getParentId() != null) {
            String countKey = RedisConstant.COMMENT_REPLY_COUNT_KEY + comment.getParentId();
            String latestKey = RedisConstant.COMMENT_LATEST_REPLY_KEY + comment.getParentId();
            
            // 清除缓存
            redisTemplate.delete(countKey);
            redisTemplate.delete(latestKey);
        }
    }

    @Override
    public PageResult<CommentVO> getTopCommentsByAdmin(Long goodsId, Long cursor, Integer size) {
        // 设置默认值
        if (cursor == null || cursor == 0) {
            cursor = System.currentTimeMillis();
        }
        if (size == null || size <= 0) {
            size = 10;
        }
        
        // 查询顶层评论（所有状态，多查一条用于判断是否还有更多）
        List<Comments> comments = commentMapper.getTopCommentsByGoodsIdForAdmin(goodsId, cursor, size + 1);
        
        if (comments.isEmpty()) {
            return PageResult.empty();
        }
        
        // 判断是否还有更多数据
        boolean hasMore = comments.size() > size;
        if (hasMore) {
            comments = comments.subList(0, size);
        }
        
        // 获取下一页游标
        Long nextCursor = hasMore ? getTimestamp(comments.get(comments.size() - 1).getCreateTime()) : null;
        
        // 批量查询回复数量和最新回复
        List<Long> commentIds = comments.stream().map(Comments::getId).collect(Collectors.toList());
        Map<Long, Integer> replyCountMap = getReplyCountMap(commentIds);
        Map<Long, Comments> latestReplyMap = getLatestReplyMap(commentIds);
        
        // 构建 CommentVO 列表
        List<CommentVO> commentVOList = comments.stream()
                .map(comment -> {
                    CommentVO vo = buildCommentVO(comment, goodsId);
                    vo.setReplyCount(replyCountMap.getOrDefault(comment.getId(), 0));
                    
                    // 如果有回复，添加最新一条回复
                    Comments latestReply = latestReplyMap.get(comment.getId());
                    if (latestReply != null) {
                        vo.setLatestReply(buildCommentVO(latestReply, goodsId));
                    }
                    
                    return vo;
                })
                .collect(Collectors.toList());
        
        return PageResult.of(commentVOList, nextCursor, hasMore);
    }

    @Override
    public PageResult<CommentVO> getRepliesByAdmin(Long parentId, Long cursor, Integer size) {
        // 设置默认值
        if (cursor == null || cursor == 0) {
            cursor = System.currentTimeMillis();
        }
        if (size == null || size <= 0) {
            size = 20;
        }
        
        // 查询子评论（所有状态，多查一条用于判断是否还有更多）
        List<Comments> replies = commentMapper.getRepliesByParentIdForAdmin(parentId, cursor, size + 1);
        
        if (replies.isEmpty()) {
            return PageResult.empty();
        }
        
        // 判断是否还有更多数据
        boolean hasMore = replies.size() > size;
        if (hasMore) {
            replies = replies.subList(0, size);
        }
        
        // 获取下一页游标
        Long nextCursor = hasMore ? getTimestamp(replies.get(replies.size() - 1).getCreateTime()) : null;
        
        // 获取父评论的商品ID（用于判断是否为卖家）
        Comments parentComment = commentMapper.getCommentByIdWithoutStatus(parentId);
        Long goodsId = parentComment != null ? parentComment.getGoodsId() : null;
        
        // 构建 CommentVO 列表
        List<CommentVO> replyVOList = replies.stream()
                .map(reply -> buildCommentVO(reply, goodsId))
                .collect(Collectors.toList());
        
        return PageResult.of(replyVOList, nextCursor, hasMore);
    }

    /**
     * 构建 CommentVO
     */
    private CommentVO buildCommentVO(Comments comment, Long goodsId) {
        Long currentUserId = BaseContext.getCurrentId();
        
        // 查询评论用户信息
        User user = userMapper.findById(comment.getUserId());
        
        // 判断是否为卖家
        Long sellerId = goodsId != null ? commentMapper.getSellerIdByGoodsId(goodsId) : null;
        boolean isSeller = sellerId != null && sellerId.equals(comment.getUserId());
        
        // 查询点赞数和是否点赞
        String commentLikeKey = RedisConstant.COMMENT_LIKE_KEY + comment.getId();
        Long likeCount = redisTemplate.opsForSet().size(commentLikeKey);
        Boolean hasLiked = redisTemplate.opsForSet().isMember(commentLikeKey, currentUserId.toString());
        
        // 如果 Redis 中没有点赞数据，从数据库获取
        if (likeCount == null || likeCount == 0) {
            likeCount = comment.getLikeCount() != null ? comment.getLikeCount().longValue() : 0L;
        }
        
        CommentVO vo = CommentVO.builder()
                .commentId(comment.getId())
                .userId(comment.getUserId())
                .userNickname(user != null ? user.getNickname() : "未知用户")
                .userAvatar(user != null ? user.getImage() : null)
                .isSeller(isSeller)
                .content(comment.getContent())
                .replyToUserId(comment.getReplyToUserId())
                .likeCount(likeCount.intValue())
                .hasLiked(Boolean.TRUE.equals(hasLiked))
                .build();
        
        // 单独设置 status
        vo.setStatus(comment.getStatus());
        
        // 单独设置 createTime，触发自定义 setter 计算 createTimestamp
        vo.setCreateTime(comment.getCreateTime());
        
        // 如果是回复评论，查询被回复用户的昵称
        if (comment.getReplyToUserId() != null) {
            User replyToUser = userMapper.findById(comment.getReplyToUserId());
            vo.setReplyToUserNickname(replyToUser != null ? replyToUser.getNickname() : "未知用户");
        }
        
        return vo;
    }

    /**
     * 批量查询回复数量
     */
    private Map<Long, Integer> getReplyCountMap(List<Long> parentIds) {
        if (parentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<Long, Integer> countMap = new HashMap<>();
        
        // 先从 Redis 查询
        for (Long parentId : parentIds) {
            String key = RedisConstant.COMMENT_REPLY_COUNT_KEY + parentId;
            String count = redisTemplate.opsForValue().get(key);
            if (count != null) {
                countMap.put(parentId, Integer.parseInt(count));
            }
        }
        
        // Redis 中没有的，从数据库查询
        List<Long> missedIds = parentIds.stream()
                .filter(id -> !countMap.containsKey(id))
                .collect(Collectors.toList());
        
        if (!missedIds.isEmpty()) {
            List<Map<String, Object>> dbCounts = commentMapper.getReplyCountsByParentIds(missedIds);
            for (Map<String, Object> map : dbCounts) {
                Long parentId = ((Number) map.get("parent_id")).longValue();
                Integer count = ((Number) map.get("reply_count")).intValue();
                countMap.put(parentId, count);
                
                // 写入 Redis 缓存
                String key = RedisConstant.COMMENT_REPLY_COUNT_KEY + parentId;
                redisTemplate.opsForValue().set(key, count.toString(), 
                        RedisConstant.COMMENT_COUNT_TTL, TimeUnit.MINUTES);
            }
        }
        
        return countMap;
    }

    /**
     * 批量查询最新回复
     */
    private Map<Long, Comments> getLatestReplyMap(List<Long> parentIds) {
        if (parentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // 从数据库批量查询最新回复
        List<Comments> latestReplies = commentMapper.getLatestRepliesByParentIds(parentIds);


        // 将结果转换为 Map
        return latestReplies.stream()
                .collect(Collectors.toMap(Comments::getParentId,
                        reply -> reply, (r1, r2) -> r1));
//        (r1, r2) -> r1 - 冲突处理（合并函数），正常情况下，由于 SQL 已经用 MAX(create_time) 筛选过，
//        不会有重复的 parentId，所以这个合并函数理论上不会被触发。
    }

    /**
     * 将 LocalDateTime 转换为时间戳（毫秒）
     */
    private Long getTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
