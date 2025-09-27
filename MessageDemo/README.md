# RabbitMQ + SpringBoot Demo 项目搭建指南

这是一个完整的 RabbitMQ 与 SpringBoot 集成的示例项目，包含消息生产者、消费者以及队列配置。

## 目录结构
```
MessageDemo/
├── README.md                   # 搭建指南
├── pom.xml                     # Maven 依赖配置
├── application.yml             # SpringBoot 配置文件
├── docker-compose.yml          # Docker 部署文件
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── example/
│                   └── rabbitmq/
│                       ├── RabbitMQDemoApplication.java    # 启动类
│                       ├── config/
│                       │   └── RabbitConfig.java           # RabbitMQ 配置
│                       ├── producer/
│                       │   └── MessageProducer.java        # 消息生产者
│                       ├── consumer/
│                       │   └── MessageConsumer.java        # 消息消费者
│                       └── controller/
│                           └── MessageController.java      # 测试控制器
├── 环境准备指南.md              # 详细的环境准备步骤
└── SpringBoot3-配置升级说明.md  # SpringBoot 3.x 升级说明
```

## 快速开始

### 1. 环境准备
推荐使用 Docker 方式安装和启动 RabbitMQ：

```bash
# 使用 Docker 启动 RabbitMQ（包含管理界面）
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=admin \
  -e RABBITMQ_DEFAULT_PASS=admin123 \
  rabbitmq:3-management

# 或者使用 docker-compose（推荐）
docker-compose up -d
```

RabbitMQ 管理界面访问：http://localhost:15672
- 用户名：admin
- 密码：admin123

### 2. 项目构建和运行

```bash
# 编译项目
mvn clean compile

# 启动 SpringBoot 应用
mvn spring-boot:run
```

### 3. 测试验证

启动成功后，可以通过以下方式测试：

#### 方式一：使用 REST API
```bash
# 发送简单消息
curl -X POST "http://localhost:8080/api/message/send?message=Hello RabbitMQ"

# 发送对象消息
curl -X POST "http://localhost:8080/api/message/send-object" \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","age":25,"email":"zhangsan@example.com"}'
```

#### 方式二：查看日志
观察控制台输出，会看到消费者接收到的消息：
```
消费者接收到消息: Hello RabbitMQ
消费者接收到用户对象: User{name='张三', age=25, email='zhangsan@example.com'}
```

#### 方式三：RabbitMQ 管理界面
访问 http://localhost:15672，在 Queues 页面可以看到队列状态和消息统计。

### 4. 项目特性

- ✅ 直连队列（Direct Queue）消息收发
- ✅ 对象序列化和反序列化
- ✅ 自动创建队列和交换机
- ✅ 消息确认机制
- ✅ 错误处理和重试机制
- ✅ REST API 测试接口

### 5. 核心配置说明

#### 队列配置
- **队列名称**: `hello.queue`
- **交换机名称**: `hello.exchange`
- **路由键**: `hello.routing.key`
- **持久化**: 是
- **自动删除**: 否

#### 连接配置
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin123
    virtual-host: /
```

## 常见问题

### Q1: 连接失败怎么办？
确保 RabbitMQ 服务正在运行：
```bash
docker ps | grep rabbitmq
```

### Q2: 消息没有被消费？
检查消费者是否正常启动，查看应用日志中是否有异常信息。

### Q3: 如何查看队列状态？
访问 RabbitMQ 管理界面 http://localhost:15672，或使用命令：
```bash
docker exec rabbitmq rabbitmqctl list_queues
```

## 下一步扩展

本 Demo 提供了基础的消息收发功能，您可以基于此扩展：
- 消息路由（Topic、Fanout 模式）
- 死信队列配置
- 消息确认和重试机制
- 集群配置
- 监控和告警

---
**注意**: 确保防火墙开放 5672 和 15672 端口，或在云服务器上配置安全组规则。