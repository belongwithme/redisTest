package com.example.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * 负责创建队列、交换机、绑定关系以及消息转换器等
 * 
 * @author Demo
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
public class RabbitConfig {

    // 从配置文件读取队列名称
    @Value("${rabbitmq.queue.hello:hello.queue}")
    private String helloQueueName;
    
    @Value("${rabbitmq.queue.user:user.queue}")
    private String userQueueName;
    
    @Value("${rabbitmq.queue.dead-letter:dead.letter.queue}")
    private String deadLetterQueueName;

    // 从配置文件读取交换机名称
    @Value("${rabbitmq.exchange.hello:hello.exchange}")
    private String helloExchangeName;
    
    @Value("${rabbitmq.exchange.user:user.exchange}")
    private String userExchangeName;
    
    @Value("${rabbitmq.exchange.dead-letter:dead.letter.exchange}")
    private String deadLetterExchangeName;

    // 从配置文件读取路由键
    @Value("${rabbitmq.routing.hello:hello.routing.key}")
    private String helloRoutingKey;
    
    @Value("${rabbitmq.routing.user:user.routing.key}")
    private String userRoutingKey;
    
    @Value("${rabbitmq.routing.dead-letter:dead.letter.routing.key}")
    private String deadLetterRoutingKey;

    // 文件处理相关配置
    @Value("${rabbitmq.queue.file-upload:file.upload.queue}")
    private String fileUploadQueueName;
    
    @Value("${rabbitmq.queue.file-process:file.process.queue}")
    private String fileProcessQueueName;
    
    @Value("${rabbitmq.queue.file-notify:file.notify.queue}")
    private String fileNotifyQueueName;
    
    @Value("${rabbitmq.exchange.file:file.exchange}")
    private String fileExchangeName;
    
    @Value("${rabbitmq.routing.file-upload:file.upload}")
    private String fileUploadRoutingKey;
    
    @Value("${rabbitmq.routing.file-process:file.process}")
    private String fileProcessRoutingKey;
    
    @Value("${rabbitmq.routing.file-notify:file.notify}")
    private String fileNotifyRoutingKey;

