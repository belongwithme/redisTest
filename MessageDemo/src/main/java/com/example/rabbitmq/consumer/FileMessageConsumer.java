package com.example.rabbitmq.consumer;

import com.example.rabbitmq.model.FileProcessMessage;
import com.example.rabbitmq.model.FileUploadInfo;
import com.example.rabbitmq.producer.FileMessageProducer;
import com.example.rabbitmq.service.FileStorageService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 文件消息消费者
 * 负责处理文件相关的消息
 * 
 * @author Demo
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileMessageConsumer {

    private final FileStorageService fileStorageService;
    private final FileMessageProducer fileMessageProducer;

    /**
     * 处理文件上传完成消息
     */
    @RabbitListener(queues = "${rabbitmq.queue.file-upload:file.upload.queue}")
    public void handleFileUploadCompleted(@Payload FileProcessMessage message,
                                        Channel channel,
                                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("Processing file upload completed message: messageId={}, fileId={}", 
                    message.getMessageId(), message.getFileInfo().getFileId());

            FileUploadInfo fileInfo = message.getFileInfo();
            
            // 根据文件类型自动触发相应的处理任务
            String contentType = fileInfo.getContentType();
            
            if (contentType.startsWith("image/")) {
                // 图片文件 - 触发缩略图生成和压缩
                fileMessageProducer.sendThumbnailGenerateMessage(fileInfo, Map.of(
                    "width", 200,
                    "height", 200,
                    "quality", 0.8
                ));
                
                if (fileInfo.getFileSize() > 1024 * 1024) { // 大于1MB的图片进行压缩
                    fileMessageProducer.sendImageCompressMessage(fileInfo, Map.of(
                        "quality", 0.7,
                        "maxWidth", 1920,
                        "maxHeight", 1080
                    ));
                }
            } else if (contentType.startsWith("video/")) {
                // 视频文件 - 触发转码
                fileMessageProducer.sendVideoTranscodeMessage(fileInfo, Map.of(
                    "format", "mp4",
                    "resolution", "720p",
                    "bitrate", "1000k"
                ));
            } else if (contentType.contains("pdf") || contentType.contains("document")) {
                // 文档文件 - 触发解析
                fileMessageProducer.sendDocumentParseMessage(fileInfo, Map.of(
                    "extractText", true,
                    "generatePreview", true
                ));
            }
            
            // 对所有文件进行安全扫描
            fileMessageProducer.sendSecurityScanMessage(fileInfo);
            
            // 手动确认消息
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException e) {
                log.error("Failed to ack message", e);
            }
            log.info("File upload completed message processed successfully: fileId={}", 
                    fileInfo.getFileId());
                    
        } catch (Exception e) {
            log.error("Failed to process file upload completed message: messageId={}", 
                    message.getMessageId(), e);
            try {
                // 重试逻辑
                if (message.getRetryCount() < message.getMaxRetryCount()) {
                    message.setRetryCount(message.getRetryCount() + 1);
                    // 可以重新发送到队列或延迟队列
                    channel.basicNack(deliveryTag, false, true);
                } else {
                    // 超过最大重试次数，发送到死信队列
                    channel.basicNack(deliveryTag, false, false);
                }
            } catch (Exception ex) {
                log.error("Failed to handle message retry", ex);
            }
        }
    }

    /**
     * 处理图片压缩消息
     */
    @RabbitListener(queues = "${rabbitmq.queue.file-process:file.process.queue}", 
                   containerFactory = "rabbitListenerContainerFactory")
    public void handleImageCompress(@Payload FileProcessMessage message,
                                  Channel channel,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        if (message.getProcessType() != FileProcessMessage.ProcessType.IMAGE_COMPRESS) {
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException e) {
                log.error("Failed to ack message", e);
            }
            return;
        }

        try {
            log.info("Processing image compress message: messageId={}, fileId={}", 
                    message.getMessageId(), message.getFileInfo().getFileId());

            FileUploadInfo fileInfo = message.getFileInfo();
            
            // 模拟图片压缩处理
            simulateImageCompress(fileInfo, message.getProcessParams());
            
            // 更新文件状态
            fileInfo.setStatus(FileUploadInfo.FileStatus.PROCESSED);
            fileInfo.setProcessProgress(100);
            
            // 发送处理完成通知
            fileMessageProducer.sendFileProcessCompletedNotification(
                fileInfo, FileProcessMessage.ProcessType.IMAGE_COMPRESS, true, 
                "Image compression completed successfully");
            
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException e) {
                log.error("Failed to ack message", e);
            }
            log.info("Image compress completed: fileId={}", fileInfo.getFileId());
            
        } catch (Exception e) {
            log.error("Failed to process image compress: messageId={}", 
                    message.getMessageId(), e);
            handleProcessError(message, channel, deliveryTag, e);
        }
    }

    /**
     * 处理视频转码消息
     */
    @RabbitListener(queues = "${rabbitmq.queue.file-process:file.process.queue}")
    public void handleVideoTranscode(@Payload FileProcessMessage message,
                                   Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        if (message.getProcessType() != FileProcessMessage.ProcessType.VIDEO_TRANSCODE) {
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException e) {
                log.error("Failed to ack message", e);
            }
            return;
        }

        try {
            log.info("Processing video transcode message: messageId={}, fileId={}", 
                    message.getMessageId(), message.getFileInfo().getFileId());

            FileUploadInfo fileInfo = message.getFileInfo();
            
            // 模拟视频转码处理（这是一个耗时操作）
            simulateVideoTranscode(fileInfo, message.getProcessParams());
            
            fileInfo.setStatus(FileUploadInfo.FileStatus.PROCESSED);
            fileInfo.setProcessProgress(100);
            
            fileMessageProducer.sendFileProcessCompletedNotification(
                fileInfo, FileProcessMessage.ProcessType.VIDEO_TRANSCODE, true, 
                "Video transcode completed successfully");
            
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException e) {
                log.error("Failed to ack message", e);
            }
            log.info("Video transcode completed: fileId={}", fileInfo.getFileId());
            
        } catch (Exception e) {
            log.error("Failed to process video transcode: messageId={}", 
                    message.getMessageId(), e);
            handleProcessError(message, channel, deliveryTag, e);
        }
    }

    /**
     * 处理文档解析消息
     */
    @RabbitListener(queues = "${rabbitmq.queue.file-process:file.process.queue}")
    public void handleDocumentParse(@Payload FileProcessMessage message,
                                  Channel channel,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        if (message.getProcessType() != FileProcessMessage.ProcessType.DOCUMENT_PARSE) {
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException e) {
                log.error("Failed to ack message", e);
            }
            return;
        }

        try {
            log.info("Processing document parse message: messageId={}, fileId={}", 
                    message.getMessageId(), message.getFileInfo().getFileId());

            FileUploadInfo fileInfo = message.getFileInfo();
            
            // 模拟文档解析处理
            simulateDocumentParse(fileInfo, message.getProcessParams());
            
            fileInfo.setStatus(FileUploadInfo.FileStatus.PROCESSED);
            fileInfo.setProcessProgress(100);
            
            fileMessageProducer.sendFileProcessCompletedNotification(
                fileInfo, FileProcessMessage.ProcessType.DOCUMENT_PARSE, true, 
                "Document parse completed successfully");
            
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException e) {
                log.error("Failed to ack message", e);
            }
            log.info("Document parse completed: fileId={}", fileInfo.getFileId());
            
        } catch (Exception e) {
            log.error("Failed to process document parse: messageId={}", 
                    message.getMessageId(), e);
            handleProcessError(message, channel, deliveryTag, e);
        }
    }

    /**
     * 处理安全扫描消息
     */
    @RabbitListener(queues = "${rabbitmq.queue.file-process:file.process.queue}")
    public void handleSecurityScan(@Payload FileProcessMessage message,
                                 Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        if (message.getProcessType() != FileProcessMessage.ProcessType.SECURITY_SCAN) {
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException e) {
                log.error("Failed to ack message", e);
            }
            return;
        }

        try {
            log.info("Processing security scan message: messageId={}, fileId={}", 
                    message.getMessageId(), message.getFileInfo().getFileId());

            FileUploadInfo fileInfo = message.getFileInfo();
            
            // 模拟安全扫描
            boolean scanResult = simulateSecurityScan(fileInfo);
            
            if (scanResult) {
                fileInfo.setStatus(FileUploadInfo.FileStatus.PROCESSED);
                fileMessageProducer.sendFileProcessCompletedNotification(
                    fileInfo, FileProcessMessage.ProcessType.SECURITY_SCAN, true, 
                    "Security scan passed");
            } else {
                fileInfo.setStatus(FileUploadInfo.FileStatus.FAILED);
                fileInfo.setErrorMessage("Security scan failed - potential threat detected");
                fileMessageProducer.sendFileProcessCompletedNotification(
                    fileInfo, FileProcessMessage.ProcessType.SECURITY_SCAN, false, 
                    "Security scan failed");
            }
            
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException e) {
                log.error("Failed to ack message", e);
            }
            log.info("Security scan completed: fileId={}, result={}", 
                    fileInfo.getFileId(), scanResult);
            
        } catch (Exception e) {
            log.error("Failed to process security scan: messageId={}", 
                    message.getMessageId(), e);
            handleProcessError(message, channel, deliveryTag, e);
        }
    }

    /**
     * 处理通知消息
     */
    @RabbitListener(queues = "${rabbitmq.queue.file-notify:file.notify.queue}")
    public void handleFileNotification(@Payload FileProcessMessage message,
                                     Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("Processing file notification: messageId={}, processType={}, context={}", 
                    message.getMessageId(), message.getProcessType(), 
                    message.getBusinessContext());

            // 这里可以发送邮件、短信、推送通知等
            // 也可以调用回调URL通知业务系统
            if (message.getCallbackUrl() != null) {
                // TODO: 实现HTTP回调
                log.info("Would call callback URL: {}", message.getCallbackUrl());
            }
            
            // 记录处理日志或更新数据库状态
            log.info("File notification processed: fileId={}, status={}", 
                    message.getFileInfo() != null ? message.getFileInfo().getFileId() : "N/A",
                    message.getBusinessContext());
            
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException e) {
                log.error("Failed to ack message", e);
            }
            
        } catch (Exception e) {
            log.error("Failed to process file notification: messageId={}", 
                    message.getMessageId(), e);
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (Exception ex) {
                log.error("Failed to nack message", ex);
            }
        }
    }

    /**
     * 模拟图片压缩处理
     */
    private void simulateImageCompress(FileUploadInfo fileInfo, Map<String, Object> params) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000)); // 模拟处理时间
            log.info("Image compressed: fileId={}, originalSize={}", 
                    fileInfo.getFileId(), fileInfo.getFileSize());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Image compress interrupted", e);
        }
    }

    /**
     * 模拟视频转码处理
     */
    private void simulateVideoTranscode(FileUploadInfo fileInfo, Map<String, Object> params) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(5000, 15000)); // 模拟较长处理时间
            log.info("Video transcoded: fileId={}, format={}", 
                    fileInfo.getFileId(), params.get("format"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Video transcode interrupted", e);
        }
    }

    /**
     * 模拟文档解析处理
     */
    private void simulateDocumentParse(FileUploadInfo fileInfo, Map<String, Object> params) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(2000, 5000)); // 模拟处理时间
            log.info("Document parsed: fileId={}, extractText={}", 
                    fileInfo.getFileId(), params.get("extractText"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Document parse interrupted", e);
        }
    }

    /**
     * 模拟安全扫描
     */
    private boolean simulateSecurityScan(FileUploadInfo fileInfo) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 2000)); // 模拟扫描时间
            // 随机返回扫描结果，实际应该是真实的安全扫描逻辑
            boolean scanResult = ThreadLocalRandom.current().nextDouble() > 0.1; // 90%通过率
            log.info("Security scan completed: fileId={}, result={}", 
                    fileInfo.getFileId(), scanResult ? "CLEAN" : "THREAT");
            return scanResult;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Security scan interrupted", e);
        }
    }

    /**
     * 处理错误的通用方法
     */
    private void handleProcessError(FileProcessMessage message, Channel channel, 
                                  long deliveryTag, Exception e) {
        try {
            if (message.getRetryCount() < message.getMaxRetryCount()) {
                message.setRetryCount(message.getRetryCount() + 1);
                log.warn("Retrying message processing: messageId={}, retryCount={}", 
                        message.getMessageId(), message.getRetryCount());
                channel.basicNack(deliveryTag, false, true);
            } else {
                log.error("Max retry count exceeded, sending to DLQ: messageId={}", 
                        message.getMessageId());
                
                // 发送失败通知
                if (message.getFileInfo() != null) {
                    message.getFileInfo().setStatus(FileUploadInfo.FileStatus.FAILED);
                    message.getFileInfo().setErrorMessage(e.getMessage());
                    fileMessageProducer.sendFileProcessCompletedNotification(
                        message.getFileInfo(), message.getProcessType(), false, 
                        "Processing failed: " + e.getMessage());
                }
                
                channel.basicNack(deliveryTag, false, false);
            }
        } catch (Exception ex) {
            log.error("Failed to handle process error", ex);
        }
    }
}