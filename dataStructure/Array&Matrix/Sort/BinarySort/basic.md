# 二分查找

## 1. 生活例子解释二分查找

想象一下：你要在一本字典里查单词“Java”。你不会一页一页翻，而是：

* 先翻到书的中间，看这一页的单词排序位置；
* 如果这一页的单词比“Java”大，就往前半本继续找；
* 如果比“Java”小，就往后半本继续找；
* 每次都把搜索范围缩小一半，直到找到目标。

这就是二分查找的核心思想：**不断折半缩小范围**。

---

## 2. 图解步骤

假设我们有一个升序数组：

```java
[2, 5, 7, 9, 13, 21, 30]
```

目标是 `13`。

* 第一次：中点是 `7` → 目标 > 7 → 搜索右边 `[9, 13, 21, 30]`
* 第二次：中点是 `13` → 找到目标。

图形化（范围逐渐缩小）：

```java
[2, 5, 7, 9, 13, 21, 30]
             ^
```

```java
[9, 13, 21, 30]
       ^
```

→ 找到 13。

---

## 3. Java 代码实现

```java
public class BinarySearch {
    public static int binarySearch(int[] arr, int target) {
        int left = 0;
        int right = arr.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2; // 避免 (left+right) 溢出

            if (arr[mid] == target) {
                return mid; // 找到返回下标
            } else if (arr[mid] < target) {
                left = mid + 1; // 搜索右半部分
            } else {
                right = mid - 1; // 搜索左半部分
            }
        }

        return -1; // 没找到返回 -1
    }

    public static void main(String[] args) {
        int[] arr = {2, 5, 7, 9, 13, 21, 30};
        int target = 13;
        int result = binarySearch(arr, target);
        System.out.println("目标元素下标：" + result);
    }
}
```

---

## 4. 常见边界错误

1. **死循环**：while 条件写成 `left < right`，可能导致遗漏最后一个元素。
2. **溢出问题**：`int mid = (left + right) / 2;` 在极大数组时可能溢出，所以用 `left + (right-left)/2`。
3. **未考虑排序**：二分查找只适用于**有序数组**。

---

## 6. 企业中的应用场景

* **数据库索引查找**：B+树底层就是类似二分的分区思想。
* **日志/监控分析**：按时间排序的日志里快速定位某个时间点。
* **大规模数据去重/查找**：比线性扫描快很多。
* **算法题**：例如“求开方”“求最小值边界”，很多都用二分思想。
