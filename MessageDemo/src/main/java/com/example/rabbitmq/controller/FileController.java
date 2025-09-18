package com.example.rabbitmq.controller;

import com.example.rabbitmq.dto.FileUploadRequest;
import com.example.rabbitmq.dto.FileUploadResponse;
import com.example.rabbitmq.model.FileUploadInfo;
import com.example.rabbitmq.model.FileProcessMessage;
import com.example.rabbitmq.producer.FileMessageProducer;
import com.example.rabbitmq.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 * 提供文件上传、下载、查询等API
 * 
 * @author Demo
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Validated
public class FileController {

    private final FileStorageService fileStorageService;
    private final FileMessageProducer fileMessageProducer;

    /**
     * 单文件上传
     */
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @ModelAttribute @Valid FileUploadRequest request) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Received file upload request: fileName={}, size={}, userId={}", 
                    file.getOriginalFilename(), file.getSize(), request.getUploadUserId());

            // 上传文件到MinIO
            FileUploadInfo fileInfo = fileStorageService.uploadFile(
                    file, request.getUploadUserId(), request.getBusinessTag());
            
            // 设置额外信息
            fileInfo.setDescription(request.getDescription());
            
            // 如果启用异步处理，发送上传完成消息到RabbitMQ
            if (request.getEnableAsyncProcess()) {
                fileMessageProducer.sendFileUploadedMessage(fileInfo);
                log.info("Sent async processing message for file: {}", fileInfo.getFileId());
            }
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            FileUploadResponse response = FileUploadResponse.success(fileInfo, 
                    "File uploaded successfully");
            response.setProcessingTime(processingTime);
            
            log.info("File upload completed: fileId={}, time={}ms", 
                    fileInfo.getFileId(), processingTime);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid file upload request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(FileUploadResponse.failure(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FileUploadResponse.failure("File upload failed", "UPLOAD_ERROR"));
        }
    }

    /**
     * 批量文件上传
     */
    @PostMapping("/upload/batch")
    public ResponseEntity<Map<String, Object>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @ModelAttribute @Valid FileUploadRequest request) {
        
        Map<String, Object> result = new HashMap<>();
        Map<String, FileUploadResponse> results = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        
        try {
            log.info("Received batch file upload request: fileCount={}, userId={}", 
                    files.length, request.getUploadUserId());

            for (MultipartFile file : files) {
                try {
                    FileUploadInfo fileInfo = fileStorageService.uploadFile(
                            file, request.getUploadUserId(), request.getBusinessTag());
                    
                    fileInfo.setDescription(request.getDescription());
                    
                    if (request.getEnableAsyncProcess()) {
                        fileMessageProducer.sendFileUploadedMessage(fileInfo);
                    }
                    
                    results.put(file.getOriginalFilename(), 
                            FileUploadResponse.success(fileInfo, "Upload successful"));
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("Failed to upload file in batch: {}", file.getOriginalFilename(), e);
                    results.put(file.getOriginalFilename(), 
                            FileUploadResponse.failure(e.getMessage(), "UPLOAD_ERROR"));
                    failCount++;
                }
            }
            
            result.put("totalCount", files.length);
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("results", results);
            
            log.info("Batch upload completed: total={}, success={}, fail={}", 
                    files.length, successCount, failCount);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Batch file upload failed", e);
            result.put("error", "Batch upload failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 文件下载
     */
    @GetMapping("/download/{bucketName}/{objectKey:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable @NotBlank String bucketName,
            @PathVariable @NotBlank String objectKey) {
        
        try {
            log.info("Downloading file: bucket={}, object={}", bucketName, objectKey);

            if (!fileStorageService.fileExists(bucketName, objectKey)) {
                return ResponseEntity.notFound().build();
            }

            InputStream inputStream = fileStorageService.downloadFile(bucketName, objectKey);
            Resource resource = new InputStreamResource(inputStream);

            String filename = objectKey.substring(objectKey.lastIndexOf("/") + 1);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Failed to download file: bucket={}, object={}", bucketName, objectKey, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取文件访问URL
     */
    @GetMapping("/url/{bucketName}/{objectKey:.+}")
    public ResponseEntity<Map<String, String>> getFileUrl(
            @PathVariable @NotBlank String bucketName,
            @PathVariable @NotBlank String objectKey) {
        
        try {
            if (!fileStorageService.fileExists(bucketName, objectKey)) {
                return ResponseEntity.notFound().build();
            }

            String presignedUrl = fileStorageService.generatePresignedUrl(bucketName, objectKey);
            
            Map<String, String> result = new HashMap<>();
            result.put("url", presignedUrl);
            result.put("bucketName", bucketName);
            result.put("objectKey", objectKey);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Failed to generate file URL: bucket={}, object={}", bucketName, objectKey, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{bucketName}/{objectKey:.+}")
    public ResponseEntity<Map<String, String>> deleteFile(
            @PathVariable @NotBlank String bucketName,
            @PathVariable @NotBlank String objectKey) {
        
        try {
            log.info("Deleting file: bucket={}, object={}", bucketName, objectKey);

            if (!fileStorageService.fileExists(bucketName, objectKey)) {
                return ResponseEntity.notFound().build();
            }

            fileStorageService.deleteFile(bucketName, objectKey);
            
            Map<String, String> result = new HashMap<>();
            result.put("message", "File deleted successfully");
            result.put("bucketName", bucketName);
            result.put("objectKey", objectKey);
            
            log.info("File deleted successfully: bucket={}, object={}", bucketName, objectKey);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Failed to delete file: bucket={}, object={}", bucketName, objectKey, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete file");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 手动触发文件处理
     */
    @PostMapping("/process/{bucketName}/{objectKey:.+}")
    public ResponseEntity<Map<String, String>> triggerFileProcess(
            @PathVariable @NotBlank String bucketName,
            @PathVariable @NotBlank String objectKey,
            @RequestParam String processType,
            @RequestParam(required = false) Integer priority,
            @RequestBody(required = false) Map<String, Object> processParams) {
        
        try {
            log.info("Triggering file process: bucket={}, object={}, type={}", 
                    bucketName, objectKey, processType);

            if (!fileStorageService.fileExists(bucketName, objectKey)) {
                return ResponseEntity.notFound().build();
            }

            // 构造文件信息（简化版本，实际应该从数据库查询）
            FileUploadInfo fileInfo = FileUploadInfo.builder()
                    .bucketName(bucketName)
                    .objectKey(objectKey)
                    .status(FileUploadInfo.FileStatus.PROCESSING)
                    .build();

            // 发送处理消息
            try {
                FileProcessMessage.ProcessType type = FileProcessMessage.ProcessType.valueOf(
                        processType.toUpperCase());
                fileMessageProducer.sendFileProcessMessage(fileInfo, type, processParams, priority);
                
                Map<String, String> result = new HashMap<>();
                result.put("message", "File processing triggered successfully");
                result.put("processType", processType);
                
                return ResponseEntity.ok(result);
                
            } catch (IllegalArgumentException e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid process type: " + processType);
                return ResponseEntity.badRequest().body(error);
            }
            
        } catch (Exception e) {
            log.error("Failed to trigger file process: bucket={}, object={}, type={}", 
                    bucketName, objectKey, processType, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to trigger file process");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "File Service");
        result.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(result);
    }

    /**
     * 获取支持的文件类型
     */
    @GetMapping("/supported-types")
    public ResponseEntity<Map<String, Object>> getSupportedTypes() {
        Map<String, Object> result = new HashMap<>();
        
        // 这里应该从配置中读取
        String[] supportedTypes = {
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "video/mp4", "video/avi", "video/mov",
            "application/pdf", "application/msword", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain", "application/zip"
        };
        
        result.put("supportedContentTypes", supportedTypes);
        result.put("maxFileSize", "100MB");
        result.put("multipartThreshold", "5MB");
        
        return ResponseEntity.ok(result);
    }
}