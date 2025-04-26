@[toc](ReentrantLock)


# 基础概念问题
## 什么是ReentrantLock？它与synchronized有什么区别？
ReentrantLock是Java并发包(java.util.concurrent.locks)中提供的一个可重入的互斥锁实现。它实现了Lock接口，提供了与synchronized关键字类似的独占锁功能，但具有更强的灵活性和功能性。
ReentrantLock与synchronized的主要区别有：
首先，使用方式不同。ReentrantLock需要显式调用lock()获取锁，必须在finally块中调用unlock()释放锁；而synchronized是隐式的，编译器会自动插入获取和释放锁的代码。
其次，ReentrantLock提供了更多高级特性：可以尝试非阻塞地获取锁(tryLock)、支持可中断的锁获取、支持超时获取锁，还可以创建公平锁。而synchronized不支持这些特性。
第三，ReentrantLock可以创建多个条件变量(Condition)，实现更精细的线程控制；synchronized只能与一个隐式条件关联，使用wait/notify机制。
第四，ReentrantLock提供了查询锁状态的方法，如isLocked()、getHoldCount()等，而synchronized没有提供这些功能。
最后，在性能方面，JDK 6之前ReentrantLock性能优于synchronized，但JDK 6后synchronized经过优化，两者性能已经非常接近，所以性能不再是选择的主要因素。


个人理解版本:
ReentrantLock 是 Java 并发包 java.util.concurrent.locks 下提供的一个可重入的互斥锁实现，它实现了 Lock 接口.

## ReentrantLock的核心特性有哪些？
ReentrantLock提供了几个核心特性，使它在并发编程中非常有用：
首先是可重入性，同一个线程可以多次获取同一把锁而不会死锁。每次获取锁时计数器加1，释放时减1，计数为0时才真正释放锁。
其次是公平性选择，通过构造函数可以创建公平锁或非公平锁。公平锁按照线程请求的顺序获取锁，避免线程饥饿；非公平锁(默认)允许'插队'，通常吞吐量更高。
第三是可中断获取锁，lockInterruptibly()方法允许在等待锁的过程中响应中断，这对于取消长时间等待的操作很有用。
第四是超时获取锁，tryLock(timeout, unit)支持在指定时间内尝试获取锁，超时返回false，避免无限期等待。
第五是条件变量，通过newCondition()方法可以创建多个条件变量，实现更精细的线程等待/通知机制。
最后，ReentrantLock提供了丰富的锁状态查询方法，如isLocked()、isHeldByCurrentThread()等，便于监控和调试。
这些特性使得ReentrantLock在需要高级锁功能的场景中非常有价值，尤其是需要超时、可中断或条件变量的情况。

## 什么是锁的可重入性？ReentrantLock如何实现可重入？
锁的可重入性是指同一个线程可以多次获取同一把锁而不会死锁。这是一个重要特性，因为它允许一个同步方法调用另一个需要相同锁的方法，避免了自己阻塞自己的情况。
可重入性的重要意义在于：
- 它支持递归调用，一个方法可以调用自身或调用其他需要相同锁的方法
- 它简化了编程模型，使锁的使用更加直观和灵活
- 它避免了同一线程内的死锁问题
ReentrantLock通过记录锁的持有线程和重入计数来实现可重入：
1. 当线程第一次获取锁时，记录当前线程为锁的持有者，并将计数设为1
2. 当同一线程再次获取锁时，识别是持有线程，增加重入计数
3. 释放锁时，减少重入计数，只有当计数变为0时，才真正释放锁
这种实现方式确保了同一线程可以多次获取锁，而其他线程必须等待锁被完全释放后才能获取。
# 实现原理问题

## ReentrantLock的内部实现原理是什么？
ReentrantLock的内部实现基于AbstractQueuedSynchronizer(AQS)框架，这是Java并发包中的核心同步组件。具体实现包括以下关键点：
1. 内部结构：
ReentrantLock有一个内部抽象类Sync，继承自AQS
两个具体实现类：FairSync（公平锁）和NonfairSync（非公平锁）
构造函数决定使用哪个实现类的实例
2. 状态管理：
- 使用AQS的state变量表示锁状态
- state = 0：锁未被占用
- state > 0：锁被占用，值表示重入次数
- 通过CAS操作原子性地修改state值
3. 锁获取流程：
- 尝试通过CAS将state从0设为1
- 如果成功，设置当前线程为锁的持有者(exclusiveOwnerThread)
- 如果失败但当前线程是持有者，增加state值（重入）
- 其他情况，线程进入等待队列
4. 锁释放流程：
- 检查当前线程是否为锁持有者
- 递减state值
- 当state变为0时，清除持有者信息并唤醒后继线程
5. 等待队列：
- 基于AQS的CLH队列变种实现
- 线程获取锁失败时，封装为Node加入队列
- 队列是FIFO的，保证了等待顺序

