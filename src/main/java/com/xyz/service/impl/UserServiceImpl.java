package com.xyz.service.impl;

import com.xyz.constant.MessageConstant;
import com.xyz.dto.UserLoginDTO;
import com.xyz.dto.UserRegisterDTO;
import com.xyz.entity.User;
import com.xyz.exception.AccountNotFoundException;
import com.xyz.exception.PasswordErrorException;
import com.xyz.mapper.UserMapper;
import com.xyz.service.UserService;

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
    

}