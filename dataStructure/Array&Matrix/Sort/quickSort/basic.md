# 三路分区快速排序（白板最简实现 + 面试口述脚本）

—— 适合 2-3 分钟白板讲解与演示，突出不变量、边界、复杂度与工程取舍

## 1) 口述脚本（开场 30 秒）

- **核心**: 分治 + 原地三路分区（荷兰国旗）。维护不变量：[0..lt-1] < pivot，[lt..i-1] = pivot，[gt+1..r] > pivot，指针 `i` 扫描 [i..gt]。
- **流程**: 选 pivot → 一趟线性分区 → 递归处理左、右两段；等于段直接跳过，避免重复元素导致的退化。
- **复杂度**: 平均/最好 O(n log n)，最坏 O(n^2)（极端不平衡）。空间 O(log n)（递归栈）。不稳定。
- **优化**: 随机化或三数取中降最坏概率；小数组切换插入排序；尾递归优化压栈深。

## 2) 白板最简 Java 实现（三路分区）

```java
import java.util.*;

public class QuickSort3Way {

    // 对外入口
    public static void sort(int[] arr) {
        if (arr == null || arr.length <= 1) return;
        quickSort3Way(arr, 0, arr.length - 1);
    }

    // 三路分区快排：不变量
    // [l..lt-1] < pivot, [lt..i-1] == pivot, [i..gt] 未处理, [gt+1..r] > pivot
    private static void quickSort3Way(int[] a, int l, int r) {
        while (l < r) { // 尾递归优化：总是先递归较小段
            // 1) 选基准：可替换为随机化/三数取中
            int pivot = a[l + ((r - l) >>> 1)];

            int lt = l, i = l, gt = r;
            while (i <= gt) {
                if (a[i] < pivot) swap(a, lt++, i++);
                else if (a[i] > pivot) swap(a, i, gt--);
                else i++;
            }

            // 2) 递归/迭代：先处理较小区间，较大区间通过循环收缩（尾递归消除）
            int leftSize = lt - l;
            int rightSize = r - gt;
            if (leftSize < rightSize) {
                if (l < lt - 1) quickSort3Way(a, l, lt - 1);
                l = gt + 1; // 尾递归优化：迭代处理右侧
            } else {
                if (gt + 1 < r) quickSort3Way(a, gt + 1, r);
                r = lt - 1; // 尾递归优化：迭代处理左侧
            }
        }
    }

    // 可选：随机化/三数取中，把更稳健的 pivot 放到中间用于比较
    @SuppressWarnings("unused")
    private static void randomizePivotToMiddle(int[] a, int l, int r) {
        int idx = l + new Random().nextInt(r - l + 1);
        int mid = l + ((r - l) >>> 1);
        swap(a, idx, mid);
    }

    @SuppressWarnings("unused")
    private static void medianOfThreeToMiddle(int[] a, int l, int r) {
        int m = l + ((r - l) >>> 1);
        if (a[l] > a[m]) swap(a, l, m);
        if (a[l] > a[r]) swap(a, l, r);
        if (a[m] > a[r]) swap(a, m, r);
        // a[m] 约为中位
    }

    private static void swap(int[] a, int i, int j) {
        if (i == j) return;
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    // 演示
    public static void main(String[] args) {
        int[] data = {5, 1, 5, 3, 2, 5, 4, 2, 3};
        sort(data);
        System.out.println(Arrays.toString(data));
    }
}
```

口述提示：

- 进入循环前给出不变量与四段定义；`i` 向右扫描，遇小交换到左端，遇大交换到右端并缩小 `gt`，等于直接跳过。
- 结束后 `lt..gt` 为等于段，递归左右两侧；通过“先递归小段 + while 收缩大段”实现尾递归优化，栈深 O(log n)。

## 3) 复杂度与退化（口述要点）

- **时间**: 平均/最好 O(n log n)，最坏 O(n^2)；三路分区在重复元素多时显著降低递归深度。
- **空间**: 平均 O(log n)，最坏 O(n)（递归栈）。
- **退化触发**: 固定基准 + 近有序；或重复元素多但仍用二路分区。
- **规避**: 随机化/三数取中、三路分区、小数组切换插入排序、尾递归优化。

## 4) 工程取舍与对比（口述要点）

- **稳定性**: 不稳定；若需稳定或做外部排序，用归并/TimSort。
- **库实现**: JDK 原始类型采用双轴快排（常数更优），对象数组采用 TimSort（稳定、利用自然有序 runs）。
- **应用**: 内存排序、TopK 用 quickselect（同样基于分区，期望 O(n)）。

## 5) 面试演示顺序（建议）

1) 先画不变量四段与三个指针移动；
2) 白板写 `while (i <= gt)` 主循环；
3) 讲明递归边界与尾递归优化；
4) 根据数据分布选择随机化/三数取中；
5) 给出复杂度与退化分析和规避手段；
6) 补充 quickselect 延伸。

## 小练习

- **练习1**: 把 pivot 改为“随机化”或“三数取中”，对比逆序与全相等数据的耗时差异。
- **练习2**: 增加阈值，当子数组长度 ≤ 16 时改用插入排序，比较性能。
- **练习3**: 手写 `quickselect(int[] a, int k)` 返回第 k 小（1-indexed），仅对一侧递归。
