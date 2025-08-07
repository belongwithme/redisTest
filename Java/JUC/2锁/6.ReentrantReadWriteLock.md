@[toc](ReentrantReadWriteLock)

# 基础概念问题
## 什么是ReentrantReadWriteLock？它解决了什么问题？
ReentrantReadWriteLock是Java并发包中提供的一个读写锁实现，它维护了一对相关的锁：一个用于只读操作的读锁和一个用于写入操作的写锁。核心特点是允许多个读线程同时访问共享资源，而写线程访问时会阻塞所有其他线程（包括读线程）。
ReentrantReadWriteLock主要解决了"读多写少"场景下的性能问题。在传统的互斥锁（如synchronized或ReentrantLock）中，无论是读操作还是写操作，同一时刻只允许一个线程访问共享资源。这意味着即使是不会修改数据的读操作也会互相阻塞，造成不必要的性能损失。
通过读写分离的设计，ReentrantReadWriteLock允许：
1. 多个读线程可以同时获取读锁并发访问共享资源
2. 写线程获取写锁时独占访问，确保数据一致性
3. 写线程获取写锁时会阻塞所有读线程，防止读到不一致的数据
这种机制在读操作远多于写操作的场景下能显著提高系统的并发性能和吞吐量，同时保证数据的一致性。


个人理解版:
1. 它是什么？
它内部维护了两个锁：一个读锁 (ReadLock) 和一个写锁 (WriteLock)。
核心特点是：允许多个线程同时持有读锁进行并发读取，但写锁是独占的。
1. 它解决了什么问题？
主要解决了“读多写少”场景下的性能瓶颈问题。
像 synchronized 或 ReentrantLock 这样的传统互斥锁，不管是读还是写，同一时间只允许一个线程访问，即使是读操作也会相互阻塞，造成性能浪费。
ReentrantReadWriteLock 通过读写分离，允许多个读线程并发访问，大大提高了这类场景下的并发性能和系统吞吐量。
1. 核心机制：
读锁共享：多个线程可以同时获取读锁，并发读取共享资源。
写锁独占：当一个线程获取写锁时，其他所有线程（包括读线程和写线程）都会被阻塞，确保写入时的数据一致性。
写锁优先（默认非公平策略下）：当有线程请求写锁时，后续的读锁请求会被阻塞，防止写线程饥饿。
1. 与 ReentrantLock 的关键区别：
ReentrantLock 是互斥锁，任何时候只有一个线程能访问。
ReentrantReadWriteLock 是读写锁，允许多个读线程并发。因此，在读远多于写的场景下，ReentrantReadWriteLock 通常性能更好。
## ReentrantReadWriteLock与ReentrantLock的主要区别是什么？
ReentrantReadWriteLock与ReentrantLock的主要区别包括：
1. 锁类型：
- ReentrantLock是互斥锁，同一时刻只允许一个线程访问共享资源
- ReentrantReadWriteLock是读写锁，区分读写操作，允许多个读线程同时访问
1. 并发性能：
- 在读多写少场景下，ReentrantReadWriteLock性能更高，因为读操作可以并发执行
- 在读写均衡或写多场景下，ReentrantLock可能更简单高效，因为ReentrantReadWriteLock有额外开销
1. API复杂度：
- ReentrantLock API更简单，只有一种锁
- ReentrantReadWriteLock需要分别管理读锁和写锁，使用更复杂
1. 功能支持：
- 两者都支持公平性选择、可重入性、可中断获取锁
- ReentrantReadWriteLock支持锁降级（写锁降级为读锁）
- ReentrantLock的Condition功能更完整，ReadWriteLock的读锁不支持Condition
1. 内部实现：
- 都基于AQS，但ReentrantReadWriteLock使用state的不同位表示读写锁状态
- ReentrantReadWriteLock内部维护了两个锁：ReadLock和WriteLock

