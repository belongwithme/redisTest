# synchronized


## 你了解synchronized吗?
我了解 synchronized。它是 Java 中用于实现线程同步的关键字，是一种内置锁。
它的核心作用主要有三点：保证互斥性，即同一时间只有一个线程能执行同步代码；保证可见性，确保线程对共享变量的修改能被其他线程看到；以及保证有序性，防止指令重排序。
synchronized 可以修饰实例方法、静态方法和代码块。修饰实例方法时锁是 this 对象，修饰静态方法时锁是类的 Class 对象，修饰代码块则可以指定锁对象。
JVM 实现 synchronized 主要依靠 monitorenter 和 monitorexit 指令（用于代码块）或方法的 ACC_SYNCHRONIZED 标志。底层依赖对象头的 Mark Word 来记录锁信息，并使用 ObjectMonitor 来管理线程。
为了提升性能，从 JDK 6 开始，synchronized 引入了锁优化机制，包括锁升级：从无锁状态，根据竞争情况可能升级为偏向锁、轻量级锁，最后到重量级锁。还有自适应自旋、锁消除和锁粗化等优化手段。
相比 volatile，synchronized 还能保证原子性。相比 ReentrantLock，synchronized 使用更简单，由 JVM 自动管理锁的释放，但在功能灵活性上，如可中断、超时获取、公平性等方面，ReentrantLock 更强大。

## 请解释synchronized关键字的作用
synchronized关键字是Java提供的一种内置锁机制，用于实现线程同步.
它具有三个核心作用：互斥访问,内存可见性保证,防止指令重排序 - 互斥性,可见性,有序性。
- 互斥访问：确保同一时刻只有一个线程可以执行被synchronized保护的代码块或方法，防止多线程并发访问共享资源时产生冲突。
- 内存可见性：synchronized不仅提供互斥访问，还保证线程在获取锁时会刷新工作内存中的变量值，释放锁时会将修改后的变量值刷新到主内存，从而保证可见性。
- 防止指令重排序：synchronized代码块的执行具有原子性、可见性和有序性，编译器和处理器不会对synchronized块内的操作进行重排序。

## synchronized 的底层原理
synchronized 之所以能保证互斥性、可见性和有序性，主要是基于 JVM 的 Monitor (监视器锁) 机制和 Java 内存模型 (JMM) 的规定。
1. 对于互斥性:
   - 它的实现依赖于 Monitor。每个 Java 对象都可以关联一个 Monitor。当线程进入 synchronized 代码块或方法时，它需要获取对象 Monitor 的所有权。
   - 关键在于一个 Monitor 同一时刻只能被一个线程持有。其他尝试获取该 Monitor 的线程会被阻塞，直到持有者释放。这保证了同步代码的互斥执行。JVM 通过 monitorenter、monitorexit 指令（代码块）或方法的 ACC_SYNCHRONIZED 标志（方法）来管理 Monitor 的获取和释放。
2. 对于可见性:
   - 这主要由 Java 内存模型 (JMM) 保证。JMM 有一条关于锁的 Happens-Before 规则：对一个锁的解锁操作 happens-before 于后续对同一个锁的加锁操作。
   - 这意味着：线程释放锁时，JMM 会强制它将工作内存中修改的共享变量刷新到主内存；线程获取锁时，JMM 会强制它清空工作内存中共享变量的缓存，从主内存重新加载。这样就确保了线程间变量修改的可见性。
3. 对于有序性:
   - synchronized 同样依赖于 JMM 的 Happens-Before 规则。这条规则隐含地禁止了某些指令重排序。
   - 具体来说，JMM 会在 synchronized 的入口 (monitorenter) 和出口 (monitorexit) 处插入内存屏障，这限制了编译器和处理器的优化行为，确保同步代码块内部的操作不会被重排序到块的外部，反之亦然，从而保证了必要的执行顺序。

总结: 所以，synchronized 通过底层的 Monitor 机制实现了互斥访问，并通过 JMM 的 Happens-Before 规则以及内存屏障技术保证了可见性和有序性，这三者共同确保了并发环境下的线程安全。