面试版本:
ReentrantLock 的内部实现完全是基于 Java 并发包中的核心框架 AQS 来构建的。
在 ReentrantLock 内部，它定义了一个关键的抽象静态内部类叫做 Sync，这个 Sync 类直接继承了 AQS。
然后，ReentrantLock 有两个具体的 Sync 实现：
NonfairSync（实现了非公平锁逻辑，是默认选项）和 FairSync（实现了公平锁逻辑）。
当我们创建 ReentrantLock 实例时（比如通过构造函数指定 fair 为 true 或 false），实际上就是创建了对应 Sync 子类的实例，后续的 lock, unlock 等操作都会委托给这个 Sync 实例来处理。

锁的状态管理完全依赖于 AQS 提供的那个 volatile 的整型变量 state。
state 为 0 表示锁当前没有被任何线程持有，是未锁定状态。
state 大于 0 表示锁已经被某个线程持有。
并且，这个 state 的值就代表了锁的重入次数。
比如一个线程第一次获取锁，state 变成 1；
它再次重入获取同一个锁，state 就变成 2。这就是“可重入”的实现方式。
对 state 的修改都是通过 AQS 提供的 CAS (Compare-And-Swap) 操作来保证原子性的，从而确保线程安全。

需要简单说一下锁获取流程和锁释放流程吗? 

当一个线程调用 lock() 方法时（其核心逻辑在对应 Sync 子类的 tryAcquire 方法中）：
1. 它会尝试通过 CAS 操作将 state 从 0 修改为 1。如果成功，表示获取锁成功，AQS 会记录下当前线程为锁的独占持有者 (exclusiveOwnerThread)。
2. 如果 CAS 失败（说明锁可能已被持有），它会检查当前尝试获取锁的线程是否就是已经持有锁的那个线程。如果是，就直接把 state 的值加 1，完成一次重入。
3. 如果 state 不为 0，并且尝试获取锁的线程也不是当前持有者，那么获取锁失败。
这时，该线程就会被包装成一个 Node 节点，加入到 AQS 的等待队列中，并可能被挂起（park），等待后续被唤醒。
补充公平/非公平差异: 非公平锁在第一步之前会尝试抢占一次，而公平锁会先检查等待队列中是否有等待者。

当线程调用 unlock() 方法时（核心逻辑在 tryRelease 方法中）：
1. 首先会检查调用 unlock 的线程是否就是当前锁的持有者，如果不是会抛异常。
2. 然后，它会把 state 的值减 1。
3. 只有当 state 减到 0 时，才表示锁被完全释放了（因为可能之前有重入）。这时，会将独占持有者线程 (exclusiveOwnerThread) 清空。
4. 锁完全释放后，AQS 会检查其等待队列中是否有正在等待的线程，如果有，就会唤醒队列头部的下一个有效节点对应的线程 (unpark)，让它有机会去尝试获取锁。

总的来说，ReentrantLock 把锁的获取、释放、重入计数、公平性策略选择以及线程的排队、阻塞、唤醒等复杂逻辑，都巧妙地委托给了底层的 AQS 框架来实现。它主要是通过在 Sync 的子类中重写 tryAcquire 和 tryRelease 方法，定义如何根据 state 变量来判断和修改锁的状态，从而赋予了 AQS 框架具体的“可重入互斥锁”的语义。

## 公平锁与非公平锁的区别是什么？ReentrantLock默认是哪种模式？
公平锁与非公平锁的主要区别在于线程获取锁的顺序：
1. 公平性定义：
- 公平锁：严格按照线程请求的FIFO顺序获取锁
- 非公平锁：允许新到达的线程"插队"尝试获取锁，不保证等待时间最长的线程优先获取锁
2. 实现差异：
- 公平锁：在获取锁前，先检查是否有线程在等待（hasQueuedPredecessors方法）
- 非公平锁：直接尝试CAS获取锁，不检查等待队列
3. 性能特性：
- 公平锁：上下文切换更频繁，吞吐量通常较低，但避免线程饥饿
- 非公平锁：减少上下文切换，吞吐量通常更高，但可能导致某些线程长时间等待
4. 默认模式：
- ReentrantLock默认使用非公平锁模式
- 构造函数：public ReentrantLock() { sync = new NonfairSync(); }
- 可以通过new ReentrantLock(true)创建公平锁
```java
   // 非公平锁的tryAcquire实现
   final boolean nonfairTryAcquire(int acquires) {
       final Thread current = Thread.currentThread();
       int c = getState();
       if (c == 0) {
           // 直接尝试CAS获取锁，不检查队列
           if (compareAndSetState(0, acquires)) {
               setExclusiveOwnerThread(current);
               return true;
           }
       }
       else if (current == getExclusiveOwnerThread()) {
           // 重入逻辑
           int nextc = c + acquires;
           setState(nextc);
           return true;
       }
       return false;
   }
   
   // 公平锁的tryAcquire实现
   protected final boolean tryAcquire(int acquires) {
       final Thread current = Thread.currentThread();
       int c = getState();
       if (c == 0) {
           // 关键区别：先检查是否有线程在等待
           if (!hasQueuedPredecessors() && 
               compareAndSetState(0, acquires)) {
               setExclusiveOwnerThread(current);
               return true;
           }
       }
       else if (current == getExclusiveOwnerThread()) {
           // 重入逻辑
           int nextc = c + acquires;
           setState(nextc);
           return true;
       }
       return false;
   }
```

