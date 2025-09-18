package com.example.rabbitmq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件上传信息模型
 * 
 * @author Demo
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadInfo {
    
    /**
     * 文件唯一标识
     */
    private String fileId;
    
    /**
     * 原始文件名
     */
    private String originalFileName;
    
    /**
     * 存储的文件名
     */
    private String storedFileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件类型/MIME类型
     */
    private String contentType;
    
    /**
     * MinIO存储桶名称
     */
    private String bucketName;
    
    /**
     * MinIO对象键名
     */
    private String objectKey;
    
    /**
     * 文件MD5哈希值
     */
    private String md5Hash;
    
    /**
     * 上传用户ID
     */
    private String uploadUserId;
    
    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
    
    /**
     * 文件状态
     */
    private FileStatus status;
    
    /**
     * 文件访问URL
     */
    private String accessUrl;
    
    /**
     * 文件描述
     */
    private String description;
    
    /**
     * 业务标签
     */
    private String businessTag;
    
    /**
     * 文件处理进度 (0-100)
     */
    private Integer processProgress;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 文件状态枚举
     */
    public enum FileStatus {
        /**
         * 上传中
         */
        UPLOADING,
        
        /**
         * 上传完成
         */
        UPLOADED,
        
        /**
         * 处理中
         */
        PROCESSING,
        
        /**
         * 处理完成
         */
        PROCESSED,
        
        /**
         * 处理失败
         */
        FAILED,
        
        /**
         * 已删除
         */
        DELETED
    }
}