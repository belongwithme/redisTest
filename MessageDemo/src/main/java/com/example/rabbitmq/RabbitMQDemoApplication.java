package com.example.rabbitmq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RabbitMQ Demo 应用启动类
 * 
 * @author Demo
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
public class RabbitMQDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RabbitMQDemoApplication.class, args);
        System.out.println("\n" +
                "========================================\n" +
                "  RabbitMQ Demo 应用启动成功！\n" +
                "  访问地址: http://localhost:8080\n" +
                "  RabbitMQ 管理界面: http://localhost:15672\n" +
                "  用户名: admin, 密码: admin123\n" +
                "========================================\n");
    }
}