# Notion文件夹同步工具 - Java版使用指南

## 📋 目录
1. [环境要求](#环境要求)
2. [项目结构](#项目结构)
3. [快速开始](#快速开始)
4. [构建和运行](#构建和运行)
5. [配置说明](#配置说明)
6. [高级功能](#高级功能)
7. [常见问题](#常见问题)

## 环境要求

- **Java**: JDK 8 或更高版本
- **Maven**: 3.6 或更高版本（用于依赖管理和构建）
- **Notion账户**: 需要有Notion账户并创建Integration

### 检查Java版本
```bash
java -version
```

### 检查Maven版本
```bash
mvn -version
```

## 项目结构

```
notion-sync-java/
├── pom.xml                 # Maven项目配置文件
├── NotionSync.java         # 核心同步类
├── NotionConfig.java       # 配置管理类
├── NotionSyncApp.java      # 主应用程序入口
├── notion-sync.properties  # 配置文件（首次运行后生成）
└── README_Java版.md        # 本文档
```

## 快速开始

### 第1步：获取Notion凭证

1. **创建Integration**
   - 访问 [Notion Integrations](https://www.notion.so/my-integrations)
   - 点击 "+ New integration"
   - 设置名称和权限
   - 复制 Integration Token

2. **创建数据库**
   - 在Notion中创建新页面
   - 输入 `/database` 创建表格数据库
   - 添加以下列：
     - 标题 (Title)
     - 文件路径 (Text)
     - 文件夹 (Text)
     - 同步时间 (Date)

3. **分享数据库**
   - 点击数据库页面右上角 "Share"
   - 邀请你的Integration
   - 确保有编辑权限

4. **获取数据库ID**
   - 从数据库URL中提取32位ID

### 第2步：配置项目

1. **首次运行生成配置文件**
   ```bash
   javac NotionConfig.java NotionSync.java NotionSyncApp.java
   java NotionSyncApp
   ```

2. **编辑配置文件**
   打开生成的 `notion-sync.properties` 文件：
   ```properties
   # Notion API配置
   notion.token=secret_你的Token
   notion.database.id=你的数据库ID
   
   # 本地文件夹配置
   local.folder=D:\\准备
   
   # 高级配置
   api.delay.ms=300
   max.file.size.mb=10
   verbose=true
   ```

## 构建和运行

### 方法1：使用Maven（推荐）

1. **安装依赖并编译**
   ```bash
   mvn clean compile
   ```

2. **运行程序**
   ```bash
   mvn exec:java -Dexec.mainClass="NotionSyncApp"
   ```

3. **打包成可执行JAR**
   ```bash
   mvn clean package
   ```
   
   运行打包后的JAR：
   ```bash
   java -jar target/notion-sync-1.0.0-jar-with-dependencies.jar
   ```

### 方法2：手动编译

1. **下载依赖JAR文件**
   - [OkHttp](https://search.maven.org/artifact/com.squareup.okhttp3/okhttp)
   - [Gson](https://search.maven.org/artifact/com.google.code.gson/gson)

2. **编译**
   ```bash
   javac -cp "okhttp-4.11.0.jar;gson-2.10.1.jar;." *.java
   ```

3. **运行**
   ```bash
   java -cp "okhttp-4.11.0.jar;gson-2.10.1.jar;." NotionSyncApp
   ```

## 配置说明

### notion-sync.properties 配置项

| 配置项 | 说明 | 示例值 |
|--------|------|--------|
| notion.token | Notion Integration Token | secret_xxx... |
| notion.database.id | 目标数据库ID | 32位字符串 |
| local.folder | 本地文件夹路径 | D:\\准备 |
| api.delay.ms | API请求间隔（毫秒） | 300 |
| max.file.size.mb | 最大文件大小（MB） | 10 |
| verbose | 是否显示详细日志 | true |

## 高级功能

### 1. 自定义文件过滤

修改 `NotionSync.java` 中的 `SUPPORTED_EXTENSIONS`：

```java
private static final Set<String> SUPPORTED_EXTENSIONS = new HashSet<>(Arrays.asList(
    ".md", ".txt", ".java", ".py", ".js", ".ts", 
    ".html", ".css", ".json", ".xml", ".yaml", ".yml",
    ".c", ".cpp", ".h", ".go", ".rs"  // 添加更多扩展名
));
```

### 2. 添加进度条

使用第三方库如 [progressbar](https://github.com/ctongfei/progressbar)：

```xml
<dependency>
    <groupId>me.tongfei</groupId>
    <artifactId>progressbar</artifactId>
    <version>0.9.5</version>
</dependency>
```

### 3. 批量处理优化

创建线程池进行并发处理：

```java
ExecutorService executor = Executors.newFixedThreadPool(5);
List<Future<String>> futures = new ArrayList<>();

for (File file : files) {
    Future<String> future = executor.submit(() -> {
        // 处理文件
        return syncFile(file, parentId, folderPath);
    });
    futures.add(future);
}
```

### 4. 增量同步

添加文件修改时间检查：

```java
// 保存同步记录
Map<String, Long> syncHistory = loadSyncHistory();

// 检查文件是否需要同步
if (file.lastModified() > syncHistory.getOrDefault(file.getPath(), 0L)) {
    // 执行同步
    syncFile(file, parentId, folderPath);
    syncHistory.put(file.getPath(), System.currentTimeMillis());
}

// 保存同步记录
saveSyncHistory(syncHistory);
```

## 常见问题

### Q1: 编译错误 "package does not exist"
**解决方案**：
- 确保已正确配置Maven依赖
- 或手动下载所需JAR文件并添加到classpath

### Q2: 运行时错误 "NoClassDefFoundError"
**解决方案**：
```bash
# Windows
java -cp ".;lib/*" NotionSyncApp

# Linux/Mac
java -cp ".:lib/*" NotionSyncApp
```

### Q3: 中文乱码
**解决方案**：
- 确保源文件使用UTF-8编码
- 运行时添加编码参数：
  ```bash
  java -Dfile.encoding=UTF-8 NotionSyncApp
  ```

### Q4: API速率限制错误
**解决方案**：
- 增加 `api.delay.ms` 的值
- 实现重试机制

### Q5: 内存溢出
**解决方案**：
```bash
java -Xmx1024m -jar notion-sync.jar
```

## 创建批处理脚本

### Windows (sync.bat)
```batch
@echo off
chcp 65001 >nul
echo ========================================
echo     Notion 文件夹同步工具 - Java版
echo ========================================
echo.

java -Dfile.encoding=UTF-8 -jar notion-sync-1.0.0-jar-with-dependencies.jar

pause
```

### Linux/Mac (sync.sh)
```bash
#!/bin/bash
echo "========================================"
echo "    Notion 文件夹同步工具 - Java版"
echo "========================================"
echo

java -Dfile.encoding=UTF-8 -jar notion-sync-1.0.0-jar-with-dependencies.jar
```

## 性能优化建议

1. **使用连接池**
   ```java
   ConnectionPool connectionPool = new ConnectionPool(5, 5, TimeUnit.MINUTES);
   OkHttpClient client = new OkHttpClient.Builder()
       .connectionPool(connectionPool)
       .build();
   ```

2. **批量请求**
   - 将多个小文件合并为一个请求
   - 使用Notion的批量API（如果可用）

3. **缓存机制**
   - 缓存已同步的文件信息
   - 避免重复同步未修改的文件

## 扩展功能

### 1. GUI界面
可以使用JavaFX或Swing创建图形界面：
- 文件夹选择器
- 进度显示
- 日志查看器

### 2. 双向同步
实现从Notion到本地的反向同步功能

### 3. 文件监控
使用WatchService监控文件变化，实现实时同步

## 总结

Java版本的优势：
- ✅ 跨平台运行
- ✅ 强类型，更稳定
- ✅ 丰富的生态系统
- ✅ 企业级应用支持

适用场景：
- 需要集成到现有Java项目
- 需要更复杂的业务逻辑
- 需要高性能处理大量文件
- 需要构建桌面GUI应用

祝使用愉快！如有问题，欢迎反馈。🎉 