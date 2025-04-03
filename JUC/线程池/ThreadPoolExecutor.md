@[TOC](ThreadPoolExecutor)

# 基础概念
## ThreadPoolExecutor的核心作用和设计目的是什么？
我理解ThreadPoolExecutor是Java并发编程中非常核心的组件.
它的主要目的是解决线程创建和销毁带来的性能开销问题。
在实际开发中，如果每个任务都创建一个新线程，会导致以下问题：
- 线程创建和销毁的开销很大，会消耗大量系统资源
- 线程数量无限制增长，可能耗尽系统资源，导致OOM
- 线程之间的竞争会导致上下文切换开销增加
ThreadPoolExecutor通过预先创建一组工作线程，重复利用这些线程来执行任务，避免了频繁创建和销毁线程的开销。
它实现了一种"池化"思想，类似于数据库连接池、对象池等资源池化技术。

## 线程池解决了哪些并发编程中的问题？
线程池解决的并发编程问题
线程池主要解决了以下并发编程中的问题：
- 资源管理问题：通过控制线程数量，避免系统资源耗尽
- 性能优化问题：减少线程创建与销毁的开销，提高响应速度
- 线程管理难题：统一管理线程的生命周期，简化并发编程模型
- 任务调度问题：提供灵活的任务提交、执行与排队机制
- 系统稳定性问题：通过拒绝策略防止系统过载
## ThreadPoolExecutor与Executor、ExecutorService接口的关系
这三者之间存在继承关系，形成了一个完整的执行框架体系：
1. Executor接口：最顶层的接口，只定义了一个execute方法，用于提交Runnable任务
```java
   public interface Executor {
       void execute(Runnable command);
   }
```
2. ExecutorService接口：扩展了Executor接口，增加了线程池生命周期管理和Future任务支持
```java
   public interface ExecutorService extends Executor {
       void shutdown();
       List<Runnable> shutdownNow();
       boolean isShutdown();
       boolean isTerminated();
       // 提交有返回值的任务
       <T> Future<T> submit(Callable<T> task);
       // 其他方法...
   }
```
3. ThreadPoolExecutor类：ExecutorService的具体实现类，提供了线程池的完整功能
```java
   public class ThreadPoolExecutor extends AbstractExecutorService {
       // 实现了执行、调度、生命周期管理等核心功能
   }
```
这种设计体现了接口与实现分离的原则，使用者可以面向接口编程，而不需要关心具体实现细节。
这种抽象层次让代码更加灵活，可以根据不同场景方便地替换不同的ExecutorService实现，比如从ThreadPoolExecutor切换到ForkJoinPool，而不需要修改大量代码。
ThreadPoolExecutor的设计体现了Java并发包的精髓 - 通过高层抽象简化并发编程，同时提供足够的灵活性来满足复杂场景的需求。

## 核心参数
### 七大核心参数的含义及其作用：
#### corePoolSize
核心线程数是线程池中长期保持活跃的线程数量。
我理解它有以下特点：
- 即使这些线程空闲，也不会被回收（除非设置allowCoreThreadTimeOut为true）
- 当任务提交时，如果当前运行的线程数小于corePoolSize，即使有空闲线程，也会创建新线程
- 核心线程是线程池的"常驻居民"，负责处理正常负载下的任务
在实际项目中，我通常根据CPU核心数来设置，例如IO密集型任务可以设置为2倍CPU核心数（如8核CPU可设为16），计算密集型任务则接近于CPU核心数（可能略高一点）。
#### maximumPoolSize
最大线程数定义了线程池可以创建的最大线程数量：
- 只有当工作队列已满时，才会创建超过corePoolSize的线程
- 这些"临时工"线程会在空闲一段时间后被回收
maximumPoolSize为线程池能够创建的线程数量设置了上限，防止资源耗尽
过大的值可能导致系统资源耗尽，过小则可能无法充分利用系统资源处理突发流量。
#### keepAliveTime
这个参数定义了非核心线程在空闲状态下的最大存活时间：    
- 仅适用于超过corePoolSize的空闲线程
- 当一个线程空闲时间超过keepAliveTime，如果当前线程数大于corePoolSize，该线程将被终止
- 相当于临时工的"解雇期限"
这个值设置得当可以在资源利用和系统开销之间取得平衡。
#### unit
与keepAliveTime配合使用的时间单位:
- TimeUnit.SECONDS
- TimeUnit.MINUTES
它提供了灵活性，让我们可以用最自然的方式表达时间。
#### workQueue
工作队列用于存储等待执行的任务：
- 当所有核心线程都在工作时，新提交的任务会进入此队列等待
- 只有当队列满了，才会创建新的非核心线程
- 不同类型的队列对线程池的行为有显著影响
常用的队列类型包括：
1. ArrayBlockingQueue：有界队列，基于数组实现，FIFO原则
```java 
  new ArrayBlockingQueue<>(1000) // 容量为1000的有界队列 
```
2. LinkedBlockingQueue：可选有界/无界队列，基于链表实现
```java 
  new LinkedBlockingQueue<>() // 无界队列
   new LinkedBlockingQueue<>(1000) // 容量为1000的有界队列 
```
3. SynchronousQueue：不存储元素的阻塞队列，每个插入操作必须等待另一个线程的移除操作
```java 
  new SynchronousQueue<>() // 直接交付任务，无缓冲
```
4. PriorityBlockingQueue：支持优先级的无界阻塞队列
```java 
  new PriorityBlockingQueue<>() // 任务按优先级执行
```
针对不同场景选择不同队列：
- 高并发短任务用SynchronousQueue，
- 常规任务用有界的LinkedBlockingQueue，
- 优先级任务用PriorityBlockingQueue。
#### threadFactory
线程工厂负责创建线程池中的工作线程：
- 可以自定义线程的命名、优先级、是否为守护线程等属性
- 有助于线程的管理和问题排查
- 可以添加额外的监控或者初始化逻辑
- 可以自定义ThreadFactory来实现更好的可观测性：
```java
ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
    .setNameFormat("order-process-pool-%d")
    .setDaemon(false)
    .setPriority(Thread.NORM_PRIORITY)
    .setUncaughtExceptionHandler((thread, e) -> log.error("线程异常：", e))
    .build();
 ```
 这样在线程转储或者日志中能立即识别出线程池中的线程。
