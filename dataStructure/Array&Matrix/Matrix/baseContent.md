# 矩阵 (Matrix) 基础知识

## 1. 矩阵基础概念

### 1.1 矩阵定义
- **定义**：矩阵是由m×n个数排成的m行n列的数表，用二维数组表示
- **表示方法**：A[i][j] 表示矩阵A的第i行第j列的元素（从0开始索引）
- **矩阵维度**：m×n矩阵，其中m是行数，n是列数

### 1.2 Java中的矩阵表示

#### 二维数组声明和初始化
```java
// 方式1：动态初始化
int[][] matrix1 = new int[3][4];  // 3行4列的矩阵

// 方式2：静态初始化
int[][] matrix2 = {
    {1, 2, 3},
    {4, 5, 6},
    {7, 8, 9}
};

// 方式3：分步初始化
int[][] matrix3 = new int[3][];
matrix3[0] = new int[]{1, 2, 3};
matrix3[1] = new int[]{4, 5, 6};
matrix3[2] = new int[]{7, 8, 9};

// 方式4：不规则矩阵（锯齿数组）
int[][] jaggedMatrix = {
    {1, 2},
    {3, 4, 5},
    {6, 7, 8, 9}
};
```

#### 矩阵的基本属性
```java
int[][] matrix = {{1, 2, 3}, {4, 5, 6}};

// 获取行数
int rows = matrix.length;        // 2

// 获取列数（假设矩阵规整）
int cols = matrix[0].length;     // 3

// 获取每行的列数（处理不规则矩阵）
for (int i = 0; i < matrix.length; i++) {
    int colsInRow = matrix[i].length;
    System.out.println("第" + i + "行有" + colsInRow + "列");
}

// 检查是否为方阵
boolean isSquare = (rows == cols);
```

## 2. 矩阵内存布局

### 2.1 行优先存储（Row-Major Order）
Java采用行优先存储方式，即矩阵按行依次存储在内存中。

```java
int[][] matrix = {
    {1, 2, 3},
    {4, 5, 6},
    {7, 8, 9}
};

/*
内存布局（逻辑视图）：
Row 0: [1] [2] [3]
Row 1: [4] [5] [6] 
Row 2: [7] [8] [9]

实际内存中的存储：
matrix[0] -> [1, 2, 3]  // 指向第一行数组
matrix[1] -> [4, 5, 6]  // 指向第二行数组
matrix[2] -> [7, 8, 9]  // 指向第三行数组

每行在内存中是连续的，但行与行之间可能不连续
*/
```

### 2.2 内存访问模式对性能的影响
```java
// 缓存友好的访问模式（按行访问）
public void efficientAccess(int[][] matrix) {
    int rows = matrix.length;
    int cols = matrix[0].length;
    
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            // 按行顺序访问，缓存命中率高
            matrix[i][j] = i * cols + j;
        }
    }
}

// 缓存不友好的访问模式（按列访问）
public void inefficientAccess(int[][] matrix) {
    int rows = matrix.length;
    int cols = matrix[0].length;
    
    for (int j = 0; j < cols; j++) {
        for (int i = 0; i < rows; i++) {
            // 按列访问，跳跃访问内存，缓存命中率低
            matrix[i][j] = i * cols + j;
        }
    }
}
```

### 2.3 一维数组模拟二维矩阵
```java
/**
 * 使用一维数组模拟二维矩阵
 * 优点：内存连续，缓存友好
 * 缺点：索引计算复杂
 */
public class Matrix1D {
    private int[] data;
    private int rows, cols;
    
    public Matrix1D(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.data = new int[rows * cols];
    }
    
    // 二维索引转一维索引
    private int getIndex(int row, int col) {
        return row * cols + col;
    }
    
    // 获取元素
    public int get(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException();
        }
        return data[getIndex(row, col)];
    }
    
    // 设置元素
    public void set(int row, int col, int value) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException();
        }
        data[getIndex(row, col)] = value;
    }
    
    // 按行访问（缓存友好）
    public void traverseByRow() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(get(i, j) + " ");
            }
            System.out.println();
        }
    }
}
```

## 3. 矩阵的基本操作

