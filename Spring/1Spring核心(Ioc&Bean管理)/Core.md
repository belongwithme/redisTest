# 基础概念问题

## 什么是 Spring IOC？

IOC即控制反转(Inversion of Control,缩写为IoC),又叫依赖倒置原则,要点在于: 程序要依赖于抽象接口,而不是具体实现.
作用是降低代码间的耦合度.
传统开发时,我们创建对象,管理对象之间的依赖,都是自己写代码控制的,但IOC是把这种控制权交给容器,由容器来负责对象的创建,装配,还有依赖关系的管理,我们有需要直接去容器拿就行.
Spring里IOC容器比如ApplicationContext就是干这个事情的,帮助我们去管理Bean的生命周期和依赖.

## Spring 中的 DI 是什么？

DI即依赖注入(Dependency Injection,缩写为DI),如果需要调用另一个对象帮助时,不需要在代码里面创建被调用者,而是依赖外部的注入.
降低对象之间的耦合度.
常见的视线方式是两种:

- 构造器注入,通过构造器注入依赖对象,适用于必须的依赖.
- Setter注入,通过对象的set方法来设置依赖,适合可选的依赖

## IOC和DI有什么关系

IOC是一种思想,而DI是IOC的一种实现方式,两者相辅相成.

## 什么是 Spring Bean？

简单说,Bean是Java里的一种对象,但不是随便写的类,需要满足一定的规范:

- 有无参构造函数
- 提供Getter和Setter方法
- 可序列化

它不是我们自己New出来的,而是交给框架管理,好处是方便组件复用,代码耦合度低,开发更灵活.

## Spring 中的 BeanFactory 是什么？

BeanFactory是Spring IoC容器的根接口，定义了容器的基本规范，是Spring框架的核心基础设施。从架构设计角度来看，它体现了工厂模式的思想。

**核心特点：**
- **延迟加载**：Bean只有在第一次被请求时才会被创建，这对于大型应用的启动性能很关键
- **基础容器功能**：提供了getBean()、containsBean()等基本的Bean管理能力
- **轻量级**：占用内存小，适合资源受限的环境

**从源码层面理解**：BeanFactory只是一个接口定义，真正的实现在DefaultListableBeanFactory等类中。它的设计遵循了"接口隔离原则"，只暴露必要的容器操作。

## Spring 中的 ApplicationContext 是什么？

ApplicationContext是BeanFactory的子接口，是企业级应用的标准容器实现。如果说BeanFactory是"毛坯房"，那ApplicationContext就是"精装修"。

**相比BeanFactory的增强功能：**

- **预加载**：容器启动时就创建所有单例Bean，便于及早发现配置问题
- **国际化支持**：实现MessageSource接口，支持多语言
- **事件发布机制**：实现ApplicationEventPublisher，支持观察者模式
- **资源访问**：继承ResourcePatternResolver，统一资源访问接口
- **环境抽象**：支持Profile和Property的统一管理

**常用实现类：**

- ClassPathXmlApplicationContext：从类路径加载XML配置
- AnnotationConfigApplicationContext：基于注解的配置

**设计思想**：ApplicationContext体现了"门面模式"，将多个接口的功能整合到一个统一的容器中，这也是为什么在实际开发中我们几乎总是使用ApplicationContext而不是BeanFactory。

## Spring 中的 FactoryBean 是什么？

FactoryBean是Spring提供的一个特殊Bean类型，它本身是一个工厂，用于创建和管理其他Bean实例。这是Spring框架灵活性的重要体现。

**核心接口方法：**

```java
public interface FactoryBean<T> {
    T getObject() throws Exception;  // 返回创建的对象
    Class<?> getObjectType();        // 返回对象类型
    default boolean isSingleton() { return true; } // 是否单例
}
```

**使用场景：**

