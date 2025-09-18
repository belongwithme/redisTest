package com.example.rabbitmq.producer;

import com.example.rabbitmq.model.User;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 消息生产者
 * 负责发送各种类型的消息到 RabbitMQ
 * 
 * @author Demo
 * @version 1.0.0
 * @since 2024-01-01
 */
@Component
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    // 从配置文件读取交换机和路由键配置
    @Value("${rabbitmq.exchange.hello:hello.exchange}")
    private String helloExchange;

    @Value("${rabbitmq.routing.hello:hello.routing.key}")
    private String helloRoutingKey;

    @Value("${rabbitmq.exchange.user:user.exchange}")
    private String userExchange;

    @Value("${rabbitmq.routing.user:user.routing.key}")
    private String userRoutingKey;

    public MessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送简单文本消息
     * 
     * @param message 要发送的消息内容
     */
    public void sendSimpleMessage(String message) {
        try {
            // 生成唯一的相关数据用于确认
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
            
            System.out.println("准备发送简单消息: " + message);
            System.out.println("发送时间: " + LocalDateTime.now());
            System.out.println("发送到交换机: " + helloExchange);
            System.out.println("路由键: " + helloRoutingKey);
            System.out.println("相关ID: " + correlationData.getId());
            
            // 发送消息
            rabbitTemplate.convertAndSend(helloExchange, helloRoutingKey, message, correlationData);
            
            System.out.println("简单消息发送完成!");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("发送简单消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发送用户对象消息
     *
     * @param user 要发送的用户对象
     */
    public void sendUserMessage(User user) {
        try {
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
            
            System.out.println("准备发送用户对象消息: " + user);
            System.out.println("发送时间: " + LocalDateTime.now());
            System.out.println("发送到交换机: " + userExchange);
            System.out.println("路由键: " + userRoutingKey);
            System.out.println("相关ID: " + correlationData.getId());
            
            // 发送用户对象，会自动序列化为JSON
            rabbitTemplate.convertAndSend(userExchange, userRoutingKey, user, correlationData);
            
            System.out.println("用户对象消息发送完成!");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("发送用户对象消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发送带有自定义属性的消息
     * 
     * @param message 消息内容
     * @param priority 消息优先级 (0-255)
     * @param expiration 消息过期时间（毫秒）
     */
    public void sendMessageWithProperties(String message, int priority, long expiration) {
        try {
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
            
            // 创建消息属性
            MessageProperties properties = new MessageProperties();
            properties.setPriority(priority);
            properties.setExpiration(String.valueOf(expiration));
            properties.setContentType("text/plain");
            properties.setContentEncoding("UTF-8");
            properties.setDeliveryMode(MessageProperties.DEFAULT_DELIVERY_MODE); // 持久化
            properties.setTimestamp(new java.util.Date());
            properties.setMessageId(UUID.randomUUID().toString());
            
            // 添加自定义头部信息
            properties.setHeader("custom-header", "custom-value");
            properties.setHeader("sender", "MessageProducer");
            properties.setHeader("version", "1.0");
            
            // 创建消息对象
            Message msg = new Message(message.getBytes(), properties);
            
            System.out.println("准备发送带属性的消息: " + message);
            System.out.println("消息优先级: " + priority);
            System.out.println("过期时间: " + expiration + " 毫秒");
            System.out.println("发送时间: " + LocalDateTime.now());
            
            // 发送消息
            rabbitTemplate.send(helloExchange, helloRoutingKey, msg, correlationData);
            
            System.out.println("带属性的消息发送完成!");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("发送带属性的消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发送延迟消息（需要插件支持）
     * 
     * @param message 消息内容
     * @param delaySeconds 延迟秒数
     */
    public void sendDelayMessage(String message, int delaySeconds) {
        try {
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
            
            System.out.println("准备发送延迟消息: " + message);
            System.out.println("延迟时间: " + delaySeconds + " 秒");
            System.out.println("发送时间: " + LocalDateTime.now());
            System.out.println("预期接收时间: " + LocalDateTime.now().plusSeconds(delaySeconds));
            
            // 发送延迟消息 (需要安装 rabbitmq_delayed_message_exchange 插件)
            rabbitTemplate.convertAndSend(helloExchange, helloRoutingKey, message, msg -> {
                msg.getMessageProperties().setDelayLong((long) delaySeconds * 1000);
                return msg;
            }, correlationData);
            
            System.out.println("延迟消息发送完成!");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("发送延迟消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 批量发送消息
     * 
     * @param messagePrefix 消息前缀
     * @param count 发送数量
     */
    public void sendBatchMessages(String messagePrefix, int count) {
        try {
            System.out.println("准备批量发送消息，数量: " + count);
            System.out.println("消息前缀: " + messagePrefix);
            System.out.println("开始时间: " + LocalDateTime.now());
            
            for (int i = 1; i <= count; i++) {
                String message = messagePrefix + " - 第 " + i + " 条消息";
                CorrelationData correlationData = new CorrelationData("batch-" + i);
                
                rabbitTemplate.convertAndSend(helloExchange, helloRoutingKey, message, correlationData);
                
                // 避免发送过快，稍微延迟
                if (i % 10 == 0) {
                    Thread.sleep(100);
                    System.out.println("已发送 " + i + " 条消息...");
                }
            }
            
            System.out.println("批量消息发送完成! 总共发送: " + count + " 条");
            System.out.println("结束时间: " + LocalDateTime.now());
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("批量发送消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发送到Topic交换机 (通配符路由)
     * 
     * @param message 消息内容
     * @param routingKey 路由键 (支持通配符)
     */
    public void sendToTopicExchange(String message, String routingKey) {
        try {
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
            
            System.out.println("准备发送到Topic交换机: " + message);
            System.out.println("路由键: " + routingKey);
            System.out.println("发送时间: " + LocalDateTime.now());
            
            rabbitTemplate.convertAndSend("topic.exchange", routingKey, message, correlationData);
            
            System.out.println("Topic消息发送完成!");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("发送Topic消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发送到Fanout交换机 (广播模式)
     * 
     * @param message 消息内容
     */
    public void sendToFanoutExchange(String message) {
        try {
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
            
            System.out.println("准备发送到Fanout交换机(广播): " + message);
            System.out.println("发送时间: " + LocalDateTime.now());
            
            // Fanout交换机忽略路由键
            rabbitTemplate.convertAndSend("fanout.exchange", "", message, correlationData);
            
            System.out.println("Fanout消息发送完成!");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("发送Fanout消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取发送统计信息
     */
    public void printSendStatistics() {
        System.out.println("========================================");
        System.out.println("消息发送统计信息:");
        System.out.println("当前时间: " + LocalDateTime.now());
        System.out.println("Hello交换机: " + helloExchange);
        System.out.println("Hello路由键: " + helloRoutingKey);
        System.out.println("User交换机: " + userExchange);
        System.out.println("User路由键: " + userRoutingKey);
        System.out.println("========================================");
    }
}