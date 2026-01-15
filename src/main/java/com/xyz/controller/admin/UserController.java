package com.xyz.controller.admin;

import com.xyz.constant.MessageConstant;
import com.xyz.service.UserService;
import com.xyz.vo.PageResult;
import com.xyz.vo.Result;
import com.xyz.vo.UserDetailVO;
import com.xyz.vo.UserListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员-用户管理控制器
 */
@RestController("adminUserController")
@RequestMapping("/admin/user")
@Tag(name = "用户管理")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 游标分页查询用户列表（支持无限滚动）
     */
    @GetMapping("/list")
    @Operation(summary = "游标分页查询用户列表")
    public Result<PageResult<UserListVO>> getUserList(
            @Parameter(description = "游标（首次请求不传或传null）") @RequestParam(required = false) Long cursor,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        try {
            PageResult<UserListVO> result = userService.getUserList(cursor, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查看用户详细信息
     */
    @GetMapping("/{userId}")
    @Operation(summary = "查看用户详细信息")
    public Result<UserDetailVO> getUserDetail(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        try {
            UserDetailVO userDetail = userService.getUserDetail(userId);
            return Result.success(userDetail);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 封禁用户
     */
    @PutMapping("/{userId}/ban")
    @Operation(summary = "封禁用户")
    public Result<String> banUser(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        try {
            userService.banUser(userId);
            return Result.success(MessageConstant.USER_BAN_SUCCESS);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 解封用户
     */
    @PutMapping("/{userId}/unban")
    @Operation(summary = "解封用户")
    public Result<String> unbanUser(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        try {
            userService.unbanUser(userId);
            return Result.success(MessageConstant.USER_UNBAN_SUCCESS);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
