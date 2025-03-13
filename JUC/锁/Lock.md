@[TOC](Lock)

# 基础概念问题

## 什么是Lock接口,核心原理是什么?
Lock 就像一个更灵活的“门卫”，用来控制多线程访问共享资源。不同于传统的 synchronized（像一扇自动锁的门，进去后自动关，出来自动开），Lock 允许我们手动控制“锁门”和“开门”，还支持高级功能，比如：
- 公平锁：线程按排队顺序获取锁，不插队。
- 超时等待：尝试获取锁，等不到就放弃。
可中断等待：等锁时被打断，可以直接离开。
简单说，Lock = 手动开关的锁 + 丰富的管理规则。

核心原理：AQS（排队管理员）
所有高级功能的核心，都依赖一个幕后大佬：`AbstractQueuedSynchronizer（AQS）`。可以把它想象成一个“排队系统”，负责管理想要获取锁的线程。
AQS 的两个核心组件：
state 变量：记录锁的状态（比如 0=无人持有，1=有人持有，>1=重入次数）。
CLH 队列变体：一个线程等待队列（没抢到锁的线程挨个排队）。

## Lock接口与synchronized的区别是什么？
lock接口与synchronized关键字的主要区别在于：
- 使用方式：synchronized是Java关键字，自动获取释放锁；而Lock是接口，需要显式调用lock()和unlock()方法。
- 灵活性：Lock提供了非阻塞获取锁(tryLock)、可中断获取锁(lockInterruptibly)和超时获取锁的机制，而synchronized不支持。
- 公平性：Lock可以实现公平锁，按照申请顺序获取锁；synchronized只能是非公平锁。
- 性能：在高竞争情况下，Lock通常有更好的性能（JDK 6后差距减小）。
- 条件变量：Lock可以绑定多个条件(Condition)，而synchronized只能与一个隐式条件关联。
## Java中有哪些常见的Lock实现类？它们有什么特点？
Java中常见的Lock实现类及其特点包括：
- ReentrantLock：可重入互斥锁，是Lock接口最常用的实现，支持公平和非公平两种模式。
- ReadWriteLock：读写锁接口，其实现类ReentrantReadWriteLock允许多个读线程同时访问，但写线程访问时会阻塞所有其他线程。
- StampedLock：Java 8引入的锁，提供了乐观读模式，性能优于ReadWriteLock，但不可重入。
- ReentrantReadWriteLock：ReadWriteLock接口的实现，提供了读锁和写锁，适合读多写少的场景。
- Semaphore：信号量，虽不是Lock的子类，但提供类似的同步机制，可控制同时访问资源的线程数量。
- CountDownLatch：允许一个或多个线程等待其他线程完成操作。
- CyclicBarrier：允许多个线程相互等待，直到所有线程都到达某个公共屏障点。

## Lock接口和lockSupoort有什么区别?
Lock 和 LockSupport 是 Java 并发中处理线程同步与阻塞的两大工具，但设计目的和使用方式有显著差异。以下从 核心用途、实现机制 和 使用场景 三个维度拆解它们的区别:


### 核心用途
| Lock | LockSupport |
|------|-------------|
| 提供显式锁机制，替代synchronized语法，支持可重入、公平锁、可中断等高级功能 | 提供线程阻塞/唤醒的底层操作，直接控制线程的挂起与恢复 |
| 用途示例：确保共享资源在某个时刻只被一个线程访问 | 用途示例：构建更灵活的同步工具（如AQS） |


### 实现机制
| Lock | LockSupport |
|------|-------------|
| 基于AQS（AbstractQueuedSynchronizer）实现，通过同步队列（CLH变种）管理线程的排队与唤醒 | 基于Unsafe类调用JVM本地方法（如park()和unpark()），直接操作线程状态 |
| 锁的获取可能涉及线程挂起（通过LockSupport.park()实现），但这是AQS内部逻辑的细节 | 无锁状态管理，通过许可证（permit）机制控制线程是否阻塞 |

### 使用方式与特性对比
| 对比点 | Lock | LockSupport |
|--------|------|-------------|
| 是否需要持有锁 | 必须在lock()和unlock()之间操作 | 不需要先获取锁，任意线程均可调用 |
| 线程唤醒的时序性 | 必须等待锁释放后才能唤醒其他线程 | unpark(thread)可以在park()前调用，线程不会阻塞 |
| 中断处理 | 支持可中断的锁获取（lockInterruptibly()） | park()返回时不会抛异常，需自行检查中断状态 |
| 超时机制 | 支持带超时的锁获取（tryLock(timeout)） | 支持parkNanos(timeout)设置阻塞时间 |


# 原理机制问题
## Lock是如何实现线程间的等待/通知机制的？
Lock通过Condition接口实现线程间的等待/通知机制，类似于Object的wait/notify，但更加灵活:
基本机制：Lock对象可以创建一个或多个Condition对象，每个Condition维护一个等待队列。
核心方法：
- await()：释放锁并等待，直到被signal或中断
- signal()：唤醒一个等待的线程
- signalAll()：唤醒所有等待的线程
优势：一个Lock可以创建多个Condition，实现更精细的线程控制，而Object的wait/notify只能与一个隐式条件关联。
使用要求：必须在获取锁之后才能使用Condition方法，否则会抛出IllegalMonitorStateException。




