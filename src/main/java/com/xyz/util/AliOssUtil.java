package com.xyz.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.xyz.properties.AliOssProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云 OSS 工具类
 */
@Slf4j
@Component
public class AliOssUtil {

    @Autowired
    private AliOssProperties aliOssProperties;

    /**
     * 上传文件到 OSS
     *
     * @param file   文件
     * @param folder 存储文件夹（如 avatar、goods）
     * @return 文件访问URL
     */
    public String upload(MultipartFile file, String folder) {
        // 获取配置
        String endpoint = aliOssProperties.getEndpoint();
        String accessKeyId = aliOssProperties.getAccessKeyId();
        String accessKeySecret = aliOssProperties.getAccessKeySecret();
        String bucketName = aliOssProperties.getBucketName();

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String objectName = folder + "/" + UUID.randomUUID().toString().replace("-", "") + extension;

        OSS ossClient = null;
        try {
            // 创建 OSS 客户端
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 设置文件元信息
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            // 上传文件
            InputStream inputStream = file.getInputStream();
            ossClient.putObject(bucketName, objectName, inputStream, metadata);

            // 返回访问URL
            String url = "https://" + bucketName + "." + endpoint + "/" + objectName;
            log.info("文件上传成功: {}", url);
            return url;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 上传头像
     */
    public String uploadAvatar(MultipartFile file) {
        return upload(file, "avatar");
    }

    /**
     * 上传商品图片
     */
    public String uploadGoodsImage(MultipartFile file) {
        return upload(file, "goods");
    }

    /**
     * 上传聊天图片
     */
    public String uploadChatImage(MultipartFile file) {
        return upload(file, "chat");
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     */
    public void delete(String fileUrl) {
        String endpoint = aliOssProperties.getEndpoint();
        String accessKeyId = aliOssProperties.getAccessKeyId();
        String accessKeySecret = aliOssProperties.getAccessKeySecret();
        String bucketName = aliOssProperties.getBucketName();

        // 从URL提取objectName
        String prefix = "https://" + bucketName + "." + endpoint + "/";
        if (!fileUrl.startsWith(prefix)) {
            log.warn("无效的文件URL: {}", fileUrl);
            return;
        }
        String objectName = fileUrl.substring(prefix.length());

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            ossClient.deleteObject(bucketName, objectName);
            log.info("文件删除成功: {}", fileUrl);
        } catch (Exception e) {
            log.error("文件删除失败", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
