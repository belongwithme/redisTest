# CompletableFuture 编程练习题

## 基础篇

### 练习题 1：创建 CompletableFuture

**目标：** 学习如何创建 `CompletableFuture` 实例。

**要求：**
1. 创建一个简单的 `CompletableFuture`，它在完成时返回字符串 "任务完成"。
2. 获取并打印 `CompletableFuture` 的结果。

**提示：** 使用 `CompletableFuture.completedFuture()` 方法。

### 练习题 2：异步执行任务 (runAsync)

**目标：** 学习使用 `runAsync` 方法在后台线程池中执行一个没有返回值的任务。

**要求：**
1. 创建一个 `Runnable` 任务，该任务打印出当前线程的名称。
2. 使用 `CompletableFuture.runAsync()` 异步执行该任务。
3. 确保主线程等待异步任务执行完毕（可以使用 `join()` 或 `get()`，注意处理异常）。

### 练习题 3：异步执行任务并获取结果 (supplyAsync)

**目标：** 学习使用 `supplyAsync` 方法在后台线程池中执行一个有返回值的任务。

**要求：**
1. 创建一个 `Supplier` 任务，该任务模拟一个耗时操作（例如 `Thread.sleep(1000)`），然后返回一个整数 `123`。
2. 使用 `CompletableFuture.supplyAsync()` 异步执行该任务。
3. 获取并打印任务的结果。

### 练习题 4：使用自定义线程池

**目标：** 学习如何为 `CompletableFuture` 指定自定义的线程池。

**要求：**
1. 创建一个固定大小的线程池（例如 `Executors.newFixedThreadPool(2)`）。
2. 使用 `supplyAsync(Supplier<U> supplier, Executor executor)` 方法，将练习题 3 中的任务提交到你创建的自定义线程池中执行。
3. 获取并打印任务的结果，并观察任务是在哪个线程中执行的。
4. 关闭自定义线程池。

## 进阶篇

### 练习题 5：结果转换 (thenApply / thenApplyAsync)

**目标：** 学习如何在 `CompletableFuture` 完成后对其结果进行转换。

**要求：**
1. 基于练习题 3，创建一个 `CompletableFuture` 异步获取整数 `123`。
2. 使用 `thenApply` 方法将获取到的整数结果乘以 `10`。
3. 获取并打印最终转换后的结果。
4. 尝试使用 `thenApplyAsync` 并观察与 `thenApply` 的区别（主要在于执行转换操作的线程）。

### 练习题 6：结果消费 (thenAccept / thenAcceptAsync)

**目标：** 学习如何在 `CompletableFuture` 完成后消费其结果，而不返回新的结果。

**要求：**
1. 基于练习题 3，创建一个 `CompletableFuture` 异步获取整数 `123`。
2. 使用 `thenAccept` 方法，在 `CompletableFuture` 完成后打印其结果，例如 "获取到的结果是: 123"。
3. 确保主线程等待消费操作完成。
4. 尝试使用 `thenAcceptAsync`。

### 练习题 7：任务完成后的操作 (thenRun / thenRunAsync)

**目标：** 学习在 `CompletableFuture` 完成后执行一个不关心结果的 `Runnable` 任务。

**要求：**
1. 创建一个 `CompletableFuture` 异步执行一个简单的任务（可以使用 `runAsync` 或 `supplyAsync`）。
2. 使用 `thenRun` 方法，在 `CompletableFuture` 完成后打印一条消息，例如 "前置任务已完成！"。
3. 确保主线程等待所有操作完成。
4. 尝试使用 `thenRunAsync`。

### 练习题 8：组合两个 CompletableFuture (thenCompose / thenComposeAsync)

**目标：** 学习如何将两个依赖的 `CompletableFuture` 串联起来，第一个的结果是第二个的输入。

**要求：**
1. 创建第一个 `CompletableFuture` (`cf1`)，使用 `supplyAsync` 异步获取用户 ID（例如，返回 `"user-123"`）。
2. 创建第二个 `CompletableFuture` 的逻辑：接收用户 ID 作为输入，然后异步获取该用户的详细信息（例如，模拟查询并返回 `"用户信息: user-123 的详细信息"`）。
3. 使用 `thenCompose` 将 `cf1` 和第二个 `CompletableFuture` 组合起来。
4. 获取并打印最终的用户详细信息。
5. 尝试使用 `thenComposeAsync`。

