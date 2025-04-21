import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class CompletableFutureAnswers {

    public static void main(String[] args) throws Exception {
        System.out.println("--- 练习题 1：创建 CompletableFuture ---");
        exercise1();
        System.out.println("\n--- 练习题 2：异步执行任务 (runAsync) ---");
        exercise2();
        System.out.println("\n--- 练习题 3：异步执行任务并获取结果 (supplyAsync) ---");
        exercise3();
        System.out.println("\n--- 练习题 4：使用自定义线程池 ---");
        exercise4();
        System.out.println("\n--- 练习题 5：结果转换 (thenApply / thenApplyAsync) ---");
        exercise5();
        System.out.println("\n--- 练习题 6：结果消费 (thenAccept / thenAcceptAsync) ---");
        exercise6();
        System.out.println("\n--- 练习题 7：任务完成后的操作 (thenRun / thenRunAsync) ---");
        exercise7();
        System.out.println("\n--- 练习题 8：组合两个 CompletableFuture (thenCompose / thenComposeAsync) ---");
        exercise8();
        System.out.println("\n--- 练习题 9：合并两个独立的 CompletableFuture (thenCombine / thenCombineAsync) ---");
        exercise9();
        System.out.println("\n--- 练习题 10：等待多个 CompletableFuture 完成 (allOf) ---");
        exercise10();
        System.out.println("\n--- 练习题 11：等待多个 CompletableFuture 中任意一个完成 (anyOf) ---");
        exercise11();
        System.out.println("\n--- 练习题 12：处理异常 (exceptionally) ---");
        exercise12();
        System.out.println("\n--- 练习题 13：处理结果或异常 (handle / handleAsync) ---");
        exercise13();
        // 练习题 14 需要 Java 9+
        // System.out.println("\n--- 练习题 14：超时处理 (orTimeout / completeOnTimeout) ---");
        // exercise14();
    }

    // 练习题 1：创建 CompletableFuture
    public static void exercise1() throws Exception {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("任务完成");
        System.out.println("结果: " + cf.get()); // 使用 get() 获取结果，注意处理异常
    }

    // 练习题 2：异步执行任务 (runAsync)
    public static void exercise2() throws Exception {
        Runnable task = () -> {
            System.out.println("任务执行中，线程: " + Thread.currentThread().getName());
            try {
                Thread.sleep(500); // 模拟耗时
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        CompletableFuture<Void> cf = CompletableFuture.runAsync(task);
        System.out.println("主线程继续执行...");
        cf.join(); // 等待异步任务完成
        System.out.println("异步任务已完成。");
    }

    // 练习题 3：异步执行任务并获取结果 (supplyAsync)
    public static void exercise3() throws Exception {
        Supplier<Integer> task = () -> {
            System.out.println("supplyAsync 任务执行中，线程: " + Thread.currentThread().getName());
            try {
                Thread.sleep(1000); // 模拟耗时
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return 123;
        };

        CompletableFuture<Integer> cf = CompletableFuture.supplyAsync(task);
        System.out.println("主线程继续执行...");
        Integer result = cf.get(); // 阻塞等待并获取结果
        System.out.println("获取到的结果: " + result);
    }

    // 练习题 4：使用自定义线程池
    public static void exercise4() throws Exception {
        ExecutorService customThreadPool = Executors.newFixedThreadPool(2);

        Supplier<Integer> task = () -> {
            System.out.println("自定义线程池任务执行中，线程: " + Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return 123;
        };

        CompletableFuture<Integer> cf = CompletableFuture.supplyAsync(task, customThreadPool);
        System.out.println("主线程继续执行...");
        Integer result = cf.get();
        System.out.println("获取到的结果: " + result);

        customThreadPool.shutdown(); // 关闭线程池
    }

    // 练习题 5：结果转换 (thenApply / thenApplyAsync)
    public static void exercise5() throws Exception {
        CompletableFuture<Integer> initialCf = CompletableFuture.supplyAsync(() -> {
            System.out.println("原始任务线程: " + Thread.currentThread().getName());
            return 123;
        });

        // thenApply: 使用执行原始任务的线程或者提交后续任务的线程
        CompletableFuture<Integer> transformedCfApply = initialCf.thenApply(result -> {
            System.out.println("thenApply 转换线程: " + Thread.currentThread().getName());
            return result * 10;
        });
        System.out.println("thenApply 转换结果: " + transformedCfApply.get());

        // thenApplyAsync: 使用默认的 ForkJoinPool 或指定的 Executor
        CompletableFuture<Integer> initialCfAsync = CompletableFuture.supplyAsync(() -> {
            System.out.println("原始任务 (Async) 线程: " + Thread.currentThread().getName());
             try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return 123;
        });
        CompletableFuture<Integer> transformedCfApplyAsync = initialCfAsync.thenApplyAsync(result -> {
            System.out.println("thenApplyAsync 转换线程: " + Thread.currentThread().getName());
            return result * 10;
        });
        System.out.println("thenApplyAsync 转换结果: " + transformedCfApplyAsync.get());
    }

    // 练习题 6：结果消费 (thenAccept / thenAcceptAsync)
    public static void exercise6() throws Exception {
         CompletableFuture<Integer> cf = CompletableFuture.supplyAsync(() -> {
             System.out.println("原始任务 (Accept) 线程: " + Thread.currentThread().getName());
             try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
             return 123;
         });

        // thenAccept
        CompletableFuture<Void> acceptCf = cf.thenAccept(result -> {
            System.out.println("thenAccept 消费线程: " + Thread.currentThread().getName());
            System.out.println("获取到的结果是: " + result);
        });
        acceptCf.join(); // 等待消费操作完成

        // thenAcceptAsync
        CompletableFuture<Integer> cfAsync = CompletableFuture.supplyAsync(() -> {
            System.out.println("原始任务 (AcceptAsync) 线程: " + Thread.currentThread().getName());
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return 456;
        });
        CompletableFuture<Void> acceptAsyncCf = cfAsync.thenAcceptAsync(result -> {
             System.out.println("thenAcceptAsync 消费线程: " + Thread.currentThread().getName());
             System.out.println("获取到的结果是: " + result);
         });
        acceptAsyncCf.join();
    }

    // 练习题 7：任务完成后的操作 (thenRun / thenRunAsync)
    public static void exercise7() throws Exception {
        CompletableFuture<Void> initialCf = CompletableFuture.runAsync(() -> {
            System.out.println("初始 runAsync 任务线程: " + Thread.currentThread().getName());
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        // thenRun
        CompletableFuture<Void> runCf = initialCf.thenRun(() -> {
            System.out.println("thenRun 操作线程: " + Thread.currentThread().getName());
            System.out.println("前置任务已完成！");
        });
        runCf.join();

         // thenRunAsync
        CompletableFuture<String> initialCfSupply = CompletableFuture.supplyAsync(() -> {
            System.out.println("初始 supplyAsync (RunAsync) 任务线程: " + Thread.currentThread().getName());
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return "Done";
        });
         CompletableFuture<Void> runAsyncCf = initialCfSupply.thenRunAsync(() -> {
             System.out.println("thenRunAsync 操作线程: " + Thread.currentThread().getName());
             System.out.println("前置任务已完成！(Async)");
         });
        runAsyncCf.join();
    }

    // 练习题 8：组合两个 CompletableFuture (thenCompose / thenComposeAsync)
    public static void exercise8() throws Exception {
        // 模拟异步获取用户ID
        CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("获取用户ID线程: " + Thread.currentThread().getName());
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return "user-123";
        });

        // 模拟根据用户ID异步获取用户详细信息
        BiFunction<String, CompletableFuture<String>, CompletableFuture<String>> getUserDetails =
                (userId, future) -> CompletableFuture.supplyAsync(() -> {
                    System.out.println("获取用户信息线程 (for " + userId + "): " + Thread.currentThread().getName());
                    try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    return "用户信息: " + userId + " 的详细信息";
                });

        // thenCompose
        CompletableFuture<String> finalCfCompose = cf1.thenCompose(userId ->
            CompletableFuture.supplyAsync(() -> {
                System.out.println("thenCompose - 获取用户信息线程: " + Thread.currentThread().getName());
                 try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return "用户信息 (Compose): " + userId + " 的详细信息";
            })
        );
        System.out.println("thenCompose 最终结果: " + finalCfCompose.get());


        // thenComposeAsync
         CompletableFuture<String> cf1Async = CompletableFuture.supplyAsync(() -> {
            System.out.println("获取用户ID线程 (Async): " + Thread.currentThread().getName());
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return "user-456";
        });

        CompletableFuture<String> finalCfComposeAsync = cf1Async.thenComposeAsync(userId ->
            CompletableFuture.supplyAsync(() -> {
                System.out.println("thenComposeAsync - 获取用户信息线程: " + Thread.currentThread().getName());
                 try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return "用户信息 (ComposeAsync): " + userId + " 的详细信息";
            })
        );
        System.out.println("thenComposeAsync 最终结果: " + finalCfComposeAsync.get());

    }

    // 练习题 9：合并两个独立的 CompletableFuture (thenCombine / thenCombineAsync)
    public static void exercise9() throws Exception {
        CompletableFuture<Integer> cf1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("获取积分线程: " + Thread.currentThread().getName());
            try { Thread.sleep(800); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return 1000;
        });

        CompletableFuture<String> cf2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("获取等级线程: " + Thread.currentThread().getName());
            try { Thread.sleep(1200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return "Gold";
        });

        // thenCombine
        CompletableFuture<String> combinedCf = cf1.thenCombine(cf2, (points, level) -> {
            System.out.println("thenCombine 合并线程: " + Thread.currentThread().getName());
            return "用户积分: " + points + ", 用户等级: " + level;
        });
        System.out.println("thenCombine 合并结果: " + combinedCf.get());

        // thenCombineAsync
         CompletableFuture<Integer> cf1Async = CompletableFuture.supplyAsync(() -> {
            System.out.println("获取积分线程 (Async): " + Thread.currentThread().getName());
            try { Thread.sleep(800); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return 2000;
        });

        CompletableFuture<String> cf2Async = CompletableFuture.supplyAsync(() -> {
            System.out.println("获取等级线程 (Async): " + Thread.currentThread().getName());
            try { Thread.sleep(1200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return "Platinum";
        });

         CompletableFuture<String> combinedCfAsync = cf1Async.thenCombineAsync(cf2Async, (points, level) -> {
            System.out.println("thenCombineAsync 合并线程: " + Thread.currentThread().getName());
            return "用户积分 (Async): " + points + ", 用户等级 (Async): " + level;
        });
        System.out.println("thenCombineAsync 合并结果: " + combinedCfAsync.get());
    }

    // 练习题 10：等待多个 CompletableFuture 完成 (allOf)
    public static void exercise10() throws Exception {
        CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            System.out.println("任务1完成");
            return "Result1";
        });
        CompletableFuture<String> cf2 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            System.out.println("任务2完成");
            return "Result2";
        });
        CompletableFuture<String> cf3 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            System.out.println("任务3完成");
            return "Result3";
        });

        CompletableFuture<Void> allOfCf = CompletableFuture.allOf(cf1, cf2, cf3);

        allOfCf.join(); // 等待所有任务完成

        System.out.println("所有任务已完成！");

        // 获取结果 (可选)
        System.out.println("任务1结果: " + cf1.get());
        System.out.println("任务2结果: " + cf2.get());
        System.out.println("任务3结果: " + cf3.get());
    }

    // 练习题 11：等待多个 CompletableFuture 中任意一个完成 (anyOf)
    public static void exercise11() throws Exception {
         CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            System.out.println("任务A完成");
            return "Result A";
        });
        CompletableFuture<String> cf2 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
             System.out.println("任务B完成");
            return "Result B";
        });
        CompletableFuture<String> cf3 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
             System.out.println("任务C完成");
            return "Result C";
        });

        CompletableFuture<Object> anyOfCf = CompletableFuture.anyOf(cf1, cf2, cf3);

        Object firstResult = anyOfCf.get(); // 等待并获取第一个完成的任务的结果
        System.out.println("第一个完成的任务结果: " + firstResult);

        // 可能需要等待其他任务完成以避免主线程退出过快
        Thread.sleep(1600);
    }

    // 练习题 12：处理异常 (exceptionally)
    public static void exercise12() throws Exception {
        CompletableFuture<Integer> cf = CompletableFuture.supplyAsync(() -> {
            System.out.println("异常任务线程: " + Thread.currentThread().getName());
            // 模拟异常
            int result = 10 / 0;
            return result;
        }).exceptionally(ex -> {
            System.err.println("捕获到异常: " + ex.getMessage());
            return -1; // 返回默认值
        });

        Integer result = cf.get();
        System.out.println("最终获取到的结果 (exceptionally): " + result);
    }

    // 练习题 13：处理结果或异常 (handle / handleAsync)
    public static void exercise13() throws Exception {
        // 成功案例
        CompletableFuture<String> cfSuccess = CompletableFuture.completedFuture("Success");
        CompletableFuture<String> handledSuccess = cfSuccess.handle((result, ex) -> {
             System.out.println("handle (Success) 线程: " + Thread.currentThread().getName());
            if (ex != null) {
                System.err.println("Success CF 遇到异常: " + ex.getMessage());
                return "Error";
            }
            System.out.println("Success CF 正常完成，结果: " + result);
            return result;
        });
        System.out.println("Handle Success 处理后结果: " + handledSuccess.get());

        // 失败案例
        CompletableFuture<String> cfFailure = CompletableFuture.supplyAsync(() -> {
             System.out.println("handle (Failure) 任务线程: " + Thread.currentThread().getName());
            if (true) { // 模拟异常条件
                 throw new RuntimeException("任务执行失败!");
            }
            return "Won't reach here";
        });

        CompletableFuture<String> handledFailure = cfFailure.handle((result, ex) -> {
            System.out.println("handle (Failure) 线程: " + Thread.currentThread().getName());
            if (ex != null) {
                System.err.println("Failure CF 遇到异常: " + ex.getCause().getMessage()); // 获取原始异常
                return "Error";
            }
            System.out.println("Failure CF 正常完成，结果: " + result); // 这部分不会执行
            return result;
        });
        System.out.println("Handle Failure 处理后结果: " + handledFailure.get());


         // handleAsync
         CompletableFuture<String> cfSuccessAsync = CompletableFuture.completedFuture("Success Async");
         CompletableFuture<String> handledSuccessAsync = cfSuccessAsync.handleAsync((result, ex) -> {
             System.out.println("handleAsync (Success) 线程: " + Thread.currentThread().getName());
             if (ex != null) {
                 return "Error Async";
             }
             return result;
         });
         System.out.println("Handle Success Async 处理后结果: " + handledSuccessAsync.get());

         CompletableFuture<String> cfFailureAsync = CompletableFuture.supplyAsync(() -> {
             System.out.println("handleAsync (Failure) 任务线程: " + Thread.currentThread().getName());
             throw new RuntimeException("任务执行失败 Async!");
         });
         CompletableFuture<String> handledFailureAsync = cfFailureAsync.handleAsync((result, ex) -> {
             System.out.println("handleAsync (Failure) 线程: " + Thread.currentThread().getName());
             if (ex != null) {
                  System.err.println("Failure Async CF 遇到异常: " + ex.getCause().getMessage());
                 return "Error Async";
             }
             return result;
         });
        System.out.println("Handle Failure Async 处理后结果: " + handledFailureAsync.get());
    }

    // 练习题 14：超时处理 (orTimeout / completeOnTimeout) - 需要 Java 9+
    /*
    // 取消注释以在 Java 9+ 环境中运行
    public static void exercise14() throws Exception {
        // orTimeout 示例
        CompletableFuture<String> longRunningTask1 = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("orTimeout 任务开始，将睡眠3秒...");
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "任务1正常完成";
        });

        try {
            String result = longRunningTask1.orTimeout(1, TimeUnit.SECONDS).get();
            System.out.println("orTimeout 结果: " + result);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof TimeoutException) {
                System.err.println("orTimeout 任务超时!");
            } else {
                System.err.println("orTimeout 任务执行出错: " + e.getMessage());
            }
        }

        System.out.println("---");

        // completeOnTimeout 示例
        CompletableFuture<String> longRunningTask2 = CompletableFuture.supplyAsync(() -> {
             try {
                System.out.println("completeOnTimeout 任务开始，将睡眠3秒...");
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "任务2正常完成";
        });

        String resultOrDefault = longRunningTask2
                .completeOnTimeout("超时默认值", 1, TimeUnit.SECONDS)
                .get();

        System.out.println("completeOnTimeout 结果: " + resultOrDefault);

        // 让主线程等待足够长的时间以观察后台线程的完成情况（如果任务没有超时）
        Thread.sleep(3500);
    }
    */
} 