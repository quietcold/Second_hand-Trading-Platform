package com.xyz.service.impl;

import com.xyz.constant.ChatConstant;
import com.xyz.constant.MessageConstant;
import com.xyz.constant.RedisConstant;
import com.xyz.dto.ChatMessageDTO;
import com.xyz.dto.ChatSessionDTO;
import com.xyz.entity.ChatMessage;
import com.xyz.entity.ChatSession;
import com.xyz.entity.User;
import com.xyz.exception.ChatPermissionException;
import com.xyz.exception.ChatSessionNotFoundException;
import com.xyz.exception.ChatMessageNotFoundException;
import com.xyz.exception.ChatRecallTimeExpiredException;
import com.xyz.mapper.ChatMessageMapper;
import com.xyz.mapper.ChatSessionMapper;
import com.xyz.mapper.GoodsMapper;
import com.xyz.mapper.UserMapper;
import com.xyz.service.ChatService;
import com.xyz.util.BaseContext;
import com.xyz.vo.ChatMessageVO;
import com.xyz.vo.ChatSessionVO;
import com.xyz.vo.GoodsCardVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 聊天服务实现类
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    
    @Autowired
    private ChatSessionMapper chatSessionMapper;
    
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private GoodsMapper goodsMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String ONLINE_USERS_KEY = "chat:online:user:";
    private static final String UNREAD_TOTAL_CACHE_KEY = "chat:unread:total:";
    
    /**
     * 生成会话ID（包含商品ID）
     */
    private String generateSessionId(Long userId1, Long userId2, Long goodsId) {
        Long minId = Math.min(userId1, userId2);
        Long maxId = Math.max(userId1, userId2);
        // 如果有商品ID，格式为：minUserId_maxUserId_goodsId
        // 如果没有商品ID，格式为：minUserId_maxUserId
        if (goodsId != null) {
            return minId + "_" + maxId + "_" + goodsId;
        } else {
            return minId + "_" + maxId;
        }
    }
    
    @Override
    @Transactional
    public String createOrGetSession(ChatSessionDTO chatSessionDTO) {
        Long currentUserId = BaseContext.getCurrentId();
        Long receiverId = chatSessionDTO.getReceiverId();
        Long goodsId = chatSessionDTO.getGoodsId();
        
        // 生成会话ID（包含商品ID）
        String sessionId = generateSessionId(currentUserId, receiverId, goodsId);
        
        // 查询会话是否存在
        ChatSession existSession = chatSessionMapper.getBySessionId(sessionId);
        if (existSession != null) {
            // 如果会话已存在,取消当前用户的隐藏状态(重新显示)
            chatSessionMapper.unhideSession(sessionId, currentUserId);
            return sessionId;
        }
        
        // 创建新会话
        ChatSession chatSession = ChatSession.builder()
                .sessionId(sessionId)
                .user1Id(Math.min(currentUserId, receiverId))
                .user2Id(Math.max(currentUserId, receiverId))
                .goodsId(goodsId)
                .lastMessage("")
                .lastMessageTime(LocalDateTime.now())
                .user1Unread(0)
                .user2Unread(0)
                .user1Hide(0)
                .user2Hide(0)
                .build();
        
        chatSessionMapper.insert(chatSession);
        log.info("创建新会话: {}", sessionId);
        
        return sessionId;
    }
    
    @Override
    @Transactional
    public ChatMessageVO sendMessage(ChatMessageDTO chatMessageDTO) {
        Long currentUserId = BaseContext.getCurrentId();
        String sessionId = chatMessageDTO.getSessionId();
        
        // 验证会话是否存在
        ChatSession session = chatSessionMapper.getBySessionId(sessionId);
        if (session == null) {
            throw new ChatSessionNotFoundException(MessageConstant.CHAT_SESSION_NOT_FOUND);
        }
        
        // 验证用户权限
        if (!session.getUser1Id().equals(currentUserId) && !session.getUser2Id().equals(currentUserId)) {
            throw new ChatPermissionException(MessageConstant.CHAT_NO_PERMISSION);
        }
        
        // 发送消息时自动取消双方的隐藏状态(重新显示会话)
        chatSessionMapper.unhideSession(sessionId, currentUserId);
        chatSessionMapper.unhideSession(sessionId, chatMessageDTO.getReceiverId());
        
        // 创建消息
        LocalDateTime now = LocalDateTime.now();
        ChatMessage chatMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .senderId(currentUserId)
                .receiverId(chatMessageDTO.getReceiverId())
                .messageType(chatMessageDTO.getMessageType())
                .content(chatMessageDTO.getContent())
                .goodsId(chatMessageDTO.getGoodsId())
                .isRead(ChatConstant.MESSAGE_UNREAD)
                .isRecalled(0)
                .sendTime(now)
                .build();
        
        chatMessageMapper.insert(chatMessage);
        
        // 更新会话最后消息
        session.setLastMessage(chatMessageDTO.getContent());
        session.setLastMessageTime(now);
        chatSessionMapper.updateLastMessage(session);
        
        // 增加接收者未读数
        chatSessionMapper.incrementUnread(sessionId, chatMessageDTO.getReceiverId());
        
        // 清除接收者的未读数缓存（因为未读数变化了）
        String receiverUnreadKey = UNREAD_TOTAL_CACHE_KEY + chatMessageDTO.getReceiverId();
        redisTemplate.delete(receiverUnreadKey);
        
        // 构建返回VO
        ChatMessageVO messageVO = buildChatMessageVO(chatMessage);
        
        log.info("发送消息成功: sessionId={}, messageId={}", sessionId, chatMessage.getId());
        
        return messageVO;
    }
    
    @Override
    public List<ChatSessionVO> listSessions() {
        Long currentUserId = BaseContext.getCurrentId();
        
        // 查询用户的所有会话
        List<ChatSession> sessions = chatSessionMapper.listByUserId(currentUserId);
        
        List<ChatSessionVO> sessionVOList = new ArrayList<>();
        for (ChatSession session : sessions) {
            ChatSessionVO vo = buildChatSessionVO(session, currentUserId);
            sessionVOList.add(vo);
        }
        
        return sessionVOList;
    }
    
    @Override
    public List<ChatMessageVO> listMessages(String sessionId, Integer page, Integer pageSize) {
        Long currentUserId = BaseContext.getCurrentId();
        
        // 验证会话是否存在
        ChatSession session = chatSessionMapper.getBySessionId(sessionId);
        if (session == null) {
            throw new ChatSessionNotFoundException(MessageConstant.CHAT_SESSION_NOT_FOUND);
        }
        
        // 验证用户权限
        if (!session.getUser1Id().equals(currentUserId) && !session.getUser2Id().equals(currentUserId)) {
            throw new ChatPermissionException(MessageConstant.CHAT_NO_PERMISSION);
        }
        
        // 分页查询消息
        if (pageSize == null || pageSize <= 0) {
            pageSize = ChatConstant.DEFAULT_PAGE_SIZE;
        }
        if (page == null || page <= 0) {
            page = 1;
        }
        int offset = (page - 1) * pageSize;
        
        List<ChatMessage> messages = chatMessageMapper.listBySessionId(sessionId, offset, pageSize);
        
        List<ChatMessageVO> messageVOList = new ArrayList<>();
        for (ChatMessage message : messages) {
            ChatMessageVO vo = buildChatMessageVO(message);
            messageVOList.add(vo);
        }
        
        return messageVOList;
    }
    
    @Override
    @Transactional
    public void markAsRead(String sessionId) {
        Long currentUserId = BaseContext.getCurrentId();
        
        // 验证会话是否存在
        ChatSession session = chatSessionMapper.getBySessionId(sessionId);
        if (session == null) {
            throw new ChatSessionNotFoundException(MessageConstant.CHAT_SESSION_NOT_FOUND);
        }
        
        // 验证用户权限
        if (!session.getUser1Id().equals(currentUserId) && !session.getUser2Id().equals(currentUserId)) {
            throw new ChatPermissionException(MessageConstant.CHAT_NO_PERMISSION);
        }
        
        // 获取对方用户ID（发送者）
        Long otherUserId = session.getUser1Id().equals(currentUserId) ? 
                session.getUser2Id() : session.getUser1Id();
        
        // 标记消息为已读
        chatMessageMapper.markAsRead(sessionId, currentUserId);
        
        // 清空未读数
        chatSessionMapper.clearUnread(sessionId, currentUserId);
        
        // 清除未读数缓存
        String key = UNREAD_TOTAL_CACHE_KEY + currentUserId;
        redisTemplate.delete(key);
        log.debug("清除未读数缓存: userId={}", currentUserId);
        
        log.info("标记消息已读: sessionId={}, userId={}", sessionId, currentUserId);
    }
    
    @Override
    @Transactional
    public List<ChatMessageVO> markAsReadAndGetMessages(String sessionId) {
        Long currentUserId = BaseContext.getCurrentId();
        
        // 验证会话是否存在
        ChatSession session = chatSessionMapper.getBySessionId(sessionId);
        if (session == null) {
            throw new ChatSessionNotFoundException(MessageConstant.CHAT_SESSION_NOT_FOUND);
        }
        
        // 验证用户权限
        if (!session.getUser1Id().equals(currentUserId) && !session.getUser2Id().equals(currentUserId)) {
            throw new ChatPermissionException(MessageConstant.CHAT_NO_PERMISSION);
        }
        
        // 获取对方用户ID（发送者）
        Long otherUserId = session.getUser1Id().equals(currentUserId) ? 
                session.getUser2Id() : session.getUser1Id();
        
        // 查询当前用户未读的消息（即对方发送给我的未读消息）
        List<ChatMessage> unreadMessages = chatMessageMapper.getUnreadMessagesByReceiver(sessionId, currentUserId);
        
        // 标记消息为已读
        chatMessageMapper.markAsRead(sessionId, currentUserId);
        
        // 清空未读数
        chatSessionMapper.clearUnread(sessionId, currentUserId);
        
        // 清除未读数缓存
        String key = UNREAD_TOTAL_CACHE_KEY + currentUserId;
        redisTemplate.delete(key);
        log.debug("清除未读数缓存: userId={}", currentUserId);
        
        // 构建已读消息的VO列表（用于推送给发送者）
        List<ChatMessageVO> messageVOList = new ArrayList<>();
        for (ChatMessage message : unreadMessages) {
            // 更新消息的已读状态
            message.setIsRead(1);
            ChatMessageVO vo = buildChatMessageVO(message);
            messageVOList.add(vo);
        }
        
        log.info("标记消息已读: sessionId={}, userId={}, 已读消息数={}", sessionId, currentUserId, messageVOList.size());
        
        return messageVOList;
    }
    
    @Override
    public Integer getTotalUnread() {
        Long currentUserId = BaseContext.getCurrentId();
        
        // 临时注释缓存，直接查数据库
        Integer total = chatSessionMapper.getTotalUnreadByUserId(currentUserId);
        total = total != null ? total : 0;
        
        return total;
        
        /* 原有缓存逻辑
        String key = UNREAD_TOTAL_CACHE_KEY + currentUserId;
        
        // 先从 Redis 获取缓存
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.debug("从缓存获取未读总数: userId={}", currentUserId);
            return (Integer) cached;
        }
        
        // Redis 没有，查数据库
        Integer total = chatSessionMapper.getTotalUnreadByUserId(currentUserId);
        total = total != null ? total : 0;
        
        // 缓存到 Redis，5分钟过期
        redisTemplate.opsForValue().set(key, total, 5, TimeUnit.MINUTES);
        log.debug("缓存未读总数: userId={}, total={}", currentUserId, total);
        
        return total;
        */
    }
    
    @Override
    @Transactional
    public void deleteSession(String sessionId) {
        Long currentUserId = BaseContext.getCurrentId();
        
        // 验证会话是否存在
        ChatSession session = chatSessionMapper.getBySessionId(sessionId);
        if (session == null) {
            throw new ChatSessionNotFoundException(MessageConstant.CHAT_SESSION_NOT_FOUND);
        }
        
        // 验证用户权限
        if (!session.getUser1Id().equals(currentUserId) && !session.getUser2Id().equals(currentUserId)) {
            throw new ChatPermissionException(MessageConstant.CHAT_NO_PERMISSION);
        }
        
        // 隐藏会话(不删除数据,只是在会话列表中隐藏)
        // 当用户再次点击私聊时,历史消息仍然存在
        chatSessionMapper.deleteBySessionId(sessionId, currentUserId);
        
        log.info("隐藏会话成功: sessionId={}, userId={}", sessionId, currentUserId);
    }
    
    @Override
    @Transactional
    public ChatMessageVO recallMessage(Long messageId) {
        Long currentUserId = BaseContext.getCurrentId();
        
        // 查询消息
        ChatMessage message = chatMessageMapper.getById(messageId);
        if (message == null) {
            throw new ChatMessageNotFoundException(MessageConstant.CHAT_MESSAGE_NOT_FOUND);
        }
        
        // 验证是否为发送者
        if (!message.getSenderId().equals(currentUserId)) {
            throw new ChatPermissionException(MessageConstant.CHAT_RECALL_NO_PERMISSION);
        }
        
        // 检查是否已撤回
        if (message.getIsRecalled() != null && message.getIsRecalled() == 1) {
            throw new ChatPermissionException(MessageConstant.CHAT_MESSAGE_ALREADY_RECALLED);
        }
        
        // 检查时间限制(2分钟)
        LocalDateTime now = LocalDateTime.now();
        long timeDiff = now.toInstant(ZoneOffset.of("+8")).toEpochMilli() 
                - message.getSendTime().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        
        if (timeDiff > ChatConstant.RECALL_TIME_LIMIT) {
            throw new ChatRecallTimeExpiredException(MessageConstant.CHAT_RECALL_TIME_EXPIRED);
        }
        
        // 撤回消息
        chatMessageMapper.recallMessage(messageId);
        
        // 更新会话最后消息
        ChatSession session = chatSessionMapper.getBySessionId(message.getSessionId());
        if (session != null && session.getLastMessage().equals(message.getContent())) {
            session.setLastMessage("此消息已被撤回");
            session.setLastMessageTime(now);
            chatSessionMapper.updateLastMessage(session);
        }
        
        log.info("撤回消息成功: messageId={}", messageId);
        
        // 返回撤回后的消息VO用于推送
        return buildChatMessageVO(message);
    }
    
    @Override
    public boolean isUserOnline(Long userId) {
        String key = ONLINE_USERS_KEY + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    @Override
    public void userOnline(Long userId) {
        String key = ONLINE_USERS_KEY + userId;
        // 设置用户在线状态,5分钟过期
        redisTemplate.opsForValue().set(key, "1", ChatConstant.ONLINE_STATUS_EXPIRE, TimeUnit.SECONDS);
        log.info("用户上线: userId={}", userId);
    }
    
    @Override
    public void userOffline(Long userId) {
        String key = ONLINE_USERS_KEY + userId;
        redisTemplate.delete(key);
        log.info("用户下线: userId={}", userId);
    }
    
    @Override
    public void userHeartbeat(Long userId) {
        String key = ONLINE_USERS_KEY + userId;
        // 心跳续期5分钟
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.expire(key, ChatConstant.ONLINE_STATUS_EXPIRE, TimeUnit.SECONDS);
            log.debug("用户心跳: userId={}", userId);
        }
    }
    
    /**
     * 从缓存获取用户信息
     */
    private User getUserWithCache(Long userId) {
        if (userId == null) {
            return null;
        }
        
        String key = RedisConstant.USER_INFO_KEY + userId;
        
        // 从 Redis 获取
        User user = (User) redisTemplate.opsForValue().get(key);
        if (user != null) {
            log.debug("从缓存获取用户信息: userId={}", userId);
            return user;
        }
        
        // Redis 没有，查数据库
        user = userMapper.findById(userId);
        if (user != null) {
            // 缓存30分钟（用户信息变化不频繁）
            redisTemplate.opsForValue().set(key, user, RedisConstant.USER_INFO_TTL, TimeUnit.MINUTES);
            log.debug("缓存用户信息: userId={}", userId);
        }
        
        return user;
    }
    
    /**
     * 构建ChatSessionVO
     */
    private ChatSessionVO buildChatSessionVO(ChatSession session, Long currentUserId) {
        // 确定对方用户ID
        Long otherUserId = session.getUser1Id().equals(currentUserId) ? 
                session.getUser2Id() : session.getUser1Id();
        
        // 使用缓存查询对方用户信息
        User otherUser = getUserWithCache(otherUserId);
        
        // 查询商品卡片信息（使用卡片而非详情，性能更好）
        GoodsCardVO goods = null;
        if (session.getGoodsId() != null) {
            goods = goodsMapper.getGoodsCardById(session.getGoodsId());
        }
        
        // 确定未读数
        Integer unreadCount = session.getUser1Id().equals(currentUserId) ? 
                session.getUser1Unread() : session.getUser2Unread();
        
        return ChatSessionVO.builder()
                .sessionId(session.getSessionId())
                .otherUserId(otherUserId)
                .otherUsername(otherUser != null ? otherUser.getNickname() : "未知用户")
                .otherAvatar(otherUser != null ? otherUser.getImage() : null)
                .goodsId(session.getGoodsId())
                .goodsTitle(goods != null ? goods.getBriefDescription() : null)
                .goodsCover(goods != null ? goods.getCoverUrl() : null)
                .goodsType(goods != null ? goods.getGoodsType() : null)
                .goodsPrice(goods != null ? 
                        (goods.getGoodsType() == 1 ? goods.getSellPrice().doubleValue() : goods.getRentPrice().doubleValue()) 
                        : null)
                .lastMessage(session.getLastMessage())
                .lastMessageTime(session.getLastMessageTime() != null ? 
                        session.getLastMessageTime().toInstant(ZoneOffset.of("+8")).toEpochMilli() : null)
                .unreadCount(unreadCount)
                .createTime(session.getCreateTime() != null ? 
                        session.getCreateTime().toInstant(ZoneOffset.of("+8")).toEpochMilli() : null)
                .build();
    }
    
    /**
     * 构建ChatMessageVO(不包含商品信息,商品信息由ChatSessionVO提供)
     */
    private ChatMessageVO buildChatMessageVO(ChatMessage message) {
        // 使用缓存查询发送者信息
        User sender = getUserWithCache(message.getSenderId());
        
        return ChatMessageVO.builder()
                .id(message.getId())
                .sessionId(message.getSessionId())
                .senderId(message.getSenderId())
                .senderUsername(sender != null ? sender.getNickname() : "未知用户")
                .senderAvatar(sender != null ? sender.getImage() : null)
                .receiverId(message.getReceiverId())
                .messageType(message.getMessageType())
                .content(message.getContent())
                .isRead(message.getIsRead())
                .isRecalled(message.getIsRecalled())
                .sendTime(message.getSendTime() != null ? 
                        message.getSendTime().toInstant(ZoneOffset.of("+8")).toEpochMilli() : null)
                .build();
    }
}
