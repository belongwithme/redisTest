@[TOC](AQS)
# 基础概念问题
## 什么是AQS？它在Java并发包中的作用是什么？
AQS（AbstractQueuedSynchronizer）是Java并发包中的一个核心基础组件，它提供了一个框架，用于构建锁和各种同步器。AQS的主要作用包括：
1. 提供统一的同步器框架：AQS封装了实现同步器的复杂性，为开发者提供了一套通用的机制来构建各种同步工具。
2. 管理线程的等待与唤醒：AQS通过一个FIFO的等待队列来管理线程的排队、阻塞和唤醒，避免了开发者直接处理这些底层细节。
3. 状态管理：AQS维护一个表示同步状态的整型变量，不同的同步器可以基于这个状态实现不同的同步语义。
4. 提供模板方法：AQS采用模板方法设计模式，子类只需要实现少量的方法就能定义自己的同步语义，而复杂的队列管理由AQS负责。

Java并发包中的许多同步类，如`ReentrantLock、Semaphore、CountDownLatch、ReentrantReadWriteLock`等，都是基于AQS实现的。这种设计使得这些类有一致的行为模式，同时避免了重复实现类似的线程等待、唤醒和排队逻辑。

个人理解版:
AQS是AbstractQueuedSynchronizer的缩写，是Java并发包中的核心基础组件。它的主要作用是提供一个框架，用于构建锁和各种同步器。
AQS的核心思想是，通过一个int类型的状态变量和一个FIFO的等待队列，来实现线程的同步功能。它采用模板方法设计模式，将复杂的线程排队、阻塞和唤醒等底层操作封装起来，子类只需要根据自己的需求实现状态变量的维护逻辑。
在Java并发包中，像ReentrantLock、Semaphore、CountDownLatch、ReentrantReadWriteLock等常用的同步工具类，都是基于AQS实现的。这种设计大大简化了并发工具的开发，避免了重复实现类似的线程控制逻辑。"


面试版:
AQS（AbstractQueuedSynchronizer）是Java并发包java.util.concurrent下的一个核心基础框架，它的主要作用就是提供一个标准模板来帮助我们构建各种锁和同步器。
它的核心原理是围绕一个volatile的int类型的state变量和一个FIFO等待队列来工作的。state变量表示同步状态，而队列则用来管理获取状态失败的线程。
AQS采用了模板方法模式，把线程排队、阻塞、唤醒这些通用且复杂的操作都封装起来了。我只需要继承它，重写像tryAcquire、tryRelease这样的方法，定义好state的含义和操作逻辑，就可以实现自定义的同步器。
Java并发包中很多重要的类，像ReentrantLock、Semaphore、CountDownLatch等，都是基于AQS构建的

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

个人理解版:
AQS的核心原理可以概括为'状态变量+等待队列'。
- 首先，AQS使用一个volatile的int类型变量state表示同步状态，通过CAS操作保证其修改的原子性。
- 其次，AQS维护一个FIFO的等待队列，当线程获取同步状态失败时，会被包装成Node节点加入队列，并可能被阻塞。当同步状态释放时，会唤醒队列中的后继线程。
- AQS支持两种同步模式：独占模式和共享模式。独占模式下，同一时刻只有一个线程能获取同步状态，如ReentrantLock；共享模式下，多个线程可以同时获取同步状态，如Semaphore。
- AQS通过模板方法模式，定义了获取和释放同步状态的框架，子类只需要实现tryAcquire、tryRelease等方法，定义自己的同步语义。线程的排队、阻塞和唤醒等复杂操作都由AQS统一处理，这大大简化了同步器的实现。"


面试版:
AQS的核心原理主要围绕两点：一个volatile的int类型state变量和一个FIFO等待队列.
- state变量表示同步状态，它的具体含义由子类定义（比如锁重入次数、信号量许可数等），通过CAS保证原子更新。
- 当线程获取state失败时，会被构造成Node节点加入FIFO等待队列，并通过LockSupport.park()阻塞。
- 当持有state的线程释放资源时（修改state），会通过LockSupport.unpark()唤醒队列中的后继线程。
- AQS使用了模板方法模式，封装了线程排队、阻塞/唤醒的通用逻辑，只需继承并实现tryAcquire/tryRelease等方法定义state的操作即可构建同步器。它还支持独占和共享两种模式。

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