#### rejectedExecutionHandler
当线程池饱和（线程数达到maximumPoolSize且队列已满）时，新提交的任务会被拒绝，拒绝策略决定了如何处理这些任务：
- AbortPolicy：默认策略，直接抛出RejectedExecutionException异常
- CallerRunsPolicy：在调用者线程执行被拒绝的任务，提供反压机制
- DiscardPolicy：静默丢弃被拒绝的任务
- DiscardOldestPolicy：丢弃队列头部（最旧）的任务，然后尝试重新提交新任务
在我的实践中，通常为关键业务使用CallerRunsPolicy，提供背压机制；对于可丢弃的任务使用DiscardPolicy；有时也会实现自定义的拒绝策略，如记录到消息队列以便后续处理。

## 把线程池大小设置越大越好吗？
1. 设置线程池大小的依据原则
CPU 密集型任务(N+1)： 这种任务消耗的主要是 CPU 资源，可以将线程数设置为 N（CPU 核心数）+1。比 CPU 核心数多出来的一个线程是为了防止线程偶发的缺页中断，或者其它原因导致的任务暂停而带来的影响。一旦任务暂停，CPU 就会处于空闲状态，而在这种情况下多出来的一个线程就可以充分利用 CPU 的空闲时间。

I/O 密集型任务(2N)： 这种任务应用起来，系统会用大部分的时间来处理 I/O 交互，而线程在处理 I/O 的时间段内不会占用 CPU 来处理，这时就可以将 CPU 交出给其它线程使用。因此在 I/O 密集型任务的应用中，我们可以多配置一些线程，具体的计算方法是 2N。

2. 把线程池大小设置越大，会增加上下文切换成本，意味着消耗了大量的 CPU 时间。

## 工作原理
### 线程池的工作流程是怎样的？
ThreadPoolExecutor的工作流程可以概括为任务提交、任务调度和任务执行三个阶段：
1. 任务提交阶段：客户端通过execute()或submit()方法提交任务
2. 任务调度阶段：线程池根据当前状态决定如何处理提交的任务
3. 任务执行阶段：工作线程从队列获取任务并执行
整个工作流程采用了生产者-消费者模式。
其中execute/submit方法是生产者，向线程池提交任务；
而Worker线程是消费者，不断从队列中获取任务执行。
### 新任务提交时的处理逻辑（任务调度策略）
当通过execute()方法提交一个新任务时，线程池会按照以下策略处理：
- 核心线程优先：如果运行的线程数少于corePoolSize，会创建新线程来处理任务，即使其他工作线程是空闲的
- 队列存储：如果运行的线程数等于或大于corePoolSize，会将任务放入队列，而不是创建新线程
- 创建临时线程：如果队列已满，但运行的线程数少于maximumPoolSize，会创建新线程来处理任务
- 拒绝策略：如果队列已满且运行的线程数达到maximumPoolSize，会触发拒绝策略

这种策略实现了资源的最优利用：优先使用核心线程，其次是队列缓冲，最后才是临时线程，在保证性能的同时避免资源过度消耗。
### Worker线程的生命周期管理
Worker是ThreadPoolExecutor中的内部类，它继承自AQS并实现了Runnable接口，每个Worker关联一个线程。
Worker线程的生命周期包括创建、运行、终止三个阶段：
1. 创建阶段
当需要新线程时，线程池会创建一个Worker实例，并启动其关联的线程：
```java
Worker w = new Worker(firstTask);
Thread t = w.thread;
t.start(); // 启动工作线程
```
2. 运行阶段
Worker线程启动后，会执行其run方法，该方法会循环从任务队列中获取任务执行:
```java
// Worker.run()方法简化版
public void run() {
    try {
        while (task != null || (task = getTask()) != null) {
            runTask(task);
            task = null; // 重置任务引用
        }
    } finally {
        processWorkerExit(); // 线程退出处理
    }
}
```
这个过程中，Worker线程会：
- 先执行初始任务（如果有）
- 然后不断从队列中获取新任务执行
- 如果获取不到任务（队列为空且等待超时），线程会退出循环
3. 终止阶段
Worker线程在以下情况下会终止：
- 线程池状态为SHUTDOWN且队列为空
- 线程池状态为STOP
- 等待任务超时（适用于超过核心线程数的线程）
- 线程执行过程中发生异常
实际调试线程池时，发现Worker线程的终止过程是受控的，会执行processWorkerExit()方法进行清理工作，包括从线程集合中移除Worker，根据需要创建新的替代线程等。
设计的生命周期管理机制确保了线程池能够动态调整线程数量，既能应对负载波动，又能避免资源浪费。