扩展:
为了实现上述“锁的解锁 happens-before 后续对同一个锁的加锁”这条规则，JVM 必须采取具体的内存操作来确保可见性。
这个具体的内存操作就是你提到的：
- 解锁时: 强制将线程工作内存中修改过的共享变量刷新 (flush) 到主内存。这确保了操作 A（解锁前的所有写操作）的结果被写入主内存。
- 加锁时: 强制清空 (invalidate) 线程工作内存中关于共享变量的缓存，使得后续读取必须从主内存重新加载 (load)。这确保了执行操作 B（加锁后的读操作）的线程能够看到操作 A 写入主内存的结果。


## synchronized修饰不同结构时，使用的锁对象是什么
synchronized可以修饰四种不同的代码结构，每种结构对应不同的锁对象：
实例方法：锁对象是当前实例对象(this)
实例对象同步块：锁对象是当前实例对象(this)
静态方法：锁对象是当前类的Class对象
类同步块:锁对象是当前类的Class对象

## synchronized除了互斥外，还保证了什么？它与Java内存模型的关系是什么？
synchronized的三大保证
- 互斥性(Mutual Exclusion)：
确保同一时刻只有一个线程可以执行被synchronized保护的代码块或方法
防止多线程并发访问共享资源时产生冲突
- 可见性(Visibility)：
当线程获取锁时，会强制从主内存刷新变量的值到工作内存
当线程释放锁时，会强制将工作内存中的变量值刷新到主内存
这确保了一个线程对共享变量的修改对其他线程可见
- 有序性(Ordering)：
防止指令重排序，保证synchronized块内的代码按照程序的顺序执行
建立happens-before关系，确保锁释放操作happens-before后续获取同一个锁的操作
### synchronized与Java内存模型的关系
Java内存模型定义了线程如何与内存交互，以及多线程程序中变量的访问规则。
synchronized在JMM中扮演着重要角色：
1. 内存屏障作用：
- synchronized在进入和退出时，会插入内存屏障指令
- 进入时的读屏障确保后续读操作不会被重排序到synchronized块之前
- 退出时的写屏障确保之前的写操作不会被重排序到synchronized块之后
2. happens-before关系：
- JMM中定义了一个重要的happens-before规则：一个锁的释放happens-before于后续对同一个锁的获取
- 这意味着线程A在synchronized块中对变量的修改，对于随后获得同一锁的线程B是可见的
3. 工作内存与主内存的同步：
- 获取锁时，JVM会清空线程的工作内存，强制从主内存重新读取变量
- 释放锁时，JVM会将线程工作内存中的修改刷新到主内存
- 这解决了JMM中工作内存与主内存数据不一致导致的可见性问题


## 能简要描述JVM是如何实现synchronized的吗
JVM实现synchronized的机制可以从字节码、锁升级和底层实现三个层面来描述:
### 字节码层面
在字节码层面，synchronized的实现依赖于两条JVM指令：
- monitorenter：在进入同步块时使用，获取对象的monitor
- monitorexit：在退出同步块时使用，释放对象的monitor
对于同步方法，JVM通过方法标志位ACC_SYNCHRONIZED来标识，而不是显式的monitorenter和monitorexit指令。
### 锁升级机制
从Java 6开始，HotSpot JVM引入了锁升级机制，synchronized锁实现了从偏向锁→轻量级锁→重量级锁的渐进升级：
1. 偏向锁(Biased Locking)：
- 目的是减少无竞争情况下的同步开销
- 当一个线程首次获得锁时，锁会"偏向"这个线程
- 该线程再次请求锁时无需进行同步操作
2. 轻量级锁(Lightweight Locking)：
- 当有线程竞争偏向锁时，锁会升级为轻量级锁
- 使用CAS(Compare-And-Swap)操作尝试获取锁
- 适用于线程交替执行同步块的情况
3. 重量级锁(Heavyweight Locking)：
- 当CAS操作失败，锁会升级为重量级锁
- 基于操作系统的互斥量(Mutex)实现
- 线程会被阻塞和唤醒，涉及用户态和内核态的切换
### 底层实现
在底层实现上，synchronized依赖于对象头中的Mark Word和monitor机制：
1. Mark Word：
存储在对象头中，包含锁状态标志位,根据锁的状态，存储不同的信息：
- 无锁状态：存储对象的hashCode和GC分代年龄
- 偏向锁：存储线程ID、偏向时间戳和GC分代年龄
- 轻量级锁：存储指向线程栈中Lock Record的指针
- 重量级锁：存储指向ObjectMonitor对象的指针
2. ObjectMonitor：
- 重量级锁的核心数据结构,包含关键字段：
   - _owner：指向持有锁的线程
   - _WaitSet：等待线程集合（执行了wait()的线程）
   - _EntryList：阻塞线程集合（等待获取锁的线程）
   - _recursions：锁的重入次数
   - _count：线程获取锁的次数
 

