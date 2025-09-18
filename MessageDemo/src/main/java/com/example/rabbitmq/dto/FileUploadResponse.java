package com.example.rabbitmq.dto;

import com.example.rabbitmq.model.FileUploadInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应DTO
 * 
 * @author Demo
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    
    /**
     * 操作是否成功
     */
    private Boolean success;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 文件信息
     */
    private FileUploadInfo fileInfo;
    
    /**
     * 错误代码
     */
    private String errorCode;
    
    /**
     * 处理耗时（毫秒）
     */
    private Long processingTime;
    
    /**
     * 创建成功响应
     */
    public static FileUploadResponse success(FileUploadInfo fileInfo, String message) {
        return FileUploadResponse.builder()
                .success(true)
                .message(message)
                .fileInfo(fileInfo)
                .build();
    }
    
    /**
     * 创建失败响应
     */
    public static FileUploadResponse failure(String message, String errorCode) {
        return FileUploadResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}