### 线程池状态-线程池的五种状态（RUNNING、SHUTDOWN、STOP、TIDYING、TERMINATED）
ThreadPoolExecutor通过一个名为ctl的AtomicInteger变量同时保存线程池状态和工作线程数量。高3位表示线程池状态，低29位表示工作线程数量。线程池共有五种状态：
1. RUNNING：接受新任务并处理队列中的任务
- 线程池创建后的初始状态
- 可以接收新任务，也会处理队列中的任务
2. SHUTDOWN：不接受新任务，但处理队列中的任务
- 调用shutdown()方法后的状态
- 拒绝新任务提交，但会继续执行队列中的任务
- 体现了优雅关闭的理念
3. STOP：不接受新任务，不处理队列中的任务，中断正在执行的任务
- 调用shutdownNow()方法后的状态
- 拒绝新任务提交，不处理队列中任务，并尝试中断正在执行的任务
- 适用于紧急关闭场景
4. TIDYING：所有任务已终止，工作线程数为0，将执行terminated()钩子方法
- 当线程池变为SHUTDOWN状态且队列和线程池均为空时，或者线程池变为STOP状态且线程池为空时
- 此时线程池中不再有任何任务和工作线程
5. TERMINATED：terminated()方法执行完成
- 线程池终止的最终状态
- terminated()钩子方法执行完毕后的状态
### 各状态之间的转换条件和过程
状态转换是单向的，遵循以下规则：
```text
RUNNING -> SHUTDOWN：调用shutdown()方法
RUNNING -> STOP：调用shutdownNow()方法
SHUTDOWN -> TIDYING：队列和线程池都为空
STOP -> TIDYING：线程池为空
TIDYING -> TERMINATED：terminated()钩子方法执行完毕
```

### 任务队列
任务队列(workQueue)是ThreadPoolExecutor的核心组件之一，用于暂存等待执行的任务。
#### 常见的阻塞队列类型及其特点
1. ArrayBlockingQueue
- 基于数组实现的有界队列
- FIFO（先进先出）原则
- 需要指定容量，如new ArrayBlockingQueue<>(100)
- 适用于任务量可预测的场景
- 由于是有界队列，当队列满时会触发创建更多线程或拒绝策略
2. LinkedBlockingQueue
- 基于链表实现的可选有界/无界队列
- 不指定容量时为无界队列，如new LinkedBlockingQueue<>()
- 指定容量时为有界队列，如new LinkedBlockingQueue<>(100)
- 无界队列理论上可以接收无限任务，但实际受内存限制
- 在Executors.newFixedThreadPool()中默认使用无界的LinkedBlockingQueue
3. SynchronousQueue
- 不存储元素的特殊队列
- 每个插入操作必须等待另一个线程执行相应的移除操作
- 直接传递模式，适合于"热交换"场景
- 在Executors.newCachedThreadPool()中使用
- 配合SynchronousQueue使用时，通常maximumPoolSize设置较大或无限
4. PriorityBlockingQueue
- 支持优先级排序的无界阻塞队列
- 任务需实现Comparable接口或提供Comparator
- 允许任务按优先级执行，而非严格的FIFO顺序
- 适用于任务优先级不同的场景，如紧急任务优先处理
5. DelayQueue
- 延迟队列，任务在指定延迟时间后才可执行
- 任务需实现Delayed接口
- 适用于定时任务、缓存过期等场景
#### 不同队列对线程池行为的影响
选择不同类型的队列会显著影响线程池的行为特性：
1. 有界队列 vs 无界队列
- 有界队列(ArrayBlockingQueue或有界LinkedBlockingQueue)：
  - 当队列满时会创建非核心线程
  - 可能触发拒绝策略
  - 避免无限制地接收任务，防止OOM
  - 适合限制系统资源使用，提供"反压"机制
- 无界队列(无界LinkedBlockingQueue或PriorityBlockingQueue)：
  - 不会创建非核心线程(maximumPoolSize参数实际无效)
  - 不会触发拒绝策略
  - 可能导致内存溢出(如任务产生速度持续大于消费速度)
  - 适合任务互相独立且内存资源充足的场景