个人理解:
AQS的等待队列是一个FIFO双向链表，基于CLH队列变种实现。
- 结构: 由head和tail指针维护，每个节点（Node）包含线程引用、等待状态（waitStatus）、前后指针。
- 入队: 当线程获取同步状态失败，会创建Node，通过CAS原子地添加到队尾。在阻塞（LockSupport.park）前，通常会确保前驱节点的waitStatus为SIGNAL，表示需要被唤醒。
- 出队/唤醒: 当线程释放同步状态，会检查head节点状态。如果需要，会找到第一个有效后继节点，通过LockSupport.unpark唤醒它。被唤醒的线程尝试获取状态，成功后将自己设为新的head。
- 核心机制: 依赖CAS保证线程安全，LockSupport负责阻塞/唤醒，waitStatus协调节点间的唤醒逻辑。"


入队流程扩展:
首先，它会调用tryAcquire(arg)方法。这个tryAcquire是需要子类去重写的，用来尝试非阻塞地获取同步状态。比如在ReentrantLock里，它会检查state是否为0，或者当前线程是否已经是持有者。"
> 2. "如果tryAcquire成功（返回true），那么acquire方法就直接返回了，表示获取成功，非常高效。"
> 3. "如果tryAcquire失败（返回false），说明暂时无法获取资源。这时，AQS会做两件事："
> * "调用addWaiter(Node.EXCLUSIVE)方法，将当前线程包装成一个Node节点（标记为独占模式），并将其加入到等待队列的末尾。"
> * "接着调用acquireQueued(node, arg)方法。这个方法会让当前节点进入一个自旋等待的过程。在循环里，它会检查自己是不是队列中的第一个等待者（即前驱是头节点），如果是，就再次尝试调用tryAcquire。如果尝试成功，就把自己设为新的头节点并返回；如果失败或者自己不是第一个等待者，就会检查是否需要阻塞（通过shouldParkAfterFailedAcquire，通常会设置前驱节点的waitStatus为SIGNAL），如果需要，就调用LockSupport.park(this)阻塞当前线程，等待被前驱节点唤醒。"
> 4. "需要注意的是，acquireQueued在等待过程中不会响应线程中断。但它会记录中断状态。如果线程在等待时被中断过，acquire方法在最后会调用selfInterrupt()来重新设置当前线程的中断状态。"


出队流程扩展:
当持有同步状态的线程调用`release(arg)`方法释放同步状态时，会触发后续节点的出队和唤醒逻辑。这个过程主要涉及到`release`方法本身以及它调用的`unparkSuccessor`方法。

1.  **调用 `release(arg)` 方法**:
    *   线程调用`release(arg)`方法，尝试释放同步状态。
    *   内部会调用由子类实现的`tryRelease(arg)`方法。这个方法会根据具体的同步器逻辑（例如ReentrantLock中减少重入计数）来改变`state`变量。

2.  **`tryRelease(arg)` 执行成功**:
    *   如果`tryRelease(arg)`返回`true`，表示同步状态成功释放。
    *   接下来，代码会检查当前队列的头节点`head`。如果`head`不为`null`且其`waitStatus`不为0（通常意味着其后继节点处于`SIGNAL`状态，需要被唤醒），则会调用`unparkSuccessor(head)`方法。
    *   `release`方法源码片段:
        ```java
        public final boolean release(int arg) {
            if (tryRelease(arg)) {
                Node h = head;
                if (h != null && h.waitStatus != 0)
                    unparkSuccessor(h); // 核心唤醒逻辑
                return true;
            }
            return false;
        }
        ```

3.  **`unparkSuccessor(Node node)` 方法执行**:
    *   这个方法是唤醒后继节点的关键。参数`node`通常是当前的头节点`head`。
    *   `unparkSuccessor`会尝试找到一个合适（非`CANCELLED`状态）的后继节点并唤醒它。
    *   它首先会检查`node`（即`head`）的`waitStatus`。如果小于0（例如`SIGNAL`），会尝试将其CAS设置为0，表示唤醒的责任已经开始处理。
    *   然后，它会找到`node`的直接后继`s = node.next`。
    *   如果后继节点`s`为`null`或者其`waitStatus`大于0（即`CANCELLED`状态，表示该线程已放弃等待），则会从队列尾部向前遍历（`for (Node t = tail; t != null && t != node; t = t.prev)`），找到离`head`最近的、`waitStatus`小于等于0的节点作为实际需要唤醒的后继节点。这是为了处理队列中可能存在的已取消节点，确保唤醒的是一个有效的、正在等待的线程。
    *   一旦找到了合适的后继节点 `s`，就会调用`LockSupport.unpark(s.thread)`来唤醒该节点对应的线程。

