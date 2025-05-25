/**
 * SPI (Service Provider Interface) 机制详解
 * 
 * SPI是什么？
 * SPI是Java提供的一种服务发现机制，允许程序在运行时发现和加载实现特定接口的服务提供者。
 * 简单说就是：定义接口，让别人实现，程序自动找到并使用这些实现。
 */

import java.util.ServiceLoader;

// ==================== 第一步：理解SPI的基本概念 ====================

/**
 * 用生活中的例子理解SPI：
 * 
 * 想象你开了一家餐厅，你制定了一个"厨师接口"标准：
 * - 所有厨师都必须会做菜
 * - 但具体怎么做菜，由每个厨师自己决定
 * 
 * SPI就是这样一个机制：
 * 1. 你（Java核心库）定义接口标准
 * 2. 厨师们（第三方库）按照标准实现
 * 3. 餐厅系统（ServiceLoader）自动找到所有厨师并让他们工作
 */

// 1. 定义服务接口（这个通常在核心库中）
interface DatabaseDriver {
    void connect(String url);
    void executeQuery(String sql);
    String getDriverName();
}

// ==================== 第二步：服务提供者实现接口 ====================

// 2. MySQL厂商实现这个接口
class MySQLDriver implements DatabaseDriver {
    @Override
    public void connect(String url) {
        System.out.println("MySQL驱动连接到: " + url);
    }
    
    @Override
    public void executeQuery(String sql) {
        System.out.println("MySQL执行SQL: " + sql);
    }
    
    @Override
    public String getDriverName() {
        return "MySQL Driver 8.0";
    }
}

// 3. Oracle厂商也实现这个接口
class OracleDriver implements DatabaseDriver {
    @Override
    public void connect(String url) {
        System.out.println("Oracle驱动连接到: " + url);
    }
    
    @Override
    public void executeQuery(String sql) {
        System.out.println("Oracle执行SQL: " + sql);
    }
    
    @Override
    public String getDriverName() {
        return "Oracle Driver 19c";
    }
}

// ==================== 第三步：SPI配置文件 ====================

/**
 * 关键步骤：在META-INF/services/目录下创建配置文件
 * 
 * 文件名：com.example.DatabaseDriver（接口的全限定名）
 * 文件内容：
 * com.mysql.MySQLDriver
 * com.oracle.OracleDriver
 * 
 * 这个文件告诉ServiceLoader："这些类实现了DatabaseDriver接口"
 */

// ==================== 第四步：使用ServiceLoader加载服务 ====================

public class SPI机制详解 {
    
    public static void main(String[] args) {
        演示SPI基本用法();
        System.out.println("\n==================================================\n");
        演示JDBC中的SPI();
        System.out.println("\n==================================================\n");
        演示SPI解决的问题();
    }
    
    // 演示SPI的基本用法
    public static void 演示SPI基本用法() {
        System.out.println("=== SPI基本用法演示 ===");
        
        // ServiceLoader会自动扫描META-INF/services/目录
        // 找到所有实现DatabaseDriver接口的类并加载
        ServiceLoader<DatabaseDriver> drivers = ServiceLoader.load(DatabaseDriver.class);
        
        System.out.println("发现的数据库驱动：");
        for (DatabaseDriver driver : drivers) {
            System.out.println("- " + driver.getDriverName());
            driver.connect("jdbc:database://localhost:3306/test");
            driver.executeQuery("SELECT * FROM users");
            System.out.println();
        }
    }
    
    // 演示JDBC中真实的SPI使用
    public static void 演示JDBC中的SPI() {
        System.out.println("=== JDBC中的SPI机制 ===");
        
        /*
         * JDBC的SPI工作流程：
         * 
         * 1. java.sql.Driver接口在rt.jar中（核心库）
         * 2. MySQL驱动jar包中有：
         *    - com.mysql.cj.jdbc.Driver类（实现java.sql.Driver）
         *    - META-INF/services/java.sql.Driver文件
         *    - 文件内容：com.mysql.cj.jdbc.Driver
         * 
         * 3. DriverManager使用ServiceLoader加载所有驱动
         */
        
        try {
            // 模拟DriverManager的内部逻辑
            System.out.println("DriverManager正在加载数据库驱动...");
            
            // 这里就是关键：使用线程上下文类加载器
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            System.out.println("使用的类加载器: " + contextClassLoader);
            
            // ServiceLoader内部会使用这个类加载器来加载驱动实现类
            ServiceLoader<java.sql.Driver> drivers = ServiceLoader.load(java.sql.Driver.class);
            
            System.out.println("扫描到的JDBC驱动：");
            for (java.sql.Driver driver : drivers) {
                System.out.println("- " + driver.getClass().getName());
            }
            
        } catch (Exception e) {
            System.out.println("驱动加载失败: " + e.getMessage());
        }
    }
    