- **复杂对象创建**：当Bean的创建逻辑复杂，需要大量初始化代码时
- **第三方库集成**：集成没有无参构造函数的第三方类
- **代理对象创建**：MyBatis的MapperFactoryBean就是典型例子

**重要特性：**

- 容器中实际存储的是FactoryBean实例，但getBean()返回的是getObject()的结果
- 如果要获取FactoryBean本身，需要在beanName前加"&"前缀

**设计价值**：FactoryBean体现了"抽象工厂模式"，让Spring容器具备了创建任意复杂对象的能力，这是框架扩展性的重要保证。

## BeanFactory 和 FactoryBean 有什么区别？

BeanFactory是Spring最基础的IOC容器接口,主要是管理Bean的生命周期.
平时用的ApplicationContext就是它的具体实现类,应用里面需要获取Bean的时候,都是通过它拿的.

FactoryBean不太一样,它本身就是一个特殊的Bean,实现了这个接口类,主要是定制化创建其他Bean.
简单来说,他自己就是一个容器的bean,但可以生成别的Bean实例.
当我从容器获取一个FactoryBean类型的Bean时,默认拿到的不是FactoryBean本身,而是它生产出来的Bean,如果要拿到本身,则要在beanName前加"&"前缀.

## Spring 中的 ObjectFactory 是什么？

ObjectFactory是一个函数式接口，提供了获取对象实例的延迟访问机制。它主要用于解决循环依赖和作用域代理等高级场景。

**接口定义：**
```java
@FunctionalInterface
public interface ObjectFactory<T> {
    T getObject() throws BeansException;
}
```

**核心作用：**

- **延迟获取**：不是直接注入Bean，而是注入一个获取Bean的工厂
- **循环依赖解决**：在三级缓存中，singletonFactories存储的就是ObjectFactory
- **作用域处理**：request、session等作用域Bean的代理创建

**实际应用场景：**

- 当需要在单例Bean中使用prototype Bean时
- 解决构造器循环依赖的辅助手段
- 在AOP代理创建过程中的关键角色

**与FactoryBean的区别：**

- ObjectFactory更轻量，只有一个方法，主要用于内部机制
- FactoryBean是面向开发者的扩展点，功能更丰富
- ObjectFactory通常不直接使用，而是由框架内部管理

**底层原理**：在Spring的三级缓存机制中，ObjectFactory是解决循环依赖的关键。它允许提前暴露正在创建中的Bean的引用，而不需要完全初始化，这是Spring容器设计精妙之处的体现。

## 说下 Spring Bean 的生命周期？

Bean的生命周期是Spring容器管理Bean从创建到销毁的完整过程,我把它理解为一个可扩展的对象管理框架,主要分为五个阶段:

1. 实例化: 通过反射创建Bean对象
2. 属性填充: 注入依赖(@Autowired生效)
3. 初始化: 执行初始化方法(@PostConstruce,InitializingBean)
4. 使用: Bean正常工作
5. 销毁: 清理资源(@PreDestroy,DisposableBean)

初始化比较复杂一点,这里我详细说一下:

1. 首先会检查Aware的相关接口并设置相关依赖
2. 然后调用BeanPostProcessor接口前置处理
3. 然后看是否实现了InitializingBean接口,如果实现了,会调用afterPropertiesSet方法
4. 再看是否配置自定义的init-method方法,如果配置了,容器会在属性都注入好以后再去执行这些初始化逻辑
5. 等这些都跑完，Spring 会再走一次 BeanPostProcessor 的后置处理,它经常被用作Bean内容的更改,这个阶段通常也是 AOP 生成代理对象的关键点。到这一步 Bean 就准备好，可以被正常使用了

## Spring Bean 一共有几种作用域？

Spring Bean的作用域本质上是控制Bean实例的生命周期和共享范围的机制,它决定了Bean在Spring容器中的行为和可见性.

Spring提供了以下几种作用域:

