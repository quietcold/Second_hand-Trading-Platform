package com.xyz.controller.user;

import com.xyz.constant.MessageConstant;
import com.xyz.dto.PasswordUpdateDTO;
import com.xyz.dto.UserLoginDTO;
import com.xyz.dto.UserRegisterDTO;
import com.xyz.dto.UserUpdateDTO;
import com.xyz.entity.User;
import com.xyz.properties.JwtProperties;
import com.xyz.service.UserService;
import com.xyz.util.BaseContext;
import com.xyz.util.JwtUtil;
import com.xyz.vo.Result;
import com.xyz.vo.UserEnterVO;
import com.xyz.vo.UserInfoVO;
import com.xyz.vo.UserPublicVO;
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
@RestController("userUserController")
@RequestMapping("/user")
@Tag(name = "用户管理")
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

    /**
     * 查看个人信息
     */
    @GetMapping("/info")
    @Operation(summary = "查看个人信息")
    public Result<UserInfoVO> getUserInfo() {
        try {
            Long userId = BaseContext.getCurrentId();
            UserInfoVO userInfoVO = userService.getUserInfo(userId);
            return Result.success(userInfoVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改个人信息
     */
    @PutMapping("/info")
    @Operation(summary = "修改个人信息")
    public Result<UserInfoVO> updateUserInfo(@RequestBody UserUpdateDTO updateDTO) {
        try {
            Long userId = BaseContext.getCurrentId();
            UserInfoVO userInfoVO = userService.updateUserInfo(userId, updateDTO);
            return Result.success("更新成功", userInfoVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查看其他用户公开信息
     */
    @GetMapping("/{userId}")
    @Operation(summary = "查看其他用户公开信息")
    public Result<UserPublicVO> getUserPublicInfo(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        try {
            UserPublicVO userPublicVO = userService.getUserPublicInfo(userId);
            return Result.success(userPublicVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public Result<String> updatePassword(@RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        try {
            Long userId = BaseContext.getCurrentId();
            userService.updatePassword(userId, passwordUpdateDTO);
            return Result.success(MessageConstant.PASSWORD_UPDATE_SUCCESS);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    

}