扩展:
1. _EntryList (入口队列/阻塞队列):
- 目的: 存放那些正在尝试获取 Monitor 锁，但是因为锁已被其他线程持有而被阻塞的线程。
- 状态: 这些线程是活跃的竞争者。它们一旦有机会（即当前持有锁的线程释放了锁），就希望能立即获得锁并继续执行同步代码块。
- 进入条件: 线程尝试进入 synchronized 代码块或方法，但在获取 Monitor 时失败（因为锁被占用），并且经过自旋等优化手段后仍未获得锁，最终进入阻塞状态。
- 离开条件: 当前持有锁的线程释放锁时，JVM 会从 _EntryList 中唤醒一个或多个线程，让它们重新尝试竞争锁。
2. _WaitSet (等待队列):
- 目的: 存放那些在已经持有 Monitor 锁的情况下，调用了该对象的 wait() 方法的线程。
- 状态: 这些线程是主动放弃了锁，并进入等待状态。它们不是在竞争锁，而是在等待某个特定的条件发生变化。它们需要其他线程调用同一个对象的 notify() 或 notifyAll() 方法来唤醒。
- 进入条件: 线程持有锁，并执行了 object.wait()。执行 wait() 时，线程会自动释放它持有的 Monitor 锁，然后进入 _WaitSet。
- 离开条件: 其他线程调用了该对象的 notify() 或 notifyAll() 方法。被唤醒的线程会从 _WaitSet 移动到 _EntryList 中（或者有时可以直接竞争锁，具体策略看 JVM 实现），重新开始竞争 Monitor 锁。关键是：线程从 wait() 方法返回之前，必须重新获取到它之前释放的那个 Monitor 锁。
3. 总结一下关键区别:
- _EntryList 中的线程是因为锁被占用而阻塞，它们在等待锁。
- _WaitSet 中的线程是因为调用了 wait() 而等待，它们在等待被通知 (notify)，并且它们在进入 _WaitSet 时已经释放了锁。

如果只有一个队列，JVM 将无法区分这两种等待状态，也就无法正确实现 wait() / notify() / notifyAll() 这一套基于条件等待的线程间通信机制。例如，无法保证调用 wait() 的线程在被 notify() 后必须重新获取锁才能继续执行。因此，这两个队列是 Monitor 机制能够同时支持互斥访问和条件等待的基础。