2. SynchronousQueue的特殊影响
  - 由于不存储任务，提交任务时：
    - 如果没有空闲线程，会立即创建新线程(直到达到maximumPoolSize)
    - 达到maximumPoolSize后会触发拒绝策略
    - 导致线程池倾向于创建新线程而非排队等待
  -   适合执行时间短但数量大的任务
  - 常见配置：Executors.newCachedThreadPool()使用SynchronousQueue配合无限maximumPoolSize
3. 优先级队列
  - 允许任务按优先级执行，而非严格的FIFO顺序
  - 适用于任务优先级不同的场景，如紧急任务优先处理
4. 延迟队列
  - 延迟队列，任务在指定延迟时间后才可执行
  - 任务需实现Delayed接口
5. 在我的实践中，队列选择通常基于以下考虑：
  - 任务量可预测且希望限制资源使用时，选择有界队列
  - 任务量不可预测但希望系统更稳定时，选择无界队列(需监控内存使用)
  - 任务执行时间短且希望最小化延迟时，选择SynchronousQueue
  - 任务有优先级差异时，选择PriorityBlockingQueue
  - 任务需要定时执行时，选择DelayQueue
例如，在一个电商系统中，我曾为不同场景使用不同队列：
  - 订单处理：有界LinkedBlockingQueue(保证系统稳定，防止过载)
  - 实时通知：SynchronousQueue(最小化延迟)
  - 批量任务：优先级队列(重要客户订单优先处理)
理解队列特性对于设计高效的并发系统至关重要，需要根据实际业务场景选择合适的队列类型。
## 拒绝策略
### 四种标准拒绝策略的特点和适用场景
当线程池饱和（线程数达到maximumPoolSize且队列已满）时，新提交的任务会被拒绝。ThreadPoolExecutor提供了四种标准的拒绝策略，每种策略适用于不同的场景：
1. AbortPolicy（默认策略）
```java
public static class AbortPolicy implements RejectedExecutionHandler {
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        throw new RejectedExecutionException("Task " + r.toString() +
                                           " rejected from " + e.toString());
    }
}
```
特点：
- 直接抛出RejectedExecutionException异常
- 不处理被拒绝的任务，由调用者处理异常
适用场景：
- 需要明确知道任务被拒绝的情况
- 调用方有能力捕获并处理异常
- 任务必须执行成功，不能丢失
2. CallerRunsPolicy
```java
public static class CallerRunsPolicy implements RejectedExecutionHandler {
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        if (!e.isShutdown()) {
            r.run();
        }
    }
}
```
特点：
- 在调用者线程中执行被拒绝的任务
- 不会丢弃任务，也不会抛出异常
- 提供反压(back pressure)机制，减缓新任务的提交速度
适用场景：
- 任务不能丢失，但可以延迟执行
- 系统负载较高时，希望自动降低任务提交速率
- 调用者线程有能力执行该任务
3. DiscardPolicy
```java
public static class DiscardPolicy implements RejectedExecutionHandler {
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        // 什么都不做，直接丢弃任务
    }
}
```
特点：
- 静默丢弃被拒绝的任务
- 不抛出异常，也不执行任务
适用场景：
- 任务可以安全丢弃，不影响业务逻辑
- 关注系统稳定性胜过任务完整性
- 有其他机制可以重试或补偿丢失的任务
4. DiscardOldestPolicy
```java
public static class DiscardOldestPolicy implements RejectedExecutionHandler {
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        // 丢弃队列头部(最旧)的任务
    }
}
```
特点：
- 丢弃队列中最旧的任务（队列头部的任务）
- 然后尝试提交新任务
- 如果队列使用PriorityBlockingQueue，会丢弃优先级最低的任务
适用场景：
- 新任务比旧任务更重要的场景
- 处理实时性要求高的任务流
- 只关心最新状态，旧状态可以被覆盖
5. 除了使用标准拒绝策略外，在许多复杂业务场景中，我们需要定制拒绝策略。自定义拒绝策略只需实现RejectedExecutionHandler接口：
```java
public interface RejectedExecutionHandler {
    void rejectedExecution(Runnable r, ThreadPoolExecutor executor);
}
```
自定义拒绝策略可以：
- 记录被拒绝的任务
- 重试或补偿丢失的任务
- 根据业务逻辑进行特殊处理

## 使用ThreadPoolExecutor自定义一个拒绝策略，是继续使用当前线程池处理这个任务，会发生什么；实际生产中确实会这么做，什么场景下会这么做。
如果自定义一个让线程池继续处理任务的拒绝策略，本质上是强制线程池接受超出其处理能力的任务。实现方式如下：
```java
public static class RetryExecutionHandler implements RejectedExecutionHandler {
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        if (!e.isShutdown()) {
            try {
                // 强制将任务放入队列
                e.getQueue().put(r);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RejectedExecutionException("当前线程被中断", ie);
            }
        }
    }
}
```
这种策略会发生什么
- 阻塞提交线程：使用put方法会导致提交任务的线程阻塞，直到队列有空间
- 潜在死锁风险：如果提交任务的线程也是执行任务的线程，可能导致死锁
- 内存压力不减：虽然任务不会丢失，但系统负载和内存压力仍然存在
- 任务处理延迟增加：队列持续增长会导致任务等待时间延长
实际生产中的应用场景
这种策略在以下场景中较为适用：
- 关键业务场景：任务绝对不能丢失，宁可处理延迟也不能丢弃
- 数据一致性要求高的场景：如订单处理、支付流程等，每个任务都必须被处理
- 批处理场景：如每日对账、结算等，可以接受处理延迟但不能丢任务
- 资源有保障的环境：系统资源充足，有足够内存支持队列增长
- 任务量波动较大的系统：在流量高峰期临时缓存任务，峰值过后逐步处理

