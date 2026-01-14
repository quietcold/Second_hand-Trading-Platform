package com.xyz.service;

import com.xyz.dto.PasswordUpdateDTO;
import com.xyz.dto.UserLoginDTO;
import com.xyz.dto.UserRegisterDTO;
import com.xyz.dto.UserUpdateDTO;
import com.xyz.entity.User;
import com.xyz.vo.UserEnterVO;
import com.xyz.vo.UserInfoVO;
import com.xyz.vo.UserPublicVO;

/**
 * 用户服务接口
 */
public interface UserService {
    /**
     * 用户注册
     */
    User register(UserRegisterDTO registerDTO);
    
    /**
     * 用户登录
     */
    User login(UserLoginDTO loginDTO);

    /**
     * 获取用户信息
     */
    UserInfoVO getUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    UserInfoVO updateUserInfo(Long userId, UserUpdateDTO updateDTO);

    /**
     * 获取用户公开信息（查看其他用户）
     */
    UserPublicVO getUserPublicInfo(Long userId);

    /**
     * 修改密码
     */
    void updatePassword(Long userId, PasswordUpdateDTO passwordUpdateDTO);

}