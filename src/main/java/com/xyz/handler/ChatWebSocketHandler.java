package com.xyz.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xyz.dto.ChatMessageDTO;
import com.xyz.service.ChatService;
import com.xyz.util.BaseContext;
import com.xyz.vo.ChatMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 原生 WebSocket 消息处理器
 */
@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {
    
    @Autowired
    private ChatService chatService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 存储所有在线用户的 WebSocket 会话
    private static final Map<Long, WebSocketSession> ONLINE_SESSIONS = new ConcurrentHashMap<>();
    
    /**
     * 连接建立成功
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserId(session);
        if (userId != null) {
            ONLINE_SESSIONS.put(userId, session);
            chatService.userOnline(userId);
            log.info("用户上线: userId={}, sessionId={}", userId, session.getId());
        }
    }
    
    /**
     * 接收到客户端消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = getUserId(session);
        if (userId == null) {
            log.warn("未找到用户ID，忽略消息");
            return;
        }
        
        try {
            String payload = message.getPayload();
            log.info("收到消息: userId={}, payload={}", userId, payload);
            
            // 解析消息
            Map<String, Object> msgMap = objectMapper.readValue(payload, Map.class);
            String type = (String) msgMap.get("type");
            
            if ("heartbeat".equals(type)) {
                // 心跳消息
                handleHeartbeat(userId, session);
            } else if ("send".equals(type)) {
                // 发送聊天消息
                handleChatMessage(userId, msgMap, session);
            } else {
                log.warn("未知消息类型: {}", type);
            }
            
        } catch (Exception e) {
            log.error("处理消息失败: userId={}, error={}", userId, e.getMessage(), e);
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 连接关闭
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserId(session);
        if (userId != null) {
            ONLINE_SESSIONS.remove(userId);
            chatService.userOffline(userId);
            log.info("用户下线: userId={}, sessionId={}", userId, session.getId());
        }
    }
    
    /**
     * 传输异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = getUserId(session);
        log.error("WebSocket传输异常: userId={}, error={}", userId, exception.getMessage());
        
        if (session.isOpen()) {
            session.close();
        }
    }
    
    /**
     * 处理心跳消息
     */
    private void handleHeartbeat(Long userId, WebSocketSession session) {
        chatService.userHeartbeat(userId);
        log.debug("心跳续期: userId={}", userId);
        
        // 回复心跳确认
        try {
            Map<String, Object> response = Map.of(
                    "type", "heartbeat",
                    "status", "ok",
                    "timestamp", System.currentTimeMillis()
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (IOException e) {
            log.error("发送心跳响应失败: {}", e.getMessage());
        }
    }
    
    /**
     * 处理聊天消息
     */
    private void handleChatMessage(Long userId, Map<String, Object> msgMap, WebSocketSession session) {
        try {
            // 设置当前用户ID到 BaseContext
            BaseContext.setCurrentId(userId);
            
            // 构建 DTO
            ChatMessageDTO dto = objectMapper.convertValue(msgMap.get("data"), ChatMessageDTO.class);
            
            // 保存消息
            ChatMessageVO messageVO = chatService.sendMessage(dto);
            
            // 发送给发送者（确认）
            sendMessageToUserInternal(userId, Map.of(
                    "type", "message",
                    "data", messageVO
            ));
            
            // 发送给接收者（如果在线）
            if (chatService.isUserOnline(dto.getReceiverId())) {
                sendMessageToUserInternal(dto.getReceiverId(), Map.of(
                        "type", "message",
                        "data", messageVO
                ));
                log.info("消息已推送给在线用户: {}", dto.getReceiverId());
            } else {
                log.info("用户离线，消息已保存: {}", dto.getReceiverId());
            }
            
        } catch (Exception e) {
            log.error("发送消息失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "发送失败: " + e.getMessage());
        } finally {
            BaseContext.removeCurrentId();
        }
    }
    
    /**
     * 发送消息给指定用户（公共方法，供外部调用）
     */
    public void sendMessageToUser(Long userId, Object messageData) {
        WebSocketSession session = ONLINE_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> message = Map.of(
                        "type", "message",
                        "data", messageData
                );
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                log.debug("发送消息给用户: userId={}", userId);
            } catch (IOException e) {
                log.error("发送消息失败: userId={}, error={}", userId, e.getMessage());
            }
        } else {
            log.debug("用户不在线，无法发送: userId={}", userId);
        }
    }
    
    /**
     * 发送消息给指定用户（内部使用）
     */
    private void sendMessageToUserInternal(Long userId, Map<String, Object> message) {
        WebSocketSession session = ONLINE_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            } catch (IOException e) {
                log.error("发送消息失败: userId={}, error={}", userId, e.getMessage());
            }
        }
    }
    
    /**
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String errorMsg) {
        try {
            Map<String, Object> error = Map.of(
                    "type", "error",
                    "message", errorMsg,
                    "timestamp", System.currentTimeMillis()
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
        } catch (IOException e) {
            log.error("发送错误消息失败: {}", e.getMessage());
        }
    }
    
    /**
     * 从 Session 中获取用户ID
     */
    private Long getUserId(WebSocketSession session) {
        return (Long) session.getAttributes().get("userId");
    }
}