- singleton: 单例模式,默认作用域,一个Spring容器中只有一个Bean实例
- prototype: 原型模式,每次获取都会创建一个新的Bean实例
- request: 请求作用域,每次HTTP请求都会创建一个新的Bean实例
- session: 会话作用域,每次HTTP会话都会创建一个新的Bean实例
- application: 应用作用域,Spring容器在启动时创建一个Bean实例,并将其与Web应用的整个生命周期绑定

**作用域选择的设计原则：**

- 优先使用singleton，除非明确需要其他作用域
- singleson多个线程访问同一个Bean时可能存在现成不安全问题
- 有状态的Bean考虑prototype
- Web相关的临时数据使用request/session
- 注意作用域混用时的依赖注入问题（短生命周期注入长生命周期）

## 如何保证Bean线程的安全

当Bean默认是单例时,多个线程会公用这个单例,如果此时Bean有可变的成员变量,就会存在线程安全问题.

1. 在Bean对象中尽量避免定义可变的成员变量(但不太现实)
2. 在类中定义一个ThreadLocal成员变量,将需要的可变成员变量保存在ThreadLocal中


## 将一个类声明为Spring 的Bean 的注解有哪些?

一般使用@Autowire注解自动装配Bean,所以要想把类标识成可用于@Autowire注解自动装备的Bean的类,最基础的是@Component,一般通用类用这个注解就可以;
然后有三个衍生注解:

- @Controller: 用于控制器层处理请求
- @Service: 用于业务层处理业务逻辑
- @Repository: 用于数据访问层处理数据操作

它们的功能和@Component没有区别,只是语义上的区分,用于提高代码的可读性.

另外还有@Bean注解,通常在@Configuration配置类里用,手动定义Bean,比如引入第三方类的时候就用@Bean来声明.

## 注入Bean的注解有哪些?

通常用到的有@Autowired,@Resource,@Inject,它们的作用都是将Bean注入到目标对象中.

- @Autowired: Spring自带,用的最多,默认按类型去找Bean注入,如果同一个类型有多个Bean,则需要用@Qualifier指定Bean名称
- @Resource: JDK自带的注解,不属于Spring,它默认按名称匹配,找不到才会按类型,也可以用你name属性指定要注入的Bean名称
- @Inject: Java EE注解,功能和@Autowired类似

## @Autowired底层实现原理是什么?

@Autowired是Spring实现依赖注入的注解,底层主要靠IOC容器和后置处理器来实现.

当Spring启动时,IOC容器会先初始化所有的Bean.
在Bean创建过程中,会有个属性注入的阶段.

这时候AutowiredAnnotationBeanPostProcessor这个后置处理器就会工作.
它会扫描Bean里带有@Autowired注解的字段和方法,然后去容器里找需要注入的Bean.
查找的时候会先按类型去找,如果找到多个,则再按属性名称或者方法参数名去匹配名称.
找到对应的Bean后,就会注入.

如果此时注解的required属性为true,那找不到匹配的Bean就会报错,如果是false,则留空.
整个过程就是通过后置处理器在bean初始化阶段完成依赖的查找和注入.

## @Autowired和@Resource的区别是什么?

主要是来源和注入时的匹配方式不同.

@Autowired是Spring自带的注解,属于Spring框架的一部分,它默认按类型注入,如果同一个类型有多个Bean,则需要用@Qualifier指定Bean名称.

@Resource是JDK自带的注解,不属于Spring,它默认按名称匹配,找不到才会按类型,也可以用name属性指定要注入的Bean名称.

## 什么是Spring的三级缓存?

Spring的三级缓存是解决单例Bean循环依赖的核心机制，位于DefaultSingletonBeanRegistry类中。这个设计体现了Spring框架对复杂依赖关系的比较优雅处理。

### 三级缓存的具体结构

**从源码角度理解，三级缓存实际上是三个Map：**

```java
// 一级缓存：完整的单例Bean实例
private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

// 二级缓存：早期Bean实例（未完全初始化）
private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);

// 三级缓存：单例Bean工厂
private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);
```

