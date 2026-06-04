package com.interview.service;

import com.aliyun.oss.OSS;
import com.interview.common.exception.BusinessException;
import com.interview.config.OssConfig;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 通用文件上传服务（基于阿里云 OSS）
 *
 * @Author Diamond
 * @Create 2026/6/4
 */
@Service
public class FileService {

    /**
     * 默认允许的图片类型
     */
    private static final List<String> IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    /**
     * 默认允许的所有文件类型
     */
    private static final List<String> ALL_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    /**
     * 默认文件大小限制：10MB
     */
    private static final long DEFAULT_MAX_SIZE = 10 * 1024 * 1024;

    @Resource
    private OSS ossClient;

    @Resource
    private OssConfig ossConfig;

    /**
     * 上传图片文件
     *
     * @param file    上传文件
     * @param folder  OSS 存储目录（如 avatar、resume）
     * @return 文件访问 URL
     */
    public String uploadImage(MultipartFile file, String folder) {
        validateFile(file, IMAGE_TYPES, DEFAULT_MAX_SIZE);
        return doUpload(file, folder);
    }

    /**
     * 上传通用文件
     *
     * @param file    上传文件
     * @param folder  OSS 存储目录
     * @return 文件访问 URL
     */
    public String uploadFile(MultipartFile file, String folder) {
        validateFile(file, ALL_TYPES, DEFAULT_MAX_SIZE);
        return doUpload(file, folder);
    }

    /**
     * 上传通用文件（自定义大小限制）
     *
     * @param file    上传文件
     * @param folder  OSS 存储目录
     * @param maxSize 最大文件大小（字节）
     * @return 文件访问 URL
     */
    public String uploadFile(MultipartFile file, String folder, long maxSize) {
        validateFile(file, ALL_TYPES, maxSize);
        return doUpload(file, folder);
    }

    /**
     * 删除 OSS 文件
     *
     * @param fileUrl 文件访问 URL
     */
    public void deleteFile(String fileUrl) {
        String key = extractKey(fileUrl);
        if (key != null) {
            ossClient.deleteObject(ossConfig.getBucketName(), key);
        }
    }

    /**
     * 校验文件类型和大小
     */
    private void validateFile(MultipartFile file, List<String> allowedTypes, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new BusinessException("不支持的文件类型");
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小超出限制");
        }
    }

    /**
     * 执行上传
     */
    private String doUpload(MultipartFile file, String folder) {
        // 生成唯一文件名：folder/yyyy/MM/dd/uuid.ext
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String objectKey = folder + "/" + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + ext;

        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(ossConfig.getBucketName(), objectKey, inputStream);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败");
        }

        return ossConfig.getUrlPrefix() + "/" + objectKey;
    }

    /**
     * 从 URL 中提取 OSS objectKey
     */
    private String extractKey(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(ossConfig.getUrlPrefix())) {
            return null;
        }
        return fileUrl.substring(ossConfig.getUrlPrefix().length() + 1);
    }

}