## ReentrantReadWriteLock的核心特性有哪些？
ReentrantReadWriteLock具有以下核心特性：
1. 读写分离：
- 读锁共享：多个线程可以同时获取读锁并发访问共享资源
- 写锁独占：写锁被获取时，其他线程无法获取任何锁
2. 写锁优先：有线程等待写锁时，新的读线程通常无法获取读锁（可通过公平性参数调整）
3. 可重入性：
- 读线程可以重复获取读锁
写线程可以重复获取写锁
- 持有写锁的线程可以获取读锁（锁降级），但持有读锁的线程不能获取写锁（不支持锁升级）
4. 锁降级：
- 支持从写锁降级到读锁：先获取写锁，再获取读锁，然后释放写锁
5. 公平性选择：
- 可以创建公平或非公平的读写锁：new ReentrantReadWriteLock(boolean fair)
- 公平锁：按照FIFO顺序获取锁，等待时间最长的线程优先获取锁
- 非公平锁（默认）：允许"插队"，提高吞吐量，但可能导致某些线程长时间等待
6. 锁获取中断：
- 支持可中断的锁获取操作：lockInterruptibly()
- 支持超时获取锁：tryLock(long timeout, TimeUnit unit)
7. 监控功能：
- 提供查询方法了解锁状态：getReadLockCount(), isWriteLocked(), getReadHoldCount()等
- 可以查询等待锁的线程信息：hasQueuedThreads(), getQueueLength()等


# 实现原理问题
## ReentrantReadWriteLock是如何实现读写分离的？
ReentrantReadWriteLock的实现基于AbstractQueuedSynchronizer(AQS)框架，通过巧妙的设计实现了读写分离的功能。其内部机制可以从以下几个方面详细分析：
1. 整体架构
ReentrantReadWriteLock内部包含三个主要组件：
- Sync类：继承自AQS，实现核心同步逻辑
- ReadLock类：读锁实现，内部持有Sync引用
- WriteLock类：写锁实现，内部持有Sync引用
Sync类有两个子类：FairSync（公平锁）和NonfairSync（非公平锁），根据构造函数的参数决定使用哪个实现。
2. 状态表示
ReentrantReadWriteLock最精妙的设计在于如何使用AQS的state变量同时表示读锁和写锁的状态
将32位的state变量分为两部分：
- 高16位（bit 16-31）：表示读锁的持有数，即有多少线程获取了读锁
- 低16位（bit 0-15）：表示写锁的重入次数
这种设计允许在一个原子变量中同时跟踪两种锁的状态.
这种位分割的设计使得：
- 每种锁的计数最大为65535（2^16-1）
- 可以通过位运算快速获取各自的计数
- 使用单个CAS操作原子性地更新状态
3. 读锁获取逻辑
读锁的获取逻辑在tryAcquireShared方法中实现，主要步骤：
1. 检查是否有写锁被其他线程持有（独占模式）
2. 如果没有写锁或写锁被当前线程持有，尝试获取读锁
3. 使用CAS操作增加state的高16位计数
4. 如果CAS成功，记录当前线程持有的读锁数量（支持重入）
```java
protected final int tryAcquireShared(int unused) {
    Thread current = Thread.currentThread();
    int c = getState();
    // 如果写锁被持有且不是当前线程持有，则失败
    if (exclusiveCount(c) != 0 && 
        getExclusiveOwnerThread() != current)
        return -1;
    
    int r = sharedCount(c);
    // 检查是否应该阻塞（公平性检查）及是否超过最大计数
    if (!readerShouldBlock() && 
        r < MAX_COUNT && 
        compareAndSetState(c, c + SHARED_UNIT)) {
        // 更新读锁计数相关的统计
        if (r == 0) {
            firstReader = current;
            firstReaderHoldCount = 1;
        } else if (firstReader == current) {
            firstReaderHoldCount++;
        } else {
            // 使用ThreadLocal跟踪每个线程的读锁持有数
            HoldCounter rh = cachedHoldCounter;
            if (rh == null || rh.tid != getThreadId(current))
                cachedHoldCounter = rh = readHolds.get();
            else if (rh.count == 0)
                readHolds.set(rh);
            rh.count++;
        }
        return 1;
    }
    // 处理CAS失败的情况，进行重试
    return fullTryAcquireShared(current);
}
```
4. 写锁获取逻辑
写锁的获取逻辑在tryAcquire方法中实现，主要步骤：
1. 检查是否有读锁或写锁被其他线程持有（共享模式）
2. 如果没有读锁或写锁，尝试获取写锁
3. 使用CAS操作增加state的低16位计数
4. 如果CAS成功，记录当前线程持有的写锁数量（支持重入）
```java
protected final boolean tryAcquire(int acquires) {
    Thread current = Thread.currentThread();
    int c = getState();
    int w = exclusiveCount(c);
    
    if (c != 0) {
        // 如果有读锁被持有或写锁被其他线程持有，则失败
        if (w == 0 || current != getExclusiveOwnerThread())
            return false;
        // 检查写锁重入是否超过最大计数
        if (w + exclusiveCount(acquires) > MAX_COUNT)
            throw new Error("Maximum lock count exceeded");
        // 重入，直接设置状态
        setState(c + acquires);
        return true;
    }
    
    // 检查是否应该阻塞（公平性检查）并尝试CAS获取锁
    if (writerShouldBlock() || 
        !compareAndSetState(c, c + acquires))
        return false;
    
    // 设置独占线程为当前线程
    setExclusiveOwnerThread(current);
    return true;
}
```
这种实现确保了：
- 多个读线程可以同时获取读锁（共享模式）
- 写线程获取写锁时独占访问（独占模式）
- 写锁被持有时，其他线程无法获取任何锁
- 支持锁重入和锁降级
锁降级是一个重要特性，指持有写锁的线程可以获取读锁，然后释放写锁。这允许线程在修改数据后安全地读取，同时允许其他读线程访问。但反向的锁升级（从读锁到写锁）不被支持，因为可能导致死锁。
为了优化性能，ReentrantReadWriteLock还使用了一些辅助数据结构来管理读锁状态，如firstReader、cachedHoldCounter和ThreadLocal变量readHolds，这减少了ThreadLocal访问的开销。
这种设计使得ReentrantReadWriteLock在读多写少的场景下能够提供很高的并发性能，同时保证数据的一致性。