### 每级缓存的作用和时机

**一级缓存（singletonObjects）：**

- **存储内容**：完全初始化完成的单例Bean
- **使用时机**：Bean完成整个生命周期后存入
- **获取优先级**：最高，优先从这里获取

**二级缓存（earlySingletonObjects）：**

- **存储内容**：实例化但未完全初始化的Bean（解决循环依赖的关键）
- **使用时机**：当检测到循环依赖时，提前暴露Bean实例
- **核心价值**：避免重复创建代理对象

**三级缓存（singletonFactories）：**

- **存储内容**：ObjectFactory工厂对象
- **使用时机**：Bean实例化后立即存入
- **作用机制**：通过工厂延迟决定是返回原始Bean还是代理Bean

### 循环依赖解决流程（关键理解点）

以A依赖B，B依赖A为例：

```java
@Service
public class ServiceA {
    @Autowired
    private ServiceB serviceB;
}

@Service  
public class ServiceB {
    @Autowired
    private ServiceA serviceA;
}
```

**详细解决步骤：**

1. **创建A的实例**
   - 实例化A（调用构造函数）
   - 将A的ObjectFactory存入三级缓存
   - 开始属性注入，发现需要B

2. **创建B的实例**
   - 实例化B（调用构造函数）
   - 将B的ObjectFactory存入三级缓存
   - 开始属性注入，发现需要A

3. **关键时刻：B需要A**
   - 从一级缓存找A：没有（A还没完全初始化）
   - 从二级缓存找A：没有
   - 从三级缓存找A：找到A的ObjectFactory
   - 调用ObjectFactory.getObject()获得A的实例（可能是代理）
   - 将A的实例放入二级缓存，从三级缓存移除
   - B拿到A的引用，完成属性注入

4. **完成B的创建**
   - B初始化完成，放入一级缓存
   - 返回给A

5. **完成A的创建**
   - A拿到B的引用，完成属性注入
   - A初始化完成，放入一级缓存

### 为什么需要三级缓存？二级不够吗？

这是面试官经常追问的核心问题：

**如果只有二级缓存的问题：**

首先明确二级缓存的设计：一级存完整的Bean，二级存早期的Bean实例。 

在Spring的Bean生命周期大概是:

1. 实例化阶段：通过反射创建Bean对象（此时只是一个普通对象）
2. 属性填充阶段：注入依赖
3. 初始化阶段：执行各种初始化回调
4. 后置处理阶段：AOP代理就是在这个阶段创建的

如果是二级缓存,意味着所有Bean在实例化后就要完成AOP代理,并放入二级缓存.
但问题是:

1. AOP切面信息还没有被完全解析
2. 动态代理条件判断负责: 需要代理的条件可能依赖于其他Bean的状态,而这些Bean可能还没有被创建完成.

三级缓存可以解决:

1. 代理决策的延迟: 真正发生循环依赖时,才通过三级缓存中ObjectFactory的getObject()方法来决定是否创建代理对象.
2. 一致性保障: 确保所有依赖方注入的是同一个对象(都是原始或者都是代理)


**三级缓存的必要性：**

- **延迟代理创建**：只有在真正需要时才决定是否创建代理
- **统一处理机制**：无论是否有AOP，都用相同的流程处理
- **避免重复代理**：确保每个Bean只有一个代理实例

**核心设计思想：**
三级缓存体现了"延迟决策"的设计模式。通过ObjectFactory，Spring将"是否需要代理"的决策延迟到真正需要Bean的时候，这样既保证了功能正确性，又避免了不必要的代理创建。

### 无法解决的循环依赖场景

**构造器循环依赖：**

```java
@Service
public class ServiceA {
    public ServiceA(ServiceB serviceB) {} // 无法解决
}
```

- 原因：构造器注入时Bean还没有实例化，无法提前暴露

