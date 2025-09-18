package com.example.rabbitmq.producer;

import com.example.rabbitmq.model.FileProcessMessage;
import com.example.rabbitmq.model.FileUploadInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 文件消息生产者
 * 负责发送文件相关的消息到RabbitMQ
 * 
 * @author Demo
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.file:file.exchange}")
    private String fileExchange;

    @Value("${rabbitmq.routing.file-upload:file.upload}")
    private String fileUploadRoutingKey;

    @Value("${rabbitmq.routing.file-process:file.process}")
    private String fileProcessRoutingKey;

    @Value("${rabbitmq.routing.file-notify:file.notify}")
    private String fileNotifyRoutingKey;

    /**
     * 发送文件上传完成消息
     */
    public void sendFileUploadedMessage(FileUploadInfo fileInfo) {
        try {
            FileProcessMessage message = FileProcessMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .fileInfo(fileInfo)
                    .processType(FileProcessMessage.ProcessType.UPLOAD_COMPLETED)
                    .createTime(LocalDateTime.now())
                    .retryCount(0)
                    .maxRetryCount(3)
                    .priority(5)
                    .build();

            rabbitTemplate.convertAndSend(fileExchange, fileUploadRoutingKey, message, msg -> {
                msg.getMessageProperties().setPriority(message.getPriority());
                msg.getMessageProperties().setDeliveryMode(MessageProperties.DEFAULT_DELIVERY_MODE);
                return msg;
            });

            log.info("Sent file upload completed message: fileId={}, fileName={}", 
                    fileInfo.getFileId(), fileInfo.getOriginalFileName());
                    
        } catch (Exception e) {
            log.error("Failed to send file upload completed message: fileId={}", 
                    fileInfo.getFileId(), e);
            throw new RuntimeException("Failed to send file upload message", e);
        }
    }

    /**
     * 发送文件处理消息
     */
    public void sendFileProcessMessage(FileUploadInfo fileInfo, 
                                     FileProcessMessage.ProcessType processType,
                                     Map<String, Object> processParams,
                                     Integer priority) {
        try {
            FileProcessMessage message = FileProcessMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .fileInfo(fileInfo)
                    .processType(processType)
                    .createTime(LocalDateTime.now())
                    .retryCount(0)
                    .maxRetryCount(5) // 处理任务可能需要更多重试
                    .processParams(processParams)
                    .priority(priority != null ? priority : 5)
                    .build();

            String routingKey = fileProcessRoutingKey + "." + processType.name().toLowerCase();

            rabbitTemplate.convertAndSend(fileExchange, routingKey, message, msg -> {
                msg.getMessageProperties().setPriority(message.getPriority());
                msg.getMessageProperties().setDeliveryMode(MessageProperties.DEFAULT_DELIVERY_MODE);
                // 设置处理超时时间
                msg.getMessageProperties().setExpiration("3600000"); // 1小时
                return msg;
            });

            log.info("Sent file process message: fileId={}, processType={}, priority={}", 
                    fileInfo.getFileId(), processType, priority);
                    
        } catch (Exception e) {
            log.error("Failed to send file process message: fileId={}, processType={}", 
                    fileInfo.getFileId(), processType, e);
            throw new RuntimeException("Failed to send file process message", e);
        }
    }

    /**
     * 发送图片压缩消息
     */
    public void sendImageCompressMessage(FileUploadInfo fileInfo, Map<String, Object> compressParams) {
        sendFileProcessMessage(fileInfo, FileProcessMessage.ProcessType.IMAGE_COMPRESS, 
                             compressParams, 7);
    }

    /**
     * 发送视频转码消息
     */
    public void sendVideoTranscodeMessage(FileUploadInfo fileInfo, Map<String, Object> transcodeParams) {
        sendFileProcessMessage(fileInfo, FileProcessMessage.ProcessType.VIDEO_TRANSCODE, 
                             transcodeParams, 8);
    }

    /**
     * 发送文档解析消息
     */
    public void sendDocumentParseMessage(FileUploadInfo fileInfo, Map<String, Object> parseParams) {
        sendFileProcessMessage(fileInfo, FileProcessMessage.ProcessType.DOCUMENT_PARSE, 
                             parseParams, 6);
    }

    /**
     * 发送安全扫描消息
     */
    public void sendSecurityScanMessage(FileUploadInfo fileInfo) {
        sendFileProcessMessage(fileInfo, FileProcessMessage.ProcessType.SECURITY_SCAN, 
                             null, 10); // 安全扫描最高优先级
    }

    /**
     * 发送缩略图生成消息
     */
    public void sendThumbnailGenerateMessage(FileUploadInfo fileInfo, Map<String, Object> thumbnailParams) {
        sendFileProcessMessage(fileInfo, FileProcessMessage.ProcessType.THUMBNAIL_GENERATE, 
                             thumbnailParams, 5);
    }

    /**
     * 发送文件处理完成通知
     */
    public void sendFileProcessCompletedNotification(FileUploadInfo fileInfo, 
                                                   FileProcessMessage.ProcessType processType,
                                                   boolean success,
                                                   String message) {
        try {
            FileProcessMessage notificationMessage = FileProcessMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .fileInfo(fileInfo)
                    .processType(processType)
                    .createTime(LocalDateTime.now())
                    .businessContext(success ? "SUCCESS" : "FAILED")
                    .retryCount(0)
                    .maxRetryCount(3)
                    .priority(success ? 3 : 8) // 失败通知优先级更高
                    .build();

            rabbitTemplate.convertAndSend(fileExchange, fileNotifyRoutingKey, notificationMessage);

            log.info("Sent file process notification: fileId={}, processType={}, success={}", 
                    fileInfo.getFileId(), processType, success);
                    
        } catch (Exception e) {
            log.error("Failed to send file process notification: fileId={}, processType={}", 
                    fileInfo.getFileId(), processType, e);
        }
    }

    /**
     * 发送批量文件清理消息
     */
    public void sendBatchCleanupMessage(String businessTag, int daysOld) {
        try {
            FileProcessMessage message = FileProcessMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .processType(FileProcessMessage.ProcessType.CLEANUP_EXPIRED)
                    .createTime(LocalDateTime.now())
                    .businessContext(businessTag)
                    .retryCount(0)
                    .maxRetryCount(3)
                    .priority(2) // 清理任务低优先级
                    .build();

            message.getProcessParams().put("daysOld", daysOld);
            message.getProcessParams().put("businessTag", businessTag);

            rabbitTemplate.convertAndSend(fileExchange, 
                    fileProcessRoutingKey + ".cleanup", message);

            log.info("Sent batch cleanup message: businessTag={}, daysOld={}", 
                    businessTag, daysOld);
                    
        } catch (Exception e) {
            log.error("Failed to send batch cleanup message: businessTag={}", businessTag, e);
        }
    }
}