面试版本:
公平锁与非公平锁的主要区别在于线程获取锁的顺序策略：
1. 公平锁严格按照线程请求的FIFO顺序获取锁，也就是说，等待时间最长的线程会优先获取锁。
这种模式下，每个线程在尝试获取锁之前，都会检查等待队列中是否有其他线程在等待（通过hasQueuedPredecessors方法）。
如果有，则当前线程会加入队列等待，而不会尝试获取锁。
2. 非公平锁则允许'插队'现象，新到达的线程会先尝试直接获取锁，不管是否有其他线程在等待。
只有在获取失败后，才会加入等待队列。这种方式可能导致某些线程长时间等待（饥饿），但通常能提供更高的吞吐量，因为减少了线程切换和唤醒的开销。
3. 从源码实现上看，关键区别在于获取锁时是否调用hasQueuedPredecessors方法检查队列。
非公平锁直接尝试CAS获取锁，而公平锁会先检查是否有线程在等待。
ReentrantLock默认使用非公平锁模式，这是出于性能考虑。
在大多数应用场景下，非公平锁的吞吐量更高。如果需要公平锁，可以通过构造函数指定：new ReentrantLock(true)。
选择哪种模式取决于应用需求：如果对线程等待公平性要求高，选择公平锁；如果更注重系统整体吞吐量，选择非公平锁。
## ReentrantLock的Condition机制是如何实现的？

Condition提供了类似Object的wait/notify的线程等待/通知机制，但更加灵活。其实现原理如下：
1. 基本结构：
- Condition是一个接口，ReentrantLock通过newCondition()方法创建实例
- 实际返回的是AQS的内部类ConditionObject的实例
- 每个Condition维护一个独立的条件等待队列
2. 两个队列：
- 同步队列：管理获取锁的等待线程（AQS的队列）
- 条件队列：管理在Condition上等待的线程（每个Condition一个）
3. 核心方法：
- await()：将线程从同步队列移到条件队列，释放锁，阻塞线程
- signal()：将线程从条件队列移回同步队列，使其可以重新竞争锁
- 线程从条件队列到同步队列的转移是通过修改节点状态和队列引用实现的
4. await实现
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
5. signal实现
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
6. 使用要求：
- 必须在持有锁的情况下调用Condition的方法
- await()会释放锁，被唤醒后会重新竞争锁
- signal()/signalAll()不会释放锁，只是将线程移到同步队列

面试版本:
ReentrantLock的Condition机制提供了类似Object的wait/notify的线程等待/通知功能，但更加灵活和强大。
它的实现基于AQS的内部类ConditionObject。
当调用ReentrantLock的newCondition()方法时，实际上是创建了一个ConditionObject实例。
每个Condition维护一个独立的条件等待队列，这与AQS的同步队列是分开的。这种设计允许一个锁关联多个条件变量，实现更精细的线程控制。
Condition的核心方法await()和signal()的工作原理如下：
1. 当线程调用await()时，会发生几个关键步骤：
- 将当前线程封装成Node加入条件队列
- 完全释放持有的锁（支持可重入性）
- 阻塞当前线程，等待通知
- 被signal唤醒后，从条件队列转移到同步队列
- 重新竞争锁，获取锁后才会从await()返回
2. 当线程调用signal()时：
- 检查当前线程是否持有锁，没有则抛出异常
- 将条件队列中的首个节点转移到同步队列
- 该节点对应的线程稍后会被唤醒并尝试获取锁
这种机制的一个重要特点是，与Object的wait/notify不同，Condition允许精确唤醒特定条件上等待的线程。
例如，在生产者-消费者模式中，可以使用两个条件变量分别管理'缓冲区不满'和'缓冲区不空'两个条件，使得生产者只唤醒消费者，消费者只唤醒生产者，避免不必要的唤醒和上下文切换。
使用Condition时必须注意，所有方法都必须在持有锁的情况下调用，否则会抛出IllegalMonitorStateException异常，这一点与Object的wait/notify是一致的。
# 使用场景问题
## 在什么场景下你会选择使用ReentrantLock而不是synchronized？
适合使用ReentrantLock的场景：
1. 需要可中断锁获取：
- 长时间操作可能需要取消
- 用户交互场景，需要响应取消请求
- 避免死锁的场景
2. 需要超时获取锁：
- 限制等待时间，提高系统可用性
- 实现请求超时功能
3. 资源竞争激烈的场景
4. 需要尝试非阻塞获取锁：
- 有替代方案的场景
- "快速失败"而非等待的设计
5. 需要公平锁：
- 要求严格按照请求顺序获取锁
- 避免线程饥饿的场景
6. 需要多个条件变量：
- 复杂的线程协作模式
- 生产者-消费者模式的精确控制
- 资源池管理
7. 需要查询锁状态：
- 监控和诊断需求
- 高级线程管理

