package com.xyz.util;

import com.xyz.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户端JWT令牌校验的拦截器
 * 支持可选登录和强制登录两种模式
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            // 当前拦截到的不是动态方法，直接放行
            return true;
        }

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        // 定义需要强制登录的接口
        boolean requireAuth = isRequireAuth(requestURI, method);
        
        // 1、从请求头中获取令牌
        String token = request.getHeader("token");

        // 2、校验令牌
        try {
            if (token != null && !token.isEmpty()) {
                log.info("jwt校验:{}", token);
                Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
                
                // 3、从 claims 中获取 userId
                Long userId = Long.valueOf(claims.get("id").toString());
                log.info("当前用户:{}", userId);
                
                // 4、将用户id存储到ThreadLocal
                BaseContext.setCurrentId(userId);
            } else if (requireAuth) {
                // 需要强制登录但没有token
                log.warn("访问需要登录的接口但未提供token: {}", requestURI);
                response.setStatus(401);
                return false;
            }
            // 如果不需要强制登录且没有token，直接放行（BaseContext中userId为null）
            
            // 5、放行
            return true;
        } catch (Exception ex) {
            // token解析失败
            if (requireAuth) {
                // 需要强制登录，返回401
                log.warn("token解析失败，访问需要登录的接口: {}", requestURI);
                response.setStatus(401);
                return false;
            } else {
                // 不需要强制登录，清除可能的错误token，继续放行
                log.warn("token解析失败，但接口不需要强制登录: {}", requestURI);
                BaseContext.removeCurrentId();
                return true;
            }
        }
    }

    /**
     * 判断接口是否需要强制登录
     */
    private boolean isRequireAuth(String requestURI, String method) {
        // 需要强制登录的接口列表
        String[] requireAuthPaths = {
            "/user/goods-query/my/favorite",      // 我的收藏
            "/user/goods-query/published/my",     // 我的发布
            "/user/goods-query/offline/my",       // 我下架的商品
            "/user/chat/",                        // 聊天相关
            "/user/comment/",                     // 评论相关（发表、删除、点赞）
        };
        
        // POST、PUT、DELETE方法的商品接口需要登录
        if (requestURI.startsWith("/user/goods/") && 
            ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))) {
            return true;
        }
        
        // 收藏相关接口需要登录
        if (requestURI.contains("/favorite") && 
            ("POST".equals(method) || "GET".equals(method))) {
            return true;
        }
        
        // 检查其他需要强制登录的路径
        for (String path : requireAuthPaths) {
            if (requestURI.startsWith(path)) {
                return true;
            }
        }
        
        return false;
    }

    // 请求完成后清理 ThreadLocal，防止内存泄漏
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.removeCurrentId();
    }
}