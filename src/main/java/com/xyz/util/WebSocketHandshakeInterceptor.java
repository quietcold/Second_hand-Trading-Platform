package com.xyz.util;

import com.xyz.constant.ChatConstant;
import com.xyz.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket握手拦截器 - JWT验证
 */
@Slf4j
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
    
    @Autowired
    private JwtProperties jwtProperties;
    
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            
            // 从请求参数中获取token
            String token = servletRequest.getServletRequest().getParameter("token");
            
            if (token != null && !token.isEmpty()) {
                try {
                    // 验证token
                    Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
                    // JWT中用户ID的字段名是"id"，不是"userId"
                    Long userId = Long.valueOf(claims.get("id").toString());
                    
                    // 将用户ID存储到attributes中，后续可以使用
                    // 原生 WebSocket 使用 "userId" 作为 key
                    attributes.put("userId", userId);
                    
                    log.info("WebSocket握手成功，用户ID: {}", userId);
                    return true;
                } catch (Exception e) {
                    log.error("WebSocket握手失败，token验证失败: {}", e.getMessage());
                    return false;
                }
            }
            
            log.warn("WebSocket握手失败，未提供token");
            return false;
        }
        
        return true;
    }
    
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {
        // 握手完成后的处理（可选）
    }
}