    // 演示SPI解决的核心问题
    public static void 演示SPI解决的问题() {
        System.out.println("=== SPI解决的核心问题 ===");
        
        System.out.println("问题场景：");
        System.out.println("1. java.sql.DriverManager在rt.jar中，由Bootstrap ClassLoader加载");
        System.out.println("2. com.mysql.cj.jdbc.Driver在应用classpath中，由Application ClassLoader加载");
        System.out.println("3. 按照双亲委派，Bootstrap ClassLoader看不到Application ClassLoader的类");
        System.out.println();
        
        System.out.println("SPI的解决方案：");
        System.out.println("1. DriverManager通过Thread.currentThread().getContextClassLoader()获取上下文类加载器");
        System.out.println("2. ServiceLoader使用这个上下文类加载器来加载驱动实现类");
        System.out.println("3. 这样就绕过了双亲委派的限制！");
        System.out.println();
        
        // 演示线程上下文类加载器的作用
        ClassLoader bootstrapLoader = String.class.getClassLoader(); // null表示Bootstrap
        ClassLoader appLoader = SPI机制详解.class.getClassLoader();
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        
        System.out.println("类加载器对比：");
        System.out.println("Bootstrap ClassLoader: " + bootstrapLoader);
        System.out.println("Application ClassLoader: " + appLoader);
        System.out.println("Thread Context ClassLoader: " + contextLoader);
        System.out.println("Context ClassLoader == App ClassLoader: " + (contextLoader == appLoader));
    }
}

// ==================== 第五步：深入理解ServiceLoader的工作原理 ====================

/**
 * ServiceLoader的工作原理（简化版）
 */
class 简化版ServiceLoader<S> {
    
    private Class<S> service;
    private ClassLoader loader;
    
    public 简化版ServiceLoader(Class<S> service, ClassLoader loader) {
        this.service = service;
        this.loader = loader;
    }
    
    public static <S> 简化版ServiceLoader<S> load(Class<S> service) {
        // 关键：使用线程上下文类加载器
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return new 简化版ServiceLoader<>(service, cl);
    }
    
    public void 演示加载过程() {
        System.out.println("=== ServiceLoader工作原理 ===");
        
        // 1. 构造配置文件路径
        String configFile = "META-INF/services/" + service.getName();
        System.out.println("1. 查找配置文件: " + configFile);
        
        // 2. 使用指定的类加载器读取配置文件
        System.out.println("2. 使用类加载器: " + loader);
        
        // 3. 解析配置文件，获取实现类名列表
        System.out.println("3. 解析配置文件，获取实现类列表");
        
        // 4. 使用类加载器加载每个实现类
        System.out.println("4. 逐个加载实现类并创建实例");
        
        // 5. 返回实现类实例的迭代器
        System.out.println("5. 返回可迭代的服务实例");
    }
}

// ==================== 第六步：SPI的实际应用场景 ====================

class SPI应用场景 {
    
    public static void 展示应用场景() {
        System.out.println("=== SPI的实际应用场景 ===");
        
        System.out.println("1. JDBC驱动加载");
        System.out.println("   - 接口：java.sql.Driver");
        System.out.println("   - 实现：MySQL、Oracle、PostgreSQL等驱动");
        System.out.println();
        
        System.out.println("2. 日志框架");
        System.out.println("   - 接口：SLF4J的ILoggerFactory");
        System.out.println("   - 实现：Logback、Log4j等");
        System.out.println();
        
        System.out.println("3. 序列化框架");
        System.out.println("   - 接口：Java的序列化SPI");
        System.out.println("   - 实现：各种序列化库");
        System.out.println();
        
        System.out.println("4. Spring Boot自动配置");
        System.out.println("   - 基于类似SPI的机制");
        System.out.println("   - spring.factories文件");
        System.out.println();
        
        System.out.println("5. Java 9模块系统");
        System.out.println("   - uses和provides关键字");
        System.out.println("   - 模块化的服务发现");
    }
}

// ==================== 第七步：SPI vs 其他模式的对比 ====================

class SPI对比分析 {
    
    public static void 对比其他模式() {
        System.out.println("=== SPI vs 其他设计模式 ===");
        
        System.out.println("SPI vs 工厂模式：");
        System.out.println("- 工厂模式：需要显式注册实现类");
        System.out.println("- SPI：通过配置文件自动发现实现类");
        System.out.println();
        
        System.out.println("SPI vs 依赖注入：");
        System.out.println("- 依赖注入：需要容器管理依赖关系");
        System.out.println("- SPI：更轻量，只需要配置文件");
        System.out.println();
        
        System.out.println("SPI的优点：");
        System.out.println("- 解耦：接口定义者不需要知道具体实现");
        System.out.println("- 扩展性：新增实现只需要添加jar包和配置");
        System.out.println("- 标准化：Java内置支持，标准统一");
        System.out.println();
        
        System.out.println("SPI的缺点：");
        System.out.println("- 性能：需要扫描classpath，启动时开销大");
        System.out.println("- 调试：实现类是动态加载的，调试困难");
        System.out.println("- 类型安全：运行时才能发现实现类问题");
    }
} 