### 3.1 矩阵遍历
```java
/**
 * 矩阵遍历的各种方式
 */
public class MatrixTraversal {
    
    // 按行遍历
    public static void traverseByRow(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
    
    // 按列遍历
    public static void traverseByColumn(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        
        for (int j = 0; j < cols; j++) {
            for (int i = 0; i < rows; i++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
    
    // 对角线遍历（主对角线）
    public static void traverseMainDiagonal(int[][] matrix) {
        int n = Math.min(matrix.length, matrix[0].length);
        for (int i = 0; i < n; i++) {
            System.out.print(matrix[i][i] + " ");
        }
        System.out.println();
    }
    
    // 反对角线遍历
    public static void traverseAntiDiagonal(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int n = Math.min(rows, cols);
        
        for (int i = 0; i < n; i++) {
            System.out.print(matrix[i][cols - 1 - i] + " ");
        }
        System.out.println();
    }
    
    // 螺旋遍历（顺时针）
    public static void spiralTraverse(int[][] matrix) {
        if (matrix.length == 0) return;
        
        int top = 0, bottom = matrix.length - 1;
        int left = 0, right = matrix[0].length - 1;
        
        while (top <= bottom && left <= right) {
            // 从左到右
            for (int j = left; j <= right; j++) {
                System.out.print(matrix[top][j] + " ");
            }
            top++;
            
            // 从上到下
            for (int i = top; i <= bottom; i++) {
                System.out.print(matrix[i][right] + " ");
            }
            right--;
            
            // 从右到左
            if (top <= bottom) {
                for (int j = right; j >= left; j--) {
                    System.out.print(matrix[bottom][j] + " ");
                }
                bottom--;
            }
            
            // 从下到上
            if (left <= right) {
                for (int i = bottom; i >= top; i--) {
                    System.out.print(matrix[i][left] + " ");
                }
                left++;
            }
        }
        System.out.println();
    }
}
```

### 3.2 矩阵创建和初始化
```java
/**
 * 矩阵创建和初始化的实用方法
 */
public class MatrixUtils {
    
    // 创建零矩阵
    public static int[][] createZeroMatrix(int rows, int cols) {
        return new int[rows][cols];  // Java中int数组默认初始化为0
    }
    
    // 创建单位矩阵
    public static int[][] createIdentityMatrix(int n) {
        int[][] matrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            matrix[i][i] = 1;
        }
        return matrix;
    }
    
    // 创建随机矩阵
    public static int[][] createRandomMatrix(int rows, int cols, int maxValue) {
        int[][] matrix = new int[rows][cols];
        Random random = new Random();
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextInt(maxValue + 1);
            }
        }
        return matrix;
    }
    
    // 填充矩阵
    public static void fillMatrix(int[][] matrix, int value) {
        for (int i = 0; i < matrix.length; i++) {
            Arrays.fill(matrix[i], value);
        }
    }
    
    // 从一维数组创建矩阵
    public static int[][] createMatrixFromArray(int[] array, int rows, int cols) {
        if (array.length != rows * cols) {
            throw new IllegalArgumentException("数组长度与矩阵大小不匹配");
        }
        
        int[][] matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = array[i * cols + j];
            }
        }
        return matrix;
    }
}
```

### 3.3 矩阵拷贝
```java
/**
 * 矩阵拷贝的各种方法
 */
public class MatrixCopy {
    
    // 浅拷贝（只拷贝引用）
    public static int[][] shallowCopy(int[][] matrix) {
        return matrix.clone();  // 只拷贝第一层引用
    }
    
    // 深拷贝（完全拷贝）
    public static int[][] deepCopy(int[][] matrix) {
        int rows = matrix.length;
        int[][] copy = new int[rows][];
        
        for (int i = 0; i < rows; i++) {
            copy[i] = matrix[i].clone();  // 拷贝每一行
        }
        return copy;
    }
    
    // 使用Arrays.copyOf进行深拷贝
    public static int[][] deepCopyWithArrays(int[][] matrix) {
        int rows = matrix.length;
        int[][] copy = new int[rows][];
        
        for (int i = 0; i < rows; i++) {
            copy[i] = Arrays.copyOf(matrix[i], matrix[i].length);
        }
        return copy;
    }
    
    // 手动深拷贝
    public static int[][] manualDeepCopy(int[][] matrix) {
        int rows = matrix.length;
        int[][] copy = new int[rows][];
        
        for (int i = 0; i < rows; i++) {
            copy[i] = new int[matrix[i].length];
            System.arraycopy(matrix[i], 0, copy[i], 0, matrix[i].length);
        }
        return copy;
    }
    
    // 演示浅拷贝和深拷贝的区别
    public static void demonstrateCopyDifference() {
        int[][] original = {{1, 2}, {3, 4}};
        
        // 浅拷贝
        int[][] shallow = shallowCopy(original);
        shallow[0][0] = 999;
        System.out.println("浅拷贝后原矩阵[0][0]: " + original[0][0]);  // 输出: 999
        
        // 深拷贝
        original[0][0] = 1;  // 重置
        int[][] deep = deepCopy(original);
        deep[0][0] = 888;
        System.out.println("深拷贝后原矩阵[0][0]: " + original[0][0]);   // 输出: 1
    }
}
```

## 4. 矩阵的特殊形式

