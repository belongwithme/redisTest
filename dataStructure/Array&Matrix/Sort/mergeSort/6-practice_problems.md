# 归并排序练习题与实战应用

## 🎯 学习目标
通过实际编程练习，深入理解归并排序的应用场景和解题技巧

## 🔥 基础练习题

### 1. 基础实现练习

#### 题目1：实现基础归并排序
```java
/**
 * 要求：
 * 1. 实现递归版本的归并排序
 * 2. 处理空数组和单元素数组的边界情况
 * 3. 确保算法的稳定性
 */
public class BasicMergeSort {
    public static void mergeSort(int[] arr) {
        // TODO: 在这里实现你的代码
    }
    
    // 测试用例
    public static void main(String[] args) {
        int[] test1 = {64, 34, 25, 12, 22, 11, 90};
        int[] test2 = {5, 2, 4, 6, 1, 3};
        int[] test3 = {1}; // 单元素
        int[] test4 = {}; // 空数组
        
        mergeSort(test1);
        // 期望输出: [11, 12, 22, 25, 34, 64, 90]
    }
}
```

#### 题目2：迭代版归并排序
```java
/**
 * 要求：实现自底向上的归并排序（非递归版本）
 * 挑战：不使用递归，用循环实现
 */
public class IterativeMergeSort {
    public static void mergeSortIterative(int[] arr) {
        // TODO: 实现迭代版本
    }
}
```

### 2. 变种实现练习

#### 题目3：归并排序优化版本
```java
/**
 * 要求：
 * 1. 小数组使用插入排序优化
 * 2. 检查数组是否已排序，提前终止
 * 3. 重用临时数组，减少内存分配
 */
public class OptimizedMergeSort {
    private static final int INSERTION_SORT_THRESHOLD = 10;
    
    public static void mergeSortOptimized(int[] arr) {
        // TODO: 实现优化版本
    }
    
    private static void insertionSort(int[] arr, int left, int right) {
        // TODO: 实现插入排序
    }
}
```

## 🎪 经典应用题

### 题目4：计算逆序对 ⭐⭐⭐
```java
/**
 * LeetCode 315: Count of Smaller Numbers After Self (变种)
 * 
 * 问题：给定整数数组，计算每个元素右边有多少个更小的数
 * 输入: [5, 2, 6, 1]
 * 输出: [2, 1, 1, 0]
 * 解释: 
 * - 5右边有2个更小的数(2, 1)
 * - 2右边有1个更小的数(1) 
 * - 6右边有1个更小的数(1)
 * - 1右边有0个更小的数
 */
public class CountSmaller {
    public List<Integer> countSmaller(int[] nums) {
        // TODO: 使用归并排序思想解决
        return new ArrayList<>();
    }
    
    // 提示：需要维护原始索引信息
    class IndexedValue {
        int value;
        int originalIndex;
        
        IndexedValue(int value, int originalIndex) {
            this.value = value;
            this.originalIndex = originalIndex;
        }
    }
}
```

### 题目5：区间合并 ⭐⭐
```java
/**
 * LeetCode 56: Merge Intervals
 * 
 * 问题：给定区间集合，合并重叠的区间
 * 输入: [[1,3],[2,6],[8,10],[15,18]]
 * 输出: [[1,6],[8,10],[15,18]]
 */
public class MergeIntervals {
    public int[][] merge(int[][] intervals) {
        // TODO: 先排序，再合并
        return new int[0][];
    }
}
```

### 题目6：链表排序 ⭐⭐⭐
```java
/**
 * LeetCode 148: Sort List
 * 
 * 要求：在 O(n log n) 时间和常数级空间复杂度下对链表排序
 */
public class SortList {
    static class ListNode {
        int val;
        ListNode next;
        ListNode() {}
        ListNode(int val) { this.val = val; }
        ListNode(int val, ListNode next) { this.val = val; this.next = next; }
    }
    
    public ListNode sortList(ListNode head) {
        // TODO: 实现链表的归并排序
        return null;
    }
    
    // 提示：需要实现链表的分割和合并
    private ListNode findMiddle(ListNode head) {
        // TODO: 快慢指针找中点
        return null;
    }
    
    private ListNode merge(ListNode l1, ListNode l2) {
        // TODO: 合并两个有序链表
        return null;
    }
}
```

## 🚀 进阶挑战题

