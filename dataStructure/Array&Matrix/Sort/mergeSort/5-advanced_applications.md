# 归并排序高级应用与变种

## 🎯 学习目标
掌握归并排序在实际问题中的应用，理解基于归并思想的算法变种

## 🔄 归并排序的经典应用

### 1. 计算逆序对数量

**问题**：给定数组，计算有多少对 (i,j) 满足 i < j 且 arr[i] > arr[j]

**思路**：在归并过程中统计逆序对

```java
public class InversionCount {
    private static long inversionCount = 0;
    
    public static long countInversions(int[] arr) {
        inversionCount = 0;
        mergeSort(arr, 0, arr.length - 1);
        return inversionCount;
    }
    
    private static void mergeSort(int[] arr, int left, int right) {
        if (left >= right) return;
        
        int mid = left + (right - left) / 2;
        mergeSort(arr, left, mid);
        mergeSort(arr, mid + 1, right);
        mergeAndCount(arr, left, mid, right);
    }
    
    private static void mergeAndCount(int[] arr, int left, int mid, int right) {
        int[] temp = new int[right - left + 1];
        int i = left, j = mid + 1, k = 0;
        
        while (i <= mid && j <= right) {
            if (arr[i] <= arr[j]) {
                temp[k++] = arr[i++];
            } else {
                temp[k++] = arr[j++];
                // 关键：arr[i] > arr[j]，形成逆序对
                // 左半部分从i到mid的所有元素都与arr[j]形成逆序对
                inversionCount += (mid - i + 1);
            }
        }
        
        while (i <= mid) temp[k++] = arr[i++];
        while (j <= right) temp[k++] = arr[j++];
        
        System.arraycopy(temp, 0, arr, left, temp.length);
    }
    
    // 测试
    public static void main(String[] args) {
        int[] arr = {8, 4, 2, 1}; // 逆序对：(8,4), (8,2), (8,1), (4,2), (4,1), (2,1)
        System.out.println("逆序对数量: " + countInversions(arr)); // 输出: 6
    }
}
```

### 2. 归并K个有序数组

**问题**：给定K个已排序的数组，将它们合并成一个有序数组

```java
import java.util.*;

public class MergeKSortedArrays {
    
    /**
     * 使用分治法归并K个有序数组
     */
    public static int[] mergeKArrays(int[][] arrays) {
        if (arrays.length == 0) return new int[0];
        
        return mergeKArraysHelper(arrays, 0, arrays.length - 1);
    }
    
    private static int[] mergeKArraysHelper(int[][] arrays, int start, int end) {
        if (start == end) {
            return arrays[start];
        }
        
        int mid = start + (end - start) / 2;
        int[] left = mergeKArraysHelper(arrays, start, mid);
        int[] right = mergeKArraysHelper(arrays, mid + 1, end);
        
        return mergeTwoArrays(left, right);
    }
    
    private static int[] mergeTwoArrays(int[] arr1, int[] arr2) {
        int[] result = new int[arr1.length + arr2.length];
        int i = 0, j = 0, k = 0;
        
        while (i < arr1.length && j < arr2.length) {
            if (arr1[i] <= arr2[j]) {
                result[k++] = arr1[i++];
            } else {
                result[k++] = arr2[j++];
            }
        }
        
        while (i < arr1.length) result[k++] = arr1[i++];
        while (j < arr2.length) result[k++] = arr2[j++];
        
        return result;
    }
    
    /**
     * 使用优先队列（堆）的方法
     * 时间复杂度: O(N log K)，其中N是总元素数，K是数组个数
     */
    public static int[] mergeKArraysHeap(int[][] arrays) {
        PriorityQueue<ArrayElement> heap = new PriorityQueue<>((a, b) -> a.value - b.value);
        List<Integer> result = new ArrayList<>();
        
        // 将每个数组的第一个元素加入堆
        for (int i = 0; i < arrays.length; i++) {
            if (arrays[i].length > 0) {
                heap.offer(new ArrayElement(arrays[i][0], i, 0));
            }
        }
        
        while (!heap.isEmpty()) {
            ArrayElement min = heap.poll();
            result.add(min.value);
            
            // 如果该数组还有下一个元素，加入堆
            if (min.index + 1 < arrays[min.arrayIndex].length) {
                heap.offer(new ArrayElement(
                    arrays[min.arrayIndex][min.index + 1], 
                    min.arrayIndex, 
                    min.index + 1
                ));
            }
        }
        
        return result.stream().mapToInt(i -> i).toArray();
    }
    
    static class ArrayElement {
        int value;
        int arrayIndex;
        int index;
        
        ArrayElement(int value, int arrayIndex, int index) {
            this.value = value;
            this.arrayIndex = arrayIndex;
            this.index = index;
        }
    }
}
```

