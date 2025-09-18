# 快速排序

## 为什么需要快排

问题场景：给你一个无序数组，要你排序。冒泡、插入能做，但太慢（O(n²)）。

快排亮点：分治思想，平均复杂度 O(n log n)，常数小，比归并省内存，比堆排简单易写。面试常考。

## 快排的核心思想

一句话：选一个基准值（pivot），把比它小的放左边，比它大的放右边，然后递归排序左右两边。
核心就是 分区（Partition） + 递归（Divide & Conquer）。

## 快排的实现

```java
import java.util.Arrays;

public class QuickSortBase {

    public void sort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        quickSort(arr, 0, arr.length - 1);
    }

    private void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            // partition a.k.a. pivot index
            int pi = partition(arr, low, high);

            quickSort(arr, low, pi - 1);  // 递归排序左半部分
            quickSort(arr, pi + 1, high); // 递归排序右半部分
        }
    }

    // Lomuto 分区方案
    private int partition(int[] arr, int low, int high) {
        int pivot = arr[high]; // 选择最后一个元素作为 pivot
        int i = (low - 1); // i 是小于 pivot 的区域的边界

        for (int j = low; j < high; j++) {
            // 如果当前元素小于或等于 pivot
            if (arr[j] <= pivot) {
                i++;
                swap(arr, i, j);
            }
        }

        // 将 pivot 放到正确的位置上
        swap(arr, i + 1, high);
        return i + 1;
    }

    private void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    public static void main(String[] args) {
        int[] arr = {10, 7, 8, 9, 1, 5};
        new QuickSortBase().sort(arr);
        System.out.println("排序后的数组: " + Arrays.toString(arr));
    }
}
```

## 快排的痛点

我们先来回顾一下基础快排的“痛点”：

- 最坏情况下的性能退化：如果每次选取的基准值（pivot）都是当前待排数组的最小值或最大值（例如，在一个已经有序的数组中，总是选择第一个或最后一个元素作为 pivot），那么每次分区都只能将一个元素“排序”，快排会退化成一个类似冒泡排序的算法，时间复杂度从平均的 O(n log n) 退化到 O(n²)。
- 递归深度过深：在最坏情况下，递归深度会达到 n，这可能导致栈溢出（StackOverflowError）。
大量重复元素：如果数组中存在大量重复元素，传统的分区方式可能会导致分区极其不平衡。

## 如何优化快排

### 1. 优化基准值（Pivot）的选择

#### 随机选择基准值

```java

```

#### 三数取中法

为了避免在有序或接近有序的数组上性能退化，我们选择子数组的 头、中、尾 三个元素的中位数作为 pivot。

```java
import java.util.Arrays;

public class QuickSortMedianOfThree {

    public void sort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        quickSort(arr, 0, arr.length - 1);
    }

    private void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pivot = getMedianOfThree(arr, low, high); // 获取中位数作为 pivot
            
            // Hoare 分区方案更适合三数取中，因为它不要求 pivot 最终在正确的位置
            int i = low, j = high;
            while (i <= j) {
                while (arr[i] < pivot) {
                    i++;
                }
                while (arr[j] > pivot) {
                    j--;
                }
                if (i <= j) {
                    swap(arr, i, j);
                    i++;
                    j--;
                }
            }

            // 递归调用
            if (low < j) {
                quickSort(arr, low, j);
            }
            if (i < high) {
                quickSort(arr, i, high);
            }
        }
    }

    private int getMedianOfThree(int[] arr, int low, int high) {
        int mid = low + (high - low) / 2;

        // 保证 arr[low] <= arr[mid] <= arr[high]
        if (arr[low] > arr[mid]) {
            swap(arr, low, mid);
        }
        if (arr[low] > arr[high]) {
            swap(arr, low, high);
        }
        if (arr[mid] > arr[high]) {
            swap(arr, mid, high);
        }

        // 此时 arr[mid] 就是中位数
        // 经典做法是将中位数藏在 high-1 的位置，然后对 low+1 到 high-2 进行分区
        // 这里为了简化，我们直接返回中位数的值，并使用双指针分区（Hoare）
        return arr[mid];
    }

    private void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
    
    public static void main(String[] args) {
        int[] arr = {9, 1, 3, 5, 2, 6, 4, 8, 7};
        new QuickSortMedianOfThree().sort(arr);
        System.out.println("排序后的数组: " + Arrays.toString(arr));
        
        int[] sortedArr = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        new QuickSortMedianOfThree().sort(sortedArr);
        System.out.println("已排序数组排序后: " + Arrays.toString(sortedArr));
    }
}
```

