package com.xyz.service;

import com.xyz.dto.AdminLoginDTO;
import com.xyz.entity.Admin;

/**
 * 管理员服务接口
 */
public interface AdminService {

    /**
     * 管理员登录
     */
    Admin login(AdminLoginDTO loginDTO);

    /**
     * 根据ID获取管理员信息
     */
    Admin getAdminById(Long adminId);
}
