# 基础概念
## 请解释FutureTask的基本概念和作用
FutureTask的出现是Java 5引入并发包（java.util.concurrent）的重要部分，
它解决了早期Java多线程编程中缺乏标准化异步结果处理机制的问题。
在此之前，开发者需要自行实现复杂的线程间通信和同步机制来处理异步计算结果，而FutureTask提供了一种规范化、易用的解决方案。
FutureTask主要作用：
- 异步计算的结果获取：在Java并发编程中，当需要执行耗时操作但又不希望阻塞当前线程时，需要一种机制来异步获取结果。
- 任务与结果的解耦：将任务的执行与结果的获取分离，使程序可以在提交任务后继续执行其他操作，并在需要结果时再获取。
- 并发任务的生命周期管理：提供了对异步任务的完整生命周期管理，包括创建、执行、取消、完成状态等。
不使用FutureTask的异步计算实现:
```java
public class WithoutFutureTaskExample {
    // 用于存储计算结果
    private static volatile Integer result = null;
    // 用于标记任务是否完成
    private static volatile boolean isDone = false;
    // 用于同步的对象锁
    private static final Object lock = new Object();
    // 用于存储可能的异常
    private static volatile Exception exception = null;
    
    public static void main(String[] args) {
        // 创建并启动计算线程
        Thread calculationThread = new Thread(() -> {
            try {
                System.out.println("开始复杂计算，线程：" + Thread.currentThread().getName());
                // 模拟耗时计算
                Thread.sleep(3000);
                int calculationResult = 42; // 假设这是计算结果
                
                // 设置结果并通知等待线程
                synchronized (lock) {
                    result = calculationResult;
                    isDone = true;
                    lock.notifyAll();
                }
            } catch (Exception e) {
                // 存储异常，并通知等待线程
                synchronized (lock) {
                    exception = e;
                    isDone = true;
                    lock.notifyAll();
                }
            }
        }, "CalculationThread");
        
        calculationThread.start();
        
        // 主线程继续执行其他操作
        System.out.println("计算已开始，主线程继续执行其他任务");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("其他工作完成");
        
        // 获取计算结果
        try {
            synchronized (lock) {
                // 如果任务未完成，等待
                while (!isDone) {
                    lock.wait(5000); // 最多等待5秒
                    if (!isDone) {
                        // 超时处理
                        calculationThread.interrupt();
                        throw new RuntimeException("计算超时");
                    }
                }
                
                // 检查是否有异常
                if (exception != null) {
                    throw new RuntimeException("计算过程中发生异常", exception);
                }
                
                System.out.println("计算结果: " + result);
            }
        } catch (InterruptedException e) {
            System.out.println("等待结果时被中断");
        }
    }
}
```
使用FutureTask的简洁实现:
```java
import java.util.concurrent.*;

public class WithFutureTaskExample {
    public static void main(String[] args) {
        // 创建计算任务
        Callable<Integer> calculator = () -> {
            System.out.println("开始复杂计算，线程：" + Thread.currentThread().getName());
            // 模拟耗时计算
            Thread.sleep(3000);
            return 42; // 假设这是计算结果
        };
        
        // 创建FutureTask
        FutureTask<Integer> futureTask = new FutureTask<>(calculator);
        
        // 启动计算线程
        Thread calculationThread = new Thread(futureTask, "CalculationThread");
        calculationThread.start();
        
        // 主线程继续执行其他操作
        System.out.println("计算已开始，主线程继续执行其他任务");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("其他工作完成");
        
        // 获取计算结果
        try {
            // 等待计算结果，最多等待5秒
            Integer result = futureTask.get(5, TimeUnit.SECONDS);
            System.out.println("计算结果: " + result);
        } catch (InterruptedException e) {
            System.out.println("等待结果时被中断");
        } catch (ExecutionException e) {
            System.out.println("计算过程中发生异常: " + e.getCause());
        } catch (TimeoutException e) {
            System.out.println("计算超时，取消任务");
            futureTask.cancel(true);
        }
    }
}
```
## FutureTask实现了哪些接口？每个接口的作用是什么？
FutureTask实现了以下接口：
- RunnableFuture<V>接口：这是一个组合接口，继承了Runnable和Future<V>接口
- Runnable接口：使FutureTask可以被线程执行，提供run()方法
- Future<V>接口：提供了管理任务生命周期和获取计算结果的方法
通过实现这些接口，FutureTask既可以被提交到ExecutorService执行，又可以用来获取任务的执行结果。