## 你了解JDK 6后对synchronized做了哪些优化吗?
JDK 6之前，synchronized关键字的性能较差，竞争激烈时容易导致线程频繁阻塞和唤醒，开销较大。因此，很多开发者倾向于使用显式锁（如ReentrantLock）替代synchronized。
JDK 6对synchronized进行了显著的性能优化，这些优化使synchronized的性能大幅提升，在很多场景下已经接近ReentrantLock等显式锁的性能。主要优化包括：
1. 锁升级/锁膨胀机制
引入了从偏向锁→轻量级锁→重量级锁的渐进式锁升级机制：
- 偏向锁(Biased Locking)：针对无竞争情况，锁会"偏向"第一个获取它的线程，该线程再次请求锁时无需同步操作
- 轻量级锁(Lightweight Locking)：当有线程竞争偏向锁时，升级为轻量级锁，使用CAS操作尝试获取锁
- 重量级锁(Heavyweight Locking)：当CAS失败，升级为传统的重量级锁，基于操作系统的互斥量实现
2. 自适应自旋锁(Adaptive Spinning)
- 线程在获取锁失败后不会立即阻塞，而是执行一定次数的自旋等待
- JVM会根据历史自旋成功率动态调整自旋次数
- 自旋成功率高时增加自旋次数，成功率低时减少或取消自旋
- 这减少了线程阻塞和唤醒的开销，特别是在锁竞争不激烈且锁持有时间短的情况下
3. 锁消除(Lock Elimination)
- JIT编译器在运行时检测到某些同步操作是不必要的（如方法内部创建的局部对象）
- 编译器会优化掉这些不必要的同步操作
例如，StringBuffer的append方法是同步的，但如果编译器检测到它只在单线程中使用，会消除锁
4. 锁粗化(Lock Coarsening)
- 当JVM检测到一系列连续的加锁、解锁操作（如循环内的同步块）
- 会将这些操作合并为一次范围更大的加锁、解锁操作
- 减少了反复加锁解锁的开销
5. 偏向锁延迟(Biased Locking Delay)
- JDK 6中默认启用了偏向锁，但有4秒延迟
- 这是因为JVM启动阶段会有大量同步操作，此时启用偏向锁反而会降低性能
- 可以通过-XX:BiasedLockingStartupDelay=0参数取消延迟

这些优化使synchronized在大多数场景下都能获得良好的性能，特别是在无竞争或轻度竞争的情况下，同时保持了代码的简洁性和可读性。"

## 除了锁升级，JVM还有哪些优化synchronized的技术？

## 在什么情况下，synchronized的性能可能不如显式锁（如ReentrantLock）？
尽管JDK 6后对synchronized进行了显著优化，但在以下几种场景中，ReentrantLock等显式锁的性能和功能仍然可能优于synchronized：
需要高级锁特性的场景
synchronized不支持以下高级特性，这些场景下必须使用ReentrantLock：
- 可中断锁获取：ReentrantLock的lockInterruptibly()方法允许线程在等待锁时响应中断，而synchronized无法中断等待
- 超时锁获取：ReentrantLock可以通过tryLock(timeout)设置获取锁的超时时间，避免无限等待
- 公平锁：ReentrantLock可以创建公平锁，按照请求顺序分配锁资源，而synchronized总是非公平的
- 条件变量：ReentrantLock支持多个条件变量（Condition），而synchronized只能与一个隐式条件关联
2. 高度竞争且锁持有时间长的场景
在高度竞争环境下，特别是当锁持有时间较长时：
- synchronized的自适应自旋可能无法有效工作，导致频繁的线程阻塞和唤醒
- ReentrantLock提供更精细的控制机制，可以根据实际情况调整锁策略
- 在极高并发下，ReentrantLock的公平锁可以避免线程饥饿问题
3. 需要非阻塞尝试获取锁的场景
- ReentrantLock提供tryLock()方法，允许线程尝试获取锁但不阻塞，获取失败时可以立即执行其他逻辑
- synchronized不支持非阻塞获取锁，线程必须等待锁释放
这在需要避免死锁或实现复杂锁获取策略时非常有用
4. 需要精细粒度锁控制的场景
- ReentrantLock允许创建多个独立的锁实例，实现更精细的锁粒度控制
synchronized的锁粒度相对固定（对象锁或类锁）
在需要对不同资源使用不同锁的场景下，ReentrantLock更灵活
5. 锁降级场景
- ReentrantLock支持从写锁降级到读锁（通过ReadWriteLock接口）
- synchronized不支持锁降级
在读多写少的场景中，这一特性可以显著提高并发性能
总的来说，虽然现代JVM中synchronized已经非常高效，但在需要高级锁特性、精细控制或特殊锁策略的场景下，ReentrantLock仍然是更好的选择。选择哪种锁机制应该基于具体的应用场景和需求，而不仅仅是性能考虑。"


