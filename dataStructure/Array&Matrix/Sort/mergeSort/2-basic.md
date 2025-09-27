# Java归并排序完全指南

## 📚 学习目标
掌握归并排序的核心思想、实现方式、复杂度分析以及实际应用

## 🔍 什么是归并排序？

归并排序是一种基于**分治思想**的稳定排序算法。它的核心理念是：
- **分解**：将数组分成两半
- **解决**：递归地对两半进行排序
- **合并**：将两个已排序的数组合并成一个有序数组

### 核心思想图解
```
原数组: [38, 27, 43, 3, 9, 82, 10]

分解阶段:
       [38, 27, 43, 3, 9, 82, 10]
            /              \
    [38, 27, 43]        [3, 9, 82, 10]
      /      \             /        \
  [38]    [27, 43]    [3, 9]    [82, 10]
           /   \       /   \      /    \
        [27]  [43]  [3]  [9]  [82]  [10]

合并阶段:
        [27]  [43]  [3]  [9]  [82]  [10]
           \   /      \   /      \    /
          [27, 43]   [3, 9]    [10, 82]
              \        /          /
            [3, 9, 27, 43]   [10, 82]
                    \          /
              [3, 9, 10, 27, 43, 82]
```

## 💻 Java实现

### 1. 基础版本
```java
public class MergeSort {
    
    /**
     * 归并排序主方法
     * @param arr 待排序数组
     */
    public static void mergeSort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        mergeSortHelper(arr, 0, arr.length - 1);
    }
    
    /**
     * 递归分解数组
     * @param arr 数组
     * @param left 左边界
     * @param right 右边界
     */
    private static void mergeSortHelper(int[] arr, int left, int right) {
        // 基准情况：只有一个元素或空数组
        if (left >= right) {
            return;
        }
        
        // 计算中点，避免溢出
        int mid = left + (right - left) / 2;
        
        // 递归排序左半部分
        mergeSortHelper(arr, left, mid);
        
        // 递归排序右半部分
        mergeSortHelper(arr, mid + 1, right);
        
        // 合并两个有序数组
        merge(arr, left, mid, right);
    }
    
    /**
     * 合并两个有序数组
     * @param arr 原数组
     * @param left 左边界
     * @param mid 中点
     * @param right 右边界
     */
    private static void merge(int[] arr, int left, int mid, int right) {
        // 创建临时数组
        int[] temp = new int[right - left + 1];
        
        int i = left;      // 左半部分指针
        int j = mid + 1;   // 右半部分指针
        int k = 0;         // 临时数组指针
        
        // 比较并合并
        while (i <= mid && j <= right) {
            if (arr[i] <= arr[j]) {
                temp[k++] = arr[i++];
            } else {
                temp[k++] = arr[j++];
            }
        }
        
        // 复制剩余元素
        while (i <= mid) {
            temp[k++] = arr[i++];
        }
        
        while (j <= right) {
            temp[k++] = arr[j++];
        }
        
        // 将临时数组复制回原数组
        for (int index = 0; index < temp.length; index++) {
            arr[left + index] = temp[index];
        }
    }
}
```

### 2. 测试代码
```java
public class MergeSortTest {
    public static void main(String[] args) {
        // 测试用例
        int[] arr1 = {38, 27, 43, 3, 9, 82, 10};
        int[] arr2 = {5, 2, 4, 6, 1, 3};
        int[] arr3 = {1}; // 边界情况
        int[] arr4 = {}; // 空数组
        
        System.out.println("测试1:");
        printArray("排序前", arr1);
        MergeSort.mergeSort(arr1);
        printArray("排序后", arr1);
        
        System.out.println("\n测试2:");
        printArray("排序前", arr2);
        MergeSort.mergeSort(arr2);
        printArray("排序后", arr2);
    }
    
    private static void printArray(String prefix, int[] arr) {
        System.out.print(prefix + ": ");
        for (int num : arr) {
            System.out.print(num + " ");
        }
        System.out.println();
    }
}
```

## 📊 复杂度分析

### 时间复杂度

- **最佳情况**: O(n log n)
- **平均情况**: O(n log n)
- **最坏情况**: O(n log n)

**分析过程**：

1. **分解阶段**：每次将数组分成两半，总共需要 log n 层
2. **合并阶段**：每层需要 O(n) 时间来合并所有子数组
3. **总时间复杂度**：O(n) × O(log n) = O(n log n)

### 空间复杂度

- **O(n)**：需要额外的临时数组来存储合并结果

### 递归调用栈

- **O(log n)**：递归深度为 log n

## ⚡ 归并排序的特点

### 优点

1. **稳定性**：相等元素的相对位置不会改变
2. **时间复杂度稳定**：无论什么情况都是 O(n log n)
3. **可预测性**：性能不依赖于数据的初始状态
4. **适合外部排序**：可以处理大量数据

### 缺点

1. **空间复杂度高**：需要 O(n) 的额外空间
2. **不是原地排序**：需要额外的存储空间

## 🔧 优化策略

### 1. 小数组优化

对于小数组，插入排序可能更快：
```java
private static void mergeSortOptimized(int[] arr, int left, int right) {
    // 对小数组使用插入排序
    if (right - left <= 10) {
        insertionSort(arr, left, right);
        return;
    }
    
    int mid = left + (right - left) / 2;
    mergeSortOptimized(arr, left, mid);
    mergeSortOptimized(arr, mid + 1, right);
    merge(arr, left, mid, right);
}
```

### 2. 判断是否已排序

```java
private static void mergeSortOptimized2(int[] arr, int left, int right) {
    if (left >= right) return;
    
    int mid = left + (right - left) / 2;
    mergeSortOptimized2(arr, left, mid);
    mergeSortOptimized2(arr, mid + 1, right);
    
    // 如果已经有序，跳过合并
    if (arr[mid] <= arr[mid + 1]) {
        return;
    }
    
    merge(arr, left, mid, right);
}
```

## 🎯 实际应用场景

1. **外部排序**：处理无法完全加载到内存的大文件
2. **稳定排序需求**：需要保持相等元素相对位置的场景
3. **链表排序**：归并排序特别适合链表
4. **分布式排序**：MapReduce框架中的排序阶段

## 🤔 常见面试问题

### Q1: 为什么归并排序是稳定的？

**答**：在合并过程中，当两个元素相等时，我们总是先取左边数组的元素，这保证了相等元素的相对位置不变。

### Q2: 归并排序和快速排序有什么区别？

**答**：
- **时间复杂度**：归并排序稳定 O(n log n)，快排最坏 O(n²)
- **空间复杂度**：归并排序 O(n)，快排 O(log n)
- **稳定性**：归并排序稳定，快排不稳定
- **实际性能**：快排通常更快（常数因子更小）

### Q3: 如何用归并排序对链表排序？

**答**：链表版本不需要额外的 O(n) 空间，只需要重新连接节点即可。

## 📝 练习题

1. 实现一个自底向上的归并排序（非递归版本）
2. 用归并排序思想解决"逆序对"问题
3. 实现链表的归并排序
4. 优化归并排序的空间使用

## 🎓 学习建议

1. **理解分治思想**：这是掌握归并排序的关键
2. **手动模拟过程**：在纸上画出分解和合并的过程
3. **编写测试用例**：包括边界情况和特殊情况
4. **分析复杂度**：理解为什么是 O(n log n)
5. **对比其他排序**：了解归并排序的优缺点

---

**下一步学习**：可以继续学习快速排序、堆排序等其他 O(n log n) 算法，并比较它们的特点。
