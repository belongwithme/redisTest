@[TOC](AQS)
# 基础概念问题
## 什么是AQS？它在Java并发包中的作用是什么？
AQS（AbstractQueuedSynchronizer）是Java并发包中的一个核心基础组件，它提供了一个框架，用于构建锁和各种同步器。AQS的主要作用包括：
1. 提供统一的同步器框架：AQS封装了实现同步器的复杂性，为开发者提供了一套通用的机制来构建各种同步工具。
2. 管理线程的等待与唤醒：AQS通过一个FIFO的等待队列来管理线程的排队、阻塞和唤醒，避免了开发者直接处理这些底层细节。
3. 状态管理：AQS维护一个表示同步状态的整型变量，不同的同步器可以基于这个状态实现不同的同步语义。
4. 提供模板方法：AQS采用模板方法设计模式，子类只需要实现少量的方法就能定义自己的同步语义，而复杂的队列管理由AQS负责。

Java并发包中的许多同步类，如`ReentrantLock、Semaphore、CountDownLatch、ReentrantReadWriteLock`等，都是基于AQS实现的。这种设计使得这些类有一致的行为模式，同时避免了重复实现类似的线程等待、唤醒和排队逻辑。

面试说法:
AQS是AbstractQueuedSynchronizer的缩写，是Java并发包中的核心基础组件。它的主要作用是提供一个框架，用于构建锁和各种同步器。
AQS的核心思想是，通过一个int类型的状态变量和一个FIFO的等待队列，来实现线程的同步功能。它采用模板方法设计模式，将复杂的线程排队、阻塞和唤醒等底层操作封装起来，子类只需要根据自己的需求实现状态变量的维护逻辑。
在Java并发包中，像ReentrantLock、Semaphore、CountDownLatch、ReentrantReadWriteLock等常用的同步工具类，都是基于AQS实现的。这种设计大大简化了并发工具的开发，避免了重复实现类似的线程控制逻辑。"

## AQS的核心原理是什么？
AQS的核心原理可以概括为以下几个方面：
1. 状态管理：
- AQS使用一个volatile的int类型变量state表示同步状态
- 通过getState()、setState()和compareAndSetState()方法安全地修改这个状态
- 不同的同步器对state赋予不同的含义（如锁的重入次数、可用许可数等）
2. 队列管理：
- 使用CLH队列（一种FIFO队列的变种）变种管理等待获取同步状态的线程
- 队列是一个双向链表，包含head和tail两个指针
- 每个节点包含线程引用、等待状态、前驱和后继指针
3. 两种同步模式：
- 独占模式(Exclusive)：同一时刻只有一个线程能获取同步状态，如ReentrantLock
- 共享模式(Shared)：同一时刻可以有多个线程获取同步状态，如Semaphore、CountDownLatch
4. 线程控制：
- 通过LockSupport的park/unpark方法实现线程的阻塞和唤醒
- 相比wait/notify，可以精确控制要唤醒的线程
5. 模板方法模式：
- AQS定义了获取和释放同步状态的框架
- 子类通过重写tryAcquire、tryRelease等方法实现具体的同步逻辑
- 队列管理、线程阻塞和唤醒由AQS统一处理
6. 工作流程：
- 线程尝试获取同步状态(tryAcquire/tryAcquireShared)
- 获取失败则将线程包装成Node加入等待队列
- 线程进入自旋或阻塞等待
- 当前驱节点释放同步状态时，唤醒后继节点

这种设计将同步状态的管理与线程的等待/唤醒机制分离，使得开发者可以专注于同步状态的逻辑，而不必关心线程管理的复杂性。

