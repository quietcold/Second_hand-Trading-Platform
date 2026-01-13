package com.xyz.mapper;

import com.xyz.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天消息Mapper
 */
@Mapper
public interface ChatMessageMapper {
    
    /**
     * 插入消息
     */
    void insert(ChatMessage chatMessage);
    
    /**
     * 根据会话ID查询消息列表(分页)
     */
    List<ChatMessage> listBySessionId(@Param("sessionId") String sessionId, 
                                      @Param("offset") Integer offset, 
                                      @Param("pageSize") Integer pageSize);
    
    /**
     * 标记消息为已读
     */
    void markAsRead(@Param("sessionId") String sessionId, @Param("receiverId") Long receiverId);
    
    /**
     * 根据ID查询消息
     */
    ChatMessage getById(Long id);
    
    /**
     * 撤回消息
     */
    void recallMessage(Long id);
    
    /**
     * 删除会话的所有消息
     */
    void deleteBySessionId(String sessionId);
}
