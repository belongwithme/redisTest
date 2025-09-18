/**
 * 归并排序Java实现
 * 包含基础版本、优化版本和测试代码
 * 
 * @author 学习者
 * @date 2024
 */
public class MergeSort {
    
    /**
     * 归并排序主方法 - 基础版本
     * 时间复杂度: O(n log n)
     * 空间复杂度: O(n)
     * 
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
     * 
     * @param arr 数组
     * @param left 左边界（包含）
     * @param right 右边界（包含）
     */
    private static void mergeSortHelper(int[] arr, int left, int right) {
        // 基准情况：只有一个元素或空数组
        if (left >= right) {
            return;
        }
        
        // 计算中点，避免整数溢出
        int mid = left + (right - left) / 2;
        
        // 递归排序左半部分 [left, mid]
        mergeSortHelper(arr, left, mid);
        
        // 递归排序右半部分 [mid+1, right]
        mergeSortHelper(arr, mid + 1, right);
        
        // 合并两个有序数组
        merge(arr, left, mid, right);
    }
    
    /**
     * 合并两个有序数组
     * 这是归并排序的核心操作
     * 
     * @param arr 原数组
     * @param left 左边界
     * @param mid 中点
     * @param right 右边界
     */
    private static void merge(int[] arr, int left, int mid, int right) {
        // 创建临时数组存储合并结果
        int[] temp = new int[right - left + 1];
        
        int i = left;      // 左半部分的指针
        int j = mid + 1;   // 右半部分的指针
        int k = 0;         // 临时数组的指针
        
        // 比较两个数组的元素，将较小的放入临时数组
        while (i <= mid && j <= right) {
            if (arr[i] <= arr[j]) {
                temp[k++] = arr[i++];
            } else {
                temp[k++] = arr[j++];
            }
        }
        
        // 复制左半部分剩余元素
        while (i <= mid) {
            temp[k++] = arr[i++];
        }
        
        // 复制右半部分剩余元素
        while (j <= right) {
            temp[k++] = arr[j++];
        }
        
        // 将临时数组的内容复制回原数组
        for (int index = 0; index < temp.length; index++) {
            arr[left + index] = temp[index];
        }
    }
    
    // ==================== 优化版本 ====================
    
    /**
     * 优化的归并排序 - 小数组使用插入排序
     * 在小数组上插入排序通常比归并排序更快
     */
    public static void mergeSortOptimized(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        mergeSortOptimizedHelper(arr, 0, arr.length - 1);
    }
    
    private static void mergeSortOptimizedHelper(int[] arr, int left, int right) {
        // 对小数组使用插入排序
        if (right - left <= 10) {
            insertionSort(arr, left, right);
            return;
        }
        
        int mid = left + (right - left) / 2;
        mergeSortOptimizedHelper(arr, left, mid);
        mergeSortOptimizedHelper(arr, mid + 1, right);
        
        // 如果数组已经有序，跳过合并步骤
        if (arr[mid] <= arr[mid + 1]) {
            return;
        }
        
        merge(arr, left, mid, right);
    }
    
    /**
     * 插入排序 - 用于小数组的优化
     */
    private static void insertionSort(int[] arr, int left, int right) {
        for (int i = left + 1; i <= right; i++) {
            int key = arr[i];
            int j = i - 1;
            
            while (j >= left && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }
    
    // ==================== 自底向上版本（非递归） ====================
    
    /**
     * 自底向上的归并排序（迭代版本）
     * 避免递归调用栈的开销
     */
    public static void mergeSortBottomUp(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        
        int n = arr.length;
        
        // 子数组大小从1开始，每次翻倍
        for (int size = 1; size < n; size *= 2) {
            // 对每个大小为size的子数组进行合并
            for (int left = 0; left < n - size; left += 2 * size) {
                int mid = left + size - 1;
                int right = Math.min(left + 2 * size - 1, n - 1);
                merge(arr, left, mid, right);
            }
        }
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 打印数组内容
     */
    public static void printArray(String prefix, int[] arr) {
        System.out.print(prefix + ": [");
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
            if (i < arr.length - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }
    
    /**
     * 验证数组是否已排序
     */
    public static boolean isSorted(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[i - 1]) {
                return false;
            }
        }
        return true;
    }
    
    // ==================== 测试代码 ====================
    
    public static void main(String[] args) {
        System.out.println("=== 归并排序测试 ===\n");
        
        // 测试用例1：一般情况
        int[] arr1 = {38, 27, 43, 3, 9, 82, 10};
        testSortingMethod("基础归并排序", arr1.clone(), MergeSort::mergeSort);
        
        // 测试用例2：已排序数组
        int[] arr2 = {1, 2, 3, 4, 5, 6, 7};
        testSortingMethod("已排序数组", arr2.clone(), MergeSort::mergeSort);
        
        // 测试用例3：逆序数组
        int[] arr3 = {7, 6, 5, 4, 3, 2, 1};
        testSortingMethod("逆序数组", arr3.clone(), MergeSort::mergeSort);
        
        // 测试用例4：包含重复元素
        int[] arr4 = {5, 2, 8, 2, 9, 1, 5, 5};
        testSortingMethod("包含重复元素", arr4.clone(), MergeSort::mergeSort);
        
        // 测试用例5：边界情况
        int[] arr5 = {42}; // 单个元素
        testSortingMethod("单个元素", arr5.clone(), MergeSort::mergeSort);
        
        int[] arr6 = {}; // 空数组
        testSortingMethod("空数组", arr6.clone(), MergeSort::mergeSort);
        
        // 性能对比测试
        System.out.println("\n=== 性能对比测试 ===");
        performanceTest();
    }
    
    /**
     * 测试排序方法的通用函数
     */
    private static void testSortingMethod(String testName, int[] arr, java.util.function.Consumer<int[]> sortMethod) {
        System.out.println("测试: " + testName);
        printArray("排序前", arr);
        
        long startTime = System.nanoTime();
        sortMethod.accept(arr);
        long endTime = System.nanoTime();
        
        printArray("排序后", arr);
        System.out.println("是否正确排序: " + isSorted(arr));
        System.out.println("用时: " + (endTime - startTime) / 1000.0 + " 微秒");
        System.out.println();
    }
    
    /**
     * 性能测试
     */
    private static void performanceTest() {
        int[] sizes = {1000, 5000, 10000, 20000};
        
        for (int size : sizes) {
            // 生成随机数组
            int[] arr = generateRandomArray(size);
            
            // 测试基础版本
            long time1 = timeSortingMethod(arr.clone(), MergeSort::mergeSort);
            
            // 测试优化版本
            long time2 = timeSortingMethod(arr.clone(), MergeSort::mergeSortOptimized);
            
            // 测试自底向上版本
            long time3 = timeSortingMethod(arr.clone(), MergeSort::mergeSortBottomUp);
            
            System.out.printf("数组大小: %d\n", size);
            System.out.printf("基础版本: %.2f ms\n", time1 / 1_000_000.0);
            System.out.printf("优化版本: %.2f ms\n", time2 / 1_000_000.0);
            System.out.printf("自底向上: %.2f ms\n", time3 / 1_000_000.0);
            System.out.println();
        }
    }
    
    /**
     * 计算排序方法的执行时间
     */
    private static long timeSortingMethod(int[] arr, java.util.function.Consumer<int[]> sortMethod) {
        long startTime = System.nanoTime();
        sortMethod.accept(arr);
        return System.nanoTime() - startTime;
    }
    
    /**
     * 生成随机数组
     */
    private static int[] generateRandomArray(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = (int) (Math.random() * 1000);
        }
        return arr;
    }
}