### 3. 链表归并排序

**优势**：链表归并排序可以做到 O(1) 空间复杂度

```java
public class LinkedListMergeSort {
    
    static class ListNode {
        int val;
        ListNode next;
        ListNode(int val) { this.val = val; }
    }
    
    public static ListNode sortList(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }
        
        // 使用快慢指针找到中点
        ListNode slow = head, fast = head, prev = null;
        while (fast != null && fast.next != null) {
            prev = slow;
            slow = slow.next;
            fast = fast.next.next;
        }
        
        // 断开链表
        prev.next = null;
        
        // 递归排序两半
        ListNode left = sortList(head);
        ListNode right = sortList(slow);
        
        // 合并两个有序链表
        return merge(left, right);
    }
    
    private static ListNode merge(ListNode l1, ListNode l2) {
        ListNode dummy = new ListNode(0);
        ListNode current = dummy;
        
        while (l1 != null && l2 != null) {
            if (l1.val <= l2.val) {
                current.next = l1;
                l1 = l1.next;
            } else {
                current.next = l2;
                l2 = l2.next;
            }
            current = current.next;
        }
        
        if (l1 != null) current.next = l1;
        if (l2 != null) current.next = l2;
        
        return dummy.next;
    }
    
    /**
     * 自底向上的链表归并排序（迭代版本）
     * 真正的 O(1) 空间复杂度
     */
    public static ListNode sortListBottomUp(ListNode head) {
        if (head == null || head.next == null) return head;
        
        // 计算链表长度
        int length = getLength(head);
        
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        
        for (int size = 1; size < length; size *= 2) {
            ListNode prev = dummy;
            ListNode current = dummy.next;
            
            while (current != null) {
                ListNode left = current;
                ListNode right = split(left, size);
                current = split(right, size);
                
                prev = mergeAndConnect(prev, left, right);
            }
        }
        
        return dummy.next;
    }
    
    private static int getLength(ListNode head) {
        int length = 0;
        while (head != null) {
            length++;
            head = head.next;
        }
        return length;
    }
    
    private static ListNode split(ListNode head, int size) {
        for (int i = 1; head != null && i < size; i++) {
            head = head.next;
        }
        
        if (head == null) return null;
        
        ListNode next = head.next;
        head.next = null;
        return next;
    }
    
    private static ListNode mergeAndConnect(ListNode prev, ListNode l1, ListNode l2) {
        while (l1 != null && l2 != null) {
            if (l1.val <= l2.val) {
                prev.next = l1;
                l1 = l1.next;
            } else {
                prev.next = l2;
                l2 = l2.next;
            }
            prev = prev.next;
        }
        
        if (l1 != null) prev.next = l1;
        if (l2 != null) prev.next = l2;
        
        while (prev.next != null) {
            prev = prev.next;
        }
        
        return prev;
    }
}
```

## 🔧 归并排序的优化变种

### 1. 原地归并排序

**挑战**：实现 O(1) 空间复杂度的归并排序

```java
public class InPlaceMergeSort {
    
    /**
     * 使用旋转操作的原地归并
     * 时间复杂度: O(n² log n) - 性能较差，主要用于理论研究
     */
    public static void inPlaceMergeSort(int[] arr) {
        inPlaceMergeSortHelper(arr, 0, arr.length - 1);
    }
    
    private static void inPlaceMergeSortHelper(int[] arr, int left, int right) {
        if (left >= right) return;
        
        int mid = left + (right - left) / 2;
        inPlaceMergeSortHelper(arr, left, mid);
        inPlaceMergeSortHelper(arr, mid + 1, right);
        inPlaceMerge(arr, left, mid, right);
    }
    
    private static void inPlaceMerge(int[] arr, int left, int mid, int right) {
        int start2 = mid + 1;
        
        // 如果已经有序，直接返回
        if (arr[mid] <= arr[start2]) {
            return;
        }
        
        while (left <= mid && start2 <= right) {
            if (arr[left] <= arr[start2]) {
                left++;
            } else {
                int value = arr[start2];
                int index = start2;
                
                // 移动元素
                while (index != left) {
                    arr[index] = arr[index - 1];
                    index--;
                }
                arr[left] = value;
                
                left++;
                mid++;
                start2++;
            }
        }
    }
}
```

