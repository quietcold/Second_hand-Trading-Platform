package com.xyz.controller;

import com.xyz.constant.MessageConstant;
import com.xyz.dto.UserLoginDTO;
import com.xyz.dto.UserRegisterDTO;
import com.xyz.entity.User;
import com.xyz.properties.JwtProperties;
import com.xyz.service.UserService;
import com.xyz.util.JwtUtil;
import com.xyz.vo.Result;
import com.xyz.vo.UserEnterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理接口", description = "")
public class UserController {
    
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<UserEnterVO> register(  @RequestBody UserRegisterDTO registerDTO) {
        try {
            User user= userService.register(registerDTO);

            Map<String, Object> claims = new HashMap<>();
            claims.put("id", user.getId());
            // 生成JWT token
            String token = JwtUtil.generateToken(jwtProperties.getExpirationTime(),jwtProperties.getSecretKey(),claims);

            UserEnterVO userEnterVO=UserEnterVO.builder()
                    .id(user.getId())
                    .token(token)
                    .build();

            return Result.success(MessageConstant.Register_SUCCESS,userEnterVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录",description = "2222登录")
    public Result<UserEnterVO> login(@RequestBody UserLoginDTO loginDTO) {
        try {
            User user= userService.login(loginDTO);

            Map<String, Object> claims = new HashMap<>();
            claims.put("id", user.getId());
            // 生成JWT token
            String token = JwtUtil.generateToken(jwtProperties.getExpirationTime(),jwtProperties.getSecretKey(),claims);

            UserEnterVO userEnterVO=UserEnterVO.builder()
                    .id(user.getId())
                    .token(token)
                    .build();

            return Result.success(userEnterVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
//    /**
//     * 获取当前用户信息
//     */
//    @GetMapping("/info")
//    public Result<UserVO> getUserInfo(@RequestHeader("Authorization") String token) {
//        try {
//            // 从token中解析用户名
//            String username = JwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
//
//            // 根据用户名获取用户信息
//            UserVO userVO = userService.findByUsername(username);
//            if (userVO == null) {
//                return Result.error("用户不存在");
//            }
//
//            return Result.success(userVO);
//        } catch (Exception e) {
//            return Result.error("获取用户信息失败: " + e.getMessage());
//        }
//    }
}