1. RunnableFuture<V>接口
FutureTask实现的核心接口是RunnableFuture<V>，这是一个组合接口，它同时继承了两个重要接口：
```java
public interface RunnableFuture<V> extends Runnable, Future<V> {
    void run();
}
```
这个组合接口有着非常牛的设计：将"可执行的任务"和"可获取结果的凭证"合二为一。
2. Runnable接口
作用：使FutureTask可以被线程直接执行。
Runnable接口只有一个核心方法：
`public void run();`
这使得FutureTask具备了以下能力：
- 可以被Thread直接执行：new Thread(futureTask).start()
- 可以被线程池接受并调度：executorService.execute(futureTask)
- 可以作为一个独立的任务单元被执行系统处理
实际上，FutureTask的run()方法是它的核心执行逻辑所在，包含了任务执行、结果设置和异常处理的完整流程。
3. Future<V>接口
作用：提供了管理任务生命周期和获取计算结果的方法。
通过实现Future接口，FutureTask能够：
- 提供异步计算结果的访问机制
- 管理任务的完整生命周期
- 支持任务取消操作
- 提供结果等待的超时控制
## Future接口提供的主要方法有哪些？
Future接口提供了以下主要方法：
1. boolean cancel(boolean mayInterruptIfRunning)
- 尝试取消任务的执行
- 参数决定是否应该中断正在执行的任务
- 返回是否成功取消
2. boolean isCancelled()
- 判断任务是否被取消
3. boolean isDone()
- 判断任务是否已完成（包括正常完成、异常完成或被取消）
4. V get()
- 获取任务的执行结果
- 如果任务未完成，将阻塞等待
- 如果任务被取消或执行异常，会抛出相应异常
5. V get(long timeout, TimeUnit unit)
- 带超时的获取任务结果
- 在指定时间内任务未完成，则抛出TimeoutException


# 内部实现
## FutureTask的线程安全是由什么保证的？
FutureTask的线程安全主要由以下机制保证：
1. 状态变量的原子更新：
- 使用volatile int state存储任务状态
- 通过unsafe.compareAndSwapInt()（CAS操作）来原子性地更新状态
2. 内存可见性保证：
- 关键字段使用volatile修饰，确保修改对所有线程立即可见
- 特别是state、runner和waiters字段都是volatile的
3. 同步等待机制：
- 使用AQS（AbstractQueuedSynchronizer）类似的机制实现等待/通知模型
- 等待线程被组织成一个等待节点链表，由WaitNode类实现
4. 原子性操作：
- 对任务生命周期的关键操作（如设置结果、取消任务）使用CAS操作确保原子性
- 例如，finishCompletion()方法使用循环CAS确保等待线程被安全唤醒

个人版本:
utureTask通过状态管理和同步机制实现了线程安全，主要包括四个核心部分：
1. 状态变量的原子更新
FutureTask使用一个volatile修饰的整型状态变量state来跟踪任务的执行状态，并通过CAS（Compare-And-Swap）操作保证状态更新的原子性。
2. 内存可见性保证
FutureTask中的关键字段都使用volatile修饰，保证了修改对所有线程立即可见：
- volatile int state：任务状态
- volatile Thread runner：执行任务的线程引用
- volatile WaitNode waiters：等待结果的线程队列
这种设计确保了在多线程环境下，一个线程对这些字段的修改能够立即被其他线程感知到。
3. 等待/通知机制
FutureTask实现了类似于AQS（AbstractQueuedSynchronizer）的等待/通知模型：
- 等待结果的线程被封装成WaitNode对象，组织成一个单向链表
- 使用LockSupport.park()实现线程阻塞，而不是synchronized和wait()
- 任务完成时通过LockSupport.unpark()唤醒所有等待的线程
这种设计比传统的wait/notify机制更加灵活和高效
4. 关键操作的原子性保证
对于任务的关键生命周期操作，如设置结果、取消任务等，FutureTask都使用了CAS操作确保原子性：
- 设置结果时确保任务状态从NEW原子转换到COMPLETING
- 取消任务时确保状态原子转换为CANCELLED或INTERRUPTING
- finishCompletion()方法中使用循环CAS确保等待线程被安全唤醒
通过这些机制的综合应用，FutureTask在不使用显式锁的情况下，实现了高效的线程安全，即使在高并发环境下也能保持正确性和一致性。