4.  **被唤醒的线程尝试获取同步状态**:
    *   被`unparkSuccessor`唤醒的线程，之前是阻塞在`acquireQueued`方法中的`LockSupport.park(this)`调用处。
    *   唤醒后，该线程会从`park`点继续执行，进入下一轮的`acquireQueued`的自旋循环。
    *   在循环中，它会再次检查自己是否是队列中的第一个等待者（即前驱是头节点，`p == head`），如果是，就再次尝试调用`tryAcquire(arg)`。

5.  **获取成功并成为新的头节点**:
    *   如果此时`tryAcquire(arg)`成功（因为前一个持有者已经通过`tryRelease`释放了同步状态），那么这个被唤醒的节点就会将自己设置为新的头节点 (`setHead(node)`)。
    *   `setHead`方法会将当前节点设为head，并将其`thread`和`prev`字段设为`null`（因为头节点不需要这些信息，且有助于GC）。
    *   原头节点的`next`引用会被断开（间接通过新head的`prev`为null实现）。
    *   这个线程成功获取了同步状态，`acquireQueued`方法返回`false`（表示未在等待过程中被中断），整个`acquire`调用完成。如果等待过程中被中断过，`acquireQueued`会返回`true`，最终`acquire`方法会调用`selfInterrupt()`。

这个过程确保了当同步状态被释放时，等待队列中的下一个合适的线程会被公平地唤醒（通常是FIFO顺序，除非有节点取消），并有机会获取同步状态，从而实现了线程的有序调度和资源的有效利用。

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

这里补充一下:
这个线程已经在同步队列中获取到了锁(执行了acquire并返回为true,此时它是头节点),但是后面发现条件不满足,然后无奈调用await进入条件队列.

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


举例来看看:
```java
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 使用 ReentrantLock 和 Condition 实现的简单的阻塞队列
 */
class SimpleBlockingQueue<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;
    private final Lock lock = new ReentrantLock();
    // 条件：队列不为空 (用于消费者等待)
    private final Condition notEmpty = lock.newCondition();
    // 条件：队列不满 (用于生产者等待)
    private final Condition notFull = lock.newCondition();

    public SimpleBlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    /**
     * 生产者方法
     * @param item 要放入的元素
     * @throws InterruptedException
     */
    public void put(T item) throws InterruptedException {
        lock.lock(); // 1. 获取锁
        try {
            // 2. 检查条件：队列是否已满?
            while (queue.size() == capacity) {
                System.out.println("队列已满，生产者 " + Thread.currentThread().getName() + " 开始等待...");
                // 3. 条件不满足，调用 await() 进入 notFull 条件队列等待
                //    await() 方法会自动释放锁，并阻塞当前线程
                notFull.await();
                //    当被 signal() 唤醒后，会从这里继续执行，并自动重新获取锁
                System.out.println("生产者 " + Thread.currentThread().getName() + " 被唤醒，继续尝试放入...");
            }
            // 4. 条件满足（队列不满），执行操作
            queue.offer(item);
            System.out.println("生产者 " + Thread.currentThread().getName() + " 放入: " + item + " 当前大小: " + queue.size());

            // 5. 通知可能在等待的消费者：队列现在不为空了
            notEmpty.signal(); // 只唤醒一个等待的消费者线程

        } finally {
            lock.unlock(); // 6. 释放锁
        }
    }

    /**
     * 消费者方法
     * @return 取出的元素
     * @throws InterruptedException
     */
    public T take() throws InterruptedException {
        lock.lock(); // 1. 获取锁
        try {
            // 2. 检查条件：队列是否为空?
            while (queue.isEmpty()) {
                System.out.println("队列为空，消费者 " + Thread.currentThread().getName() + " 开始等待...");
                // 3. 条件不满足，调用 await() 进入 notEmpty 条件队列等待
                //    await() 方法会自动释放锁，并阻塞当前线程
                notEmpty.await();
                //    当被 signal() 唤醒后，会从这里继续执行，并自动重新获取锁
                System.out.println("消费者 " + Thread.currentThread().getName() + " 被唤醒，继续尝试取出...");
            }
            // 4. 条件满足（队列不为空），执行操作
            T item = queue.poll();
            System.out.println("消费者 " + Thread.currentThread().getName() + " 取出: " + item + " 当前大小: " + queue.size());

            // 5. 通知可能在等待的生产者：队列现在不满了
            notFull.signal(); // 只唤醒一个等待的生产者线程

            return item;
        } finally {
            lock.unlock(); // 6. 释放锁
        }
    }

    public static void main(String[] args) {
        SimpleBlockingQueue<Integer> queue = new SimpleBlockingQueue<>(5); // 容量为 5

        // 生产者线程
        Thread producer1 = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    queue.put(i);
                    Thread.sleep((long) (Math.random() * 100)); // 模拟生产耗时
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer-1");

        // 消费者线程
        Thread consumer1 = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    queue.take();
                    Thread.sleep((long) (Math.random() * 500)); // 模拟消费耗时
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer-1");
         // 消费者线程2
        Thread consumer2 = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    queue.take();
                    Thread.sleep((long) (Math.random() * 500)); // 模拟消费耗时
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer-2");

        producer1.start();
        consumer1.start();
        consumer2.start();
    }
}
```

