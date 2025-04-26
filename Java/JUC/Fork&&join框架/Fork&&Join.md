@[TOC](Fork&&Join)
# 基础概念问题
## 请您简要介绍一下什么是Fork/Join框架，它的设计目标是什么？
Fork/Join框架是Java 7引入的一个并行执行任务的框架，是ExecutorService接口的一个实现。其设计目标是为了更高效地利用多处理器环境，通过将大任务分解成小任务，然后并行执行这些小任务，最后合并结果，从而提高程序的执行效率。
设计目标主要包括：
- 提高多核环境下的运算性 能
- 简化并行编程模型
- 自动化线程管理和负载均衡
- 提供工作窃取机制以最大化CPU利用率
个人理解版:
Fork/Join框架其实是对分治算法的并行实现。
我理解它本质上是解决了Java中并行编程的两个痛点：
一是如何高效拆分任务.
二是如何平衡各个处理单元的工作负载。
在我看来，它的设计初衷是将复杂的并行编程模型简化，让开发者能够用接近顺序编程的思维来实现并行计算。
通过提供框架化的工具，开发者只需要关注"如何分解问题"和"如何合并结果"，而无需关心线程的创建、调度和同步等底层细节。
这种设计理念非常符合现代CPU多核架构的发展趋势。随着处理器核心数量的增加，如何充分利用这些计算资源成为了一个关键问题，而Fork/Join框架正是为此而生。
## Fork/Join框架和传统的线程池ExecutorService有什么区别？
主要区别如下：
- 任务分解机制：Fork/Join专注于"分而治之"的问题解决策略，而传统ExecutorService处理的是独立的任务
- 工作窃取算法：Fork/Join采用工作窃取算法实现负载均衡，而传统线程池没有此机制
- 阻塞处理：Fork/Join使用轻量级的阻塞处理，工作线程会主动寻找新任务
- 任务类型：Fork/Join处理的任务通常是可递归分解的，而ExecutorService处理的一般是独立的Runnable或Callable任务
- 执行效率：在适合的场景下，Fork/Join对于大规模计算密集型任务效率更高
个人理解版:
传统的ExecutorService更像是一个任务调度器，它并不关心任务之间的关系和依赖性。而Fork/Join框架则更像是一个计算框架，它关注的是问题的解决策略。
我认为最核心的区别在于计算模型的不同：
- ExecutorService采用的是"生产者-消费者"模型，主线程提交任务，工作线程执行任务
- Fork/Join采用的是"分治-归并"模型，每个工作线程既可以分解任务(生产者)，也可以执行任务(消费者)
这种模型上的区别导致了Fork/Join在处理递归分解型问题时有显著优势，特别是在数据并行处理方面，如大数组的排序、矩阵乘法等计算密集型任务。
另外，ForkJoinPool中的线程更"积极"和"自主"，它们会主动寻找工作，而不是被动地等待分配，这也是提高CPU利用率的关键。

## 能否解释一下Fork/Join框架中的"工作窃取"(Work-Stealing)算法是如何工作的？
工作窃取(Work-Stealing)算法的工作原理如下：
- 每个工作线程维护自己的双端队列(deque)来存储待执行的任务
- 工作线程优先处理自己队列中的任务，从队列的头部获取任务执行
- 当一个工作线程空闲时(即完成了自己队列中的所有任务)，它会随机选择另一个工作线程，从其队列的尾部"窃取"任务来执行

这种机制确保了忙碌的线程不会被空闲的线程干扰，同时保证了所有线程都能保持忙碌状态
通过工作窃取，Fork/Join框架实现了更好的负载均衡，提高了整体的执行效率