### 2. 优化小数组的排序方式

思路：递归的一个问题是函数调用的开销。当待排序的数组规模非常小时（例如，长度小于 10 或 16），递归调用快排的开销可能比直接使用一种更简单的排序算法还要大。插入排序在处理小规模、近乎有序的数组时效率非常高。

实现：在快排的递归函数入口处增加一个判断。当 high - low + 1 小于一个预设的阈值（CUTOFF）时，不再继续递归调用快排，而是调用插入排序来处理这个小数组。

```java
import java.util.Arrays;

public class QuickSortWithCutoff {

    // 切换到插入排序的阈值，通常在 5 到 15 之间
    private static final int CUTOFF = 10;

    public void sort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        quickSort(arr, 0, arr.length - 1);
    }

    private void quickSort(int[] arr, int low, int high) {
        // 如果数组规模小，使用插入排序
        if (high - low + 1 <= CUTOFF) {
            insertionSort(arr, low, high);
            return;
        }

        // 继续使用三数取中和分区
        int pivot = getMedianOfThree(arr, low, high);
        int i = low, j = high;
        while (i <= j) {
            while (arr[i] < pivot) i++;
            while (arr[j] > pivot) j--;
            if (i <= j) {
                swap(arr, i, j);
                i++;
                j--;
            }
        }

        if (low < j) quickSort(arr, low, j);
        if (i < high) quickSort(arr, i, high);
    }

    // 对 arr 数组的 [low, high] 区间进行插入排序
    private void insertionSort(int[] arr, int low, int high) {
        for (int i = low + 1; i <= high; i++) {
            int temp = arr[i];
            int j = i;
            while (j > low && arr[j - 1] > temp) {
                arr[j] = arr[j - 1];
                j--;
            }
            arr[j] = temp;
        }
    }

    // (getMedianOfThree 和 swap 方法与上一个例子相同)
    private int getMedianOfThree(int[] arr, int low, int high) {
        // ... 代码同上 ...
        int mid = low + (high - low) / 2;
        if (arr[low] > arr[mid]) swap(arr, low, mid);
        if (arr[low] > arr[high]) swap(arr, low, high);
        if (arr[mid] > arr[high]) swap(arr, mid, high);
        return arr[mid];
    }
    private void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    public static void main(String[] args) {
        int[] arr = {24, 69, 100, 99, 79, 78, 67, 36, 26, 19, 58, 4, 2, 1, -1};
        new QuickSortWithCutoff().sort(arr);
        System.out.println("排序后的数组: " + Arrays.toString(arr));
    }
}
```

### 3. 优化递归操作

#### 尾递归优化

思路：在标准的快排实现中，我们通常会对两个子数组都进行递归调用。

```java
quickSort(arr, low, pivotIndex - 1);
quickSort(arr, pivotIndex + 1, high);
```

我们可以将其中一个递归调用（通常是对规模较大的那个子数组的调用）改成迭代（循环）的形式，从而减少一层递归深度。这被称为尾递归优化。

实现：

```java
import java.util.Arrays;

public class QuickSortTailRecursion {

    private static final int CUTOFF = 10;

    public void sort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        quickSort(arr, 0, arr.length - 1);
    }

    private void quickSort(int[] arr, int low, int high) {
        while (low < high) {
            // 如果数组规模小，使用插入排序
            if (high - low + 1 <= CUTOFF) {
                insertionSort(arr, low, high);
                return; // 排序完成后直接返回
            }

            int pivot = getMedianOfThree(arr, low, high);
            int i = low, j = high;
            while (i <= j) {
                while (arr[i] < pivot) i++;
                while (arr[j] > pivot) j--;
                if (i <= j) {
                    swap(arr, i, j);
                    i++;
                    j--;
                }
            }

            // 尾递归优化：对较短的子数组进行递归，对较长的子数组进行循环
            if (j - low < high - i) {
                // 左半部分更短
                if (low < j) {
                    quickSort(arr, low, j);
                }
                low = i; // 循环处理右半部分
            } else {
                // 右半部分更短或相等
                if (i < high) {
                    quickSort(arr, i, high);
                }
                high = j; // 循环处理左半部分
            }
        }
    }
    
    // (insertionSort, getMedianOfThree, swap 方法同上)
    private void insertionSort(int[] arr, int low, int high) { /* ... */ }
    private int getMedianOfThree(int[] arr, int low, int high) { /* ... */ return 0; }
    private void swap(int[] arr, int i, int j) { /* ... */ }
    
    // 省略重复代码的完整实现...
}
```