### 题目7：归并K个有序数组 ⭐⭐⭐⭐
```java
/**
 * LeetCode 23: Merge k Sorted Lists (数组版本)
 * 
 * 问题：将K个已排序的数组合并为一个排序数组
 * 
 * 方法1：分治法 O(N log K)
 * 方法2：优先队列 O(N log K)  
 * 方法3：逐一合并 O(NK)
 */
public class MergeKSortedArrays {
    
    // 方法1：分治法
    public int[] mergeKArraysDivideConquer(int[][] arrays) {
        // TODO: 使用分治思想
        return new int[0];
    }
    
    // 方法2：优先队列
    public int[] mergeKArraysHeap(int[][] arrays) {
        // TODO: 使用最小堆
        return new int[0];
    }
    
    // 测试用例
    public static void main(String[] args) {
        int[][] arrays = {
            {1, 4, 5},
            {1, 3, 4}, 
            {2, 6}
        };
        // 期望输出: [1, 1, 2, 3, 4, 4, 5, 6]
    }
}
```

### 题目8：外部排序模拟 ⭐⭐⭐⭐⭐
```java
/**
 * 挑战：模拟处理超大文件的排序
 * 
 * 场景：有一个包含10亿个整数的文件，内存只能存储1000万个整数
 * 要求：设计外部排序算法
 */
public class ExternalSortChallenge {
    private static final int MEMORY_LIMIT = 10_000_000; // 内存限制
    
    /**
     * 外部排序主方法
     * @param inputFile 输入文件名
     * @param outputFile 输出文件名
     */
    public void externalSort(String inputFile, String outputFile) {
        // TODO: 实现外部排序
        // 步骤1：分割文件并排序
        // 步骤2：多路归并
    }
    
    /**
     * 第一阶段：分割并排序
     */
    private List<String> splitAndSort(String inputFile) {
        // TODO: 将大文件分割成小的有序文件
        return new ArrayList<>();
    }
    
    /**
     * 第二阶段：多路归并
     */
    private void multiWayMerge(List<String> tempFiles, String outputFile) {
        // TODO: 归并所有临时文件
    }
}
```

### 题目9：并行归并排序 ⭐⭐⭐⭐⭐
```java
/**
 * 挑战：实现多线程并行归并排序
 * 要求：充分利用多核CPU，提升排序性能
 */
import java.util.concurrent.*;

public class ParallelMergeSort {
    private static final int THRESHOLD = 1000; // 并行阈值
    private final ForkJoinPool pool;
    
    public ParallelMergeSort() {
        this.pool = new ForkJoinPool();
    }
    
    public void parallelMergeSort(int[] arr) {
        // TODO: 使用ForkJoinPool实现并行排序
    }
    
    class MergeSortTask extends RecursiveAction {
        private final int[] arr;
        private final int left;
        private final int right;
        
        MergeSortTask(int[] arr, int left, int right) {
            this.arr = arr;
            this.left = left;
            this.right = right;
        }
        
        @Override
        protected void compute() {
            // TODO: 实现并行分治逻辑
        }
    }
}
```

## 📊 性能测试题

### 题目10：算法性能对比
```java
/**
 * 任务：比较不同排序算法的性能
 * 要求：测试归并排序 vs 快速排序 vs 堆排序 vs Java内置排序
 */
public class SortingBenchmark {
    
    public static void main(String[] args) {
        int[] sizes = {1000, 10000, 100000, 1000000};
        
        for (int size : sizes) {
            System.out.println("\n=== 数组大小: " + size + " ===");
            
            // TODO: 测试不同数据分布下的性能
            testRandomData(size);
            testSortedData(size);
            testReverseSortedData(size);
            testDuplicateData(size);
        }
    }
    
    private static void testRandomData(int size) {
        // TODO: 生成随机数据并测试各种排序算法
    }
    
    private static void testSortedData(int size) {
        // TODO: 测试已排序数据
    }
    
    private static void testReverseSortedData(int size) {
        // TODO: 测试逆序数据
    }
    
    private static void testDuplicateData(int size) {
        // TODO: 测试包含大量重复元素的数据
    }
    
    private static long timeAlgorithm(int[] arr, String algorithmName, Runnable sortFunction) {
        long startTime = System.nanoTime();
        sortFunction.run();
        long endTime = System.nanoTime();
        
        System.out.printf("%s: %.2f ms\n", algorithmName, (endTime - startTime) / 1_000_000.0);
        return endTime - startTime;
    }
}
```

## 🎯 面试题型

### 题目11：稳定性证明
**问题**：证明归并排序是稳定的，并给出反例说明快速排序为什么不稳定。