个人版本:
它解决了并行计算中的一个核心难题：如何在不引入中央调度器的情况下实现动态负载均衡。
在实际开发中，我观察到当任务拆分不均匀时，传统线程池容易出现一些线程过载而另一些线程闲置的情况。工作窃取巧妙地解决了这个问题：
采用双端队列的数据结构非常关键，它让线程可以从不同端操作任务，减少了并发冲突
"窃取"机制本身就是一种自适应的负载均衡策略，系统会自动将工作从忙碌的线程转移到空闲的线程
这种去中心化的设计减少了线程间的同步开销，提高了系统的可扩展性
从实践经验来看，工作窃取在处理不规则计算问题时特别有效，比如递归下降的树形计算，每个分支的计算量可能差异很大，工作窃取可以自动平衡这种差异。

## Fork/Join框架如何分割任务
Fork/Join 框架分割任务的核心思想是 “分而治之” (Divide and Conquer)。它采用递归的方式将一个大的复杂任务拆分成若干个更小的、易于管理和计算的子任务，直到这些子任务足够小，可以直接计算为止。
具体的分割过程通常是这样实现的：
1. 继承基类: 开发者需要创建一个任务类，继承自 RecursiveTask<V>（如果任务有返回值）或 RecursiveAction（如果任务没有返回值）。
2. 实现 compute() 方法: 任务分割和计算的核心逻辑都在这个 compute() 方法中实现。
3. 判断阈值: 在 compute() 方法内部，首先会判断当前任务的规模是否已经小到可以直接计算的程度。这通常通过设置一个 “阈值” (Threshold) 来决定。这个阈值是根据具体问题和性能测试来确定的。
4. 直接计算 (小于阈值): 如果当前任务的规模小于或等于设定的阈值，说明任务已经足够小，不再需要进一步分割。此时，就在 compute() 方法中直接执行该任务的计算逻辑，并返回结果（如果是 RecursiveTask）。
5. 分割任务 (大于阈值): 如果当前任务的规模大于阈值，就需要进行分割：
- 将当前任务拆分成两个或多个规模更小的子任务。
- 对于每个（或除了第一个之外的）子任务，调用其 fork() 方法。fork() 方法会将这个子任务 异步地提交 给 Fork/Join 线程池中的工作线程去执行（或者更准确地说，是放入工作队列，等待被某个工作线程“窃取”并执行）。
- 通常，当前任务会 递归地调用 其中一个子任务的 compute() 方法（或者直接 fork 所有的子任务）。
6. 等待并合并结果: 在 fork() 子任务之后，当前任务需要等待这些子任务执行完成并获取它们的结果。这通过调用子任务的 join() 方法来实现。join() 方法会 阻塞当前线程，直到对应的子任务执行完毕并返回结果。
7. 合并结果: 当所有子任务的 join() 方法都返回后，将这些子任务的结果 合并 起来，形成当前大任务的最终结果，并在 compute() 方法中返回。

总结来说： Fork/Join 框架通过在 compute 方法中递归地检查任务大小，与预设阈值比较，决定是直接计算还是通过 fork 方法将任务分解给其他线程处理，最后通过 join 方法同步等待并合并子任务结果，从而实现了大任务的有效分割和并行处理。阈值的合理设定对于平衡任务分割开销和并行效率至关重要。
# 核心组件问题
## Fork/Join框架中的核心类有哪些？它们各自的作用是什么？
Fork/Join框架的核心类主要包括：
- ForkJoinPool：执行ForkJoinTask任务的特殊线程池，实现了工作窃取算法
- ForkJoinTask：在ForkJoinPool中执行的任务的基类，提供了fork()和join()等核心方法
- RecursiveTask：ForkJoinTask的子类，用于有返回结果的任务
- RecursiveAction：ForkJoinTask的子类，用于没有返回结果的任务
- CountedCompleter：ForkJoinTask的另一个子类，在任务完成时可以触发回调
这些类共同构成了Fork/Join框架的基础结构，支持分治算法的并行实现。
个人版本:
我认为Fork/Join框架的核心组件可以类比为一个完整的"并行工厂"：
- ForkJoinPool：相当于这个工厂的车间，负责统筹安排工人和任务。它不仅仅是一个普通的线程池，而是一个支持分治算法执行的专用"智能车间"，能够自动调配资源，实现工作窃取。
- ForkJoinTask：是工厂中的"工作模板"，定义了工作的基本流程。它抽象了fork（分叉）和join（合并）这两个核心操作，使得分治算法的并行实现变得简单直观。在实际使用中，我们很少直接使用- ForkJoinTask，而是使用它的两个主要子类。
- RecursiveTask和RecursiveAction：这是两种具体的"工作模式"。一个带有"成果物"（返回值），一个只完成工作不产出实体成果。它们要求开发者实现compute()方法，这是分治算法的核心所在，决定了如何分割任务和合并结果。
- CountedCompleter：这是一种特殊的"工作模式"，适合那些需要在完成后触发其他动作的场景，比如依赖关系复杂的任务网络。