优点：可以有效地将递归深度从最坏的 O(n) 降低到 O(logn)，避免栈溢出。

### 4.优化分区方法 (处理重复元素)

#### 三路快排 (3-Way Quicksort)

背景：当你处理的数组中包含大量重复元素时，标准的分区方法（例如 Lomuto 或 Hoare）会将所有与 pivot 相等的元素都放到一边（通常是左边或右边），这仍然可能导致分区不平衡。

思路：将数组分成三部分，而不是两部分：

小于 pivot 的部分

等于 pivot 的部分

大于 pivot 的部分
然后，只需要对“小于”和“大于”的两部分进行递归排序，中间“等于”的部分已经天然有序，无需再处理。

实现：需要维护两个指针 lt (less than) 和 gt (greater than)，以及一个遍历指针 i。

arr[i] < pivot：交换 arr[lt] 和 arr[i]，lt 和 i 都向右移动。

arr[i] > pivot：交换 arr[gt] 和 arr[i]，gt 向左移动。

arr[i] == pivot：i 直接向右移动。

优点：在有大量重复元素的场景下，性能有极大的提升，时间复杂度可以退化到接近线性的 O(n)。

```java
import java.util.Arrays;

public class QuickSort3Way {

    public void sort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        // 为了简单，我们可以在排序前打乱数组，以避免最坏情况
        // Collections.shuffle(Arrays.asList(arr));
        quickSort(arr, 0, arr.length - 1);
    }

    private void quickSort(int[] arr, int low, int high) {
        if (low >= high) {
            return;
        }

        // 选择 arr[low] 作为 pivot
        int pivot = arr[low];
        
        // 维护三个区间的指针
        int lt = low;     // [low...lt-1] 存储小于 pivot 的元素
        int i = low + 1;  // [lt...i-1] 存储等于 pivot 的元素
        int gt = high;    // [gt+1...high] 存储大于 pivot 的元素

        // 分区过程
        while (i <= gt) {
            if (arr[i] < pivot) {
                swap(arr, lt, i);
                lt++;
                i++;
            } else if (arr[i] > pivot) {
                swap(arr, i, gt);
                gt--;
            } else { // arr[i] == pivot
                i++;
            }
        }

        // 现在 [low...lt-1] < pivot, [lt...gt] == pivot, [gt+1...high] > pivot
        // 递归排序小于和大于 pivot 的部分
        quickSort(arr, low, lt - 1);
        quickSort(arr, gt + 1, high);
    }

    private void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
    
    public static void main(String[] args) {
        int[] arrWithDuplicates = {2, 1, 5, 2, 4, 8, 2, 5, 2, 9};
        new QuickSort3Way().sort(arrWithDuplicates);
        System.out.println("三路快排后的数组: " + Arrays.toString(arrWithDuplicates));
    }
}
```

## QA清单

### 尾递归优化中quickSort里有些内容没看明白,不懂为什么,这段代码哪里有用迭代了?如果有一长一短的话,处理完短数组后,对于长数组要循环处理?怎么个循环处理法?不还是要走长数组的递归吗?

好的，这个问题非常好。之前的流程图展示了“什么”在发生，但没有解释“怎么样”的感觉。我们换一种方式，用一个详细的“剧本”来描述，让你能在大脑里模拟出这个过程。

#### 建立一个心智模型：把它想象成一个“经理”和一个“实习生”

* **经理 (The Manager)**：就是那个 `while` 循环。他很资深，**一直待在自己的办公室里（同一个函数栈帧）**，负责处理主要的、大的任务。
* **实习生 (The Intern)**：就是那个递归调用 `quickSort()`。他被临时叫来处理一个**明确的、小范围**的任务。他做完就走，然后向经理汇报。
* **任务清单 (The Desk)**：经理桌上的便签，上面写着当前要处理的数组范围 `low` 和 `high`。

现在，我们用这个模型来“上演”一下尾递归优化的快排过程。

假设我们的数组是：`[2, 8, 7, 1, 3, 5, 6, 4]`

---

#### **第一幕：经理开始工作**

