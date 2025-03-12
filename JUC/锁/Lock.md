@[TOC](Lock)

# 基础概念问题
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