### 2. 多路归并排序

**应用**：外部排序，处理超大文件

```java
public class MultiWayMergeSort {
    
    /**
     * k-路归并排序
     * 适用于外部排序场景
     */
    public static void kWayMergeSort(int[] arr, int k) {
        if (k <= 1) {
            mergeSort(arr);
            return;
        }
        
        kWayMergeSortHelper(arr, 0, arr.length - 1, k);
    }
    
    private static void kWayMergeSortHelper(int[] arr, int left, int right, int k) {
        if (left >= right) return;
        
        if (right - left + 1 <= k) {
            // 小数组直接排序
            Arrays.sort(arr, left, right + 1);
            return;
        }
        
        int size = (right - left + 1) / k;
        int[] boundaries = new int[k + 1];
        
        for (int i = 0; i < k; i++) {
            boundaries[i] = left + i * size;
        }
        boundaries[k] = right + 1;
        
        // 递归排序k个部分
        for (int i = 0; i < k; i++) {
            kWayMergeSortHelper(arr, boundaries[i], boundaries[i + 1] - 1, k);
        }
        
        // k-路归并
        kWayMerge(arr, boundaries, k);
    }
    
    private static void kWayMerge(int[] arr, int[] boundaries, int k) {
        // 使用优先队列进行k-路归并
        PriorityQueue<Element> heap = new PriorityQueue<>((a, b) -> a.value - b.value);
        int[] indices = new int[k];
        
        // 初始化堆
        for (int i = 0; i < k; i++) {
            if (boundaries[i] < boundaries[i + 1]) {
                heap.offer(new Element(arr[boundaries[i]], i));
                indices[i] = boundaries[i] + 1;
            }
        }
        
        int[] temp = new int[boundaries[k] - boundaries[0]];
        int tempIndex = 0;
        
        while (!heap.isEmpty()) {
            Element min = heap.poll();
            temp[tempIndex++] = min.value;
            
            int arrayIndex = min.arrayIndex;
            if (indices[arrayIndex] < boundaries[arrayIndex + 1]) {
                heap.offer(new Element(arr[indices[arrayIndex]], arrayIndex));
                indices[arrayIndex]++;
            }
        }
        
        // 复制回原数组
        System.arraycopy(temp, 0, arr, boundaries[0], temp.length);
    }
    
    static class Element {
        int value;
        int arrayIndex;
        
        Element(int value, int arrayIndex) {
            this.value = value;
            this.arrayIndex = arrayIndex;
        }
    }
    
    private static void mergeSort(int[] arr) {
        Arrays.sort(arr); // 简化实现
    }
}
```

## 🌍 外部排序应用

### 外部归并排序实现

