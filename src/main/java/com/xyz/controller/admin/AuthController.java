package com.xyz.controller.admin;

import com.xyz.dto.AdminLoginDTO;
import com.xyz.entity.Admin;
import com.xyz.properties.JwtProperties;
import com.xyz.service.AdminService;
import com.xyz.util.JwtUtil;
import com.xyz.vo.AdminLoginVO;
import com.xyz.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-认证控制器
 */
@Slf4j
@RestController("adminAuthController")
@RequestMapping("/admin")
@Tag(name = "管理员认证")
public class AuthController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public Result<AdminLoginVO> login(@RequestBody AdminLoginDTO loginDTO) {
        try {
            log.info("管理员登录请求: username={}", loginDTO.getUsername());
            
            // 验证登录
            Admin admin = adminService.login(loginDTO);

            // 生成JWT token
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", admin.getId());
            claims.put("type", "admin"); // 标识为管理员token
            String token = JwtUtil.generateToken(
                jwtProperties.getExpirationTime(),
                jwtProperties.getSecretKey(),
                claims
            );

            // 构建返回VO
            AdminLoginVO adminLoginVO = AdminLoginVO.builder()
                    .id(admin.getId())
                    .username(admin.getUsername())
                    .realName(admin.getRealName())
                    .token(token)
                    .build();

            log.info("管理员登录成功: id={}, username={}", admin.getId(), admin.getUsername());
            return Result.success("登录成功", adminLoginVO);
        } catch (Exception e) {
            log.error("管理员登录失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    @Operation(summary = "管理员退出登录")
    public Result<String> logout() {
        // JWT是无状态的，退出登录由前端删除token即可
        // 如果需要服务端控制，可以将token加入黑名单（使用Redis）
        log.info("管理员退出登录");
        return Result.success("退出成功");
    }
}