## ForkJoinPool与ThreadPoolExecutor相比有哪些特殊的特性？
ForkJoinPool与ThreadPoolExecutor相比具有以下特殊特性：
- 工作窃取机制：ForkJoinPool实现了工作窃取算法，空闲线程可以从其他线程队列中窃取任务
- 双端队列：每个工作线程维护自己的双端队列，适合工作窃取算法
- 动态调整并行度：支持自动调整活跃线程数量，根据系统负载情况
- join等待优化：当一个线程等待另一个任务的结果时，可以执行其他等待中的任务
- 异常处理机制：提供了完整的异常捕获和处理机制
- 线程池模式：提供了同步和异步两种模式，适应不同场景需求
个人版本:
ForkJoinPool采用了一种"自助餐式"的工作分配模式，而不是传统ThreadPoolExecutor的"点餐式"模式。在这种模式下，线程不再被动地等待分配任务，而是主动寻找任务执行。这就像一个高效的团队，每个成员都能够在完成自己的工作后，主动去帮助其他负载较重的成员。
特别值得一提的是它的join等待优化机制。在传统模型中，一个线程在等待另一个任务完成时通常会阻塞或空转，这是对计算资源的浪费。而ForkJoinPool的线程在等待时会去执行其他待处理的任务，这大大提高了线程的利用效率，减少了因同步等待导致的性能损失。
另外，ForkJoinPool支持两种执行模式：同步模式（LIFO）和异步模式（FIFO）。同步模式适合那些子任务之间有依赖关系的场景，而异步模式则适合子任务相互独立的场景。这种灵活性使得ForkJoinPool能够适应不同类型的并行计算需求。
## RecursiveTask与RecursiveAction的区别是什么？什么情况下应该使用哪一个？
RecursiveTask与RecursiveAction的主要区别：
- 返回值：RecursiveTask有返回值（泛型类型），而RecursiveAction没有返回值（void）
- 使用场景：RecursiveTask适用于需要返回计算结果的场景，如数值计算、查找等；RecursiveAction适用于不需要返回结果的场景，如数据处理、并行更新等
- 实现方法：两者都需要实现compute()方法，但RecursiveTask的compute()方法需要返回计算结果
- 合并结果：RecursiveTask需要合并子任务的结果，而RecursiveAction只需要确保子任务完成即可
个人理解版:
选择RecursiveTask还是RecursiveAction不仅仅取决于是否需要返回值，还应考虑任务的性质和系统设计：
数据流转方式：RecursiveTask适合"自下而上"的数据流转，即子任务的结果需要向上传递和合并；RecursiveAction适合"自上而下"的操作，即主要关注任务的执行过程而非结果收集。
内存占用：RecursiveTask因为需要存储和传递结果，通常会消耗更多内存，特别是当返回结果较大时；而RecursiveAction则更轻量级。
错误处理策略：RecursiveTask更适合需要严格错误控制的场景，因为每个子任务的结果都会被检查；RecursiveAction则适合"尽力而为"型的操作，即即使部分子任务失败也不影响整体功能。
# 实现细节问题
## 在使用Fork/Join框架时，如何确定任务的拆分粒度？拆分粒度过大或过小会带来什么问题？
在使用Fork/Join框架时，任务拆分粒度的确定通常基于以下几点：
- 任务量大小：当子任务足够小，直接执行比继续拆分更有效率时应停止拆分
- 处理器核心数：拆分的子任务数应与可用处理器核心数相适应
- 任务处理时间：子任务的执行时间应足够长，以抵消任务拆分和合并的开销
拆分粒度过大或过小会带来以下问题：
- 粒度过大：并行度不足，无法充分利用多核处理器，性能提升有限
- 粒度过小：任务管理开销过大，频繁的任务切换和调度会导致性能下降
- 负载不均衡：粒度不当可能导致工作线程之间的负载不均衡，影响整体效率
个人版本:
这需要在"足够细以利用并行"和"足够粗以减少开销"之间找到平衡点。
任务拆分粒度决策应考虑三个维度：
- 计算密度：CPU密集型任务适合更细粒度的拆分，而IO密集型任务则需要相对粗粒度的拆分。这是因为CPU密集型任务能够更充分地利用多核优势，而IO密集型任务则更多受限于IO操作。
- 数据局部性：任务拆分应当尊重数据的局部性原则。例如，处理一个大数组时，连续的数组元素往往被加载到同一个CPU缓存行中，如果按照连续区间拆分任务，可以提高缓存命中率，减少内存访问延迟。
- 任务平衡性：不均衡的任务划分会导致一些工作线程过载而其他线程空闲。虽然工作窃取算法可以缓解这个问题，但良好的初始任务划分仍然是提高效率的关键。
在拆分粒度上走极端都会带来问题：
粒度过大时，本质上就变成了顺序执行，无法发挥并行计算的优势。
粒度过小时，任务调度和上下文切换的成本会超过并行计算带来的收益，甚至可能导致系统资源耗尽（如内存溢出）。
我通常采用自适应的拆分策略：开始时进行粗粒度拆分，然后根据系统负载和任务执行情况动态调整拆分粒度。这比固定粒度的拆分更灵活，也更能适应不同的计算环境。
## 请解释一下ForkJoinPool的内部工作队列结构，以及它是如何支持工作窃取算法的？
ForkJoinPool的内部工作队列结构：
- 每个工作线程都维护一个工作队列（WorkQueue），这是一个双端队列（Deque）
- 工作线程自己产生的任务放入队列头部（LIFO方式）
- 工作线程优先从自己的队列头部获取任务执行
- 当一个线程的队列为空时，会从其他线程的队列尾部窃取任务（FIFO方式）
这种设计支持工作窃取算法：
- 双端队列允许工作线程和窃取线程从不同端操作，减少了竞争
- 当线程自己的队列为空时，会随机选择另一个线程的队列尝试窃取任务
- 窃取操作总是从队列尾部获取任务，这样可以减少与队列所有者的竞争
- 窃取失败时会尝试其他线程的队列，直至找到可窃取的任务