## 使用synchronized时有哪些常见的错误或陷阱？

### 锁对象选择不当
- 使用非final对象作为锁：如果锁对象可变，可能导致锁被替换，破坏同步机制。
- 使用String字面量或包装类作为锁：由于字符串常量池和包装类对象池的存在，看似不同的锁可能实际上是同一个对象。
### 锁粒度问题
- 锁粒度过粗：使用类级别的锁或全局锁导致不相关操作互相阻塞，降低并发性。
- 锁粒度过细：多个相关操作使用不同的锁，无法保证复合操作的原子性。
### 死锁问题
- 嵌套锁获取顺序不一致：不同方法获取多个锁的顺序不同，容易导致死锁。
### 同步范围不当
- 同步范围过大：包含耗时操作（如I/O）在同步块内，降低并发性能。
- 同步范围过小：没有覆盖所有需要原子执行的操作，导致线程安全问题。

## 能详细描述一下synchronized的锁升级过程吗？什么情况下会触发升级？
synchronized的锁升级是JDK 6引入的重要性能优化，它通过三种不同状态的锁实现了从低到高的渐进式升级机制。这个过程是不可逆的，一旦升级就不会降级。
### 锁的四种状态
synchronized锁有四种状态，按照升级顺序为：
1. 无锁状态：对象初始状态
2. 偏向锁：针对单线程访问优化
3. 轻量级锁：针对线程交替执行优化
4. 重量级锁：针对高竞争情况
### 升级过程与触发条件
1. 无锁状态
- 对象刚创建时处于无锁状态
- 没有线程持有锁
- 对象头中的Mark Word存储对象的HashCode、GC分代年龄等信息
2. 偏向锁
获取条件：第一个线程访问同步块且没有竞争
工作原理：将线程ID记录在对象头的Mark Word中，后续该线程再次获取锁时只需比对线程ID，无需CAS操作
升级触发：
- 当有其他线程尝试获取该锁
- 调用对象的hashCode方法（Mark Word空间冲突）
- 调用wait/notify方法
应用场景: 适合单线程反复获取同一把锁的场景
3. 轻量级锁
获取过程：
- 线程在栈帧中创建Lock Record，复制对象头的Mark Word
- 使用CAS操作尝试将对象头更新为指向Lock Record的指针
升级触发：
- CAS操作失败且自旋获取锁达到阈值（竞争激烈）
- 有线程调用wait/notify方法
应用场景: 适合线程交替执行同步块的场景
4. 重量级锁
工作原理：基于操作系统的互斥量实现，涉及线程阻塞和唤醒
特点：未获取到锁的线程会被阻塞，由操作系统调度
应用场景: 适合高并发、竞争激烈的场景

### 优化机制
1. 自适应自旋：
- JVM根据历史自旋成功率动态调整自旋次数
- 考虑CPU核数、线程状态等因素
2. 锁消除：
JIT编译时，去除不必要的加锁操作
3. 锁粗化：
合并相邻的同步块，减少加锁解锁次数

## synchronized和volatile在解决并发问题上有什么异同？
synchronized和volatile都是Java解决并发问题的关键字，但它们的作用机制和适用场景有明显区别:
### 相同点
1. 内存可见性：两者都能保证多线程之间的内存可见性，确保一个线程的修改对其他线程可见
2. 禁止重排序：都能在一定程度上禁止指令重排序，保证程序执行的有序性
### 不同点
1. 互斥性
synchronized提供互斥访问，同一时刻只允许一个线程执行同步块
volatile不提供互斥性，不能保证线程安全
2. 原子性
synchronized可以保证代码块的原子性执行
volatile只保证单个变量读/写的原子性，不保证复合操作（如i++）的原子性
3. 实现机制
synchronized基于Monitor锁机制实现，有锁的获取和释放过程
volatile基于内存屏障实现，通过禁止指令重排序和强制内存刷新实现可见性
4. 适用场景
synchronized适用于需要原子性操作的场景，如复合操作、资源互斥访问
volatile适用于一写多读的场景，如状态标志、DCL模式中的实例引用
5. 性能开销
synchronized有锁的获取和释放开销，JDK 6后通过锁升级机制优化性能
volatile性能开销较小，主要是内存屏障的开销