## FutureTask内部状态转换机制是怎样的？
FutureTask内部使用一个state变量来追踪任务状态，状态转换如下：
1. 初始状态：NEW (0)
- 任务被创建但尚未开始执行
2. 中间状态：
- COMPLETING (1)：任务已执行完毕，正在设置结果
- INTERRUPTING (5)：正在中断执行任务的线程
3. 终止状态：
- NORMAL (2)：任务正常完成
- EXCEPTIONAL (3)：任务执行过程中抛出异常
- CANCELLED (4)：任务被取消但未中断线程
- INTERRUPTED (6)：任务被取消并且线程被中断
4. 状态转换路径：
- 正常完成：NEW → COMPLETING → NORMAL
- 异常完成：NEW → COMPLETING → EXCEPTIONAL
- 取消（不中断）：NEW → CANCELLED
- 取消（中断）：NEW → INTERRUPTING → INTERRUPTED

这些状态是单向转换的，一旦离开NEW状态，任务就不能重新执行；一旦进入终止状态，状态就不再改变。
## FutureTask如何处理任务的取消操作？
FutureTask通过以下机制处理任务的取消操作：
1. cancel方法实现：
- FutureTask提供了cancel(boolean mayInterruptIfRunning)方法来取消任务
- 该方法接收一个布尔参数，决定是否中断正在执行任务的线程
2. 状态转换：
- 当调用cancel方法时，会尝试将任务状态从NEW转换为终止状态
- 如果mayInterruptIfRunning为false，直接转为CANCELLED状态
- 如果mayInterruptIfRunning为true，先转为INTERRUPTING状态，然后中断执行线程，最后转为INTERRUPTED状态
3. 中断处理：
- 如果选择中断执行线程，FutureTask会获取当前执行任务的线程引用（runner字段）
- 通过Thread.interrupt()方法向执行线程发送中断信号
- 被中断的线程可以通过检查中断状态来响应取消请求
4. 资源释放：
- 无论取消方式如何，都会调用finishCompletion()方法
- 该方法会释放callable引用和唤醒所有等待结果的线程
5. 取消条件限制：
- 只有处于NEW状态的任务才能被取消
- 已经完成、已经取消或已经开始处理结果的任务无法被取消
- 如果任务已经开始但尚未完成，是否能取消取决于具体任务是否响应中断
6. 取消结果通知：
- 取消成功返回true，否则返回false
- 被取消的任务，调用get()会抛出CancellationException异常
## FutureTask是如何确保结果只被设置一次的？
FutureTask通过以下机制确保结果只被设置一次：
1. 状态管理：
- 使用volatile整型变量state追踪任务状态
- 结果只能在任务处于NEW状态时设置
- 一旦状态变为COMPLETING或其他终止状态，就无法再设置结果
2. CAS原子操作：
- 在设置结果时使用CAS（Compare-And-Swap）操作原子性地更新状态
- UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)确保只有一个线程能成功将状态从NEW变为COMPLETING
- 这种原子性保证了即使在多线程环境下，结果也只会被设置一次
3. 状态转换流程：
- 正常完成时：NEW → COMPLETING → NORMAL
- 异常完成时：NEW → COMPLETING → EXCEPTIONAL
- 中间状态COMPLETING是瞬时的，确保结果设置的原子性
4. 结果存储：
- 结果存储在outcome字段中
- 只有成功将状态从NEW变为COMPLETING的线程才能设置outcome
- 一旦outcome被设置，就不会再被修改
5. run方法保护：
- run方法会先检查状态和runner字段，确保任务不会被多次执行
- 只有第一个调用run方法的线程能设置runner字段并执行任务
6. set方法实现:
```java
   protected void set(V v) {
       if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
           outcome = v;
           UNSAFE.putOrderedInt(this, stateOffset, NORMAL); // final state
           finishCompletion();
       }
   }
```
个人版本:
FutureTask确保结果只被设置一次是通过精心设计的状态管理和原子操作实现的，主要有以下几个关键机制：
首先，通过状态管理控制结果设置的时机。FutureTask使用一个volatile修饰的整型变量state来跟踪任务状态，结果只能在任务处于初始的NEW状态时设置，一旦状态发生变化，就无法再设置结果。这种设计保证了结果的一次性设置。
其次，使用CAS原子操作保证状态转换的线程安全。在设置结果时，代码会先尝试使用CAS操作将状态从NEW原子性地转换为COMPLETING.
第三，设计了明确的状态转换流程。任务正常完成时，状态从NEW→COMPLETING→NORMAL；异常完成时，状态从NEW→COMPLETING→EXCEPTIONAL。COMPLETING是一个瞬态，表示正在设置结果，这个设计进一步确保了结果设置的原子性。
第四，在run方法中也有保护机制，确保任务不会被多次执行。run方法首先会检查状态和runner字段，只有第一个调用run方法的线程能设置runner字段并执行任务。
最后，结果被存储在outcome字段中，且只有成功将状态从NEW变为COMPLETING的线程才能设置outcome。一旦结果被设置，就不会再被修改。
通过这些机制，FutureTask确保了结果只会被成功设置一次，即使在高并发环境下也能保持一致性和正确性。