```java
public class StabilityDemo {
    static class Element {
        int value;
        int originalIndex;
        
        Element(int value, int originalIndex) {
            this.value = value;
            this.originalIndex = originalIndex;
        }
        
        @Override
        public String toString() {
            return value + "(" + originalIndex + ")";
        }
    }
    
    public static void demonstrateStability() {
        // TODO: 创建包含相等元素的数组，证明归并排序的稳定性
    }
}
```

### 题目12：复杂度分析
**问题**：手写递推关系式，用主定理证明归并排序的时间复杂度为O(n log n)。

### 题目13：空间优化
**问题**：设计一个空间复杂度为O(1)的归并排序算法。

```java
public class InPlaceMergeSort {
    /**
     * 挑战：实现原地归并排序
     * 提示：可以使用旋转操作，但时间复杂度会增加到O(n²log n)
     */
    public static void inPlaceMergeSort(int[] arr) {
        // TODO: 实现原地版本
    }
}
```

## 🏆 项目实战

### 项目：日志文件分析系统
```java
/**
 * 实际项目：构建一个日志分析系统
 * 需求：
 * 1. 处理GB级别的日志文件
 * 2. 按时间戳排序日志条目
 * 3. 支持并行处理
 * 4. 内存使用受限
 */
public class LogAnalysisSystem {
    
    static class LogEntry {
        long timestamp;
        String content;
        
        LogEntry(long timestamp, String content) {
            this.timestamp = timestamp;
            this.content = content;
        }
    }
    
    /**
     * 主处理方法
     */
    public void processLargeLogFile(String inputFile, String outputFile) {
        // TODO: 实现完整的日志处理系统
        // 1. 解析日志文件
        // 2. 外部排序
        // 3. 输出排序结果
    }
    
    /**
     * 解析单行日志
     */
    private LogEntry parseLogLine(String line) {
        // TODO: 解析日志格式 "timestamp|log_content"
        return null;
    }
}
```

## 📚 学习建议

### 练习顺序
1. **基础题 (1-3)**：掌握基本实现
2. **应用题 (4-6)**：理解实际应用
3. **进阶题 (7-9)**：提升算法能力
4. **性能题 (10)**：培养性能意识
5. **面试题 (11-13)**：准备技术面试
6. **项目实战**：综合应用能力

### 调试技巧
```java
public class DebuggingTips {
    
    /**
     * 可视化归并过程
     */
    public static void visualizeMergeSort(int[] arr) {
        System.out.println("开始排序: " + Arrays.toString(arr));
        mergeSortWithVisualization(arr, 0, arr.length - 1, 0);
    }
    
    private static void mergeSortWithVisualization(int[] arr, int left, int right, int depth) {
        if (left >= right) return;
        
        String indent = "  ".repeat(depth);
        System.out.println(indent + "分解: [" + left + "," + right + "] " + 
                          Arrays.toString(Arrays.copyOfRange(arr, left, right + 1)));
        
        int mid = left + (right - left) / 2;
        mergeSortWithVisualization(arr, left, mid, depth + 1);
        mergeSortWithVisualization(arr, mid + 1, right, depth + 1);
        
        merge(arr, left, mid, right);
        System.out.println(indent + "合并: [" + left + "," + right + "] " + 
                          Arrays.toString(Arrays.copyOfRange(arr, left, right + 1)));
    }
    
    private static void merge(int[] arr, int left, int mid, int right) {
        // 标准归并实现...
    }
}
```

### 常见错误
1. **边界条件处理不当**：空数组、单元素数组
2. **中点计算溢出**：使用 `left + (right - left) / 2`
3. **合并逻辑错误**：忘记处理剩余元素
4. **稳定性破坏**：比较时使用 `<` 而不是 `<=`
5. **内存泄漏**：临时数组未及时释放

## 🎓 检验标准

完成练习后，您应该能够：

✅ **基础掌握**
- 独立实现递归和迭代版本的归并排序
- 正确处理各种边界情况
- 理解算法的稳定性

✅ **应用能力**  
- 使用归并思想解决逆序对、区间合并等问题
- 实现链表的归并排序
- 处理K个有序数组的合并

✅ **优化意识**
- 针对小数组进行优化
- 实现外部排序处理大文件
- 了解并行归并排序的思路

✅ **分析能力**
- 准确分析时间和空间复杂度
- 比较不同排序算法的优缺点
- 选择适合的排序算法解决实际问题

继续努力，让归并排序成为您算法工具箱中的得力助手！🚀