### 常见的线程池使用误区
### 线程池监控与动态调整

## 执行顺序
### 线程执行的优先级（核心线程、队列、非核心线程）
hreadPoolExecutor对任务的处理遵循一定的优先级顺序，这个顺序直接影响着线程池的性能表现和资源利用效率。根据我的理解和实践经验，线程池处理任务的优先级顺序如下：
1. 核心线程优先
当任务提交到线程池时，首先会尝试使用核心线程来执行任务：
- 如果当前运行的线程数少于corePoolSize，线程池会创建新的核心线程来执行任务，即使此时可能有其他空闲的核心线程
- 这种设计初衷是为了快速响应任务，不需要从队列中获取任务的额外开销
- 核心线程是"常驻军"，即使空闲也不会被回收（除非设置allowCoreThreadTimeOut为true）
在源码中的体现：
```java
// ThreadPoolExecutor.execute()方法中的部分逻辑
if (workerCountOf(c) < corePoolSize) {
    if (addWorker(command, true))  // true表示是核心线程
        return;
    c = ctl.get();
}
```
2. 工作队列次之
当所有核心线程都在忙碌时，新提交的任务会被放入工作队列等待执行：
- 如果运行的线程数大于或等于corePoolSize，新任务会被放入workQueue
- 只有当队列已满时，才会考虑创建非核心线程
- 队列的类型和容量会显著影响线程池的行为
源码体现：
```java
// ThreadPoolExecutor.execute()方法中的部分逻辑
if (isRunning(c) && workQueue.offer(command)) {
    // 任务成功放入队列
    int recheck = ctl.get();
    // 二次检查线程池状态，防止状态变化导致任务无法执行
    if (! isRunning(recheck) && remove(command))
        reject(command);
    else if (workerCountOf(recheck) == 0)
        addWorker(null, false);  // 确保至少有一个线程处理队列中的任务
    return;
}
```
3. 非核心线程最后
只有当工作队列已满，无法再接收新任务时，才会考虑创建非核心线程：
- 如果当前线程数小于maximumPoolSize，会创建新的非核心线程来执行任务
- 非核心线程是"临时工"，在空闲keepAliveTime时间后会被回收
- 这种机制使线程池能够应对突发的高负载情况
源码体现：
```java
// ThreadPoolExecutor.execute()方法中的部分逻辑
if (addWorker(command, false))  // false表示是非核心线程
    return;
// 如果创建非核心线程也失败，执行拒绝策略
reject(command);
```
### 任务的提交、执行和完成过程
ThreadPoolExecutor中任务的全生命周期可以分为提交、调度、执行和完成四个阶段：
1. 任务提交阶段
任务可以通过两种主要方式提交到线程池：
- execute(Runnable) 方法：用于提交没有返回值的任务
```java
  threadPool.execute(() -> System.out.println("任务执行"));
```
- submit(Callable/Runnable) 方法：用于提交有返回值的任务，返回Future对象
```java
  Future<String> future = threadPool.submit(() -> "任务结果");
```
submit方法实际上是在execute基础上包装了一层，将任务封装为FutureTask：
```java
// AbstractExecutorService.submit()简化版
public <T> Future<T> submit(Callable<T> task) {
    FutureTask<T> ftask = new FutureTask<T>(task);
    execute(ftask);  // 最终还是调用execute方法
    return ftask;
}
```
2. 任务调度阶段
提交的任务并不会立即执行，而是要经过线程池的调度决策：
1. 检查线程池状态，如果不是RUNNING状态，拒绝任务
2. 尝试创建核心线程执行任务
3. 如果核心线程已满，尝试将任务放入工作队列
4. 如果队列已满，尝试创建非核心线程
5. 如果线程数达到maximumPoolSize，执行拒绝策略
这个决策过程体现在execute方法中：
```java
public void execute(Runnable command) {
    if (command == null)
        throw new NullPointerException();
    int c = ctl.get();
    // 尝试以核心线程执行
    if (workerCountOf(c) < corePoolSize) {
        if (addWorker(command, true))
            return;
        c = ctl.get();
    }
    // 尝试放入队列
    if (isRunning(c) && workQueue.offer(command)) {
        // 再次检查
        int recheck = ctl.get();
        if (!isRunning(recheck) && remove(command))
            reject(command);
        else if (workerCountOf(recheck) == 0)
            addWorker(null, false);
    }
    // 尝试以非核心线程执行
    else if (!addWorker(command, false))
        // 拒绝任务
        reject(command);
}
```
3. 任务执行阶段
一旦任务被分配给Worker线程，就进入执行阶段：
- Worker线程从队列获取任务
- 执行任务的run()方法
- 处理执行过程中可能出现的异常
Worker线程的执行逻辑（简化版）：
```java
final void runWorker(Worker w) {
    Thread wt = Thread.currentThread();
    Runnable task = w.firstTask;
    w.firstTask = null;
    try {
        // 循环获取任务并执行
        while (task != null || (task = getTask()) != null) {
            w.lock();  // 获取锁，表示线程正在执行任务
            try {
                // 执行任务前的钩子方法
                beforeExecute(wt, task);
                Throwable thrown = null;
                try {
                    task.run();  // 实际执行任务
                } catch (Throwable x) {
                    thrown = x;
                    throw x;
                } finally {
                    // 执行任务后的钩子方法
                    afterExecute(task, thrown);
                }
            } finally {
                task = null;
                w.completedTasks++;
                w.unlock();
            }
        }
    } finally {
        processWorkerExit(w, completedAbruptly);
    }
}
```
4. 任务完成阶段
任务执行完成后，有几种可能的情况：
  - 对于通过execute提交的任务，执行完成后没有返回值
  - 对于通过submit提交的任务，结果会被设置到Future对象中：