## FutureTask get()如果结果没返回会怎样，怎么打破阻塞
FutureTask的get()方法是一个阻塞调用，当我们调用get()方法而任务尚未完成时，调用线程会被阻塞，直到任务完成并返回结果。
这种设计符合异步计算的模式，允许我们在必要时等待异步任务的结果。
具体来说，当get()方法被调用时，如果任务已完成（正常完成、异常完成或已取消），get()方法会立即返回结果或抛出相应异常；如果任务尚未完成，调用线程会被加入等待队列并通过LockSupport.park()方法阻塞，直到任务完成时被唤醒。
有常用的有两种方式可以打破FutureTask.get()方法的阻塞状态:
1. 使用带超时参数的get(timeout, unit)方法
这是最常用也是最推荐的方式。当等待超过指定时间后，方法会抛出TimeoutException异常，我们可以捕获这个异常并进行相应处理，避免无限期等待。
2. 调用cancel(boolean mayInterruptIfRunning)方法取消任务
当我们不再需要任务结果或等待时间过长时，可以主动取消任务。如果参数设为true，还会尝试中断执行线程。

## 等待多个FutureTask返回结果提交给上游，怎么处理
使用CompletableFuture进行转换,提供了灵活的API,可以使用allOf等待所有任务完成,也可以使用anyOf等待任意一个任务完成.
```java
List<CompletableFuture<Result>> completableFutures = tasks.stream()
    .map(task -> CompletableFuture.supplyAsync(() -> {
        try {
            return task.get();
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    }))
    .collect(Collectors.toList());

// 等待所有任务完成后提交
CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]))
    .thenAccept(v -> {
        List<Result> results = completableFutures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
        uploadAllToUpstream(results);
    });
```
# 使用场景
## 描述FutureTask在线程池中的应用
在线程池中，FutureTask的应用非常广泛：
1. 任务提交与结果获取分离：
- 通过ExecutorService.submit()方法提交FutureTask
- 返回的Future对象可以在稍后用于获取结果
- 实现了任务执行与结果处理的时间和空间上的解耦
2. 资源管理优化：
- 线程池控制并发线程数量，避免过多线程导致的资源浪费
- FutureTask在线程池中被调度执行，不需要为每个任务创建新线程
- 适合处理大量短时或计算密集型任务
3. 批量任务处理：
- 可以将多个FutureTask提交到线程池执行
- 使用ExecutorCompletionService可以按完成顺序获取结果
- 适合实现"完成一个处理一个"的模式
```java
   ExecutorService executor = Executors.newFixedThreadPool(5);
   
   // 创建多个任务
   List<FutureTask<String>> tasks = new ArrayList<>();
   for (int i = 0; i < 10; i++) {
       final int id = i;
       FutureTask<String> task = new FutureTask<>(() -> {
           // 模拟处理时间
           Thread.sleep(1000);
           return "Task " + id + " result";
       });
       tasks.add(task);
       executor.submit(task);
   }
   
   // 获取所有结果
   for (FutureTask<String> task : tasks) {
       try {
           String result = task.get();
           System.out.println(result);
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
   
   executor.shutdown();
```
## 如何使用FutureTask实现异步计算？
使用FutureTask实现异步计算的步骤如下：
1. 定义计算任务：
- 创建Callable对象封装需要异步执行的逻辑
- Callable接口允许返回结果并抛出异常
2. 创建FutureTask：
- 使用上述Callable初始化FutureTask
- FutureTask包装了异步计算的逻辑和结果获取机制
3. 执行任务：
- 通过线程池提交任务：executorService.submit(futureTask)
- 或者直接启动新线程：new Thread(futureTask).start()
4. 异步处理：
- 主线程继续执行其他操作，不被阻塞
- 在需要结果的时候才调用futureTask.get()
5. 结果获取策略：
- 可以在结果需要时阻塞等待：result = futureTask.get()
- 可以先检查任务是否完成：if(futureTask.isDone()) { result = futureTask.get(); }
- 可以设置等待超时：result = futureTask.get(timeout, unit)

