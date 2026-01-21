package com.xyz.service;

import com.xyz.dto.PasswordUpdateDTO;
import com.xyz.dto.UserLoginDTO;
import com.xyz.dto.UserRegisterDTO;
import com.xyz.entity.User;
import com.xyz.vo.*;

import java.util.List;

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
     * 修改基础资料（昵称、手机号、邮箱、个人简介）
     */
    UserInfoVO updateProfile(Long userId, String nickname, String phone, String email, String bio);

    /**
     * 获取用户公开信息（查看其他用户）
     */
    UserPublicVO getUserPublicInfo(Long userId);

    /**
     * 修改密码
     */
    void updatePassword(Long userId, PasswordUpdateDTO passwordUpdateDTO);

    /**
     * 修改头像
     */
    UserInfoVO updateAvatar(Long userId, String avatar);

    /**
     * 游标分页查询用户列表（管理员）
     * 使用Redis ZSet缓存 + 用户卡片缓存
     */
    PageResult<UserListVO> getUserList(Long cursor, int size);

    /**
     * 获取用户详细信息（管理员）
     * 复用 getUserInfo，返回 UserDetailVO
     */
    UserDetailVO getUserDetail(Long userId);

    /**
     * 封禁用户
     */
    void banUser(Long userId);

    /**
     * 解封用户
     */
    void unbanUser(Long userId);

}