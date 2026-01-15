package com.xyz.service.impl;

import com.xyz.constant.MessageConstant;
import com.xyz.dto.AdminLoginDTO;
import com.xyz.entity.Admin;
import com.xyz.exception.AccountBannedException;
import com.xyz.exception.AccountNotFoundException;
import com.xyz.exception.PasswordErrorException;
import com.xyz.mapper.AdminMapper;
import com.xyz.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 管理员服务实现类
 */
@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    @Transactional
    public Admin login(AdminLoginDTO loginDTO) {
        log.info("管理员登录: username={}", loginDTO.getUsername());
        
        // 根据用户名查询管理员
        Admin adminExist = adminMapper.findByUsername(loginDTO.getUsername());
        if (adminExist == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_EXIST);
        }

        // 验证密码
        Admin admin = adminMapper.findByUsernameAndPassword(loginDTO.getUsername(), loginDTO.getPassword());
        if (admin == null) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        // 检查账号状态
        if (admin.getStatus() == 0) {
            throw new AccountBannedException(MessageConstant.ACCOUNT_BANNED);
        }

        // 更新最后登录时间
        admin.setLastLoginTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        adminMapper.updateLastLoginTime(admin);

        log.info("管理员登录成功: id={}, username={}", admin.getId(), admin.getUsername());
        return admin;
    }

    @Override
    public Admin getAdminById(Long adminId) {
        Admin admin = adminMapper.findById(adminId);
        if (admin == null) {
            throw new AccountNotFoundException("管理员不存在");
        }
        return admin;
    }
}