面试说法:
AQS的核心原理可以概括为'状态变量+等待队列'。
- 首先，AQS使用一个volatile的int类型变量state表示同步状态，通过CAS操作保证其修改的原子性。
- 其次，AQS维护一个FIFO的等待队列，当线程获取同步状态失败时，会被包装成Node节点加入队列，并可能被阻塞。当同步状态释放时，会唤醒队列中的后继线程。
- AQS支持两种同步模式：独占模式和共享模式。独占模式下，同一时刻只有一个线程能获取同步状态，如ReentrantLock；共享模式下，多个线程可以同时获取同步状态，如Semaphore。
- AQS通过模板方法模式，定义了获取和释放同步状态的框架，子类只需要实现tryAcquire、tryRelease等方法，定义自己的同步语义。线程的排队、阻塞和唤醒等复杂操作都由AQS统一处理，这大大简化了同步器的实现。"

## AQS中的state变量有什么作用？
AQS中的state变量是一个volatile修饰的int类型成员变量，它是整个同步器的核心，具有以下作用：
1. 表示同步状态：
- state是同步器的状态标志，不同的同步器对state赋予不同的含义
- 线程通过获取和修改state来实现同步
- 不同同步器中的含义：
    - ReentrantLock：state表示锁的重入次数，0表示未锁定
    - Semaphore：state表示剩余的许可数量
    - CountDownLatch：state表示计数器的值，当减到0时释放所有等待线程
    - ReentrantReadWriteLock：
        - 高16位表示读锁的持有数
        - 低16位表示写锁的重入次数
2. 线程安全的访问方式：
    - getState()：获取当前状态
    - setState(int)：设置状态
    - compareAndSetState(int expect, int update)：CAS方式更新状态，保证原子性
3. 可见性保证：
    - state变量使用volatile修饰，保证多线程之间的可见性
    - 当一个线程修改state后，其他线程能立即看到最新值
4. 同步决策依据：
    - 线程能否获取同步状态取决于state的值和同步器的规则
例如，在ReentrantLock中，如果state为0，线程可以获取锁；如果不为0但当前线程已持有锁，可以重入
    - 条件判断：
        - 在独占模式下，通常state为0表示资源可用，非0表示被占用
        - 在共享模式下，state通常表示可同时访问的线程数量
通过这个单一的状态变量，AQS能够支持各种不同类型的同步器，这是AQS设计的精妙之处。子类通过重写tryAcquire、tryRelease等方法，定义自己对state的解释和操作规则，从而实现不同的同步语义。

面试说法:
state是AQS中的一个volatile修饰的int类型成员变量，是整个同步器的核心，表示同步状态。
不同的同步器对state赋予不同的含义：
- 在ReentrantLock中，state表示锁的重入次数，0表示未锁定
- 在Semaphore中，state表示剩余的许可数量
- 在CountDownLatch中，state表示计数器的值
- 在ReentrantReadWriteLock中，state的高16位表示读锁持有数，低16位表示写锁重入次数
AQS提供了三个方法来安全地操作state：
- getState()：获取当前状态
- setState()：设置状态
- compareAndSetState()：CAS方式更新状态，保证原子性
线程能否获取同步状态，取决于state的值和同步器的规则。
例如，在ReentrantLock中，如果state为0，线程可以获取锁；如果不为0但当前线程已持有锁，可以重入。
通过这个单一的状态变量，AQS能够支持各种不同类型的同步器，AQS设计还是非常牛的。"

# 实现机制问题
## AQS如何实现线程的阻塞和唤醒？
AQS实现线程的阻塞和唤醒主要依靠LockSupport类的静态方法。
具体来说，当线程获取同步状态失败需要阻塞时，AQS会调用LockSupport.park(this)方法阻塞当前线程。
当持有同步状态的线程释放状态时，会调用LockSupport.unpark(thread)方法唤醒后继节点的线程。
相比于传统的wait/notify机制，LockSupport的park/unpark方法有几个显著优势：
- 可以精确唤醒指定线程，而不是随机或全部唤醒
- 不需要获取对象监视器（不需要synchronized）
- unpark可以在park之前调用，不会导致线程永久阻塞
在AQS中:
- 线程阻塞的典型流程是：线程获取同步状态失败，加入等待队列，检查前驱节点状态，如果需要阻塞则调用park方法。
- 唤醒流程是：线程释放同步状态后，查找合适的后继节点，调用unpark方法唤醒该节点对应的线程。
这种机制使得AQS能够高效地管理线程状态，避免了使用Object的wait/notify时可能出现的'信号丢失'和'惊群效应'问题。

