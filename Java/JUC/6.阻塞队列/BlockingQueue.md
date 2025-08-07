@[TOC](BlockingQueue)
# 基础知识考察
## 什么是BlockingQueue？它的主要特点和作用是什么？
BlockingQueue是Java并发包(java.util.concurrent)中的一个接口，继承自Queue接口，是线程安全的队列实现，提供了阻塞操作的功能。
主要特点：
1. 线程安全：内部实现了同步机制，可以安全地在多线程环境下使用
2. 阻塞操作：提供了阻塞的入队和出队操作，当队列满时，入队操作会阻塞；当队列空时，出队操作会阻塞
3. 可选的超时机制：支持设置阻塞操作的超时时间，超时后返回特殊值或抛出异常
4. 多种处理策略：对于队列满/空情况提供了多种处理方式：抛出异常、返回特殊值、阻塞等待、超时等待
5. 有界/无界队列：提供了有限容量和无限容量的不同实现
主要作用：
1. 线程协作：实现生产者-消费者模式，协调多线程间的工作
2. 流量控制：控制数据流转速率，防止生产者速度过快导致系统过载
3. 任务调度：在线程池和任务执行框架中作为任务队列使用
4. 数据缓冲：在异步系统中用作数据缓冲区，平滑处理速率差异
5. 背压机制：实现反馈控制，当消费者处理速度跟不上时，自然地阻塞生产者

个人理解版:
它解决了多线程编程中一个基本且关键的问题：如何安全高效地在线程间传递数据。

本质上，BlockingQueue是一个线程安全的队列，但它的特别之处在于"阻塞"特性。这种阻塞机制为线程协作提供了一种优雅的解决方案：当消费者尝试从空队列获取元素时，它会被自动挂起等待；当生产者尝试向已满队列添加元素时，同样会被挂起。这种机制保证了线程能够在适当的时机自动等待和唤醒，无需编写复杂的条件检查和信号发送代码。

BlockingQueue的核心价值在于它将并发控制的复杂性封装在队列内部，对外提供了简洁的接口。使用它可以轻松实现生产者-消费者模式，这是并发编程中的一个基础模式。在没有BlockingQueue之前，正确实现这种模式需要手动管理锁、条件变量和线程状态，容易出错且难以维护。

它的另一个重要特性是提供了流量控制能力，这在系统架构中非常关键.

我认为BlockingQueue最大的价值在于它简化了多线程编程模型，将复杂的同步逻辑隐藏在抽象背后，让开发者能够专注于业务逻辑而非并发控制细节。

## BlockingQueue接口定义了哪些核心方法？这些方法在处理队列满/空时有什么不同行为？
BlockingQueue接口定义了四类操作方法，它们在处理队列满/空时有不同的行为：
1. 抛出异常的方法：
add(E e)：队列满时，抛出IllegalStateException异常
remove()：队列空时，抛出NoSuchElementException异常
element()：队列空时，抛出NoSuchElementException异常
2. 返回特殊值的方法：
offer(E e)：队列满时，返回false
poll()：队列空时，返回null
peek()：队列空时，返回null
3. 阻塞方法：
put(E e)：队列满时，阻塞等待，直到有空间可用
take()：队列空时，阻塞等待，直到有元素可用
4. 超时方法：
offer(E e, long timeout, TimeUnit unit)：队列满时，阻塞指定时间，超时返回false
poll(long timeout, TimeUnit unit)：队列空时，阻塞指定时间，超时返回null
不同方法的行为对比：

| 操作 | 抛出异常 | 返回特殊值 | 阻塞 | 超时 |
|------|---------|------------|-----|------|
| 入队 | add(e) | offer(e) | put(e) | offer(e, time, unit) |
| 出队 | remove() | poll() | take() | poll(time, unit) |
| 查看 | element() | peek() | 不支持 | 不支持 |

个人版本:
通过深入研究BlockingQueue接口，我发现它的方法设计非常精妙，体现了一种"多策略"的思想。它提供了四类操作方法，每类方法对队列满/空状态有不同的处理策略，这使得开发者可以根据实际需求选择合适的交互方式。

第一类是"抛出异常"的方法，如add()、remove()和element()。这些方法源自Collection接口，行为直接而鲜明：操作无法完成就抛出异常。这类方法适合于那些"要么成功，要么立即失败"的场景，能够快速暴露问题。

