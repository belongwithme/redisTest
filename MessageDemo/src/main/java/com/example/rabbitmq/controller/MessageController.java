package com.example.rabbitmq.controller;

import com.example.rabbitmq.model.User;
import com.example.rabbitmq.producer.MessageProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息测试控制器
 * 提供REST API接口用于测试RabbitMQ消息发送功能
 * 
 * @author Demo
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/message")
@CrossOrigin(origins = "*")
public class MessageController {

    private final MessageProducer messageProducer;

    public MessageController(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    /**
     * 首页欢迎信息
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "欢迎使用 RabbitMQ Demo API!");
        response.put("description", "这是一个完整的 RabbitMQ 与 SpringBoot 集成示例");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("发送简单消息", "POST /api/message/send?message=消息内容");
        endpoints.put("发送用户对象", "POST /api/message/send-object");
        endpoints.put("发送带属性消息", "POST /api/message/send-with-properties");
        endpoints.put("批量发送消息", "POST /api/message/send-batch");
        endpoints.put("发送Topic消息", "POST /api/message/send-topic");
        endpoints.put("发送Fanout消息", "POST /api/message/send-fanout");
        endpoints.put("发送延迟消息", "POST /api/message/send-delay");
        endpoints.put("获取统计信息", "GET /api/message/statistics");
        
        response.put("available_endpoints", endpoints);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 发送简单文本消息
     * 
     * @param message 要发送的消息内容
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestParam String message) {
        try {
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("消息内容不能为空", null));
            }
            
            messageProducer.sendSimpleMessage(message);
            
            Map<String, Object> response = createSuccessResponse(
                "简单消息发送成功", 
                Map.of("message", message, "type", "simple")
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("发送简单消息失败", e.getMessage()));
        }
    }

    /**
     * 发送用户对象消息
     * 
     * @param user 用户对象
     */
    @PostMapping("/send-object")
    public ResponseEntity<Map<String, Object>> sendUserObject(@RequestBody User user) {
        try {
            if (user == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户对象不能为空", null));
            }
            
            // 基本验证
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户姓名不能为空", null));
            }
            
            messageProducer.sendUserMessage(user);
            
            Map<String, Object> response = createSuccessResponse(
                "用户对象消息发送成功", 
                Map.of("user", user, "type", "object")
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("发送用户对象消息失败", e.getMessage()));
        }
    }

    /**
     * 发送带属性的消息
     * 
     * @param request 请求参数
     */
    @PostMapping("/send-with-properties")
    public ResponseEntity<Map<String, Object>> sendMessageWithProperties(@RequestBody Map<String, Object> request) {
        try {
            String message = (String) request.get("message");
            Integer priority = (Integer) request.getOrDefault("priority", 5);
            Long expiration = ((Number) request.getOrDefault("expiration", 60000)).longValue();
            
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("消息内容不能为空", null));
            }
            
            messageProducer.sendMessageWithProperties(message, priority, expiration);
            
