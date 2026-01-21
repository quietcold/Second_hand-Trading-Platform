package com.xyz.controller.user;

import com.xyz.constant.MessageConstant;
import com.xyz.dto.PasswordUpdateDTO;
import com.xyz.dto.UserLoginDTO;
import com.xyz.dto.UserRegisterDTO;
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
     * 修改基础资料（昵称、手机号）
     */
    @PutMapping("/profile")
    @Operation(summary = "修改基础资料")
    public Result<UserInfoVO> updateProfile(@RequestBody Map<String, String> request) {
        try {
            Long userId = BaseContext.getCurrentId();
            String nickname = request.get("nickname");
            String phone = request.get("phone");
            String email = request.get("email");
            String bio = request.get("bio");
            
            // 昵称必填验证
            if (nickname == null || nickname.trim().isEmpty()) {
                return Result.error("昵称不能为空");
            }
            
            nickname = nickname.trim();
            
            // 昵称长度验证（2-15个字符）
            if (nickname.length() < 2 || nickname.length() > 15) {
                return Result.error("昵称长度必须在2-15个字符之间");
            }
            
            // 昵称格式验证：只能包含中英文、数字、"_"、"-"
            if (!nickname.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9_-]+$")) {
                return Result.error("昵称只能包含中英文、数字、下划线和横线");
            }
            
            // 昵称不能是纯数字
            if (nickname.matches("^\\d+$")) {
                return Result.error("昵称不能是纯数字");
            }
            
            UserInfoVO userInfoVO = userService.updateProfile(userId, nickname, phone, email, bio);
            return Result.success("修改成功", userInfoVO);
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

    /**
     * 修改头像
     */
    @PutMapping("/avatar")
    @Operation(summary = "修改头像")
    public Result<UserInfoVO> updateAvatar(@RequestBody Map<String, String> request) {
        try {
            Long userId = BaseContext.getCurrentId();
            String avatar = request.get("avatar");
            if (avatar == null || avatar.trim().isEmpty()) {
                return Result.error("头像URL不能为空");
            }
            UserInfoVO userInfoVO = userService.updateAvatar(userId, avatar);
            return Result.success("头像更新成功", userInfoVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    

}