package com.xyz.configuration;

import com.xyz.util.JwtTokenAdminInterceptor;
import com.xyz.util.JwtTokenUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;
    
    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    /**
     * 注册自定义拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 管理员接口拦截器（严格验证）
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")  // 只拦截管理员接口
                .excludePathPatterns(
                        "/admin/login",      // 管理员登录接口不需要token
                        "/admin/logout"      // 管理员退出接口不需要token
                );
        
        // 用户端接口拦截器（支持可选登录）
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")  // 拦截所有用户接口
                .excludePathPatterns(
                        "/user/login",       // 用户登录接口
                        "/user/register"     // 用户注册接口
                );
        
        // 文档接口不需要任何验证
        // Swagger等文档接口会被上面的拦截器自动跳过
    }
}