**prototype作用域循环依赖：**

```java
@Service
@Scope("prototype")
public class ServiceA {
    @Autowired
    private ServiceB serviceB;
}
```

- 原因：prototype Bean不会被缓存，每次都创建新实例

## 什么是循环依赖？

循环依赖是指两个或多个Bean在依赖注入时形成了闭环依赖关系，即A依赖B，B又依赖A，或者A→B→C→A这样的依赖链。

### 循环依赖的类型

**1. 直接循环依赖（最常见）：**

```java
@Service
public class ServiceA {
    @Autowired
    private ServiceB serviceB;
}

@Service  
public class ServiceB {
    @Autowired
    private ServiceA serviceA;
}
```

**2. 间接循环依赖：**

```java
@Service
public class ServiceA {
    @Autowired
    private ServiceB serviceB;
}

@Service
public class ServiceB {
    @Autowired
    private ServiceC serviceC;
}

@Service
public class ServiceC {
    @Autowired
    private ServiceA serviceA; // A→B→C→A
}
```

### 循环依赖的问题

如果没有特殊处理机制，循环依赖会导致：

1. **无限递归创建**：创建A时需要B，创建B时又需要A，陷入死循环
2. **StackOverflowError**：递归调用栈溢出
3. **应用启动失败**：容器无法完成Bean的初始化

### Spring如何检测循环依赖

Spring通过一个正在创建的Bean集合（`singletonsCurrentlyInCreation`）来检测循环依赖：

```java
// 伪代码示意
Set<String> singletonsCurrentlyInCreation = new HashSet<>();

public Object createBean(String beanName) {
    if (singletonsCurrentlyInCreation.contains(beanName)) {
        // 检测到循环依赖
        throw new BeanCurrentlyInCreationException(beanName);
    }
    
    singletonsCurrentlyInCreation.add(beanName);
    try {
        // 创建Bean的逻辑
        return doCreateBean(beanName);
    } finally {
        singletonsCurrentlyInCreation.remove(beanName);
    }
}
```

### 循环依赖的解决策略

**Spring能解决的情况：**

- 单例Bean的属性注入循环依赖（通过三级缓存）
- 设值注入（Setter注入）的循环依赖

**Spring无法解决的情况：**

- 构造器注入的循环依赖
- prototype作用域的循环依赖
- 单例Bean的构造器循环依赖

### 最佳实践

**1. 代码设计层面避免：**

```java
// 不好的设计
@Service
public class UserService {
    @Autowired
    private OrderService orderService;
}

@Service
public class OrderService {
    @Autowired
    private UserService userService;
}

// 更好的设计：引入中间层
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
}

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserService userService; // 单向依赖
}
```

**2. 使用@Lazy注解：**

```java
@Service
public class ServiceA {
    @Autowired
    @Lazy // 延迟加载，打破循环
    private ServiceB serviceB;
}
```

**3. 重新设计架构：**

- 遵循单一职责原则
- 合理划分层次结构
- 考虑使用事件驱动模式
- 引入中介者模式

循环依赖虽然Spring能够解决，但它往往暴露了设计上的问题，最好的解决方案是重新设计代码架构来避免循环依赖的产生。

## Spring可以出现两个ID相同的Bean吗?如果不行会在什么时候报错?

Spring里不能有两个ID相同的Bean,因为ID是容器中标识Bean的唯一标识,重复会导致容器无法确定要注入哪个Bean.
在启动时容器加载配置文件或者扫描组件的时候会检测到然后抛出异常,导致启动失败.

## 什么是Spring内部的Bean?

定义在另一个Bean里面的Bean.
主要用来给外部Bean做属性注入的,比如一个类里定义了另一个类,这个类就是内部Bean.
这个Bean只能被包含它的那个外部Bean使用,其他Bean无法直接使用它.
简单说: 内部bean就是满足局部依赖的,减少不必要的全局Bean定义,让配置更清爽.