第二类是"返回特殊值"的方法，如offer()、poll()和peek()。这些方法来自Queue接口，采用更温和的策略：操作失败不抛异常，而是返回一个表示失败的值（false或null）。这种设计让调用者可以优雅地处理失败情况，适合于队列满/空是预期可能发生的场景。

第三类是BlockingQueue接口真正的亮点：阻塞方法put()和take()。这类方法引入了线程等待的概念，当操作无法立即完成时，线程会进入等待状态，直到条件满足。这种自动的阻塞-唤醒机制大大简化了线程协作的编程模型，是实现生产者-消费者模式的理想工具。

第四类是带超时的方法，如offer(e, time, unit)和poll(time, unit)。它们结合了阻塞和超时特性，提供了一种折中方案：愿意等待，但不会无限等待。这在实时系统或需要响应性的应用中特别有用，可以避免线程永久阻塞。


## Java中有哪些常见的BlockingQueue实现类？它们有什么区别和适用场景？
1. ArrayBlockingQueue：
- 基于数组实现的有界阻塞队列
- 构造时需指定容量，一旦创建容量不可变
- 可选公平性参数，默认非公平
- 所有操作使用同一把锁，生产和消费相互竞争
- 适用场景：明确知道队列边界，需要较好性能的场景
2. LinkedBlockingQueue：
- 基于链表实现的可选有界阻塞队列
- 可指定容量上限，默认为Integer.MAX_VALUE（实际上接近无界）
- 使用两把锁分离生产和消费，可能比ArrayBlockingQueue吞吐量更高
- 适用场景：不确定队列边界或需要很大容量，消费与生产速率差异较大的场景
3. PriorityBlockingQueue：
- 基于优先级堆实现的无界阻塞队列
- 元素按照自然顺序或指定比较器排序
- 不保证同优先级元素的顺序
- 适用场景：需要按优先级处理的场景，如任务调度系统
4. DelayQueue：
- 无界延迟队列，元素需实现Delayed接口
- 元素只有到期后才能被取出
- 适用场景：延迟任务调度，如定时任务系统、缓存过期清理
5. SynchronousQueue：
- 没有实际容量的阻塞队列
- 每个插入操作必须等待对应的移除操作，反之亦然
- 可选公平性参数
- 适用场景：直接交付模式，适合"手递手"传递数据
6. LinkedTransferQueue：
- 基于链表实现的无界阻塞队列
- 融合了SynchronousQueue和LinkedBlockingQueue的特性
- 提供了transfer方法，允许生产者等待消费者处理元素
- 适用场景：需要高吞吐量且希望低延迟的场景
7. LinkedBlockingDeque：
- 基于链表实现的双端阻塞队列
- 支持在队列两端进行阻塞操作
- 适用场景：需要从两端操作队列的场景，如工作密取(work stealing)算法

区别与选择：
- 边界考虑：有界(ArrayBlockingQueue)vs近似无界(LinkedBlockingQueue)
- 性能考虑：单锁(ArrayBlockingQueue)vs分离锁(LinkedBlockingQueue)
- 功能考虑：FIFO(基本队列)vs优先级(PriorityBlockingQueue)vs延时(DelayQueue)
- 直接传递：SynchronousQueue适合生产和消费速率匹配的场景
- 内存占用：ArrayBlockingQueue预分配所有空间，LinkedBlockingQueue按需分配