```java
   public class AsyncCalculationExample {
       public static void main(String[] args) {
           // 创建计算任务
           Callable<Integer> calculator = () -> {
               System.out.println("开始复杂计算，线程：" + Thread.currentThread().getName());
               try {
                   // 模拟耗时计算
                   Thread.sleep(3000);
                   return performComplexCalculation();
               } catch (InterruptedException e) {
                   Thread.currentThread().interrupt();
                   return -1;
               }
           };
           
           // 创建FutureTask
           FutureTask<Integer> futureTask = new FutureTask<>(calculator);
           
           // 启动计算线程
           Thread calculationThread = new Thread(futureTask, "CalculationThread");
           calculationThread.start();
           
           // 主线程继续执行其他操作
           System.out.println("计算已开始，主线程继续执行其他任务");
           doOtherWork();
           
           // 获取计算结果
           try {
               // 等待计算结果，最多等待5秒
               Integer result = futureTask.get(5, TimeUnit.SECONDS);
               System.out.println("计算结果: " + result);
           } catch (InterruptedException e) {
               System.out.println("等待结果时被中断");
           } catch (ExecutionException e) {
               System.out.println("计算过程中发生异常: " + e.getCause());
           } catch (TimeoutException e) {
               System.out.println("计算超时，取消任务");
               futureTask.cancel(true);
           }
       }
       
       private static int performComplexCalculation() {
           // 实际的复杂计算逻辑
           return 42;
       }
       
       private static void doOtherWork() {
           System.out.println("主线程执行其他工作...");
           try {
               Thread.sleep(1000);
           } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
           }
           System.out.println("其他工作完成");
       }
   }
```
## FutureTask与CompletableFuture的区别是什么？
FutureTask和CompletableFuture是Java并发编程中两种不同的异步计算工具，它们有以下主要区别：
1. 设计理念：
- FutureTask：基于阻塞模型，需要显式调用get()方法获取结果
- CompletableFuture：基于回调机制，支持函数式编程，可以注册完成时的回调函数
2. 功能丰富性：
- FutureTask：功能相对简单，主要提供异步计算和结果获取
- CompletableFuture：提供丰富的组合、转换和异常处理操作（如thenApply、thenCombine、exceptionally等）
3. 任务组合能力：
- FutureTask：不支持任务组合，每个任务相互独立
- CompletableFuture：支持多种任务组合方式（串行、并行、条件组合）
4. 异常处理：
- FutureTask：异常在get()方法中以ExecutionException形式抛出
- CompletableFuture：提供专门的异常处理方法，可以设置恢复值或执行补偿操作
5. 取消操作：
- FutureTask：提供cancel()方法取消任务
- CompletableFuture：取消操作不直接支持，需要通过关联的任务或线程来实现
6. 异步性：
- FutureTask：需要显式提供执行线程
- CompletableFuture：可以自动使用ForkJoinPool.commonPool()或指定Executor
7. API设计：
- FutureTask：使用传统的阻塞式API
- CompletableFuture：使用现代的流式API，支持方法链
8. 使用场景对比：
- FutureTask适合：
- 简单的异步计算
- 与线程池结合使用
- 需要精确控制线程执行的场景
9. CompletableFuture适合：
- 复杂的异步工作流
- 依赖多个异步操作的结果
- 需要非阻塞式处理的场景
- 响应式编程模型
个人版本:
首先，从设计理念上看，FutureTask采用的是阻塞式模型，调用者需要通过get()方法主动获取结果，这会阻塞当前线程；而CompletableFuture基于回调机制，支持函数式编程范式，可以注册完成时的回调函数，实现非阻塞的异步编程。
其次，功能丰富度方面，FutureTask相对简单，主要提供基本的异步计算和结果获取；CompletableFuture则提供了丰富的组合和转换操作，如thenApply、thenCombine、exceptionally等，使异步流程控制更加灵活。
在任务组合能力上，FutureTask不支持任务之间的组合，每个任务是相互独立的；而CompletableFuture支持多种任务组合方式，包括串行执行、并行执行和条件组合，能够构建复杂的异步工作流。
异常处理方面，FutureTask中的异常会在调用get()时以ExecutionException形式抛出；CompletableFuture提供了专门的异常处理方法，可以设置恢复值或执行补偿操作，更加灵活。
从API设计角度看，FutureTask使用传统的阻塞式API；CompletableFuture采用现代的流式API，支持方法链式调用，代码更加简洁优雅。
因此，在实际应用中，如果是简单的异步计算或需要精确控制线程执行，我会选择FutureTask；而对于复杂的异步工作流、依赖多个异步操作或需要非阻塞处理的场景，CompletableFuture是更好的选择。"
# 源码分析
## 请解释FutureTask中的run()方法实现
主要实现要点：
1. 首先通过CAS设置runner为当前线程，确保任务只被执行一次
2. 调用callable.call()执行实际任务，并捕获所有可能的异常
3. 任务成功完成时，调用set方法设置结果
4. 任务抛出异常时，调用setException方法存储异常
5. finally块中释放runner引用，并处理可能的取消中断状态
## get()方法的阻塞等待机制是如何实现的？
1. 首先检查任务状态，如果已完成直接返回结果
2. 如果未完成，则创建一个WaitNode加入等待队列
3. 使用LockSupport.park()阻塞当前线程
4. 当任务完成时，会调用finishCompletion方法遍历等待队列，通过LockSupport.unpark()唤醒所有等待线程
5. 线程被唤醒后再次检查状态，根据状态返回结果或抛出异常
这种实现避免了使用synchronized，提供了更高效的等待-通知机制。
## FutureTask如何处理异常情况？
FutureTask对异常处理很完善：
1. 在run方法中捕获所有Callable执行时可能抛出的异常
2. 通过setException方法将异常存储在outcome字段
3. 状态转为EXCEPTIONAL，表示任务异常完成
4. 调用get方法时，会通过report方法将异常包装为ExecutionException抛出
5. 如果任务被取消，get方法会抛出CancellationException
6. 对于超时等待，会抛出TimeoutException
7. 如果等待过程中线程被中断，会抛出InterruptedException
这种设计保证了异常能够被正确捕获并传递给调用者。