## 独占模式和共享模式有什么区别？分别用于实现什么类型的同步器？
AQS支持两种基本的同步模式：独占模式和共享模式，它们在资源访问策略上有本质区别。
独占模式(Exclusive)：
- 同一时刻只允许一个线程获取同步状态
- 对应的核心方法是tryAcquire和tryRelease
- 典型实现包括ReentrantLock、ThreadPoolExecutor.Worker
- 工作机制是'一锁一线程'，其他线程必须等待持有锁的线程释放
共享模式(Shared)：
- 同一时刻允许多个线程同时获取同步状态
- 对应的核心方法是tryAcquireShared和tryReleaseShared
- 典型实现包括Semaphore、CountDownLatch、ReadWriteLock中的读锁
- 工作机制是'多线程共享'，只要有剩余资源，就可以不断唤醒等待线程

一个很好的例子是ReentrantReadWriteLock，它的写锁使用独占模式（同时只能有一个线程写），而读锁使用共享模式（多个线程可以同时读）。
这种设计充分体现了AQS框架的灵活性。"

## AQS的等待队列是如何工作的？
AQS的等待队列是一个FIFO的双向链表，基于CLH锁队列的变种实现。这个队列对于理解AQS的工作机制至关重要。
队列结构：
- 队列由head和tail两个指针维护，分别指向队列的头和尾
- 每个节点(Node)包含线程引用、等待状态(waitStatus)、前驱和后继指针
- 节点的waitStatus有几个关键值：CANCELLED(1)表示线程已取消，SIGNAL(-1)表示后继节点需要唤醒，CONDITION(-2)表示节点在条件队列中等待，PROPAGATE(-3)表示共享模式下状态需要向后传播
入队过程：
当线程获取同步状态失败时，会执行以下步骤：
1. 创建一个代表当前线程的节点
2. 使用CAS操作将节点设置为尾节点，并链接到前驱
3. 前驱节点设置next指针指向新节点
4. 如果需要阻塞，会先将前驱节点状态设为SIGNAL，然后调用park阻塞自己
出队过程：
当线程释放同步状态时：
1. 修改头节点的状态，表示资源可用
2. 唤醒头节点的后继节点
3. 当后继节点获取同步状态成功后，会将自己设为新的头节点
4. 原头节点的thread和prev引用被清空，帮助GC
这种设计使得AQS能够高效地管理线程的等待和唤醒，同时通过双向链表结构，支持从队列中取消节点的操作。在高并发环境下，使用CAS操作保证了入队和出队操作的线程安全。"

# 源码分析问题
## 请解释AQS中acquire和release方法的实现原理
AQS中的acquire和release方法是独占模式下获取和释放同步状态的核心方法，它们采用了模板方法设计模式.
```java
public final void acquire(int arg) {
    if (!tryAcquire(arg) && 
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```
acquire方法的实现原理：
1. 首先调用tryAcquire尝试获取同步状态，这个方法由子类实现
2. 如果获取失败，调用addWaiter创建一个独占模式的节点并加入等待队列
3. 然后调用acquireQueued方法，使该节点以自旋方式尝试获取同步状态
4. 如果在等待过程中线程被中断，最后会调用selfInterrupt重新设置中断状态
acquireQueued方法会让线程在队列中自旋或阻塞，直到获取到同步状态。它的特点是不响应中断，即使线程被中断也会继续尝试获取，只是会记录中断状态并在最后返回。
release方法的实现原理：
```java
public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}
```
这个方法的执行流程是：
1. 调用tryRelease尝试释放同步状态，这个方法由子类实现
2. 如果释放成功，检查头节点状态，如果需要唤醒后继节点
3. 调用unparkSuccessor唤醒头节点的后继节点

