// 双亲委派模型的问题示例

// 1. 首先，让我们看看双亲委派模型的正常工作流程
public class 双亲委派问题示例 {
    
    // 问题场景1：SPI机制的困境
    // 假设我们有一个接口在Java核心库中定义
    // 比如 java.sql.Driver 接口（在rt.jar中，由Bootstrap ClassLoader加载）
    
    public static void 演示JDBC驱动加载问题() {
        /*
         * 问题描述：
         * 1. java.sql.Driver 接口在 rt.jar 中，由 Bootstrap ClassLoader 加载
         * 2. MySQL驱动实现类在应用的classpath中，由 Application ClassLoader 加载
         * 3. 按照双亲委派模型，Bootstrap ClassLoader 无法"看到"子加载器加载的类
         * 4. 这就产生了矛盾：核心库需要使用应用层的实现类
         */
        
        // 传统方式会失败：
        // DriverManager（在核心库中）无法直接加载MySQL驱动类
        
        // 解决方案：使用线程上下文类加载器
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        System.out.println("当前线程上下文类加载器: " + contextClassLoader);
        
        // DriverManager内部会使用这个上下文类加载器来加载驱动实现类
        // 这样就绕过了双亲委派的限制
    }
    
    // 问题场景2：Web容器的类隔离需求
    public static void 演示Web容器问题() {
        /*
         * 问题描述：
         * 在Tomcat这样的Web容器中，需要实现：
         * 1. 不同Web应用之间的类隔离
         * 2. Web应用可以使用自己版本的类库，即使与容器的版本冲突
         * 3. 某些类需要在应用间共享
         * 
         * 如果严格遵循双亲委派：
         * - 所有应用都会使用容器级别的类库版本
         * - 无法实现应用间的类隔离
         * - 无法支持不同应用使用不同版本的同一个库
         */
        
        System.out.println("Tomcat的解决方案：");
        System.out.println("1. WebappClassLoader 优先加载应用自己的类");
        System.out.println("2. 只有加载不到时才委派给父加载器");
        System.out.println("3. 这样实现了应用级别的类隔离");
    }
    
    // 问题场景3：热部署的需求
    public static void 演示热部署问题() {
        /*
         * 问题描述：
         * 在开发环境或某些生产环境中，我们希望能够：
         * 1. 在不重启JVM的情况下更新类定义
         * 2. 动态加载新的类版本
         * 
         * 双亲委派的限制：
         * - 一旦类被加载，就无法被卸载（除非类加载器被回收）
         * - 同一个类加载器不能加载同名类的不同版本
         */
        
        System.out.println("热部署的解决方案：");
        System.out.println("1. 为每个版本创建新的类加载器");
        System.out.println("2. 绕过双亲委派，直接加载新版本的类");
        System.out.println("3. 通过反射或接口来使用新版本的类");
    }
}

// 具体的代码示例：自定义类加载器打破双亲委派
class 打破双亲委派的类加载器 extends ClassLoader {
    
    private String classPath;
    
    public 打破双亲委派的类加载器(String classPath) {
        this.classPath = classPath;
    }
    
    // 重写loadClass方法来打破双亲委派
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        // 对于我们想要特殊处理的类，不委派给父加载器
        if (name.startsWith("com.myapp.hotdeploy")) {
            return findClass(name);
        }
        
        // 对于其他类，仍然使用双亲委派
        return super.loadClass(name);
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            // 从指定路径读取类文件
            byte[] classData = loadClassData(name);
            if (classData == null) {
                throw new ClassNotFoundException();
            }
            
            // 直接定义类，不经过父加载器
            return defineClass(name, classData, 0, classData.length);
        } catch (Exception e) {
            throw new ClassNotFoundException("无法加载类: " + name, e);
        }
    }
    
    private byte[] loadClassData(String className) {
        // 这里应该实现从文件系统、网络或其他地方读取类字节码的逻辑
        // 为了示例简化，这里返回null
        return null;
    }
}

// 线程上下文类加载器的使用示例
class SPI机制示例 {
    
    public static void 演示SPI机制() {
        // 保存原来的上下文类加载器
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        
        try {
            // 设置自定义的类加载器作为上下文类加载器
            ClassLoader customClassLoader = new 打破双亲委派的类加载器("/custom/path");
            Thread.currentThread().setContextClassLoader(customClassLoader);
            
            // 现在，SPI机制会使用我们的自定义类加载器来加载服务实现类
            // 这样就绕过了双亲委派的限制
            
            System.out.println("使用自定义类加载器进行SPI加载");
            
        } finally {
            // 恢复原来的上下文类加载器
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
}

// 实际应用场景：模拟JDBC驱动加载
class JDBC驱动加载示例 {
    
    public static void 模拟DriverManager工作原理() {
        /*
         * DriverManager的工作原理：
         * 1. DriverManager类在rt.jar中，由Bootstrap ClassLoader加载
         * 2. 但它需要加载应用classpath中的数据库驱动实现类
         * 3. 解决方案：使用线程上下文类加载器
         */
        
        System.out.println("=== JDBC驱动加载过程 ===");
        
        // 1. DriverManager获取当前线程的上下文类加载器
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        System.out.println("上下文类加载器: " + contextClassLoader);
        
        // 2. 使用上下文类加载器加载驱动类
        try {
            // 这里模拟DriverManager内部的逻辑
            Class<?> driverClass = contextClassLoader.loadClass("com.mysql.cj.jdbc.Driver");
            System.out.println("成功加载驱动类: " + driverClass.getName());
            System.out.println("驱动类的类加载器: " + driverClass.getClassLoader());
            
        } catch (ClassNotFoundException e) {
            System.out.println("驱动类加载失败: " + e.getMessage());
        }
        
        System.out.println("\n这样就解决了核心库访问应用类的问题！");
    }
} 