个人版本:
ForkJoinPool的内部工作队列设计体现了并发编程中的一个重要思想：减少共享，减少竞争。
每个工作线程拥有自己的工作队列，这是一种"线程本地存储"的思想，允许线程在大部分时间内独立工作，不需要与其他线程竞争共享资源。这种设计大大减少了线程间的竞争和同步开销。
更精妙的是双端队列的使用。双端队列允许从两端操作，工作线程从队列前端（LIFO方式）操作，而窃取线程从队列后端（FIFO方式）操作：
- 工作线程使用LIFO方式有利于保持数据局部性和提高缓存命中率，因为最近入队的任务通常与当前任务相关
- 窃取线程使用FIFO方式有利于减少竞争，因为窃取的是队列中最早的任务，避免了与工作线程在同一端操作
这种设计在实践中非常高效，因为：
- 大部分时间内，线程只操作自己的队列，不需要加锁
- 窃取操作虽然需要加锁，但发生频率相对较低，且通常只发生在某些线程忙碌而其他线程空闲时
- 工作线程和窃取线程从不同端操作，即使发生窃取，也最大程度地减少了冲突
## ForkJoinTask中的fork()和invoke()方法有什么区别？它们在使用时有什么注意事项？
ForkJoinTask中的fork()和invoke()方法的区别：
- fork()：异步执行任务，不等待任务完成，立即返回
- invoke()：同步执行任务，等待任务完成并返回结果
- 执行方式：fork()将任务提交到工作线程的队列中，而invoke()则是直接执行任务
使用注意事项：
- fork()后应该紧跟join()以获取结果，否则任务可能永远不会被执行
- 对于子任务，推荐使用invoke()而不是fork()+join()，因为invoke()可能直接在当前线程执行
- 在拆分任务时，最后一个子任务应使用compute()直接计算，而不是fork()，以减少不必要的任务创建
- 当一个任务依赖另一个任务的结果时，应该先调用一个任务的fork()，然后再调用另一个任务的compute()，最后调用第一个任务的join()