ArrayBlockingQueue是一个基于数组的有界队列，结构简单高效。它最大的特点是必须在创建时指定容量，一旦创建，容量不可变。内部使用单个锁控制所有操作，这意味着生产者和消费者会相互竞争。它的优势在于实现简单，内存占用固定，不会因为元素增加而无限扩展。我认为它适合于"明确知道边界"的场景，例如处理固定大小的批次任务。
相比之下，LinkedBlockingQueue基于链表实现，更加灵活。它可以是有界的，也可以是近似无界的（默认容量为Integer.MAX_VALUE）。它的关键优势是使用了两把锁分别控制入队和出队操作，这意味着在多核环境下，生产者和消费者可以并行工作，理论上能够提供更高的吞吐量。它适合于"边界不确定"或生产消费速率差异较大的场景。
PriorityBlockingQueue为元素增加了优先级维度，它是一个基于优先级堆的无界队列。元素按照自然顺序或指定的比较器排序，而不是简单的FIFO。这种特性使其非常适合于需要按优先级处理任务的系统，如任务调度器或事件处理系统。需要注意的是，虽然队列无界，但仍受限于可用内存，当元素数量过多时可能导致OutOfMemoryError。
DelayQueue是一个特殊的无界阻塞队列，队列中的元素必须实现Delayed接口。它的独特之处在于：元素只有到达其指定的延迟时间后才能被取出。这种机制使它成为实现定时任务的理想选择，例如缓存过期清理、定时提醒等场景。
SynchronousQueue是一个没有容量的队列，它的行为更像是一个交换点：每个插入操作必须等待一个对应的移除操作，反之亦然。这种设计使得它在"直接传递"场景中表现出色，适合于生产者和消费者速率匹配的场景。在线程池中，SynchronousQueue常用于实现"无队列"的执行器，每个任务要么立即执行，要么创建新线程，要么被拒绝。
LinkedTransferQueue是Java 7引入的一个强大实现，它结合了LinkedBlockingQueue和SynchronousQueue的特性。除了标准的队列操作外，它还提供了transfer方法，允许生产者等待消费者处理元素。这种灵活性使其在高性能场景中表现优秀。
选择合适的BlockingQueue实现需要考虑多个因素：队列边界、性能需求、功能特性、内存约束等。
不同的实现在这些方面有不同的权衡，没有一种实现适合所有场景。

## PriorityBlockingQueue数据结构
PriorityBlockingQueue的数据结构是基于二叉堆实现的线程安全优先级队列。其内部使用可扩容的对象数组作为存储结构，通过ReentrantLock保证线程安全，并使用Condition实现阻塞机制。
它的核心特点包括：
1. 底层使用数组实现的完全二叉树结构
2. 默认是小顶堆，即每个节点值都小于或等于其子节点值
3. 使用单一锁控制并发访问
4. 无界队列设计，不会因队列满而阻塞
5. 支持自动扩容
关键操作的时间复杂度为：入队O(log n)、出队O(log n)、查看队首O(1)。
元素排序支持两种方式：元素实现Comparable接口或提供Comparator比较器。适用于任务调度等需要按优先级处理的多线程场景。
个人理解版本:
PriorityBlockingQueue在我看来是一个巧妙融合了"优先级"和"线程安全"的数据结构。
它的核心是一个二叉堆，像一个特殊的金字塔 - 最高优先级的元素总是在塔尖，随时准备被取走。当我们取走塔尖元素后，整个结构会自动调整，确保新的塔尖仍然是当前最高优先级的元素。
PriorityBlockingQueue使用单一锁设计。为什么做这个选择？我认为这是因为优先级队列的核心操作 - 入队和出队都需要调整堆结构，这不是简单的头尾操作，而是可能影响整个数据结构的重排。使用单一锁虽然降低了并发度，但极大简化了实现复杂度，避免了多锁可能引起的死锁和不一致问题。
作为无界队列，它只会在出队时阻塞（队列空），而不会在入队时阻塞。这使它特别适合"多生产者-单消费者"场景.

## 阻塞队列的实现原理是什么?
从本质上看，阻塞队列的实现是"生产者-消费者"模式的一种具体实现。
这种模式的关键挑战在于：如何安全地在多线程间传递数据，以及如何在队列空/满时让线程等待而不是失败或轮询。
以Java中最典型的ArrayBlockingQueue为例，其核心实现依赖于两个关键组件：锁(ReentrantLock)和条件变量(Condition)。锁确保了对队列的并发访问安全，而条件变量则提供了线程等待和通知的机制。
从内存模型角度看，阻塞队列的实现充分利用了Java的happens-before关系：锁的释放happens-before随后对同一锁的获取，这保证了一个线程的修改对后续获取锁的线程可见。这种可见性保证是线程安全的基础。
也体现了一种优雅的资源管理模式：线程不需要主动轮询资源状态，而是在资源不可用时自动挂起，在资源可用时自动恢复。这种"事件驱动"的设计减少了CPU资源的浪费，提高了系统效率。
总的来说，阻塞队列的实现是并发编程中锁、条件变量、原子操作和线程协作的一种结合