## 为什么偏向锁在多线程竞争时反而会降低性能？
偏向锁是JDK 6引入的一种锁优化机制，专门针对一个线程反复获取同一把锁的场景。但在多线程竞争环境下，偏向锁反而会降低性能，主要原因如下：
偏向锁是JDK 6引入的一种锁优化，专为单线程重复获取同一把锁设计。但在多线程竞争环境下，偏向锁反而会降低性能，主要有以下几个原因：
1. 撤销成本高昂：当第二个线程尝试获取偏向锁时，需要执行撤销操作，这个过程需要：
- 等待全局安全点（Stop-The-World）
- 暂停持有偏向锁的线程
- 遍历线程栈判断锁状态
- 执行锁升级
这些操作的开销远大于直接使用轻量级锁。
2. 频繁的锁撤销：在竞争环境下，不同线程交替获取锁会导致频繁的偏向锁撤销和重偏向，每次都需要全局安全点，严重影响性能。
3. 设计目标冲突：偏向锁的设计假设是'一把锁主要被同一个线程持有'，这与多线程竞争的场景本质上冲突。
4. JVM应对策略：
- 延迟开启偏向锁（默认4秒）
- 对频繁竞争的类执行批量重偏向或批量撤销
- 提供-XX:-UseBiasedLocking参数允许完全禁用偏向锁

在实际应用中，对于确定会有多线程竞争的场景，禁用偏向锁反而能提高性能。这也是为什么一些高并发框架会在启动参数中禁用偏向锁。"