面试版本:
我会在需要synchronized无法提供的高级特性时选择ReentrantLock。具体场景包括：
首先，当需要可中断的锁获取操作时。例如，在一个用户可能取消的长时间操作中，我会使用lockInterruptibly()方法，这样当用户取消操作时，线程可以响应中断并释放资源，而synchronized无法做到这一点。
其次，需要超时获取锁的场景。在高负载系统中，为了避免线程长时间等待，我会使用tryLock(timeout)设置获取锁的超时时间，如果在指定时间内无法获取锁，线程可以执行替代逻辑或重试，提高系统的可用性。
第三，需要非阻塞尝试获取锁的场景。有时候我们希望尝试获取锁，但如果锁被占用就立即执行其他操作而不是等待，这时可以使用tryLock()方法。
第四，需要公平锁的场景。当系统对线程获取锁的顺序有严格要求，需要避免线程饥饿时，可以使用ReentrantLock的公平锁模式。
第五，需要多个条件变量的场景。例如在复杂的生产者-消费者模型中，我可能需要多个等待条件（如'缓冲区不满'和'缓冲区不空'），ReentrantLock可以创建多个Condition对象，而synchronized只能与一个隐式条件关联。
最后，当需要查询锁状态或等待线程信息时，ReentrantLock提供了isLocked()、getHoldCount()等方法，这在调试和监控方面非常有用。
不过，在简单的同步场景下，我仍然倾向于使用synchronized，因为它的代码更简洁，自动释放锁，并且在JDK 6后性能已经很好了。选择哪种机制主要取决于是否需要这些高级特性，而不仅仅是性能考虑。

## 如何正确使用ReentrantLock来避免死锁和资源泄露？
1. 使用try-finally确保释放锁：
- 标准模式展示
- 异常处理的重要性
2. 避免嵌套锁：
- 死锁风险分析
- 如何重构嵌套锁
3. 使用tryLock避免死锁：
- 超时获取多个锁
- 检测和恢复策略
4. 保持锁粒度最小：
- 只锁定必要的代码段
- 避免在锁内执行耗时操作
5. 使用锁顺序约定：
- 固定获取多个锁的顺序
- 如何设计锁层级
6. 使用lockInterruptibly处理中断：
- 响应中断的重要性
- 取消长时间等待

面试版本:
正确使用ReentrantLock需要遵循几个关键实践，以避免死锁和资源泄露：
最重要的是使用try-finally模式确保锁的释放。由于ReentrantLock需要显式释放，必须将unlock()放在finally块中，确保无论是正常执行还是发生异常，锁都能被释放：
```java
Lock lock = new ReentrantLock();
lock.lock();
try {
    // 临界区代码
} finally {
    lock.unlock();  // 确保锁始终被释放
}
```
其次，避免在持有锁的情况下调用外部方法，特别是那些可能获取其他锁的方法，这可能导致嵌套锁获取和死锁。如果必须调用外部方法，考虑先释放当前锁。
第三，使用tryLock()带超时参数的方式获取多个锁，可以有效避免死锁：
```java
boolean getLocks(Lock lock1, Lock lock2, long timeout) {
    long deadline = System.currentTimeMillis() + timeout;
    while (System.currentTimeMillis() < deadline) {
        if (lock1.tryLock()) {
            try {
                if (lock2.tryLock()) {
                    return true;
                }
            } finally {
                lock1.unlock();
            }
        }
        Thread.sleep(10); // 短暂休眠避免CPU忙等
    }
    return false;
}
```
第四，保持锁粒度最小，只锁定真正需要同步的代码段，避免在锁内执行耗时操作如I/O或网络请求，这不仅减少了死锁风险，也提高了并发性能。
第五，如果需要获取多个锁，始终按照相同的顺序获取，这是避免死锁的经典策略。可以基于锁对象的hashCode或其他固定属性确定获取顺序。
最后，合理使用lockInterruptibly()方法，允许线程在等待锁时响应中断，这提供了一种从潜在死锁中恢复的机制。
