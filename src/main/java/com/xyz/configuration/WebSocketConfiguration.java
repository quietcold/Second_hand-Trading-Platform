package com.xyz.configuration;

import com.xyz.handler.ChatWebSocketHandler;
import com.xyz.util.WebSocketHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类（原生 WebSocket）
 */
@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {
    
    @Autowired
    private WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;
    
    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册原生 WebSocket 端点
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOriginPatterns("*")  // 允许跨域
                .addInterceptors(webSocketHandshakeInterceptor);  // JWT 验证拦截器
    }
}
