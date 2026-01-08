package com.xyz.controller;

import com.xyz.util.AliOssUtil;
import com.xyz.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用接口（文件上传等）
 */
@Slf4j
@RestController
@RequestMapping("/common")
@Tag(name = "通用接口", description = "文件上传等通用功能")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 上传头像（单张）
     */
    @PostMapping("/upload/avatar")
    @Operation(summary = "上传头像")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }
        
        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }
        
        // 校验文件大小（最大2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.error("头像文件大小不能超过2MB");
        }
        
        String url = aliOssUtil.uploadAvatar(file);
        return Result.success("上传成功", url);
    }

    /**
     * 上传商品图片（支持多张）
     */
    @PostMapping("/upload/goods")
    @Operation(summary = "上传商品图片（支持多张）")
    public Result<List<String>> uploadGoodsImages(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Result.error("请选择要上传的文件");
        }
        
        if (files.length > 9) {
            return Result.error("最多上传9张图片");
        }
        
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            
            // 校验文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只能上传图片文件");
            }
            
            // 校验文件大小（单张最大5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("单张图片大小不能超过5MB");
            }
            
            String url = aliOssUtil.uploadGoodsImage(file);
            urls.add(url);
        }
        
        return Result.success("上传成功", urls);
    }
}