## 什么是锁降级？为什么ReentrantReadWriteLock支持锁降级但不支持锁升级？
锁降级是指持有写锁的线程获取读锁，然后释放写锁的过程。在ReentrantReadWriteLock中，这个过程必须严格按照'获取写锁→获取读锁→释放写锁'的顺序执行。
锁降级的重要意义在于：
- 保证数据可见性：线程能够看到自己的写入结果
- 提高并发性：释放写锁后，其他读线程可以获取读锁，提高系统吞吐量
- 保持数据一致性：防止在读取过程中数据被其他线程修改
ReentrantReadWriteLock支持锁降级但不支持锁升级（从读锁到写锁）的原因有几个：
首先，锁升级可能导致死锁。假设两个线程都持有读锁，并且都尝试升级到写锁，它们会互相等待对方释放读锁，形成死锁。而锁降级不会有这个问题，因为写锁是独占的，同时只有一个线程能持有写锁。
其次，从实现角度看，当一个线程持有写锁时，可以安全地获取读锁，因为写锁已经保证了独占访问。但当一个线程持有读锁时，可能有其他线程也持有读锁，无法安全地直接获取写锁。
第三，锁降级符合数据访问的自然模式：先独占修改数据，然后共享读取数据。这种模式在缓存更新、数据预处理等场景中很常见。
最后,从性能角度考虑，支持锁升级会使实现更复杂，可能引入额外开销。而锁降级的实现相对简单，不会影响正常的锁获取性能。

## ReentrantReadWriteLock如何处理写线程饥饿问题？
写线程饥饿是ReentrantReadWriteLock在高并发读场景下可能面临的一个问题。当持续有新的读线程获取读锁时，等待写锁的线程可能长时间无法获取锁，导致'饥饿'。ReentrantReadWriteLock通过几种机制来缓解这个问题：
首先，ReentrantReadWriteLock实现了一种写线程优先的策略。在默认的非公平模式下，当有线程等待获取写锁时，新到达的读线程会被阻塞，即使当前没有线程持有写锁。
其次，ReentrantReadWriteLock提供了公平锁模式，可以通过构造函数参数启用：new ReentrantReadWriteLock(true)。在公平模式下，所有线程（包括读线程和写线程）都严格按照FIFO顺序获取锁。这确保了等待时间最长的线程（无论是读线程还是写线程）优先获取锁，从而避免任何线程长时间饥饿。