    /**
     * 消息转换器 - 支持JSON序列化
     * 将Java对象自动转换为JSON格式发送，接收时自动反序列化
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 配置
     * 设置消息转换器和确认机制
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        
        // 设置发布确认回调
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("消息发送成功: " + correlationData);
            } else {
                System.err.println("消息发送失败: " + cause);
            }
        });
        
        // 设置返回回调
        template.setReturnsCallback(returned -> {
            System.err.println("消息未路由到队列: " + returned.getMessage());
            System.err.println("回复码: " + returned.getReplyCode());
            System.err.println("回复文本: " + returned.getReplyText());
            System.err.println("交换机: " + returned.getExchange());
            System.err.println("路由键: " + returned.getRoutingKey());
        });
        
        return template;
    }

    /**
     * 监听器容器工厂配置
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(1);
        return factory;
    }

    // ======================== 基础消息队列配置 ========================

    /**
     * 声明基础队列 - 用于简单字符串消息
     */
    @Bean
    public Queue helloQueue() {
        return QueueBuilder
                .durable(helloQueueName)  // 持久化队列
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)  // 死信交换机
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)  // 死信路由键
                .withArgument("x-message-ttl", 600000)  // 消息TTL 10分钟
                .build();
    }

    /**
     * 声明基础交换机 - 直连模式
     */
    @Bean
    public DirectExchange helloExchange() {
        return ExchangeBuilder
                .directExchange(helloExchangeName)
                .durable(true)
                .build();
    }

    /**
     * 绑定基础队列到交换机
     */
    @Bean
    public Binding helloBinding() {
        return BindingBuilder
                .bind(helloQueue())
                .to(helloExchange())
                .with(helloRoutingKey);
    }

    // ======================== 用户对象队列配置 ========================

    /**
     * 声明用户队列 - 用于用户对象消息
     */
    @Bean
    public Queue userQueue() {
        return QueueBuilder
                .durable(userQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .withArgument("x-message-ttl", 300000)  // 消息TTL 5分钟
                .build();
    }

    /**
     * 声明用户交换机 - 直连模式
     */
    @Bean
    public DirectExchange userExchange() {
        return ExchangeBuilder
                .directExchange(userExchangeName)
                .durable(true)
                .build();
    }

    /**
     * 绑定用户队列到交换机
     */
    @Bean
    public Binding userBinding() {
        return BindingBuilder
                .bind(userQueue())
                .to(userExchange())
                .with(userRoutingKey);
    }

    // ======================== 死信队列配置 ========================

    /**
     * 声明死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder
                .durable(deadLetterQueueName)
                .build();
    }

    /**
     * 声明死信交换机
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder
                .directExchange(deadLetterExchangeName)
                .durable(true)
                .build();
    }

    /**
     * 绑定死信队列到死信交换机
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(deadLetterRoutingKey);
    }

    // ======================== Topic 交换机示例（可选） ========================

    /**
     * 主题交换机示例 - 支持通配符路由
     */
    @Bean
    public TopicExchange topicExchange() {
        return ExchangeBuilder
                .topicExchange("topic.exchange")
                .durable(true)
                .build();
    }

    /**
     * Topic 队列示例
     */
    @Bean
    public Queue topicQueue() {
        return QueueBuilder
                .durable("topic.queue")
                .build();
    }

    /**
     * Topic 绑定示例 - 支持 user.* 路由键
     */
    @Bean
    public Binding topicBinding() {
        return BindingBuilder
                .bind(topicQueue())
                .to(topicExchange())
                .with("user.*");
    }

    // ======================== Fanout 交换机示例（可选） ========================

    /**
     * 广播交换机示例 - 发送给所有绑定的队列
     */
    @Bean
    public FanoutExchange fanoutExchange() {
        return ExchangeBuilder
                .fanoutExchange("fanout.exchange")
                .durable(true)
                .build();
    }

    /**
     * Fanout 队列示例
     */
    @Bean
    public Queue fanoutQueue() {
        return QueueBuilder
                .durable("fanout.queue")
                .build();
    }

    /**
     * Fanout 绑定示例 - 无需路由键
     */
    @Bean
    public Binding fanoutBinding() {
        return BindingBuilder
                .bind(fanoutQueue())
                .to(fanoutExchange());
    }

    // ======================== 文件处理队列配置 ========================

    /**
     * 文件上传队列 - 处理文件上传完成事件
     */
    @Bean
    public Queue fileUploadQueue() {
        return QueueBuilder
                .durable(fileUploadQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .withArgument("x-message-ttl", 1800000) // 30分钟TTL
                .build();
    }

    /**
     * 文件处理队列 - 处理文件转换、压缩等任务
     */
    @Bean
    public Queue fileProcessQueue() {
        return QueueBuilder
                .durable(fileProcessQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .withArgument("x-message-ttl", 3600000) // 1小时TTL，处理任务可能较长
                .withArgument("x-max-priority", 10) // 支持优先级
                .build();
    }

    /**
     * 文件通知队列 - 处理文件处理完成通知
     */
    @Bean
    public Queue fileNotifyQueue() {
        return QueueBuilder
                .durable(fileNotifyQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .withArgument("x-message-ttl", 600000) // 10分钟TTL
                .build();
    }

    /**
     * 文件处理交换机 - Topic模式支持灵活路由
     */
    @Bean
    public TopicExchange fileExchange() {
        return ExchangeBuilder
                .topicExchange(fileExchangeName)
                .durable(true)
                .build();
    }

    /**
     * 文件上传队列绑定
     */
    @Bean
    public Binding fileUploadBinding() {
        return BindingBuilder
                .bind(fileUploadQueue())
                .to(fileExchange())
                .with(fileUploadRoutingKey);
    }

    /**
     * 文件处理队列绑定 - 支持通配符 file.process.*
     */
    @Bean
    public Binding fileProcessBinding() {
        return BindingBuilder
                .bind(fileProcessQueue())
                .to(fileExchange())
                .with(fileProcessRoutingKey + ".*");
    }

    /**
     * 文件通知队列绑定
     */
    @Bean
    public Binding fileNotifyBinding() {
        return BindingBuilder
                .bind(fileNotifyQueue())
                .to(fileExchange())
                .with(fileNotifyRoutingKey);
    }
}