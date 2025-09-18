package com.example.rabbitmq.service;

import com.example.rabbitmq.config.MinioConfig;
import com.example.rabbitmq.model.FileUploadInfo;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 文件存储服务
 * 负责与MinIO交互进行文件存储
 * 
 * @author Demo
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;
    private final MinioConfig.MinioProperties minioProperties;

    /**
     * 初始化存储桶
     */
    public void initializeBucket(String bucketName) {
        try {
            // 检查存储桶是否存在
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            
            if (!exists) {
                // 创建存储桶
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Created bucket: {}", bucketName);
                
                // 设置存储桶策略（公开读取）
                String policy = generateBucketPolicy(bucketName);
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(policy)
                                .build());
                log.info("Set bucket policy for: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize bucket: {}", bucketName, e);
            throw new RuntimeException("Failed to initialize bucket: " + bucketName, e);
        }
    }

    /**
     * 上传文件
     */
    public FileUploadInfo uploadFile(MultipartFile file, String uploadUserId, String businessTag) {
        try {
            // 验证文件
            validateFile(file);
            
            // 初始化默认存储桶
            initializeBucket(minioProperties.getDefaultBucket());
            
            // 生成文件信息
            String fileId = generateFileId();
            String originalFileName = file.getOriginalFilename();
            String storedFileName = generateStoredFileName(originalFileName);
            String objectKey = generateObjectKey(businessTag, storedFileName);
            
            // 计算文件MD5
            String md5Hash = calculateMD5(file.getInputStream());
            
            // 上传文件到MinIO
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(minioProperties.getDefaultBucket())
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();
            
            ObjectWriteResponse response = minioClient.putObject(putObjectArgs);
            log.info("File uploaded successfully: bucket={}, object={}, etag={}", 
                    minioProperties.getDefaultBucket(), objectKey, response.etag());
            
            // 生成访问URL
            String accessUrl = generatePresignedUrl(minioProperties.getDefaultBucket(), objectKey);
            
            // 构建文件信息
            return FileUploadInfo.builder()
                    .fileId(fileId)
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .bucketName(minioProperties.getDefaultBucket())
                    .objectKey(objectKey)
                    .md5Hash(md5Hash)
                    .uploadUserId(uploadUserId)
                    .uploadTime(LocalDateTime.now())
                    .status(FileUploadInfo.FileStatus.UPLOADED)
                    .accessUrl(accessUrl)
                    .businessTag(businessTag)
                    .processProgress(0)
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    /**
     * 下载文件
     */
    public InputStream downloadFile(String bucketName, String objectKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build());
        } catch (Exception e) {
            log.error("Failed to download file: bucket={}, object={}", bucketName, objectKey, e);
            throw new RuntimeException("Failed to download file", e);
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String bucketName, String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build());
            log.info("File deleted successfully: bucket={}, object={}", bucketName, objectKey);
        } catch (Exception e) {
            log.error("Failed to delete file: bucket={}, object={}", bucketName, objectKey, e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    /**
     * 生成预签名URL
     */
    public String generatePresignedUrl(String bucketName, String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectKey)
                            .expiry(minioProperties.getUrlExpiry())
                            .build());
        } catch (Exception e) {
            log.error("Failed to generate presigned URL: bucket={}, object={}", bucketName, objectKey, e);
            return null;
        }
    }

    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String bucketName, String objectKey) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (file.getSize() > minioProperties.getMaxFileSize()) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new IllegalArgumentException("File type not allowed: " + contentType);
        }
    }

    /**
     * 检查是否为允许的文件类型
     */
    private boolean isAllowedContentType(String contentType) {
        String[] allowedTypes = minioProperties.getAllowedContentTypes();
        for (String allowedType : allowedTypes) {
            if (contentType.equals(allowedType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成文件ID
     */
    private String generateFileId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成存储文件名
     */
    private String generateStoredFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }

    /**
     * 生成对象键名
     */
    private String generateObjectKey(String businessTag, String storedFileName) {
        String prefix = businessTag != null ? businessTag : "default";
        String datePath = LocalDateTime.now().toString().substring(0, 10); // yyyy-MM-dd
        return String.format("%s/%s/%s", prefix, datePath, storedFileName);
    }

    /**
     * 计算文件MD5
     */
    private String calculateMD5(InputStream inputStream) {
        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(bytes);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Failed to calculate MD5", e);
            return null;
        }
    }

    /**
     * 生成存储桶策略
     */
    private String generateBucketPolicy(String bucketName) {
        return String.format("""
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Principal": {"AWS": "*"},
                            "Action": ["s3:GetObject"],
                            "Resource": ["arn:aws:s3:::%s/*"]
                        }
                    ]
                }
                """, bucketName);
    }
}