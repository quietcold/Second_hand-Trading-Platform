package com.xyz.service;

import com.xyz.dto.ChatMessageDTO;
import com.xyz.dto.ChatSessionDTO;
import com.xyz.vo.ChatMessageVO;
import com.xyz.vo.ChatSessionVO;

import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService {
    
    /**
     * 创建或获取会话
     */
    String createOrGetSession(ChatSessionDTO chatSessionDTO);
    
    /**
     * 发送消息
     */
    ChatMessageVO sendMessage(ChatMessageDTO chatMessageDTO);
    
    /**
     * 获取会话列表
     */
    List<ChatSessionVO> listSessions();
    
    /**
     * 获取历史消息
     */
    List<ChatMessageVO> listMessages(String sessionId, Integer page, Integer pageSize);
    
    /**
     * 标记消息已读
     */
    void markAsRead(String sessionId);
    
    /**
     * 获取未读消息总数
     */
    Integer getTotalUnread();
    
    /**
     * 删除会话
     */
    void deleteSession(String sessionId);
    
    /**
     * 撤回消息
     * @return 撤回的消息VO（用于实时通知）
     */
    ChatMessageVO recallMessage(Long messageId);
    
    /**
     * 检查用户是否在线
     */
    boolean isUserOnline(Long userId);
    
    /**
     * 用户上线
     */
    void userOnline(Long userId);
    
    /**
     * 用户下线
     */
    void userOffline(Long userId);
    
    /**
     * 用户心跳(续期在线状态)
     */
    void userHeartbeat(Long userId);
}
