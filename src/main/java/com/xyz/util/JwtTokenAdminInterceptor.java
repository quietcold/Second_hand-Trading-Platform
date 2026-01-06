package com.xyz.util;

import com.xyz.constant.MessageConstant;
import com.xyz.util.BaseContext;
import com.xyz.properties.JwtProperties;
import com.xyz.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

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

        // 1、从请求头中获取令牌
        // 前端通常约定 header key 为 "token" 或者 "Authorization"
        String token = request.getHeader("token");
        // 如果你的前端传的是 "Authorization: Bearer xxxx"，需要这里处理一下：
        // if (token != null && token.startsWith("Bearer ")) {
        //     token = token.substring(7);
        // }

        // 2、校验令牌
        try {
            log.info("jwt校验:{}", token);

            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);

            // 3、从 claims 中获取 userId (注意类型转换)
            Long userId = Long.valueOf(claims.get("id").toString());

            log.info("当前用户:{}", userId);

            // 4、将用户id存储到ThreadLocal
            BaseContext.setCurrentId(userId);

            // 5、放行
            return true;
        } catch (Exception ex) {
            // 4、不通过，响应 401 状态码
            response.setStatus(401);
            return false;
        }
    }

    // 请求完成后清理 ThreadLocal，防止内存泄漏
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.removeCurrentId();
    }
}