## blockingqueue实现原理
1. 核心机制
BlockingQueue的核心实现原理是基于锁和条件变量的线程阻塞与唤醒机制。现代JDK实现主要使用ReentrantLock和Condition来控制线程的等待和通知。
最基本的机制是：当队列已满时，生产者线程会在一个'notFull'条件上等待；当队列为空时，消费者线程会在一个'notEmpty'条件上等待。每次入队成功后会唤醒等待的消费者，每次出队成功后会唤醒等待的生产者。
这体现了一种优雅的资源管理模式：线程不需要主动轮询资源状态，而是在资源不可用时自动挂起，在资源可用时自动恢复。这种'事件驱动'的设计减少了CPU资源的浪费，提高了系统效率。
2. 不同实现的特点
JDK提供了多种BlockingQueue实现，主要区别在于内部数据结构和锁策略：
- ArrayBlockingQueue使用单锁设计，所有操作共享一把锁，基于循环数组实现，容量固定，创建时一次性分配内存。
- LinkedBlockingQueue采用分离锁设计，使用putLock和takeLock两把锁分别控制入队和出队操作，允许生产者和消费者并发执行，基于链表实现，容量可动态增长。
- 此外还有特殊用途的实现，如按优先级出队的PriorityBlockingQueue，延迟获取的DelayQueue，以及直接交付模式的SynchronousQueue等。
3. 操作类型与等待策略
BlockingQueue提供了三类操作方法，对应不同的等待策略：
- 阻塞操作：put()和take()方法在队列满/空时会无限期阻塞，直到条件满足。
- 超时操作：offer(e, time, unit)和poll(time, unit)方法提供了带超时的阻塞，在指定时间内等待，超时后返回失败。
- 非阻塞操作：offer()和poll()方法不会阻塞，如果操作不能立即完成，则立即返回false或null.
4. 线程安全保证
BlockingQueue实现保证了线程安全，主要通过以下机制：
- 所有共享状态的修改都受到锁的保护
- 使用volatile变量或AtomicInteger等确保可见性和原子性
- 条件变量的等待和唤醒机制确保线程协调的正确性
- 避免虚假唤醒问题，条件判断通常放在while循环中

这些机制共同确保了在高并发环境下BlockingQueue的正确性和可靠性，使其成为构建线程安全的生产者-消费者模式的理想选择。
## ArrayBlockingQueue为什么不能同时进行存取
ArrayBlockingQueue不能同时进行存取操作的根本原因在于它的单锁设计。具体来说：
1. 单锁机制
ArrayBlockingQueue内部只使用了一个ReentrantLock来控制对队列的所有访问操作：
```java
// ArrayBlockingQueue核心成员变量
final Object[] items;      // 存储元素的数组
final ReentrantLock lock;  // 控制所有访问的锁
```
由于只有一把锁，当一个线程获取到锁进行入队(put/offer)操作时，其他线程无法同时获取锁进行出队(take/poll)操作，反之亦然。这导致入队和出队操作无法并发执行。

2. 实现原理分析
查看ArrayBlockingQueue源码可以发现，所有修改队列的方法都需要先获取这个唯一的锁：
```java
// 入队操作
public boolean offer(E e) {
    // 空值检查
    if (e == null) throw new NullPointerException();
    final ReentrantLock lock = this.lock;
    lock.lock();  // 获取锁
    try {
        // 队列满时直接返回false
        if (count == items.length)
            return false;
        else {
            enqueue(e);  // 入队操作
            return true;
        }
    } finally {
        lock.unlock();  // 释放锁
    }
}

// 出队操作
public E poll() {
    final ReentrantLock lock = this.lock;
    lock.lock();  // 获取锁
    try {
        return (count == 0) ? null : dequeue();  // 队列空时返回null，否则出队
    } finally {
        lock.unlock();  // 释放锁
    }
}
```
3. 设计考量
ArrayBlockingQueue采用单锁设计主要基于以下考量：
- 简化实现：单锁设计简化了队列的实现复杂度，减少了死锁风险
- 避免同步开销：避免了使用多把锁可能引入的额外同步开销
- 数组结构特性：基于数组的循环队列中，入队和出队操作都需要修改共享状态(count、putIndex、takeIndex)，使用单锁更容易确保一致性
- 适用场景：设计用于平衡吞吐量较小的场景，而非高并发环境