1.  **初始状态**:
    * **经理** 接到一个大任务：排序整个数组。
    * 他拿出便签（`low` 和 `high` 变量），写下：**任务范围 (0, 7)**。
    * 经理启动他的工作模式 (`while (low < high)`，即 `while (0 < 7)`，条件成立，开始循环)。

2.  **经理处理任务**:
    * 经理亲自执行 `partition` 操作。他选了 `arr[0]` (值为2) 作为基准。一通操作后，数组变成了 `[1, 2, 7, 8, 3, 5, 6, 4]`，基准 `2` 的最终位置是索引 `1`。
    * 现在，任务被分成了两块：
        * **左边任务**: 排序 `(0, 0)` -> 长度为 1 (短)
        * **右边任务**: 排序 `(2, 7)` -> 长度为 6 (长)

3.  **经理的决策 (关键！)**:
    * 经理看着这两个任务，心想：“左边这个 `(0, 0)` 范围太小了，不值得我亲自跟进。**我找个实习生来处理。**”
    * 于是，经理打了个电话，说：“喂，实习生吗？过来帮我把 `(0, 0)` 这个范围排一下序。”
    * **这就是递归调用 `quickSort(arr, 0, 0)`**。一个实习生被创建出来，接手了这个小任务。
    * 经理接着想：“至于右边这个 `(2, 7)` 的大任务，**我自己留着，等会儿接着干**。”
    * 他在自己的便签上，划掉了原来的 `(0, 7)`，改成了新的任务范围：**`low = 2`, `high = 7`**。

4.  **实习生出场**:
    * 实习生接到任务 `quickSort(0, 0)`。
    * 他一看 `low` 不小于 `high`，这是个“基础情况”(Base Case)，啥也不用干。
    * 实习生立刻回去向经理报告：“老板，`0,0` 的活干完了！” 然后他就下班了（函数返回，实习生占用的内存/栈空间被释放）。

---

#### **第二幕：经理继续他的循环**

1.  **回到经理办公室**:
    * 经理等实习生干完活回来后，他的 `while` 循环的第一次迭代结束了。
    * 他并没有离开办公室（**没有从 `quickSort(0, 7)` 这个函数返回**）。
    * 他看了看自己的便签，现在的任务是 `(2, 7)`。
    * 他检查 `while` 循环条件：`while (2 < 7)`，成立，继续工作！

2.  **经理处理新任务**:
    * 经理对 `(2, 7)` 这个范围的子数组 `[7, 8, 3, 5, 6, 4]` 进行 `partition`。假设他选了 `7` 做基准，一通操作后，子数组可能变成 `[3, 5, 6, 4, 7, 8]`，基准 `7` 的新位置在索引 `6`。
    * 任务又被分成了两块：
        * **左边任务**: 排序 `(2, 5)` -> 长度为 4 (长)
        * **右边任务**: 排序 `(7, 7)` -> 长度为 1 (短)

3.  **经理的再次决策**:
    * 经理一看：“右边这个 `(7, 7)` 范围很小，**再叫个实习生来干！**”
    * **新的递归调用 `quickSort(arr, 7, 7)` 发生**。第二个实习生出场，干完活（发现是Base Case），然后走人。
    * 经理又想：“左边 `(2, 5)` 这个任务还是挺大的，**我留着自己干**。”
    * 他再次更新自己的便签，把 `(2, 7)` 划掉，写上新的任务范围：**`low = 2`, `high = 5`**。

---

#### **第三幕及以后**

经理的 `while` 循环会一直这样持续下去：
-   在一个固定的“办公室”里（同一个初始的函数栈帧）。
-   不断处理自己便签上“较长”的任务。
-   遇到“较短”的任务，就派发给一个临时的“实习生”（新的递归调用）。
-   直到他便签上的任务范围缩小到 `low >= high`，他的 `while` 循环才结束，整个排序工作完成。

#### **总结：到底是什么样子？**

“在迭代里递归”的样子就是：
**一个持久的“主循环”（经理），在自己的每一次迭代中，可能会创建一个短暂的“子任务”（实习生）去处理一小部分工作，然后主循环自己继续处理剩下的大部分工作。**

-   **迭代 (`while`)** 体现在：经理始终在自己的办公室里，不断更新便签，处理下一个主要矛盾。这保证了**栈的深度不会因为处理长数组而增加**。
-   **递归 (`quickSort()`)** 体现在：经理把小问题外包出去，让别人（新的函数栈帧）去解决。这保证了即使需要深入解决问题，也总是沿着分支最少、最短的那条路走。