```java
  // FutureTask.run()方法中设置结果
  protected void set(V v) {
      outcome = v;
      state = NORMAL;  // 设置状态为完成
      releaseWaiters();  // 释放等待线程
  }
```
  - 调用方可以通过Future.get()获取结果，可能会阻塞等待：
```java
  String result = future.get();  // 可能阻塞
  String resultWithTimeout = future.get(1, TimeUnit.SECONDS);  // 有超时的等待
```
  - 任务执行出现异常时：
    - execute方法：异常会传递给线程的UncaughtExceptionHandler
    - submit方法：异常会被包装在Future中，在调用get()时抛出ExecutionException
## 源码分析
### execute()和submit()方法的区别
ThreadPoolExecutor提供了两种主要的任务提交方式：execute()和submit()方法。通过源码分析，我发现它们有以下关键区别：
1. 返回值不同
- execute()方法：直接执行任务，没有返回值
```java
// ThreadPoolExecutor类中的方法
public void execute(Runnable command) {
    if (command == null)
        throw new NullPointerException();
    // ...执行任务的逻辑
}
```
- submit()方法：返回Future对象，可以获取任务执行结果或取消任务
```java
// AbstractExecutorService类中的方法
public <T> Future<T> submit(Callable<T> task) {
    if (task == null) throw new NullPointerException();
    RunnableFuture<T> ftask = newTaskFor(task);
    execute(ftask);  // 内部调用execute
    return ftask;
}
```
2. 任务类型支持不同
- execute()方法：只接受Runnable类型的任务
```java
public void execute(Runnable command)
```

- submit()方法：有多个重载版本，支持Callable和Runnable
```java
public <T> Future<T> submit(Callable<T> task)
public Future<?> submit(Runnable task)
public <T> Future<T> submit(Runnable task, T result)
```

3. 异常处理机制不同
- execute()方法：任务执行过程中的异常会传递给线程的UncaughtExceptionHandler，如果没有设置，则由ThreadGroup处理（默认打印堆栈信息）
```java
// Worker.run()中的异常处理（简化版）
try {
    task.run();
} catch (Throwable ex) {
    throw ex;  // 异常会传递给线程的异常处理器
}
```
- submit()方法：任务执行过程中的异常会被捕获并存储在Future中，调用Future.get()时才会抛出包装为ExecutionException的异常
```java
// FutureTask.run()中的异常处理（简化版）
try {
    result = callable.call();
    set(result);  // 设置正常结果
} catch (Throwable ex) {
    setException(ex);  // 存储异常
}

// setException内部实现
protected void setException(Throwable t) {
    outcome = t;  // 将异常存储在outcome字段
    state = EXCEPTIONAL;  // 设置状态为异常
    releaseWaiters();  // 唤醒等待的线程
}
```

4. 内部实现层次不同
- execute()方法：ThreadPoolExecutor的直接实现，是任务执行的核心方法
- submit()方法：在AbstractExecutorService中实现，是对execute()的封装，增加了Future支持
```java
// submit最终会调用execute
public <T> Future<T> submit(Callable<T> task) {
    RunnableFuture<T> ftask = newTaskFor(task);  // 将任务包装为FutureTask
    execute(ftask);  // 调用execute执行
    return ftask;
}
```
### addWorker()方法的实现细节
addWorker()是ThreadPoolExecutor的核心私有方法，负责创建和启动新的工作线程。通过仔细阅读源码，我发现这个方法的实现非常精巧，包含了线程池状态检查、并发安全控制和线程创建等多个关键步骤：
- 方法签名和参数含义
```java
private boolean addWorker(Runnable firstTask, boolean core)
```
- firstTask：新Worker线程要执行的第一个任务，可以为null，表示创建线程但不分配初始任务
- core：是否作为核心线程创建，影响线程数量的检查边界（corePoolSize或maximumPoolSize）
- 返回值：创建并启动工作线程是否成功