## LinkedBlockingQueue咋设计的，和ArrayBlockingQueue的区别，使用的时候如何选择
1. 设计原理
"LinkedBlockingQueue和ArrayBlockingQueue都是Java并发包中的阻塞队列实现，核心区别在于它们的锁机制和底层数据结构。
LinkedBlockingQueue采用了分离锁设计，使用两个独立的ReentrantLock分别控制队列的头部和尾部操作，这允许入队和出队操作可以并发执行，大大提高了并发吞吐量。而它的底层结构是链表，可以动态增长。
相比之下，ArrayBlockingQueue采用单锁设计，所有操作共享同一把ReentrantLock，这意味着入队和出队操作无法同时进行。它基于定长数组实现，创建时必须指定容量。"
2. 实现区别
"从实现细节看，两者有几个关键差异：
- 锁实现：LinkedBlockingQueue使用putLock和takeLock两把锁，并通过AtomicInteger类型的count变量协调；ArrayBlockingQueue只有一把锁。
- 容量特性：LinkedBlockingQueue默认容量是Integer.MAX_VALUE，实际上接近于无界队列；而ArrayBlockingQueue必须在创建时指定固定容量。
- 内存分配：LinkedBlockingQueue按需分配节点，内存占用随元素增加而增长；ArrayBlockingQueue在创建时一次性分配固定内存。
- 公平性支持：ArrayBlockingQueue支持创建时指定是否为公平锁，而LinkedBlockingQueue不支持公平锁选项。"
3. 性能特点
"性能方面，两者各有优势：
LinkedBlockingQueue由于采用分离锁设计，在高并发场景下吞吐量通常更高，特别是当生产者和消费者速率不匹配时优势更明显。但它的节点创建和GC压力较大。
ArrayBlockingQueue由于内存连续，缓存亲和性更好，在元素数量固定且生产消费速率接近平衡的场景中表现出色。但高并发下锁竞争更严重，可能成为瓶颈。"
4. 选择标准
"在实际应用中，我会根据以下标准选择：
a. 选择LinkedBlockingQueue的场景：
1. 需要较高并发吞吐量
2. 队列大小不确定或波动较大
3. 生产者消费者速率差异大
b. 选择ArrayBlockingQueue的场景：
1. 明确知道容量上限且内存敏感
2. 需要严格控制资源使用
3. 需要公平队列访问保证
4. 对GC敏感的应用
总的来说，LinkedBlockingQueue适合追求高吞吐量的场景，而ArrayBlockingQueue适合追求内存确定性和稳定性的场景。"
##  编程题：写一个组件，可以缓存请求，请求到达一定数量或者过一段时间统一入库等操作。（核心是使用BlockingQueue实现）
```java
public class BatchRequestProcessor<T> {
    // 阻塞队列存储请求
    private final BlockingQueue<T> requestQueue;
    // 批处理阈值
    private final int batchSize;
    // 超时时间(毫秒)
    private final long timeoutMs;
    // 处理器
    private final Consumer<List<T>> processor;
    // 是否运行
    private volatile boolean running = true;
    // 处理线程
    private Thread processorThread;

    public BatchRequestProcessor(int capacity, int batchSize, long timeoutMs, Consumer<List<T>> processor) {
        this.requestQueue = new LinkedBlockingQueue<>(capacity);
        this.batchSize = batchSize;
        this.timeoutMs = timeoutMs;
        this.processor = processor;
    }

    // 添加请求到队列
    public boolean addRequest(T request) throws InterruptedException {
        return requestQueue.offer(request, 100, TimeUnit.MILLISECONDS);
    }

    // 启动处理
    public void start() {
        processorThread = new Thread(() -> {
            List<T> batch = new ArrayList<>(batchSize);
            while (running) {
                try {
                    // 尝试在超时时间内获取第一个元素
                    T firstRequest = requestQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
                    
                    if (firstRequest != null) {
                        // 添加第一个元素
                        batch.add(firstRequest);
                        
                        // 继续非阻塞地获取更多元素直到达到批量大小
                        requestQueue.drainTo(batch, batchSize - 1);
                        
                        // 处理批量请求
                        processor.accept(new ArrayList<>(batch));
                        
                        // 清空批次为下一轮做准备
                        batch.clear();
                    }
                    // 如果超时且队列为空，处理周期性批处理逻辑，但当前无数据
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    // 处理异常但不中断主循环
                    e.printStackTrace();
                }
            }
        });
        processorThread.start();
    }

    // 关闭处理器
    public void shutdown() {
        running = false;
        processorThread.interrupt();
        // 处理剩余请求
        List<T> remaining = new ArrayList<>();
        requestQueue.drainTo(remaining);
        if (!remaining.isEmpty()) {
            processor.accept(remaining);
        }
    }
}

```
1. 核心数据结构选择
- 使用BlockingQueue作为缓存区，它能自动处理线程安全问题
- 选择LinkedBlockingQueue而非ArrayBlockingQueue，因为前者无固定容量限制，更适合处理流量波动的请求
2. 双触发机制设计
- 数量触发：当缓存的请求达到预设的batchSize时触发处理
- 时间触发：即使请求数量未达阈值，也会在指定时间后处理，避免请求长时间滞留
3. 处理线程模型
- 创建单独的消费者线程，不阻塞主线程
- 使用poll(timeout)实现超时等待，比wait/notify更简洁可靠
- 处理完一批后立即开始下一批的收集，形成连续处理
4. 批量获取优化
- 先用poll(timeout)获取第一个元素(可能阻塞)
- 然后用非阻塞的drainTo()一次性获取剩余元素，减少多次获取的开销
5. 优雅关闭机制
- 通过volatile标志控制处理线程的运行状态
- 关闭时处理剩余请求，确保数据不丢失
- 使用中断机制停止处理线程
6. 异常处理策略
- 捕获并处理异常，确保处理线程不会因单次处理失败而终止
- 对中断异常特殊处理，允许线程正常退出
## 利用blockingQueue设计一个生产者消费者模式，如果是nonBlockingQueue应该怎么修改
```java
public class ProducerConsumerWithBlockingQueue {
    private final BlockingQueue<Integer> queue;
    private final int poisonPill = -1;
    private final int producerCount;
    private final int consumerCount;

    public ProducerConsumerWithBlockingQueue(int capacity, int producerCount, int consumerCount) {
        this.queue = new LinkedBlockingQueue<>(capacity);
        this.producerCount = producerCount;
        this.consumerCount = consumerCount;
    }

    public void start() {
        // 创建并启动生产者
        List<Thread> producers = new ArrayList<>();
        for (int i = 0; i < producerCount; i++) {
            Thread producer = new Thread(new Producer(queue, poisonPill, consumerCount), "Producer-" + i);
            producers.add(producer);
            producer.start();
        }

        // 创建并启动消费者
        List<Thread> consumers = new ArrayList<>();
        for (int i = 0; i < consumerCount; i++) {
            Thread consumer = new Thread(new Consumer(queue, poisonPill), "Consumer-" + i);
            consumers.add(consumer);
            consumer.start();
        }

        // 等待生产者完成
        for (Thread producer : producers) {
            try {
                producer.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 等待消费者完成
        for (Thread consumer : consumers) {
            try {
                consumer.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Producer implements Runnable {
        private final BlockingQueue<Integer> queue;
        private final int poisonPill;
        private final int consumerCount;

        Producer(BlockingQueue<Integer> queue, int poisonPill, int consumerCount) {
            this.queue = queue;
            this.poisonPill = poisonPill;
            this.consumerCount = consumerCount;
        }

        @Override
        public void run() {
            try {
                // 生产数据
                for (int i = 0; i < 10; i++) {
                    queue.put(i); // 阻塞式放入数据
                    System.out.println(Thread.currentThread().getName() + " 生产: " + i);
                    Thread.sleep(100); // 模拟生产耗时
                }

                // 放入，通知消费者结束
                for (int i = 0; i < consumerCount; i++) {
                    queue.put(poisonPill);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Consumer implements Runnable {
        private final BlockingQueue<Integer> queue;
        private final int poisonPill;

        Consumer(BlockingQueue<Integer> queue, int poisonPill) {
            this.queue = queue;
            this.poisonPill = poisonPill;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Integer value = queue.take(); // 阻塞式获取数据
                    if (value == poisonPill) {
                        System.out.println(Thread.currentThread().getName() + " 收到结束信号");
                        break;
                    }
                    System.out.println(Thread.currentThread().getName() + " 消费: " + value);
                    Thread.sleep(200); // 模拟消费耗时
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

```
NonBlockingQueue实现版本
```java
public class ProducerConsumerWithNonBlockingQueue {
    private final Queue<Integer> queue;
    private final int capacity;
    private final int poisonPill = -1;
    private final int producerCount;
    private final int consumerCount;
    
    // 使用锁和条件变量代替阻塞队列的阻塞功能
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public ProducerConsumerWithNonBlockingQueue(int capacity, int producerCount, int consumerCount) {
        this.queue = new ConcurrentLinkedQueue<>(); // 非阻塞队列
        this.capacity = capacity;
        this.producerCount = producerCount;
        this.consumerCount = consumerCount;
    }

    public void start() {
        // 创建并启动生产者
        List<Thread> producers = new ArrayList<>();
        for (int i = 0; i < producerCount; i++) {
            Thread producer = new Thread(new Producer(queue, capacity, lock, notFull, notEmpty, poisonPill, consumerCount), "Producer-" + i);
            producers.add(producer);
            producer.start();
        }

        // 创建并启动消费者
        List<Thread> consumers = new ArrayList<>();
        for (int i = 0; i < consumerCount; i++) {
            Thread consumer = new Thread(new Consumer(queue, lock, notFull, notEmpty, poisonPill), "Consumer-" + i);
            consumers.add(consumer);
            consumer.start();
        }

        // 等待所有线程完成
        for (Thread t : producers) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        for (Thread t : consumers) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Producer implements Runnable {
        private final Queue<Integer> queue;
        private final int capacity;
        private final Lock lock;
        private final Condition notFull;
        private final Condition notEmpty;
        private final int poisonPill;
        private final int consumerCount;

        Producer(Queue<Integer> queue, int capacity, Lock lock, Condition notFull, 
                 Condition notEmpty, int poisonPill, int consumerCount) {
            this.queue = queue;
            this.capacity = capacity;
            this.lock = lock;
            this.notFull = notFull;
            this.notEmpty = notEmpty;
            this.poisonPill = poisonPill;
            this.consumerCount = consumerCount;
        }

        @Override
        public void run() {
            try {
                // 生产数据
                for (int i = 0; i < 10; i++) {
                    lock.lock();
                    try {
                        // 等待队列有空间
                        while (queue.size() >= capacity) {
                            notFull.await();
                        }
                        
                        // 添加数据
                        queue.offer(i);
                        System.out.println(Thread.currentThread().getName() + " 生产: " + i);
                        
                        // 通知消费者
                        notEmpty.signalAll();
                    } finally {
                        lock.unlock();
                    }
                    Thread.sleep(100); // 模拟生产耗时
                }

                // 放入毒丸
                for (int i = 0; i < consumerCount; i++) {
                    lock.lock();
                    try {
                        while (queue.size() >= capacity) {
                            notFull.await();
                        }
                        queue.offer(poisonPill);
                        notEmpty.signalAll();
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Consumer implements Runnable {
        private final Queue<Integer> queue;
        private final Lock lock;
        private final Condition notFull;
        private final Condition notEmpty;
        private final int poisonPill;

        Consumer(Queue<Integer> queue, Lock lock, Condition notFull, 
                 Condition notEmpty, int poisonPill) {
            this.queue = queue;
            this.lock = lock;
            this.notFull = notFull;
            this.notEmpty = notEmpty;
            this.poisonPill = poisonPill;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Integer value = null;
                    
                    lock.lock();
                    try {
                        // 等待队列有数据
                        while (queue.isEmpty()) {
                            notEmpty.await();
                        }
                        
                        // 获取数据
                        value = queue.poll();
                        
                        // 通知生产者
                        notFull.signalAll();
                    } finally {
                        lock.unlock();
                    }
                    
                    if (value == poisonPill) {
                        System.out.println(Thread.currentThread().getName() + " 收到结束信号");
                        break;
                    }
                    
                    System.out.println(Thread.currentThread().getName() + " 消费: " + value);
                    Thread.sleep(200); // 模拟消费耗时
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

主要修改点:
从BlockingQueue转为NonBlockingQueue实现时的主要修改：
1. 手动实现阻塞功能：
- 使用Lock和Condition替代BlockingQueue的阻塞特性
- 手动实现队列满和队列空的等待条件
2. 显式线程同步：
- 使用lock/unlock明确控制临界区
- 使用条件变量notFull/notEmpty替代阻塞队列的内置等待机制
3. 线程通信：
- 添加元素后需手动signalAll通知消费者
- 取出元素后需手动signalAll通知生产者
4. 边界检查：
- 需显式检查队列大小和容量限制
- 使用while循环防止虚假唤醒