## 请设计一个线程安全的缓存类，使用synchronized实现。
```java
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.LinkedHashMap;

/**
 * 一个线程安全的缓存实现，使用synchronized关键字保证线程安全
 * 支持缓存项过期和缓存容量限制
 */
public class ThreadSafeCache<K, V> {
    
    // 使用LinkedHashMap保存缓存项，支持按访问顺序排序，便于实现LRU策略
    private final Map<K, CacheItem<V>> cache;
    
    // 缓存最大容量，超过此容量时会移除最久未使用的项
    private final int maxSize;
    
    // 默认过期时间（毫秒），-1表示永不过期
    private final long defaultExpiration;
    
    // 清理过期缓存项的调度器
    private final ScheduledExecutorService cleanupScheduler;
    
    /**
     * 缓存项包装类，存储值和过期时间
     */
    private static class CacheItem<V> {
        private final V value;
        private final long expirationTime;
        
        public CacheItem(V value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }
        
        public boolean isExpired() {
            return expirationTime > 0 && System.currentTimeMillis() > expirationTime;
        }
        
        public V getValue() {
            return value;
        }
    }
    
    /**
     * 创建一个缓存实例
     * @param maxSize 缓存最大容量
     * @param defaultExpiration 默认过期时间（毫秒），-1表示永不过期
     * @param cleanupInterval 清理过期项的时间间隔（秒）
     */
    public ThreadSafeCache(int maxSize, long defaultExpiration, long cleanupInterval) {
        this.maxSize = maxSize;
        this.defaultExpiration = defaultExpiration;
        
        // 创建一个基于访问顺序的LinkedHashMap，当达到最大容量时移除最久未使用的项
        this.cache = new LinkedHashMap<K, CacheItem<V>>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, CacheItem<V>> eldest) {
                return size() > maxSize;
            }
        };
        
        // 创建定时清理任务
        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "cache-cleanup-thread");
            thread.setDaemon(true); // 设置为守护线程，不阻止JVM退出
            return thread;
        });
        
        // 定期执行清理任务
        this.cleanupScheduler.scheduleAtFixedRate(
            this::removeExpiredItems, 
            cleanupInterval, 
            cleanupInterval, 
            TimeUnit.SECONDS
        );
    }
    
    /**
     * 添加缓存项，使用默认过期时间
     */
    public void put(K key, V value) {
        put(key, value, defaultExpiration);
    }
    
    /**
     * 添加缓存项，指定过期时间
     * @param key 缓存键
     * @param value 缓存值
     * @param expiration 过期时间（毫秒），-1表示永不过期
     */
    public synchronized void put(K key, V value, long expiration) {
        long expirationTime = expiration < 0 ? -1 : System.currentTimeMillis() + expiration;
        cache.put(key, new CacheItem<>(value, expirationTime));
    }
    
    /**
     * 获取缓存项
     * @param key 缓存键
     * @return 缓存值，如果不存在或已过期则返回null
     */
    public synchronized V get(K key) {
        CacheItem<V> item = cache.get(key);
        
        if (item == null) {
            return null;
        }
        
        // 如果缓存项已过期，移除并返回null
        if (item.isExpired()) {
            cache.remove(key);
            return null;
        }
        
        return item.getValue();
    }
    
    /**
     * 检查缓存中是否包含指定键的有效（未过期）缓存项
     */
    public synchronized boolean containsKey(K key) {
        CacheItem<V> item = cache.get(key);
        if (item == null || item.isExpired()) {
            if (item != null) {
                cache.remove(key); // 移除已过期项
            }
            return false;
        }
        return true;
    }
    
    /**
     * 从缓存中移除指定键的缓存项
     * @return 被移除的缓存值，如果不存在则返回null
     */
    public synchronized V remove(K key) {
        CacheItem<V> item = cache.remove(key);
        return (item != null && !item.isExpired()) ? item.getValue() : null;
    }
    
    /**
     * 清空缓存
     */
    public synchronized void clear() {
        cache.clear();
    }
    
    /**
     * 获取缓存中的项数
     */
    public synchronized int size() {
        removeExpiredItems(); // 先清理过期项
        return cache.size();
    }
    
    /**
     * 移除所有过期的缓存项
     */
    private synchronized void removeExpiredItems() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * 关闭缓存，停止清理任务
     */
    public void shutdown() {
        cleanupScheduler.shutdown();
    }
}
```

### 使用示例
```java
public class CacheExample {
    public static void main(String[] args) throws InterruptedException {
        // 创建缓存：最大容量100，默认过期时间5秒，每2秒清理一次过期项
        ThreadSafeCache<String, String> cache = new ThreadSafeCache<>(100, 5000, 2);
        
        // 添加缓存项
        cache.put("key1", "value1");
        cache.put("key2", "value2", 10000); // 10秒后过期
        cache.put("key3", "value3", -1);    // 永不过期
        
        // 获取缓存项
        System.out.println("key1: " + cache.get("key1"));
        System.out.println("key2: " + cache.get("key2"));
        System.out.println("key3: " + cache.get("key3"));
        
        // 等待6秒，key1应该过期
        Thread.sleep(6000);
        
        System.out.println("After 6 seconds:");
        System.out.println("key1: " + cache.get("key1")); // 应该返回null
        System.out.println("key2: " + cache.get("key2")); // 应该仍然有效
        System.out.println("key3: " + cache.get("key3")); // 永不过期
        
        // 测试并发访问
        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                String key = "concurrent-" + i;
                cache.put(key, "value-" + i);
                cache.get(key);
                if (i % 100 == 0) {
                    cache.remove(key);
                }
            }
        };
        
        // 创建10个线程并发访问缓存
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(task);
            threads[i].start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("Cache size after concurrent operations: " + cache.size());
        
        // 关闭缓存
        cache.shutdown();
    }
}
```
