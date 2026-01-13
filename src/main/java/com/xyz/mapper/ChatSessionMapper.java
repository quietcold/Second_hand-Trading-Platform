package com.xyz.mapper;

import com.xyz.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天会话Mapper
 */
@Mapper
public interface ChatSessionMapper {
    
    /**
     * 根据会话ID查询会话
     */
    ChatSession getBySessionId(String sessionId);
    
    /**
     * 插入会话
     */
    void insert(ChatSession chatSession);
    
    /**
     * 更新会话最后消息信息
     */
    void updateLastMessage(ChatSession chatSession);
    
    /**
     * 增加未读数
     */
    void incrementUnread(@Param("sessionId") String sessionId, @Param("userId") Long userId);
    
    /**
     * 清空未读数
     */
    void clearUnread(@Param("sessionId") String sessionId, @Param("userId") Long userId);
    
    /**
     * 获取用户的会话列表
     */
    List<ChatSession> listByUserId(Long userId);
    
    /**
     * 获取用户的未读消息总数
     */
    Integer getTotalUnreadByUserId(Long userId);
    
    /**
     * 隐藏会话(不删除数据,只是在会话列表中隐藏)
     */
    void deleteBySessionId(@Param("sessionId") String sessionId, @Param("userId") Long userId);
    
    /**
     * 取消隐藏会话(当用户再次发送消息时自动显示)
     */
    void unhideSession(@Param("sessionId") String sessionId, @Param("userId") Long userId);
}