个人版本:
fork()和invoke()方法的区别反映了并行编程中同步与异步的权衡。在项目实践中，我发现这两个方法的正确使用对性能影响很大：
- fork() 是一种"发射后不管"的异步操作。它仅仅将任务放入队列，然后立即返回。这种方式的优点是不阻塞当前线程，允许它继续处理其他任务；缺点是增加了任务调度的开销。
- invoke() 是一种"直接执行"的同步操作。它会立即执行任务，并等待结果返回。优点是减少了任务调度开销；缺点是可能导致当前线程阻塞。
我在使用这两个方法时遵循的原则是：
- 最小化任务创建：对于子任务，尤其是最后一个子任务，优先使用compute()直接计算，而不是fork()然后join()。这样可以节省一次任务创建和队列操作的开销。
- 平衡工作负载：在拆分任务时，通常对N-1个子任务调用fork()，然后直接计算第N个子任务。这样可以确保当前线程继续工作，而不是等待所有子任务完成。
- 优化依赖关系：当两个任务有依赖关系时，先对独立任务调用fork()，然后执行依赖任务，最后join()第一个任务。这样可以最大化并行性。

# 性能与应用场景问题
## 在什么场景下使用Fork/Join框架比较合适？能否举一个实际的应用例子？
适合使用Fork/Join框架的场景及实例
Fork/Join框架适合以下场景：
- 可分解的计算密集型任务：需要进行大量计算且可以分解为独立子任务的场景
- 数据并行处理：对大型数据集进行相同操作的场景
- 递归算法并行化：可以并行执行的递归算法，如快速排序、归并排序等
- 树结构的并行遍历和处理：如XML解析、文件系统遍历等
实际应用例子：
- 大型数组排序：将数组分成多个部分并行排序，然后合并结果
- 矩阵乘法：将矩阵分块，实现并行的矩阵计算
- 图像处理：将图像分成多个区域进行并行处理
- 文件系统扫描：并行处理文件系统的目录树
## 如何评估Fork/Join框架的性能？有哪些因素会影响Fork/Join框架的性能？
性能评估方法：
- 吞吐量测试：测量单位时间内完成的任务数量
- 响应时间测试：测量任务从提交到完成的时间
- 可扩展性测试：随着核心数增加，性能提升的比例
- 资源利用率：CPU、内存等资源的使用情况
影响性能的因素：
- 任务拆分粒度：过大或过小都会影响性能
- 线程数量：通常设置为可用处理器核心数
- 任务平衡性：任务工作量分布是否均匀
- 任务执行时间：太短的任务并行开销可能超过收益
- 内存访问模式：缓存局部性对性能影响很大
- 任务间数据依赖：依赖关系会限制并行度
- 系统负载：系统其他进程的活动也会影响性能
## JDK中有哪些内置功能使用了Fork/Join框架？它们是如何实现的？
JDK中使用Fork/Join框架的功能包括：
- Arrays.parallelSort()：Java 8引入的并行数组排序方法
- 并行流(Parallel Streams)：Collection接口的parallelStream()方法
- CompletableFuture：异步编程API，部分操作基于ForkJoinPool实现
java.util.concurrent.ForkJoinPool.commonPool()：用于执行任务的公共ForkJoinPool实例
实现方式：
- parallelSort() 使用双轴快速排序(DualPivotQuicksort)算法，通过ForkJoinPool实现并行排序
- 并行流 使用ForkJoinPool.commonPool()作为其执行环境，将流操作分解为可并行执行的子任务
- CompletableFuture 的某些方法使用ForkJoinPool.commonPool()执行异步任务
# 高级问题
## Fork/Join框架在处理任务时可能会遇到哪些异常情况？如何正确处理这些异常？
Fork/Join框架在处理任务时可能遇到以下异常情况：
- 运行时异常：任务执行过程中抛出的未捕获异常
- 取消异常：任务被取消时产生的CancellationException
- 超时异常：任务执行超时时产生的TimeoutException
- 中断异常：线程被中断时产生的InterruptedException
- 拒绝执行异常：任务提交被拒绝时产生的RejectedExecutionException
正确处理这些异常的建议：
- 实现compute()方法时应捕获并处理可能的异常
- 使用ForkJoinTask的isCompletedAbnormally()和getException()方法检查任务是否异常完成及异常原因
- 可以使用ForkJoinTask的completeExceptionally(Throwable ex)方法主动设置任务的异常状态
- 对于需要处理子任务异常的场景，可以使用invoke()而非fork()+join()组合，以便及时捕获异常
- 在任务中使用try-catch-finally结构确保资源正确释放
个人版本:
Fork/Join框架的异常处理是我在实际开发中遇到的最棘手的问题之一。与传统的同步代码不同，并行执行的任务可能在不同线程中抛出异常，这使得错误追踪和恢复变得复杂。
异常传播机制：Fork/Join框架中的异常不会直接抛出，而是被封装在任务结果中。当调用join()方法时，如果任务异常完成，异常才会被重新抛出。这种机制的好处是防止异常丢失，但也容易导致开发者忽视异常处理。
异常恢复策略：在分治算法中，一个子任务的失败不一定意味着整个计算必须失败。我通常采用"部分容错"策略，即：
- 对关键路径上的异常，立即传播并终止整个计算
- 对非关键路径的异常，记录日志并尝试使用默认或备选结果继续
- 实现重试机制，特别是对于因资源竞争导致的暂时性失败
资源管理：异常处理中最容易被忽视的是资源泄漏问题。在Fork/Join任务中，我强调：
- 使用try-with-resources确保资源自动关闭
- 在finally块中显式释放昂贵资源
- 对于长期运行的应用，实现周期性健康检查，识别和清理潜在的资源泄漏
## 请解释一下ForkJoinPool中的asyncMode参数的作用，以及它对工作窃取策略的影响？
ForkJoinPool的asyncMode参数作用：
- 同步模式(LIFO, asyncMode=false)：工作线程以后进先出(LIFO)的顺序执行任务，优先执行最近提交的任务
- 异步模式(FIFO, asyncMode=true)：工作线程以先进先出(FIFO)的顺序执行任务，按提交顺序执行任务
对工作窃取策略的影响：
- 在同步模式下，工作线程从自己队列头部获取任务(LIFO)，其他线程从尾部窃取任务(FIFO)
- 在异步模式下，所有线程都使用FIFO顺序，工作线程和窃取线程都从队列尾部获取任务
- 同步模式适合递归分治算法，因为它优先处理最深层的任务，减少任务数量膨胀
- 异步模式适合独立任务或广度优先算法，它确保任务按提交顺序大致执行，更公平
## Fork/Join框架与Java 8引入的并行流(Parallel Streams)有什么关系？如何选择使用哪一种？
Fork/Join框架与并行流的关系：
- 并行流(Parallel Streams)在内部使用Fork/Join框架实现并行计算
- 并行流使用ForkJoinPool.commonPool()作为其默认执行环境
- Stream API的并行操作(map, filter, reduce等)被转换为Fork/Join任务执行
选择使用哪一种的考虑因素：
API复杂度：并行流提供更简洁的函数式API，而Fork/Join需要更多自定义代码
- 控制粒度：Fork/Join框架提供更细粒度的控制，包括任务分割策略和线程池配置
- 性能优化：对于需要精细优化的场景，直接使用Fork/Join更适合
- 集成性：如果已在使用Stream API，则并行流提供更无缝的集成
- 复用性：对于需要重复使用的复杂并行算法，封装为Fork/Join任务更有利于代码复用
一般建议：简单数据转换操作使用并行流，复杂或需要优化的算法使用Fork/Join框架。