### 练习题 9：合并两个独立的 CompletableFuture (thenCombine / thenCombineAsync)

**目标：** 学习如何合并两个**独立**执行的 `CompletableFuture` 的结果。

**要求：**
1. 创建第一个 `CompletableFuture` (`cf1`)，异步获取用户的积分（例如，返回 `1000`）。
2. 创建第二个 `CompletableFuture` (`cf2`)，异步获取用户的等级（例如，返回 `"Gold"`）。
3. 使用 `thenCombine` 合并 `cf1` 和 `cf2` 的结果，生成一个新的字符串，例如 `"用户积分: 1000, 用户等级: Gold"`。
4. 获取并打印合并后的结果。
5. 尝试使用 `thenCombineAsync`。

### 练习题 10：等待多个 CompletableFuture 完成 (allOf)

**目标：** 学习如何等待多个 `CompletableFuture` 全部完成。

**要求：**
1. 创建三个独立的 `CompletableFuture`，每个都模拟一个耗时不同的异步任务（例如，使用 `supplyAsync` 和 `Thread.sleep`）。
2. 使用 `CompletableFuture.allOf()` 等待这三个任务全部完成。
3. 在所有任务完成后，打印一条确认消息，例如 "所有任务已完成！"。
4. **注意：** `allOf` 返回的 `CompletableFuture<Void>` 本身不包含所有子任务的结果。如果需要获取结果，需要在 `allOf` 完成后单独获取每个子任务的结果。

### 练习题 11：等待多个 CompletableFuture 中任意一个完成 (anyOf)

**目标：** 学习如何等待多个 `CompletableFuture` 中的任意一个完成。

**要求：**
1. 创建三个独立的 `CompletableFuture`，每个都模拟一个耗时不同的异步任务，并返回不同的字符串结果。
2. 使用 `CompletableFuture.anyOf()` 获取第一个完成的任务的结果。
3. 打印出第一个完成的任务的结果。
4. **注意：** `anyOf` 返回的 `CompletableFuture<Object>` 的结果是第一个完成的子任务的结果。

## 异常处理篇

### 练习题 12：处理异常 (exceptionally)

**目标：** 学习如何处理 `CompletableFuture` 执行过程中可能出现的异常。

**要求：**
1. 创建一个 `CompletableFuture`，使用 `supplyAsync` 执行一个会抛出异常的任务（例如，除以零 `10 / 0`）。
2. 使用 `exceptionally` 方法捕获异常，并在发生异常时返回一个默认值（例如，`-1`）。
3. 获取并打印 `CompletableFuture` 的结果（应该是默认值）。

### 练习题 13：处理结果或异常 (handle / handleAsync)

**目标：** 学习使用 `handle` 方法，无论 `CompletableFuture` 是正常完成还是异常完成，都能进行处理。

**要求：**
1. 创建两个 `CompletableFuture`：
    * `cfSuccess`: 正常完成，返回字符串 `"Success"`。
    * `cfFailure`: 异步执行时抛出异常。
2. 对 `cfSuccess` 使用 `handle` 方法。在 `handle` 的 `BiFunction` 中，检查结果和异常，如果正常完成，返回结果本身；如果异常，返回 "Error"。打印处理后的结果。
3. 对 `cfFailure` 使用 `handle` 方法。在 `handle` 的 `BiFunction` 中，执行相同的逻辑。打印处理后的结果。
4. 尝试使用 `handleAsync`。

## 超时处理篇

### 练习题 14：超时处理 (orTimeout / completeOnTimeout) - Java 9+

**目标：** 学习如何为 `CompletableFuture` 设置超时。

**要求：** (需要 Java 9 或更高版本)
1. 创建一个 `CompletableFuture`，使用 `supplyAsync` 模拟一个耗时较长的任务（例如 `Thread.sleep(3000)`）。
2. 使用 `orTimeout(long timeout, TimeUnit unit)` 方法设置一个较短的超时时间（例如 1 秒）。
3. 尝试获取结果，并使用 `try-catch` 块捕获可能抛出的 `TimeoutException`。
4. 创建另一个耗时较长的 `CompletableFuture`。
5. 使用 `completeOnTimeout(T value, long timeout, TimeUnit unit)` 方法设置一个超时时间，并在超时发生时使用默认值完成 `CompletableFuture`。
6. 获取并打印结果，观察在超时情况下是否得到了默认值。

---

祝你练习愉快，对 `CompletableFuture` 的掌握更上一层楼！