### 4.1 方阵（Square Matrix）
```java
/**
 * 方阵：行数等于列数的矩阵
 */
public class SquareMatrix {
    private int[][] matrix;
    private int size;
    
    public SquareMatrix(int size) {
        this.size = size;
        this.matrix = new int[size][size];
    }
    
    // 检查是否为对称矩阵
    public boolean isSymmetric() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (matrix[i][j] != matrix[j][i]) {
                    return false;
                }
            }
        }
        return true;
    }
    
    // 检查是否为对角矩阵
    public boolean isDiagonal() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i != j && matrix[i][j] != 0) {
                    return false;
                }
            }
        }
        return true;
    }
    
    // 检查是否为单位矩阵
    public boolean isIdentity() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j && matrix[i][j] != 1) {
                    return false;
                }
                if (i != j && matrix[i][j] != 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
```

### 4.2 三角矩阵
```java
/**
 * 三角矩阵的表示和操作
 */
public class TriangularMatrix {
    
    // 上三角矩阵（下三角部分为0）
    public static boolean isUpperTriangular(int[][] matrix) {
        int n = matrix.length;
        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (matrix[i][j] != 0) {
                    return false;
                }
            }
        }
        return true;
    }
    
    // 下三角矩阵（上三角部分为0）
    public static boolean isLowerTriangular(int[][] matrix) {
        int n = matrix.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (matrix[i][j] != 0) {
                    return false;
                }
            }
        }
        return true;
    }
    
    // 创建上三角矩阵
    public static int[][] createUpperTriangular(int n) {
        int[][] matrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                matrix[i][j] = i + j + 1;  // 示例填充
            }
        }
        return matrix;
    }
}
```

## 5. 矩阵与数组的对比

### 5.1 存储效率对比

| 特性 | 一维数组 | 二维数组 | 压缩存储 |
|------|---------|---------|----------|
| **内存连续性** | 完全连续 | 行内连续 | 完全连续 |
| **缓存友好性** | 最好 | 较好 | 最好 |
| **索引复杂度** | 简单 | 简单 | 复杂 |
| **空间利用率** | 100% | 100% | 取决于稀疏度 |

### 5.2 访问模式性能
```java
/**
 * 不同访问模式的性能测试
 */
public class MatrixPerformanceTest {
    
    public static void testAccessPatterns() {
        int size = 1000;
        int[][] matrix = new int[size][size];
        
        // 初始化矩阵
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = i * size + j;
            }
        }
        
        // 测试按行访问
        long startTime = System.nanoTime();
        long sum1 = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                sum1 += matrix[i][j];
            }
        }
        long rowTime = System.nanoTime() - startTime;
        
        // 测试按列访问
        startTime = System.nanoTime();
        long sum2 = 0;
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                sum2 += matrix[i][j];
            }
        }
        long colTime = System.nanoTime() - startTime;
        
        System.out.println("按行访问时间: " + rowTime + " ns");
        System.out.println("按列访问时间: " + colTime + " ns");
        System.out.println("性能差异: " + (double)colTime / rowTime + "x");
    }
}
```

## 6. 实际应用场景

### 6.1 图像处理
```java
/**
 * 图像处理中的矩阵应用
 */
public class ImageMatrix {
    private int[][] pixels;  // 灰度图像像素矩阵
    
    // 图像滤波（3x3均值滤波）
    public int[][] meanFilter() {
        int rows = pixels.length;
        int cols = pixels[0].length;
        int[][] filtered = new int[rows][cols];
        
        for (int i = 1; i < rows - 1; i++) {
            for (int j = 1; j < cols - 1; j++) {
                int sum = 0;
                // 3x3邻域求平均
                for (int di = -1; di <= 1; di++) {
                    for (int dj = -1; dj <= 1; dj++) {
                        sum += pixels[i + di][j + dj];
                    }
                }
                filtered[i][j] = sum / 9;
            }
        }
        return filtered;
    }
}
```

### 6.2 游戏地图
```java
/**
 * 游戏地图的矩阵表示
 */
public class GameMap {
    private int[][] map;
    private static final int EMPTY = 0;
    private static final int WALL = 1;
    private static final int PLAYER = 2;
    
    public GameMap(int rows, int cols) {
        this.map = new int[rows][cols];
    }
    
    // 检查坐标是否有效
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < map.length && 
               col >= 0 && col < map[0].length &&
               map[row][col] != WALL;
    }
    
    // 获取邻居位置
    public List<int[]> getNeighbors(int row, int col) {
        List<int[]> neighbors = new ArrayList<>();
        int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            if (isValidPosition(newRow, newCol)) {
                neighbors.add(new int[]{newRow, newCol});
            }
        }
        return neighbors;
    }
}
```