这种模板方法的设计使得AQS能够适应不同的同步需求.
子类只需要实现tryAcquire和tryRelease方法，定义获取和释放同步状态的规则，而线程的排队、阻塞和唤醒等复杂逻辑都由AQS统一处理。"
## 条件变量(Condition)是如何与AQS结合工作的？
Condition接口提供了类似Object的wait/notify的线程等待/通知机制，但更加灵活。在AQS中，Condition是通过内部类ConditionObject实现的。
条件变量的基本原理：
1. 每个Condition对象维护一个独立的条件等待队列
2. 当线程调用await()时，会从同步队列转移到条件队列
3. 当线程调用signal()时，会从条件队列转移到同步队列
4. 条件队列是单向链表，而同步队列是双向链表

await方法的实现原理：
```java
public final void await() throws InterruptedException {
    if (Thread.interrupted())
        throw new InterruptedException();
    // 添加到条件队列
    Node node = addConditionWaiter();
    // 完全释放锁
    int savedState = fullyRelease(node);
    int interruptMode = 0;
    // 检查节点是否在同步队列中
    while (!isOnSyncQueue(node)) {
        // 阻塞当前线程
        LockSupport.park(this);
        // 检查中断
        if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
            break;
    }
    // 重新竞争锁
    if (acquireQueued(node, savedState) && interruptMode != -1)
        interruptMode = 1;
    if (node.nextWaiter != null)
        unlinkCancelledWaiters();
    if (interruptMode != 0)
        reportInterruptAfterWait(interruptMode);
}
```
await方法的执行流程是：
1. 将当前线程封装成Node加入条件队列
2. 完全释放同步状态（支持可重入）
3. 阻塞当前线程，直到被signal或中断
4. 被唤醒后，从条件队列转移到同步队列
5. 重新竞争同步状态
6. 处理中断状态

signal方法的实现原理：
```java
public final void signal() {
    // 检查当前线程是否持有锁
    if (!isHeldExclusively())
        throw new IllegalMonitorStateException();
    // 获取条件队列中第一个节点
    Node first = firstWaiter;
    if (first != null)
        // 将节点从条件队列转移到同步队列
        doSignal(first);
}
```
signal方法的执行流程是：
1. 检查当前线程是否持有锁
2. 将条件队列中的首节点转移到同步队列
3. 被转移的线程稍后会被唤醒并重新竞争锁
这种设计使得一个Lock可以创建多个Condition，每个Condition管理不同的等待条件，实现更精细的线程控制。
例如，在生产者-消费者模式中，可以使用两个条件变量分别管理'缓冲区不满'和'缓冲区不空'两个条件。"

## 如何实现一个基于AQS的自定义同步器？


## 你认为AQS设计中最精妙的部分是什么？为什么？
我认为AQS设计中最精妙的部分是它的'模板方法模式'与'状态管理'的结合，这种设计实现了框架与具体实现的完美分离。
AQS通过一个volatile的int类型state变量和一套精心设计的模板方法，成功地将复杂的线程同步机制拆分为'框架逻辑'和'具体同步语义'两部分。框架部分由AQS负责，包括线程排队、阻塞和唤醒等通用操作；而具体同步语义则由子类通过重写tryAcquire、tryRelease等方法来实现。
这种设计的精妙之处在于，它既保证了高度的代码复用，又提供了极大的灵活性。通过简单地重新定义state的含义和获取/释放的规则，开发者可以实现从互斥锁到信号量、从读写锁到倒计时器等各种不同的同步工具，而无需关心线程管理的复杂细节。
这种'一次设计、多处使用'的思想，使得Java并发包能够提供一系列功能强大且行为一致的同步工具，同时保持了较低的维护成本和较高的性能。这也是为什么我认为AQS是Java并发包中最优雅的设计之一。"