实现分为两个主要部分：
第一部分：线程池状态检查和线程计数器增加
```java
private boolean addWorker(Runnable firstTask, boolean core) {
    // 标签用于快速退出多层循环
    retry:
    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);  // 获取线程池运行状态

        // 检查线程池状态，判断是否允许添加线程
        // 如果线程池状态 >= SHUTDOWN，一般不再接受新任务
        // 但有特例：状态为SHUTDOWN、firstTask为null且队列非空时可以添加线程
        if (rs >= SHUTDOWN &&
            ! (rs == SHUTDOWN &&
               firstTask == null &&
               ! workQueue.isEmpty()))
            return false;

        // CAS循环增加线程计数
        for (;;) {
            int wc = workerCountOf(c);  // 获取当前worker数量
            
            // 检查是否超过容量限制
            if (wc >= CAPACITY ||
                wc >= (core ? corePoolSize : maximumPoolSize))
                return false;
                
            // CAS增加worker计数，成功则跳出外层循环
            if (compareAndIncrementWorkerCount(c))
                break retry;
                
            // CAS失败，重新获取ctl的值
            c = ctl.get();
            
            // 如果线程池状态改变，重新开始第一步检查
            if (runStateOf(c) != rs)
                continue retry;
            // 否则继续尝试CAS操作
        }
    }
```
这部分代码使用双重循环和CAS操作保证在并发环境下安全地增加工作线程计数。retry标签和continue retry的组合使得在状态变化时能够快速重新检查条件。

第二部分：创建Worker实例并启动线程
```java
    // worker计数增加成功，下面创建worker实例
    boolean workerStarted = false;  // 工作线程是否启动的标志
    boolean workerAdded = false;    // 工作线程是否添加到workers集合的标志
    Worker w = null;
    
    try {
        // 创建新的Worker实例
        w = new Worker(firstTask);
        final Thread t = w.thread;  // 获取Worker关联的线程
        
        if (t != null) {
            // 获取线程池全局锁，保护workers集合
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                // 再次检查线程池状态，防止获取锁期间状态改变
                int rs = runStateOf(ctl.get());
                
                if (rs < SHUTDOWN ||
                    (rs == SHUTDOWN && firstTask == null)) {
                    // 检查线程是否已启动，防止threadFactory创建的线程已经开始运行
                    if (t.isAlive())
                        throw new IllegalThreadStateException();
                        
                    // 将worker添加到workers集合
                    workers.add(w);
                    workerAdded = true;
                    
                    // 更新largestPoolSize统计信息
                    int s = workers.size();
                    if (s > largestPoolSize)
                        largestPoolSize = s;
                }
            } finally {
                mainLock.unlock();  // 释放锁
            }
            
            // 如果worker成功添加，启动线程
            if (workerAdded) {
                t.start();  // 这会调用Worker的run方法
                workerStarted = true;
            }
        }
    } finally {
        // 如果线程启动失败，执行清理操作
        if (! workerStarted)
            addWorkerFailed(w);
    }
    
    return workerStarted;  // 返回线程是否成功启动
}
```
这部分代码主要完成了:
1. 创建Worker实例和关联的线程
2. 在全局锁保护下将Worker添加到workers集合
3. 启动工作线程
4. 处理失败情况的清理工作
Worker类的关键设计
Worker类是ThreadPoolExecutor的内部类，既实现了Runnable接口，又继承了AbstractQueuedSynchronizer (AQS)：
```java
private final class Worker extends AbstractQueuedSynchronizer implements Runnable {
    final Thread thread;           // Worker关联的线程
    Runnable firstTask;            // 初始任务
    volatile long completedTasks;  // 完成的任务计数
    
    // 构造方法
    Worker(Runnable firstTask) {
        setState(-1);  // 初始状态设为-1，防止在线程启动前被中断
        this.firstTask = firstTask;
        this.thread = getThreadFactory().newThread(this);  // 创建新线程
    }
    
    // 实现Runnable接口的run方法
    public void run() {
        runWorker(this);  // 调用外部类的runWorker方法
    }
    
    // AQS相关方法实现，用于实现不可重入锁
    // ...
}
```
Worker继承AQS的目的是实现一个简单的不可重入锁，用于线程执行任务期间防止被中断。
通过分析addWorker()方法，我更深入地理解了线程池如何安全地创建和管理工作线程，特别是如何处理并发和状态变化问题。
### 线程池关闭过程的源码分析
ThreadPoolExecutor提供了两种关闭线程池的方法：shutdown()和shutdownNow()。通过源码分析，我们可以深入理解线程池的关闭过程：
1. shutdown()方法 - 优雅关闭
shutdown()方法会发起一个"温和"的关闭过程：拒绝新任务，但会处理完已提交的任务（包括队列中等待的任务）。
```java
public void shutdown() {
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        // 检查权限
        checkShutdownAccess();
        
        // 设置线程池状态为SHUTDOWN
        advanceRunState(SHUTDOWN);
        
        // 尝试中断空闲的工作线程
        interruptIdleWorkers();
        
        // 调用钩子方法，可以由子类实现
        onShutdown();
    } finally {
        mainLock.unlock();
    }
    
    // 尝试终止线程池（如果条件满足）
    tryTerminate();
}
```
关键点解析：
advanceRunState(SHUTDOWN)：将线程池状态改为SHUTDOWN，这会导致新提交的任务被拒绝
interruptIdleWorkers()：中断空闲线程，让它们检查线程池状态并退出
tryTerminate()：尝试将线程池过渡到TERMINATED状态（只有当所有工作线程都停止且队列为空时才会成功）

