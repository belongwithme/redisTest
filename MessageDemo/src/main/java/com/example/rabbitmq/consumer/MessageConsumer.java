package com.example.rabbitmq.consumer;

import com.example.rabbitmq.model.User;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 消息消费者
 * 负责接收和处理来自 RabbitMQ 的各种类型消息
 * 
 * @author Demo
 * @version 1.0.0
 * @since 2024-01-01
 */
@Component
public class MessageConsumer {

    /**
     * 消费简单文本消息
     * 
     * @param message 接收到的消息内容
     * @param channel RabbitMQ通道
     * @param deliveryTag 消息标签
     */
    @RabbitListener(queues = "${rabbitmq.queue.hello:hello.queue}")
    public void receiveSimpleMessage(
            @Payload String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        
        try {
            System.out.println("========================================");
            System.out.println("【简单消息消费者】收到新消息!");
            System.out.println("接收时间: " + LocalDateTime.now());
            System.out.println("消息内容: " + message);
            System.out.println("消息标签: " + deliveryTag);
            
            // 模拟业务处理
            processSimpleMessage(message);
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            System.out.println("消息处理成功，已确认!");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("处理简单消息失败: " + e.getMessage());
            try {
                // 拒绝消息，不重新入队
                channel.basicNack(deliveryTag, false, false);
                System.err.println("消息已拒绝，不重新入队");
            } catch (Exception ackException) {
                System.err.println("确认消息失败: " + ackException.getMessage());
            }
        }
    }

    /**
     * 消费用户对象消息
     * 
     * @param user 接收到的用户对象（自动反序列化）
     * @param channel RabbitMQ通道
     * @param deliveryTag 消息标签
     */
    @RabbitListener(queues = "${rabbitmq.queue.user:user.queue}")
    public void receiveUserMessage(
            @Payload User user,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        
        try {
            System.out.println("========================================");
            System.out.println("【用户对象消费者】收到新用户!");
            System.out.println("接收时间: " + LocalDateTime.now());
            System.out.println("用户信息: " + user);
            System.out.println("用户姓名: " + user.getName());
            System.out.println("用户年龄: " + user.getAge());
            System.out.println("用户邮箱: " + user.getEmail());
            System.out.println("是否成年: " + (user.isAdult() ? "是" : "否"));
            System.out.println("邮箱格式: " + (user.isValidEmail() ? "有效" : "无效"));
            System.out.println("消息标签: " + deliveryTag);
            
            // 模拟业务处理
            processUserMessage(user);
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            System.out.println("用户对象处理成功，已确认!");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("处理用户对象消息失败: " + e.getMessage());
            try {
                // 重新入队，等待下次处理
                channel.basicNack(deliveryTag, false, true);
                System.err.println("消息已拒绝，重新入队等待处理");
            } catch (Exception ackException) {
                System.err.println("确认消息失败: " + ackException.getMessage());
            }
        }
    }

    /**
     * 消费带有完整消息对象的消息（可以获取更多消息信息）
     * 
     * @param message 完整的消息对象
     * @param channel RabbitMQ通道
     * @param deliveryTag 消息标签
     */
    @RabbitListener(queues = "${rabbitmq.queue.hello:hello.queue}")
    public void receiveMessageWithProperties(
            Message message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        
        try {
            // 获取消息体
            String body = new String(message.getBody());
            
            // 获取消息属性
            var properties = message.getMessageProperties();
            
            System.out.println("========================================");
            System.out.println("【完整消息消费者】收到带属性的消息!");
            System.out.println("接收时间: " + LocalDateTime.now());
            System.out.println("消息内容: " + body);
            System.out.println("消息ID: " + properties.getMessageId());
            System.out.println("时间戳: " + properties.getTimestamp());
            System.out.println("优先级: " + properties.getPriority());
            System.out.println("过期时间: " + properties.getExpiration());
            System.out.println("内容类型: " + properties.getContentType());
            System.out.println("编码: " + properties.getContentEncoding());
            System.out.println("交换机: " + properties.getReceivedExchange());
            System.out.println("路由键: " + properties.getReceivedRoutingKey());
            
            // 打印自定义头部信息
            if (properties.getHeaders() != null) {
                System.out.println("自定义头部:");
                properties.getHeaders().forEach((key, value) -> 
                    System.out.println("  " + key + " = " + value));
            }
            
            System.out.println("消息标签: " + deliveryTag);
            
            // 模拟业务处理
            processMessageWithProperties(body, properties);
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            System.out.println("带属性消息处理成功，已确认!");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("处理带属性消息失败: " + e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ackException) {
                System.err.println("确认消息失败: " + ackException.getMessage());
            }
        }
    }

