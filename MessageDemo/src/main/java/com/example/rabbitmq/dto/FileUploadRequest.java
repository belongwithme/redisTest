package com.example.rabbitmq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * 文件上传请求DTO
 * 
 * @author Demo
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequest {
    
    /**
     * 上传用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String uploadUserId;
    
    /**
     * 业务标签
     */
    @Size(max = 50, message = "业务标签长度不能超过50字符")
    private String businessTag;
    
    /**
     * 文件描述
     */
    @Size(max = 500, message = "文件描述长度不能超过500字符")
    private String description;
    
    /**
     * 是否启用异步处理
     */
    @Builder.Default
    private Boolean enableAsyncProcess = true;
    
    /**
     * 处理优先级 (1-10, 10最高)
     */
    @Builder.Default
    private Integer priority = 5;
    
    /**
     * 回调URL
     */
    private String callbackUrl;
    
    /**
     * 自定义处理参数
     */
    private Map<String, Object> processParams;
    
    /**
     * 覆盖已存在的文件
     */
    @Builder.Default
    private Boolean overwrite = false;
}