2. shutdown()方法 - 优雅关闭
shutdown()方法会发起一个"温和"的关闭过程：拒绝新任务，但会处理完已提交的任务（包括队列中等待的任务）。
```java
public void shutdown() {
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        // 检查权限
        checkShutdownAccess();
        
        // 设置线程池状态为SHUTDOWN
        advanceRunState(SHUTDOWN);
        
        // 尝试中断空闲的工作线程
        interruptIdleWorkers();
        
        // 调用钩子方法，可以由子类实现
        onShutdown();
    } finally {
        mainLock.unlock();
    }
    
    // 尝试终止线程池（如果条件满足）
    tryTerminate();
}
```
关键点解析：
- advanceRunState(SHUTDOWN)：将线程池状态改为SHUTDOWN，这会导致新提交的任务被拒绝
- interruptIdleWorkers()：中断空闲线程，让它们检查线程池状态并退出
- tryTerminate()：尝试将线程池过渡到TERMINATED状态（只有当所有工作线程都停止且队列为空时才会成功）
3. shutdownNow()方法 - 立即关闭
shutdownNow()方法发起一个"强制"关闭过程：拒绝新任务，中断所有正在执行的任务，并返回队列中未执行的任务列表。
```java
public List<Runnable> shutdownNow() {
    List<Runnable> tasks;
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        // 检查权限
        checkShutdownAccess();
        
        // 设置线程池状态为STOP
        advanceRunState(STOP);
        
        // 中断所有工作线程（不管是否空闲）
        interruptWorkers();
        
        // 从工作队列中移除所有未执行的任务
        tasks = drainQueue();
    } finally {
        mainLock.unlock();
    }
    
    // 尝试终止线程池
    tryTerminate();
    
    // 返回未执行的任务列表
    return tasks;
}
```
关键点解析：
- advanceRunState(STOP)：将线程池状态改为STOP，不再接受新任务，也不处理队列中的任务
- interruptWorkers()：中断所有工作线程，包括正在执行任务的线程
- drainQueue()：清空工作队列，返回未执行的任务列表  
4. 工作线程的终止过程
了解工作线程如何响应关闭请求也很重要。以下是Worker执行的runWorker方法中相关代码（简化）：
```java
final void runWorker(Worker w) {
    Thread wt = Thread.currentThread();
    Runnable task = w.firstTask;
    w.firstTask = null;
    
    // 允许中断
    w.unlock();
    
    boolean completedAbruptly = true;
    try {
        // 循环获取任务执行
        while (task != null || (task = getTask()) != null) {
            // 执行任务...
            task = null;
        }
        completedAbruptly = false;
    } finally {
        // 线程退出处理
        processWorkerExit(w, completedAbruptly);
    }
}
```
其中，getTask()方法会检查线程池状态并决定是否返回任务：
```java
private Runnable getTask() {
    boolean timedOut = false;
    
    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);
        
        // 检查状态和队列
        // 如果线程池状态 >= SHUTDOWN且队列为空或状态 >= STOP，减少worker计数并返回null
        if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
            decrementWorkerCount();
            return null;  // 这会导致工作线程退出
        }
        
        // ... 获取任务的逻辑
    }
}
```

5. tryTerminate()方法 - 终止处理
tryTerminate()方法尝试将线程池状态从SHUTDOWN或STOP转换到TIDYING，然后执行terminated()钩子方法并最终转换到TERMINATED状态：
```java
final void tryTerminate() {
    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);
        
        // 如果以下条件之一为真，则不进行终止：
        // 1. 线程池仍处于RUNNING状态
        // 2. 已经是TIDYING或TERMINATED状态
        // 3. SHUTDOWN状态且队列非空
        if (rs >= TIDYING || 
            rs == RUNNING || 
            (rs == SHUTDOWN && !workQueue.isEmpty()))
            return;
            
        // 如果worker数量不为0，中断一个空闲worker并返回
        if (workerCountOf(c) != 0) {
            interruptIdleWorkers(ONLY_ONE);
            return;
        }
        
        // 所有条件都满足，可以进行终止
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            // CAS尝试设置状态为TIDYING
            if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
                try {
                    // 调用terminated()钩子方法
                    terminated();
                } finally {
                    // 设置状态为TERMINATED
                    ctl.set(ctlOf(TERMINATED, 0));
                    // 唤醒在awaitTermination()中等待的线程
                    termination.signalAll();
                }
                return;
            }
        } finally {
            mainLock.unlock();
        }
        // 如果CAS失败，可能是其他线程修改了状态，重试
    }
}
```
通过这个方法，线程池能够在适当的时机完成从SHUTDOWN/STOP到TERMINATED的状态转换。

