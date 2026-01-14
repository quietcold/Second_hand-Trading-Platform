package com.xyz.service.impl;

import com.xyz.constant.MessageConstant;
import com.xyz.dto.PasswordUpdateDTO;
import com.xyz.dto.UserLoginDTO;
import com.xyz.dto.UserRegisterDTO;
import com.xyz.dto.UserUpdateDTO;
import com.xyz.entity.User;
import com.xyz.exception.AccountNotFoundException;
import com.xyz.exception.PasswordErrorException;
import com.xyz.mapper.UserMapper;
import com.xyz.service.UserService;
import com.xyz.vo.UserInfoVO;
import com.xyz.vo.UserPublicVO;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    

    
    @Override
    @Transactional  // 添加事务管理
    public User register(UserRegisterDTO registerDTO) {
        // 检查用户名是否已存在
        User userExist = userMapper.findByAcc(registerDTO.getAccountNum());
        if (userExist != null) {
            throw new RuntimeException("用户已存在");
        }

        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        
        // 处理空字符串转 null（避免唯一约束冲突）
        if (user.getEmail() != null && user.getEmail().trim().isEmpty()) {
            user.setEmail(null);
        }
        if (user.getPhone() != null && user.getPhone().trim().isEmpty()) {
            user.setPhone(null);
        }
        
        // 设置默认值
        user.setStatus(1); // 1-启用
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 保存用户到数据库
        userMapper.insert(user);

        // 返回用户VO（不包含密码）
        return user;
    }

    
    @Override
    public User login(UserLoginDTO loginDTO) {
        // 根据用户名查询用户
        User userExist = userMapper.findByAcc(loginDTO.getAccountNum());
        if (userExist == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_EXIST);
        }
        
        // 验证密码
        User user=userMapper.findByAcc_Pss(loginDTO.getAccountNum(), loginDTO.getPassword());
        if (userExist == null) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        return user;
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new AccountNotFoundException("用户不存在");
        }

        // 将User转换为UserInfoVO（不包含密码）
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);
        return userInfoVO;
    }

    @Override
    @Transactional
    public UserInfoVO updateUserInfo(Long userId, UserUpdateDTO updateDTO) {
        // 查询用户是否存在
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new AccountNotFoundException("用户不存在");
        }

        // 更新用户信息
        BeanUtils.copyProperties(updateDTO, user);
        
        // 处理空字符串转 null（避免唯一约束冲突）
        if (user.getEmail() != null && user.getEmail().trim().isEmpty()) {
            user.setEmail(null);
        }
        if (user.getPhone() != null && user.getPhone().trim().isEmpty()) {
            user.setPhone(null);
        }
        
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);

        // 返回更新后的用户信息
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);
        return userInfoVO;
    }

    @Override
    public UserPublicVO getUserPublicInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.USER_NOT_FOUND);
        }

        // 将User转换为UserPublicVO（只包含公开信息）
        UserPublicVO userPublicVO = new UserPublicVO();
        BeanUtils.copyProperties(user, userPublicVO);
        return userPublicVO;
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, PasswordUpdateDTO passwordUpdateDTO) {
        // 查询用户是否存在
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.USER_NOT_FOUND);
        }

        // 验证旧密码
        if (!user.getPassword().equals(passwordUpdateDTO.getOldPassword())) {
            throw new PasswordErrorException(MessageConstant.OLD_PASSWORD_ERROR);
        }

        // 更新密码
        user.setPassword(passwordUpdateDTO.getNewPassword());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updatePassword(user);
    }

}