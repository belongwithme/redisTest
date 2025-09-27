package com.example.rabbitmq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 文件处理消息模型
 * 用于RabbitMQ消息传递
 * 
 * @author Demo
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileProcessMessage {
    
    /**
     * 消息唯一标识
     */
    private String messageId;
    
    /**
     * 文件信息
     */
    private FileUploadInfo fileInfo;
    
    /**
     * 处理类型
     */
    private ProcessType processType;
    
    /**
     * 消息创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;
    
    /**
     * 处理参数
     */
    private Map<String, Object> processParams;
    
    /**
     * 业务上下文
     */
    private String businessContext;
    
    /**
     * 回调URL
     */
    private String callbackUrl;
    
    /**
     * 优先级 (1-10, 10最高)
     */
    private Integer priority;
    
    /**
     * 处理类型枚举
     */
    public enum ProcessType {
        /**
         * 文件上传完成通知
         */
        UPLOAD_COMPLETED,
        
        /**
         * 文件安全扫描
         */
        SECURITY_SCAN,
        
        /**
         * 图片压缩
         */
        IMAGE_COMPRESS,
        
        /**
         * 视频转码
         */
        VIDEO_TRANSCODE,
        
        /**
         * 文档解析
         */
        DOCUMENT_PARSE,
        
        /**
         * 缩略图生成
         */
        THUMBNAIL_GENERATE,
        
        /**
         * 内容审核
         */
        CONTENT_AUDIT,
        
        /**
         * 备份处理
         */
        BACKUP_PROCESS,
        
        /**
         * 清理过期文件
         */
        CLEANUP_EXPIRED
    }
}