```java
import java.io.*;
import java.util.*;

public class ExternalMergeSort {
    private static final int MEMORY_LIMIT = 1000; // 内存限制：1000个整数
    
    /**
     * 外部归并排序主方法
     * 处理大文件，无法完全加载到内存
     */
    public static void externalSort(String inputFile, String outputFile) throws IOException {
        // 第一阶段：分割并排序
        List<String> tempFiles = splitAndSort(inputFile);
        
        // 第二阶段：多路归并
        multiWayMerge(tempFiles, outputFile);
        
        // 清理临时文件
        for (String tempFile : tempFiles) {
            new File(tempFile).delete();
        }
    }
    
    /**
     * 第一阶段：将大文件分割成小的有序文件
     */
    private static List<String> splitAndSort(String inputFile) throws IOException {
        List<String> tempFiles = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            List<Integer> buffer = new ArrayList<>();
            String line;
            int fileIndex = 0;
            
            while ((line = reader.readLine()) != null) {
                buffer.add(Integer.parseInt(line.trim()));
                
                if (buffer.size() >= MEMORY_LIMIT) {
                    String tempFileName = "temp_" + fileIndex++ + ".txt";
                    sortAndWriteToFile(buffer, tempFileName);
                    tempFiles.add(tempFileName);
                    buffer.clear();
                }
            }
            
            // 处理剩余数据
            if (!buffer.isEmpty()) {
                String tempFileName = "temp_" + fileIndex + ".txt";
                sortAndWriteToFile(buffer, tempFileName);
                tempFiles.add(tempFileName);
            }
        }
        
        return tempFiles;
    }
    
    private static void sortAndWriteToFile(List<Integer> data, String fileName) throws IOException {
        Collections.sort(data);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            for (int value : data) {
                writer.println(value);
            }
        }
    }
    
    /**
     * 第二阶段：多路归并临时文件
     */
    private static void multiWayMerge(List<String> tempFiles, String outputFile) throws IOException {
        PriorityQueue<FileElement> heap = new PriorityQueue<>((a, b) -> a.value - b.value);
        List<BufferedReader> readers = new ArrayList<>();
        
        // 打开所有临时文件
        for (int i = 0; i < tempFiles.size(); i++) {
            BufferedReader reader = new BufferedReader(new FileReader(tempFiles.get(i)));
            readers.add(reader);
            
            String line = reader.readLine();
            if (line != null) {
                heap.offer(new FileElement(Integer.parseInt(line.trim()), i));
            }
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            while (!heap.isEmpty()) {
                FileElement min = heap.poll();
                writer.println(min.value);
                
                // 从对应文件读取下一个元素
                String line = readers.get(min.fileIndex).readLine();
                if (line != null) {
                    heap.offer(new FileElement(Integer.parseInt(line.trim()), min.fileIndex));
                }
            }
        }
        
        // 关闭所有文件
        for (BufferedReader reader : readers) {
            reader.close();
        }
    }
    
    static class FileElement {
        int value;
        int fileIndex;
        
        FileElement(int value, int fileIndex) {
            this.value = value;
            this.fileIndex = fileIndex;
        }
    }
    
    // 测试外部排序
    public static void main(String[] args) throws IOException {
        // 创建测试文件
        createLargeTestFile("large_input.txt", 10000);
        
        // 执行外部排序
        long startTime = System.currentTimeMillis();
        externalSort("large_input.txt", "sorted_output.txt");
        long endTime = System.currentTimeMillis();
        
        System.out.println("外部排序完成，用时: " + (endTime - startTime) + "ms");
        
        // 验证结果
        if (verifySorted("sorted_output.txt")) {
            System.out.println("排序正确！");
        } else {
            System.out.println("排序错误！");
        }
    }
    
    private static void createLargeTestFile(String fileName, int count) throws IOException {
        Random random = new Random();
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            for (int i = 0; i < count; i++) {
                writer.println(random.nextInt(100000));
            }
        }
    }
    
    private static boolean verifySorted(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line = reader.readLine();
            if (line == null) return true;
            
            int prev = Integer.parseInt(line.trim());
            while ((line = reader.readLine()) != null) {
                int current = Integer.parseInt(line.trim());
                if (current < prev) {
                    return false;
                }
                prev = current;
            }
        }
        return true;
    }
}
```

## 🏆 实际项目中的应用

### 1. 数据库排序
- **索引构建**：B+树索引的构建使用归并排序
- **ORDER BY 查询**：大结果集的排序
- **JOIN 操作**：排序-归并连接算法

### 2. 大数据处理
- **MapReduce 框架**：Shuffle 阶段的排序
- **Spark**：`sortBy` 操作的实现
- **Hadoop**：外部排序处理大文件

### 3. 系统软件
- **操作系统**：进程调度算法
- **编译器**：符号表排序
- **搜索引擎**：倒排索引构建

## 📚 总结

归并排序不仅是一个经典的排序算法，更是：

1. **分治思想的典型应用**
2. **稳定排序的代表**
3. **外部排序的基础**
4. **多路归并的起点**
5. **并行算法的雏形**

掌握归并排序及其变种，为后续学习更复杂的算法奠定了坚实基础！