    /**
     * 消费Topic队列的消息
     * 
     * @param message 消息内容
     * @param channel RabbitMQ通道
     * @param deliveryTag 消息标签
     * @param routingKey 路由键
     */
    @RabbitListener(queues = "topic.queue")
    public void receiveTopicMessage(
            @Payload String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        
        try {
            System.out.println("========================================");
            System.out.println("【Topic消费者】收到消息!");
            System.out.println("接收时间: " + LocalDateTime.now());
            System.out.println("消息内容: " + message);
            System.out.println("路由键: " + routingKey);
            System.out.println("消息标签: " + deliveryTag);
            
            // 根据路由键进行不同的处理
            if (routingKey.startsWith("user.")) {
                System.out.println("这是用户相关的消息");
            } else {
                System.out.println("这是其他类型的消息");
            }
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            System.out.println("Topic消息处理成功!");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("处理Topic消息失败: " + e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ackException) {
                System.err.println("确认消息失败: " + ackException.getMessage());
            }
        }
    }

    /**
     * 消费Fanout队列的消息
     * 
     * @param message 消息内容
     * @param channel RabbitMQ通道
     * @param deliveryTag 消息标签
     */
    @RabbitListener(queues = "fanout.queue")
    public void receiveFanoutMessage(
            @Payload String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        
        try {
            System.out.println("========================================");
            System.out.println("【Fanout消费者】收到广播消息!");
            System.out.println("接收时间: " + LocalDateTime.now());
            System.out.println("消息内容: " + message);
            System.out.println("消息标签: " + deliveryTag);
            
            // 模拟广播消息处理
            System.out.println("处理广播消息...");
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            System.out.println("Fanout消息处理成功!");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("处理Fanout消息失败: " + e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ackException) {
                System.err.println("确认消息失败: " + ackException.getMessage());
            }
        }
    }

    /**
     * 消费死信队列的消息
     * 
     * @param message 消息内容
     * @param channel RabbitMQ通道
     * @param deliveryTag 消息标签
     */
    @RabbitListener(queues = "${rabbitmq.queue.dead-letter:dead.letter.queue}")
    public void receiveDeadLetterMessage(
            @Payload String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        
        try {
            System.out.println("========================================");
            System.out.println("【死信队列消费者】收到死信消息!");
            System.out.println("接收时间: " + LocalDateTime.now());
            System.out.println("消息内容: " + message);
            System.out.println("消息标签: " + deliveryTag);
            System.out.println("这条消息可能是:");
            System.out.println("1. 处理失败的消息");
            System.out.println("2. 过期的消息");
            System.out.println("3. 被拒绝的消息");
            
            // 死信消息的特殊处理逻辑
            processDeadLetterMessage(message);
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            System.out.println("死信消息处理完成!");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("处理死信消息失败: " + e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ackException) {
                System.err.println("确认消息失败: " + ackException.getMessage());
            }
        }
    }

    // ======================== 私有业务处理方法 ========================

    /**
     * 处理简单消息的业务逻辑
     */
    private void processSimpleMessage(String message) {
        System.out.println("正在处理简单消息: " + message);
        
        // 模拟业务处理时间
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 这里可以添加具体的业务逻辑
        // 例如：保存到数据库、调用其他服务、发送通知等
        
        System.out.println("简单消息业务处理完成");
    }

    /**
     * 处理用户对象的业务逻辑
     */
    private void processUserMessage(User user) {
        System.out.println("正在处理用户对象: " + user.getName());
        
        // 模拟业务处理
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 业务逻辑示例
        if (user.isAdult()) {
            System.out.println("用户是成年人，可以进行相关操作");
        } else {
            System.out.println("用户是未成年人，需要监护人同意");
        }
        
        if (user.isValidEmail()) {
            System.out.println("用户邮箱格式正确，可以发送邮件");
        } else {
            System.out.println("用户邮箱格式错误，需要重新验证");
        }
        
        System.out.println("用户对象业务处理完成");
    }

    /**
     * 处理带属性消息的业务逻辑
     */
    private void processMessageWithProperties(String message, org.springframework.amqp.core.MessageProperties properties) {
        System.out.println("正在处理带属性的消息: " + message);
        
        // 根据消息属性进行不同的处理
        if (properties.getPriority() != null && properties.getPriority() > 5) {
            System.out.println("这是高优先级消息，优先处理");
        }
        
        // 模拟处理时间
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("带属性消息业务处理完成");
    }

    /**
     * 处理死信消息的业务逻辑
     */
    private void processDeadLetterMessage(String message) {
        System.out.println("正在处理死信消息: " + message);
        
        // 死信消息的特殊处理
        // 例如：记录日志、发送告警、人工干预等
        
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("死信消息业务处理完成");
    }
}