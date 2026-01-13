package com.xyz.controller;

import com.xyz.dto.ChatMessageDTO;
import com.xyz.dto.ChatSessionDTO;
import com.xyz.service.ChatService;
import com.xyz.vo.ChatMessageVO;
import com.xyz.vo.ChatSessionVO;
import com.xyz.vo.Result;
import com.xyz.vo.UnreadCountVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.xyz.handler.ChatWebSocketHandler;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/**
 * 聊天HTTP接口控制器
 */
@RestController
@RequestMapping("/chat")
@Tag(name = "聊天接口")
@Slf4j
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    //点击“聊一聊”按钮的时候访问
    @PostMapping("/session/create")
    @Operation(summary = "创建或获取会话")
    public Result<String> createOrGetSession(@Valid @RequestBody ChatSessionDTO chatSessionDTO) {
        log.info("创建或获取会话: {}", chatSessionDTO);
        String sessionId = chatService.createOrGetSession(chatSessionDTO);
        return Result.success(sessionId);
    }

    //点击“消息”按钮的时候访问
    @GetMapping("/sessions")
    @Operation(summary = "获取会话列表")
    public Result<List<ChatSessionVO>> listSessions() {
        log.info("获取会话列表");
        List<ChatSessionVO> sessions = chatService.listSessions();
        return Result.success(sessions);
    }
    
    @GetMapping("/messages/{sessionId}")
    @Operation(summary = "获取历史消息")
    public Result<List<ChatMessageVO>> listMessages(
            @Parameter(description = "会话ID") @PathVariable String sessionId,
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "50") Integer pageSize) {
        log.info("获取历史消息: sessionId={}, page={}, pageSize={}", sessionId, page, pageSize);
        List<ChatMessageVO> messages = chatService.listMessages(sessionId, page, pageSize);
        return Result.success(messages);
    }
    
    @PutMapping("/messages/read/{sessionId}")
    @Operation(summary = "标记消息已读")
    public Result<Void> markAsRead(@Parameter(description = "会话ID") @PathVariable String sessionId) {
        log.info("标记消息已读: sessionId={}", sessionId);
        chatService.markAsRead(sessionId);
        return Result.success();
    }
    
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读消息总数")
    public Result<UnreadCountVO> getTotalUnread() {
        log.info("获取未读消息总数");
        Integer total = chatService.getTotalUnread();
        UnreadCountVO vo = UnreadCountVO.builder()
                .totalUnread(total)
                .build();
        return Result.success(vo);
    }
    
    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "删除会话")
    public Result<Void> deleteSession(@Parameter(description = "会话 ID") @PathVariable String sessionId) {
        log.info("删除会话: sessionId={}", sessionId);
        chatService.deleteSession(sessionId);
        return Result.success();
    }
    
    @PutMapping("/message/recall/{messageId}")
    @Operation(summary = "撤回消息")
    public Result<Void> recallMessage(@Parameter(description = "消息ID") @PathVariable Long messageId) {
        log.info("撤回消息: messageId={}", messageId);
        
        // 撤回消息并获取撤回后的消息VO
        ChatMessageVO recalledMessage = chatService.recallMessage(messageId);
        
        // 实时通知接收方消息被撤回
        if (chatService.isUserOnline(recalledMessage.getReceiverId())) {
            chatWebSocketHandler.sendMessageToUser(
                    recalledMessage.getReceiverId(),
                    recalledMessage
            );
            log.info("撤回通知已推送给在线用户: {}", recalledMessage.getReceiverId());
        }
        
        // 同时通知发送方本人（确认撤回成功）
        chatWebSocketHandler.sendMessageToUser(
                recalledMessage.getSenderId(),
                recalledMessage
        );
        
        return Result.success();
    }
}