代码解释：
获取锁 (lock.lock()): 生产者 (put) 或消费者 (take) 线程首先尝试获取 ReentrantLock。如果获取成功，它就进入了临界区。
检查条件 (while (queue.size() == capacity) 或 while (queue.isEmpty())): 在持有锁的情况下，线程检查它关心的业务条件是否满足。注意这里使用 while 循环而不是 if，这是为了防止"虚假唤醒"（spurious wakeup），即线程被唤醒了但条件仍然不满足。
条件不满足，调用 await():
如果队列满了（生产者）或空了（消费者），线程会调用相应 Condition 的 await() 方法。
这时，线程会自动释放它持有的 lock。
然后，线程进入与该 Condition 关联的条件队列中阻塞等待。
条件满足，执行操作: 如果条件满足（队列不满或不空），线程执行相应的操作（放入元素或取出元素）。
发出信号 (signal()): 操作完成后，线程可能会改变另一个条件的状态（比如放入元素使队列不再为空，取出元素使队列不再为满）。因此，它调用另一个 Condition 的 signal() 方法，尝试唤醒一个在该条件上等待的线程（如果有的话）。
释放锁 (lock.unlock()): 最后，线程在 finally 块中释放锁。


## 什么是临界区
在并发编程中，临界区指的是一段代码，这段代码会访问或修改共享资源（比如共享变量、共享数据结构、共享文件等）。
关键点在于：
1. 共享资源: 临界区操作的对象是多个线程都可能访问或修改的东西。在之前的 SimpleBlockingQueue 例子中，那个 Queue<T> queue 就是一个共享资源，生产者线程和消费者线程都会访问和修改它。
2. 并发访问问题: 如果有多个线程同时执行同一个临界区的代码，就可能会导致问题，比如：
竞态条件 (Race Condition): 执行结果依赖于线程执行的不可预测的顺序。
数据不一致: 共享资源可能被破坏，处于一个无效或错误的状态。
3. 需要保护: 为了保证程序的正确性和数据的一致性，临界区必须受到保护，以确保在任何时候最多只有一个线程能够执行这段代码。
如何保护临界区？
我们使用同步机制 (Synchronization Mechanisms) 来保护临界区，最常见的就是锁 (Lock)，比如我们例子中用的 ReentrantLock。
  - 线程在进入临界区之前，必须先获取锁 (lock.lock())。
  -如果锁已经被其他线程持有，那么当前线程就会被阻塞，直到锁被释放。
  - 线程在离开临界区之后，必须释放锁 (lock.unlock())，这样其他等待的线程才有机会获取锁并进入临界区。

## 如何实现一个基于AQS的自定义同步器？


## 你认为AQS设计中最精妙的部分是什么？为什么？
我认为AQS设计中最精妙的部分是它的'模板方法模式'与'状态管理'的结合，这种设计实现了框架与具体实现的完美分离。
AQS通过一个volatile的int类型state变量和一套精心设计的模板方法，成功地将复杂的线程同步机制拆分为'框架逻辑'和'具体同步语义'两部分。框架部分由AQS负责，包括线程排队、阻塞和唤醒等通用操作；而具体同步语义则由子类通过重写tryAcquire、tryRelease等方法来实现。
这种设计的精妙之处在于，它既保证了高度的代码复用，又提供了极大的灵活性。通过简单地重新定义state的含义和获取/释放的规则，开发者可以实现从互斥锁到信号量、从读写锁到倒计时器等各种不同的同步工具，而无需关心线程管理的复杂细节。
这种'一次设计、多处使用'的思想，使得Java并发包能够提供一系列功能强大且行为一致的同步工具，同时保持了较低的维护成本和较高的性能。这也是为什么我认为AQS是Java并发包中最优雅的设计之一。"