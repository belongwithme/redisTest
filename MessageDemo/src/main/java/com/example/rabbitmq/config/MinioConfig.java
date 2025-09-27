package com.example.rabbitmq.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * MinIO 配置类
 * 
 * @author Demo
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
@EnableConfigurationProperties(MinioConfig.MinioProperties.class)
public class MinioConfig {

    /**
     * 创建 MinIO 客户端
     */
    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    /**
     * MinIO 配置属性
     */
    @Data
    @ConfigurationProperties(prefix = "minio")
    public static class MinioProperties {
        
        /**
         * MinIO 服务地址
         */
        private String endpoint = "http://localhost:9000";
        
        /**
         * 访问密钥
         */
        private String accessKey = "minioadmin";
        
        /**
         * 秘密密钥
         */
        private String secretKey = "minioadmin";
        
        /**
         * 默认存储桶名称
         */
        private String defaultBucket = "file-storage";
        
        /**
         * 文件URL过期时间（秒）
         */
        private Integer urlExpiry = 7200;
        
        /**
         * 单个文件最大大小（字节）
         */
        private Long maxFileSize = 100L * 1024 * 1024; // 100MB
        
        /**
         * 允许的文件类型
         */
        private String[] allowedContentTypes = {
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "video/mp4", "video/avi", "video/mov",
            "application/pdf", "application/msword", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain", "application/zip", "application/rar"
        };
        
        /**
         * 分片上传阈值（字节）
         */
        private Long multipartThreshold = 5L * 1024 * 1024; // 5MB
        
        /**
         * 分片大小（字节）
         */
        private Long partSize = 5L * 1024 * 1024; // 5MB
    }
}