            Map<String, Object> response = createSuccessResponse(
                "带属性消息发送成功", 
                Map.of(
                    "message", message, 
                    "priority", priority, 
                    "expiration", expiration,
                    "type", "with-properties"
                )
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("发送带属性消息失败", e.getMessage()));
        }
    }

    /**
     * 批量发送消息
     * 
     * @param request 请求参数
     */
    @PostMapping("/send-batch")
    public ResponseEntity<Map<String, Object>> sendBatchMessages(@RequestBody Map<String, Object> request) {
        try {
            String messagePrefix = (String) request.getOrDefault("messagePrefix", "批量消息");
            Integer count = (Integer) request.getOrDefault("count", 10);
            
            if (count <= 0 || count > 1000) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("发送数量必须在1-1000之间", null));
            }
            
            messageProducer.sendBatchMessages(messagePrefix, count);
            
            Map<String, Object> response = createSuccessResponse(
                "批量消息发送成功", 
                Map.of(
                    "messagePrefix", messagePrefix, 
                    "count", count,
                    "type", "batch"
                )
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("批量发送消息失败", e.getMessage()));
        }
    }

    /**
     * 发送到Topic交换机
     * 
     * @param request 请求参数
     */
    @PostMapping("/send-topic")
    public ResponseEntity<Map<String, Object>> sendTopicMessage(@RequestBody Map<String, Object> request) {
        try {
            String message = (String) request.get("message");
            String routingKey = (String) request.getOrDefault("routingKey", "user.info");
            
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("消息内容不能为空", null));
            }
            
            messageProducer.sendToTopicExchange(message, routingKey);
            
            Map<String, Object> response = createSuccessResponse(
                "Topic消息发送成功", 
                Map.of(
                    "message", message, 
                    "routingKey", routingKey,
                    "type", "topic"
                )
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("发送Topic消息失败", e.getMessage()));
        }
    }

    /**
     * 发送到Fanout交换机（广播）
     * 
     * @param request 请求参数
     */
    @PostMapping("/send-fanout")
    public ResponseEntity<Map<String, Object>> sendFanoutMessage(@RequestBody Map<String, Object> request) {
        try {
            String message = (String) request.get("message");
            
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("消息内容不能为空", null));
            }
            
            messageProducer.sendToFanoutExchange(message);
            
            Map<String, Object> response = createSuccessResponse(
                "Fanout广播消息发送成功", 
                Map.of("message", message, "type", "fanout")
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("发送Fanout消息失败", e.getMessage()));
        }
    }

    /**
     * 发送延迟消息
     * 
     * @param request 请求参数
     */
    @PostMapping("/send-delay")
    public ResponseEntity<Map<String, Object>> sendDelayMessage(@RequestBody Map<String, Object> request) {
        try {
            String message = (String) request.get("message");
            Integer delaySeconds = (Integer) request.getOrDefault("delaySeconds", 10);
            
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("消息内容不能为空", null));
            }
            
            if (delaySeconds <= 0 || delaySeconds > 3600) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("延迟时间必须在1-3600秒之间", null));
            }
            
            messageProducer.sendDelayMessage(message, delaySeconds);
            
            Map<String, Object> response = createSuccessResponse(
                "延迟消息发送成功", 
                Map.of(
                    "message", message, 
                    "delaySeconds", delaySeconds,
                    "estimatedReceiveTime", LocalDateTime.now().plusSeconds(delaySeconds),
                    "type", "delay"
                )
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("发送延迟消息失败", e.getMessage()));
        }
    }

    /**
     * 创建示例用户
     */
    @PostMapping("/create-sample-user")
    public ResponseEntity<Map<String, Object>> createSampleUser() {
        try {
            User sampleUser = new User("张三", 25, "zhangsan@example.com");
            sampleUser.setId(System.currentTimeMillis());
            sampleUser.setPhone("13800138000");
            sampleUser.setAddress("北京市朝阳区");
            
            messageProducer.sendUserMessage(sampleUser);
            
            Map<String, Object> response = createSuccessResponse(
                "示例用户创建并发送成功", 
                Map.of("user", sampleUser, "type", "sample-user")
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("创建示例用户失败", e.getMessage()));
        }
    }

    /**
     * 获取发送统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            messageProducer.printSendStatistics();
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("timestamp", LocalDateTime.now());
            statistics.put("status", "运行中");
            statistics.put("description", "RabbitMQ Demo 统计信息");
            
            Map<String, String> queues = new HashMap<>();
            queues.put("hello.queue", "简单消息队列");
            queues.put("user.queue", "用户对象队列");
            queues.put("topic.queue", "Topic队列");
            queues.put("fanout.queue", "Fanout队列");
            queues.put("dead.letter.queue", "死信队列");
            
            Map<String, String> exchanges = new HashMap<>();
            exchanges.put("hello.exchange", "简单消息交换机");
            exchanges.put("user.exchange", "用户对象交换机");
            exchanges.put("topic.exchange", "Topic交换机");
            exchanges.put("fanout.exchange", "Fanout交换机");
            exchanges.put("dead.letter.exchange", "死信交换机");
            
            statistics.put("queues", queues);
            statistics.put("exchanges", exchanges);
            
            Map<String, Object> response = createSuccessResponse("统计信息获取成功", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("获取统计信息失败", e.getMessage()));
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "RabbitMQ Demo");
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }

    // ======================== 工具方法 ========================

    /**
     * 创建成功响应
     */
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message, String error) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        if (error != null) {
            response.put("error", error);
        }
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}