## Fork/Join框架原理，如何自己实现一样的效果
Fork/Join框架是一种基于"分治"思想的并行计算框架，其核心原理包括：
1. 分治思想：将大任务递归拆分为小任务，直到任务足够小可以直接计算。
2. 工作窃取算法：每个工作线程维护自己的双端队列，当线程空闲时会从其他繁忙线程的队列尾部"窃取"任务，实现负载均衡。
3. 执行模型：工作线程采用LIFO方式处理自己的任务，窃取其他线程任务时采用FIFO方式，减少竞争。
4. Join优化：在等待子任务结果时，工作线程不会闲置，而是去执行其他任务。

设计核心组件:
```java

// 任务基类
public abstract class MyForkJoinTask<T> {
    protected abstract T compute();
    public abstract MyForkJoinTask<T> fork();
    public abstract T join();
}

// 工作线程
public class MyWorker extends Thread {
    private final Deque<MyForkJoinTask<?>> localTasks = new LinkedList<>();
    private final MyForkJoinPool pool;
    
    // 工作线程执行逻辑
    @Override
    public void run() {
        while (!isInterrupted()) {
            MyForkJoinTask<?> task = getTask();
            if (task != null) {
                task.compute();
            }
        }
    }
    
    // 获取任务：优先从自己队列头部获取，没有则尝试窃取
    private MyForkJoinTask<?> getTask() {
        MyForkJoinTask<?> task = localTasks.pollFirst(); // LIFO
        if (task == null) {
            task = stealTask(); // 尝试窃取
        }
        return task;
    }
    
    // 窃取其他线程队列尾部任务
    private MyForkJoinTask<?> stealTask() {
        for (MyWorker worker : pool.getWorkers()) {
            if (worker != this) {
                MyForkJoinTask<?> task = worker.localTasks.pollLast(); // FIFO
                if (task != null) return task;
            }
        }
        return null;
    }
}

// 线程池
public class MyForkJoinPool {
    private final List<MyWorker> workers;
    
    public MyForkJoinPool(int parallelism) {
        workers = new ArrayList<>(parallelism);
        for (int i = 0; i < parallelism; i++) {
            MyWorker worker = new MyWorker(this);
            workers.add(worker);
            worker.start();
        }
    }
    
    public <T> T invoke(MyForkJoinTask<T> task) {
        // 提交任务并等待结果
        return task.compute();
    }
}
```

```java
public class MySumTask extends MyForkJoinTask<Integer> {
    private final int[] array;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 1000; // 任务拆分阈值
    
    public MySumTask(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }
    
    @Override
    protected Integer compute() {
        if (end - start <= THRESHOLD) {
            // 任务足够小，直接计算
            int sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }
            return sum;
        } else {
            // 任务太大，拆分为两个子任务
            int mid = start + (end - start) / 2;
            MySumTask left = new MySumTask(array, start, mid);
            MySumTask right = new MySumTask(array, mid, end);
            
            left.fork(); // 异步执行左侧任务
            int rightResult = right.compute(); // 直接执行右侧任务
            int leftResult = left.join(); // 等待左侧结果
            
            return leftResult + rightResult;
        }
    }
}
```