# 使用场景问题
## 在什么场景下应该使用ReentrantReadWriteLock而不是其他锁？
ReentrantReadWriteLock最适合用在'读多写少'的并发场景中。具体来说，以下几种情况特别适合使用读写锁：
首先，当读操作显著多于写操作时，读写锁能发挥最大价值。例如，缓存系统通常有大量的读取请求，但更新操作相对较少；配置管理系统中，配置信息频繁被读取，但很少修改。在这些场景中，允许多个线程同时读取可以显著提高系统吞吐量。
其次，当读操作耗时较长时，使用读写锁收益更大。如果读操作执行时间短暂，锁的获取和释放开销可能超过并发带来的收益。但对于复杂数据结构的遍历、大量数据的聚合计算等耗时操作，读写锁可以让多个操作并行执行，提高资源利用率。
第三，当需要保证数据一致性，同时又希望最大化读并发时。读写锁确保写操作独占访问，保证数据完整性，而读操作可以并发执行，不会看到不一致的中间状态。
具体应用场景包括：
- 本地缓存实现，如GuavaCache的早期版本
- 数据库连接池管理，读取连接状态与分配连接
- 配置中心，配置信息被多个组件频繁读取

不过，在以下情况下应避免使用ReadWriteLock：
- 写操作频繁的场景，锁竞争会导致性能下降
- 锁持有时间极短的场景，锁的开销可能超过收益
- 简单的原子操作可以用AtomicInteger等更轻量级的方案
- 需要多个条件变量的场景，因为读锁不支持Condition
## 如何正确使用ReentrantReadWriteLock来实现缓存？
缓存实现的关键点
- 读操作获取读锁：允许并发读取缓存数据
- 写操作获取写锁：独占更新缓存数据
- 处理缓存未命中：需要特别注意锁的获取和释放顺序
- 考虑锁降级：在某些场景下使用锁降级提高性能
- 避免死锁：确保锁的获取和释放正确配对
```java
public class CacheWithReadWriteLock<K, V> {
    private final Map<K, V> cache = new HashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();
    
    // 简单读取，如果不存在返回null
    public V get(K key) {
        readLock.lock();
        try {
            return cache.get(key);
        } finally {
            readLock.unlock();
        }
    }
    
    // 写入数据
    public void put(K key, V value) {
        writeLock.lock();
        try {
            cache.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }
    
    // 如果不存在则计算并存入
    public V computeIfAbsent(K key, Function<K, V> mappingFunction) {
        // 先尝试读取
        readLock.lock();
        try {
            V value = cache.get(key);
            if (value != null) {
                return value;
            }
        } finally {
            readLock.unlock();
        }
        
        // 值不存在，需要计算并写入
        writeLock.lock();
        try {
            // 再次检查，因为可能在释放读锁和获取写锁之间被其他线程修改
            V value = cache.get(key);
            if (value == null) {
                value = mappingFunction.apply(key);
                cache.put(key, value);
            }
            return value;
        } finally {
            writeLock.unlock();
        }
    }
    
    // 使用锁降级的复合操作示例
    public V getOrCompute(K key, Function<K, V> mappingFunction) {
        writeLock.lock();
        try {
            V value = cache.get(key);
            if (value == null) {
                value = mappingFunction.apply(key);
                cache.put(key, value);
            }
            
            // 获取读锁（开始锁降级）
            readLock.lock();
            try {
                // 释放写锁（完成锁降级）
                writeLock.unlock();
                
                // 执行可能耗时的操作，此时其他线程可以获取读锁
                return processValue(value);
            } finally {
                readLock.unlock();
            }
        } catch (Exception e) {
            writeLock.unlock();
            throw e;
        }
    }
    
    private V processValue(V value) {
        // 模拟一些耗时处理
        return value;
    }
    
    // 清除过期数据
    public void cleanup() {
        writeLock.lock();
        try {
            // 清理逻辑
        } finally {
            writeLock.unlock();
        }
    }
    
    // 获取缓存大小
    public int size() {
        readLock.lock();
        try {
            return cache.size();
        } finally {
            readLock.unlock();
        }
    }
}
```

