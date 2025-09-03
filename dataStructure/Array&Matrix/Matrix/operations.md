# 矩阵常见操作详解

## 1. 矩阵转置 (Transpose)

### 1.1 矩阵转置基础
矩阵转置是将矩阵的行和列互换，即 A^T[i][j] = A[j][i]

```java
/**
 * 矩阵转置 - 时间复杂度: O(m*n)，空间复杂度: O(m*n)
 */
public static int[][] transpose(int[][] matrix) {
    int rows = matrix.length;
    int cols = matrix[0].length;
    
    // 创建转置后的矩阵
    int[][] transposed = new int[cols][rows];
    
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            transposed[j][i] = matrix[i][j];
        }
    }
    
    return transposed;
}

// 使用示例
int[][] matrix = {{1, 2, 3}, {4, 5, 6}};
int[][] result = transpose(matrix);
// 原矩阵: [[1, 2, 3], [4, 5, 6]]
// 转置后: [[1, 4], [2, 5], [3, 6]]
```

### 1.2 方阵原地转置
对于方阵（n×n矩阵），可以实现原地转置，节省空间。

```java
/**
 * 方阵原地转置 - 时间复杂度: O(n²)，空间复杂度: O(1)
 */
public static void transposeInPlace(int[][] matrix) {
    int n = matrix.length;
    
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {  // 只处理上三角部分
            // 交换对称位置的元素
            int temp = matrix[i][j];
            matrix[i][j] = matrix[j][i];
            matrix[j][i] = temp;
        }
    }
}

// 使用示例
int[][] squareMatrix = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
transposeInPlace(squareMatrix);
// 转置后: [[1, 4, 7], [2, 5, 8], [3, 6, 9]]
```

### 1.3 对角线转置
```java
/**
 * 沿主对角线转置（普通转置）
 */
public static void transposeMainDiagonal(int[][] matrix) {
    transposeInPlace(matrix);  // 与普通转置相同
}

/**
 * 沿反对角线转置
 */
public static void transposeAntiDiagonal(int[][] matrix) {
    int n = matrix.length;
    
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n - i - 1; j++) {
            // 交换关于反对角线对称的元素
            int temp = matrix[i][j];
            matrix[i][j] = matrix[n - 1 - j][n - 1 - i];
            matrix[n - 1 - j][n - 1 - i] = temp;
        }
    }
}
```

## 2. 矩阵旋转 (Rotation)

### 2.1 90度顺时针旋转
```java
/**
 * 90度顺时针旋转 - 时间复杂度: O(n²)，空间复杂度: O(n²)
 * 方法：先转置，再水平翻转
 */
public static int[][] rotateClockwise90(int[][] matrix) {
    int n = matrix.length;
    int[][] rotated = new int[n][n];
    
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            rotated[j][n - 1 - i] = matrix[i][j];
        }
    }
    
    return rotated;
}

/**
 * 原地90度顺时针旋转 - 空间复杂度: O(1)
 * 分解步骤：1. 转置  2. 水平翻转每一行
 */
public static void rotateClockwise90InPlace(int[][] matrix) {
    int n = matrix.length;
    
    // 步骤1：转置矩阵
    transposeInPlace(matrix);
    
    // 步骤2：水平翻转每一行
    for (int i = 0; i < n; i++) {
        reverseRow(matrix[i]);
    }
}

/**
 * 反转数组（水平翻转）
 */
private static void reverseRow(int[] row) {
    int left = 0, right = row.length - 1;
    while (left < right) {
        int temp = row[left];
        row[left] = row[right];
        row[right] = temp;
        left++;
        right--;
    }
}

// 使用示例
int[][] matrix = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
rotateClockwise90InPlace(matrix);
// 旋转前: [[1, 2, 3], [4, 5, 6], [7, 8, 9]]
// 旋转后: [[7, 4, 1], [8, 5, 2], [9, 6, 3]]
```

### 2.2 其他角度旋转
```java
/**
 * 90度逆时针旋转
 */
public static void rotateCounterClockwise90(int[][] matrix) {
    int n = matrix.length;
    
    // 方法：先水平翻转，再转置
    // 步骤1：水平翻转每一行
    for (int i = 0; i < n; i++) {
        reverseRow(matrix[i]);
    }
    
    // 步骤2：转置矩阵
    transposeInPlace(matrix);
}

/**
 * 180度旋转
 */
public static void rotate180(int[][] matrix) {
    int rows = matrix.length;
    int cols = matrix[0].length;
    
    for (int i = 0; i < rows / 2; i++) {
        for (int j = 0; j < cols; j++) {
            int temp = matrix[i][j];
            matrix[i][j] = matrix[rows - 1 - i][cols - 1 - j];
            matrix[rows - 1 - i][cols - 1 - j] = temp;
        }
    }
    
    // 如果是奇数行，处理中间行
    if (rows % 2 == 1) {
        int midRow = rows / 2;
        for (int j = 0; j < cols / 2; j++) {
            int temp = matrix[midRow][j];
            matrix[midRow][j] = matrix[midRow][cols - 1 - j];
            matrix[midRow][cols - 1 - j] = temp;
        }
    }
}

/**
 * 四层同心旋转（经典原地旋转算法）
 */
public static void rotateByLayers(int[][] matrix) {
    int n = matrix.length;
    
    for (int layer = 0; layer < n / 2; layer++) {
        int first = layer;
        int last = n - 1 - layer;
        
        for (int i = first; i < last; i++) {
            int offset = i - first;
            
            // 保存上边
            int top = matrix[first][i];
            
            // 左 -> 上
            matrix[first][i] = matrix[last - offset][first];
            
            // 下 -> 左
            matrix[last - offset][first] = matrix[last][last - offset];
            
            // 右 -> 下
            matrix[last][last - offset] = matrix[i][last];
            
            // 上 -> 右
            matrix[i][last] = top;
        }
    }
}
```

## 3. 矩阵翻转 (Flip)

### 3.1 水平翻转（左右翻转）
```java
/**
 * 水平翻转（左右翻转）- 时间复杂度: O(m*n)
 */
public static void flipHorizontal(int[][] matrix) {
    int rows = matrix.length;
    int cols = matrix[0].length;
    
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols / 2; j++) {
            int temp = matrix[i][j];
            matrix[i][j] = matrix[i][cols - 1 - j];
            matrix[i][cols - 1 - j] = temp;
        }
    }
}

/**
 * 更简洁的水平翻转实现
 */
public static void flipHorizontalSimple(int[][] matrix) {
    for (int i = 0; i < matrix.length; i++) {
        reverseRow(matrix[i]);
    }
}
```

### 3.2 垂直翻转（上下翻转）
```java
/**
 * 垂直翻转（上下翻转）- 时间复杂度: O(m*n)
 */
public static void flipVertical(int[][] matrix) {
    int rows = matrix.length;
    int cols = matrix[0].length;
    
    for (int i = 0; i < rows / 2; i++) {
        for (int j = 0; j < cols; j++) {
            int temp = matrix[i][j];
            matrix[i][j] = matrix[rows - 1 - i][j];
            matrix[rows - 1 - i][j] = temp;
        }
    }
}

/**
 * 使用行交换的垂直翻转
 */
public static void flipVerticalByRows(int[][] matrix) {
    int rows = matrix.length;
    
    for (int i = 0; i < rows / 2; i++) {
        int[] temp = matrix[i];
        matrix[i] = matrix[rows - 1 - i];
        matrix[rows - 1 - i] = temp;
    }
}
```

### 3.3 对角线翻转
```java
/**
 * 沿主对角线翻转（等同于转置）
 */
public static void flipMainDiagonal(int[][] matrix) {
    transposeInPlace(matrix);
}

/**
 * 沿反对角线翻转
 */
public static void flipAntiDiagonal(int[][] matrix) {
    int n = matrix.length;
    
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n - i - 1; j++) {
            int temp = matrix[i][j];
            matrix[i][j] = matrix[n - 1 - j][n - 1 - i];
            matrix[n - 1 - j][n - 1 - i] = temp;
        }
    }
}
```

## 4. 矩阵压缩存储

### 4.1 对称矩阵压缩存储
对称矩阵只需存储上三角或下三角部分。

```java
/**
 * 对称矩阵的压缩存储
 * 只存储上三角部分（包括主对角线）
 */
public class SymmetricMatrix {
    private int[] data;
    private int size;
    
    public SymmetricMatrix(int n) {
        this.size = n;
        // 上三角元素个数：n(n+1)/2
        this.data = new int[n * (n + 1) / 2];
    }
    
    /**
     * 二维索引转一维索引（上三角存储）
     * 公式：index = i*(i+1)/2 + j  (其中 i <= j)
     */
    private int getIndex(int i, int j) {
        if (i > j) {
            // 利用对称性，交换i和j
            int temp = i;
            i = j;
            j = temp;
        }
        return i * (i + 1) / 2 + j;
    }
    
    public int get(int i, int j) {
        return data[getIndex(i, j)];
    }
    
    public void set(int i, int j, int value) {
        data[getIndex(i, j)] = value;
    }
    
    /**
     * 从完整矩阵创建对称矩阵
     */
    public static SymmetricMatrix fromMatrix(int[][] matrix) {
        int n = matrix.length;
        SymmetricMatrix sym = new SymmetricMatrix(n);
        
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {  // 只存储上三角
                sym.set(i, j, matrix[i][j]);
            }
        }
        return sym;
    }
    
    /**
     * 空间压缩率计算
     */
    public double getCompressionRatio() {
        int originalSize = size * size;
        int compressedSize = size * (size + 1) / 2;
        return (double) compressedSize / originalSize;
    }
}
```

### 4.2 三角矩阵压缩存储
```java
/**
 * 上三角矩阵压缩存储
 * 下三角部分都为0，只存储上三角部分
 */
public class UpperTriangularMatrix {
    private int[] data;
    private int size;
    
    public UpperTriangularMatrix(int n) {
        this.size = n;
        this.data = new int[n * (n + 1) / 2];
    }
    
    /**
     * 上三角矩阵索引计算
     * 按行存储：第i行有(n-i)个元素
     * 前i行总元素：sum = n + (n-1) + ... + (n-i+1) = i*n - i*(i-1)/2
     */
    private int getIndex(int i, int j) {
        if (i > j) {
            throw new IllegalArgumentException("下三角元素为0");
        }
        return i * size - i * (i - 1) / 2 + (j - i);
    }
    
    public int get(int i, int j) {
        if (i > j) return 0;  // 下三角为0
        return data[getIndex(i, j)];
    }
    
    public void set(int i, int j, int value) {
        if (i > j) {
            throw new IllegalArgumentException("不能设置下三角元素");
        }
        data[getIndex(i, j)] = value;
    }
}

/**
 * 下三角矩阵压缩存储
 */
public class LowerTriangularMatrix {
    private int[] data;
    private int size;
    
    public LowerTriangularMatrix(int n) {
        this.size = n;
        this.data = new int[n * (n + 1) / 2];
    }
    
    /**
     * 下三角矩阵索引计算
     * 按行存储：第i行有(i+1)个元素
     * 前i行总元素：sum = 1 + 2 + ... + i = i*(i+1)/2
     */
    private int getIndex(int i, int j) {
        if (i < j) {
            throw new IllegalArgumentException("上三角元素为0");
        }
        return i * (i + 1) / 2 + j;
    }
    
    public int get(int i, int j) {
        if (i < j) return 0;  // 上三角为0
        return data[getIndex(i, j)];
    }
    
    public void set(int i, int j, int value) {
        if (i < j) {
            throw new IllegalArgumentException("不能设置上三角元素");
        }
        data[getIndex(i, j)] = value;
    }
}
```

### 4.3 带状矩阵压缩存储
```java
/**
 * 三对角矩阵（特殊的带状矩阵）
 * 只有主对角线和其上下相邻的对角线有非零元素
 */
public class TridiagonalMatrix {
    private int[] mainDiag;     // 主对角线
    private int[] upperDiag;    // 上对角线
    private int[] lowerDiag;    // 下对角线
    private int size;
    
    public TridiagonalMatrix(int n) {
        this.size = n;
        this.mainDiag = new int[n];
        this.upperDiag = new int[n - 1];
        this.lowerDiag = new int[n - 1];
    }
    
    public int get(int i, int j) {
        if (i == j) {
            return mainDiag[i];
        } else if (i == j - 1) {
            return upperDiag[i];
        } else if (i == j + 1) {
            return lowerDiag[j];
        } else {
            return 0;  // 其他位置都为0
        }
    }
    
    public void set(int i, int j, int value) {
        if (i == j) {
            mainDiag[i] = value;
        } else if (i == j - 1) {
            upperDiag[i] = value;
        } else if (i == j + 1) {
            lowerDiag[j] = value;
        } else {
            throw new IllegalArgumentException("不能设置带外元素");
        }
    }
    
    /**
     * 空间压缩效果
     */
    public double getCompressionRatio() {
        int originalSize = size * size;
        int compressedSize = 3 * size - 2;
        return (double) compressedSize / originalSize;
    }
}
```

## 5. 矩阵的高级操作

### 5.1 矩阵乘法
```java
/**
 * 标准矩阵乘法 - 时间复杂度: O(n³)
 */
public static int[][] multiply(int[][] A, int[][] B) {
    int m = A.length;        // A的行数
    int n = A[0].length;     // A的列数 = B的行数
    int p = B[0].length;     // B的列数
    
    if (n != B.length) {
        throw new IllegalArgumentException("矩阵维度不匹配");
    }
    
    int[][] C = new int[m][p];
    
    for (int i = 0; i < m; i++) {
        for (int j = 0; j < p; j++) {
            for (int k = 0; k < n; k++) {
                C[i][j] += A[i][k] * B[k][j];
            }
        }
    }
    
    return C;
}

/**
 * 缓存友好的矩阵乘法（分块算法）
 */
public static int[][] multiplyBlocked(int[][] A, int[][] B, int blockSize) {
    int n = A.length;
    int[][] C = new int[n][n];
    
    for (int i0 = 0; i0 < n; i0 += blockSize) {
        for (int j0 = 0; j0 < n; j0 += blockSize) {
            for (int k0 = 0; k0 < n; k0 += blockSize) {
                
                // 处理一个块
                for (int i = i0; i < Math.min(i0 + blockSize, n); i++) {
                    for (int j = j0; j < Math.min(j0 + blockSize, n); j++) {
                        for (int k = k0; k < Math.min(k0 + blockSize, n); k++) {
                            C[i][j] += A[i][k] * B[k][j];
                        }
                    }
                }
            }
        }
    }
    
    return C;
}
```

### 5.2 矩阵加法和减法
```java
/**
 * 矩阵加法 - 时间复杂度: O(m*n)
 */
public static int[][] add(int[][] A, int[][] B) {
    int rows = A.length;
    int cols = A[0].length;
    
    if (rows != B.length || cols != B[0].length) {
        throw new IllegalArgumentException("矩阵维度不匹配");
    }
    
    int[][] result = new int[rows][cols];
    
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            result[i][j] = A[i][j] + B[i][j];
        }
    }
    
    return result;
}

/**
 * 矩阵减法 - 时间复杂度: O(m*n)
 */
public static int[][] subtract(int[][] A, int[][] B) {
    int rows = A.length;
    int cols = A[0].length;
    
    if (rows != B.length || cols != B[0].length) {
        throw new IllegalArgumentException("矩阵维度不匹配");
    }
    
    int[][] result = new int[rows][cols];
    
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            result[i][j] = A[i][j] - B[i][j];
        }
    }
    
    return result;
}

/**
 * 标量乘法
 */
public static int[][] scalarMultiply(int[][] matrix, int scalar) {
    int rows = matrix.length;
    int cols = matrix[0].length;
    int[][] result = new int[rows][cols];
    
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            result[i][j] = matrix[i][j] * scalar;
        }
    }
    
    return result;
}
```

### 5.3 矩阵的统计操作
```java
/**
 * 矩阵统计操作集合
 */
public class MatrixStatistics {
    
    // 矩阵求和
    public static int sum(int[][] matrix) {
        int total = 0;
        for (int[] row : matrix) {
            for (int value : row) {
                total += value;
            }
        }
        return total;
    }
    
    // 行求和
    public static int[] rowSums(int[][] matrix) {
        int rows = matrix.length;
        int[] sums = new int[rows];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                sums[i] += matrix[i][j];
            }
        }
        return sums;
    }
    
    // 列求和
    public static int[] columnSums(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int[] sums = new int[cols];
        
        for (int j = 0; j < cols; j++) {
            for (int i = 0; i < rows; i++) {
                sums[j] += matrix[i][j];
            }
        }
        return sums;
    }
    
    // 对角线和
    public static int mainDiagonalSum(int[][] matrix) {
        int sum = 0;
        int n = Math.min(matrix.length, matrix[0].length);
        
        for (int i = 0; i < n; i++) {
            sum += matrix[i][i];
        }
        return sum;
    }
    
    // 反对角线和
    public static int antiDiagonalSum(int[][] matrix) {
        int sum = 0;
        int rows = matrix.length;
        int cols = matrix[0].length;
        int n = Math.min(rows, cols);
        
        for (int i = 0; i < n; i++) {
            sum += matrix[i][cols - 1 - i];
        }
        return sum;
    }
    
    // 找最大值及其位置
    public static class MaxResult {
        public final int value;
        public final int row;
        public final int col;
        
        public MaxResult(int value, int row, int col) {
            this.value = value;
            this.row = row;
            this.col = col;
        }
    }
    
    public static MaxResult findMax(int[][] matrix) {
        int maxValue = matrix[0][0];
        int maxRow = 0, maxCol = 0;
        
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] > maxValue) {
                    maxValue = matrix[i][j];
                    maxRow = i;
                    maxCol = j;
                }
            }
        }
        
        return new MaxResult(maxValue, maxRow, maxCol);
    }
}
```

## 6. 实用工具类

### 6.1 矩阵打印和格式化
```java
/**
 * 矩阵显示工具类
 */
public class MatrixPrinter {
    
    // 简单打印矩阵
    public static void print(int[][] matrix) {
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }
    
    // 格式化打印矩阵（对齐）
    public static void printFormatted(int[][] matrix) {
        // 找到最大数字的宽度
        int maxWidth = 0;
        for (int[] row : matrix) {
            for (int value : row) {
                maxWidth = Math.max(maxWidth, String.valueOf(value).length());
            }
        }
        
        String format = "%" + (maxWidth + 1) + "d";
        
        for (int[] row : matrix) {
            for (int value : row) {
                System.out.printf(format, value);
            }
            System.out.println();
        }
    }
    
    // 打印矩阵边框
    public static void printWithBorder(int[][] matrix) {
        if (matrix.length == 0) return;
        
        int cols = matrix[0].length;
        String border = "+" + "-".repeat(cols * 4 + 1) + "+";
        
        System.out.println(border);
        for (int[] row : matrix) {
            System.out.print("|");
            for (int value : row) {
                System.out.printf("%4d", value);
            }
            System.out.println(" |");
        }
        System.out.println(border);
    }
}
```

### 6.2 矩阵验证工具
```java
/**
 * 矩阵验证工具类
 */
public class MatrixValidator {
    
    // 检查是否为矩形矩阵
    public static boolean isRectangular(int[][] matrix) {
        if (matrix.length == 0) return true;
        
        int expectedCols = matrix[0].length;
        for (int[] row : matrix) {
            if (row.length != expectedCols) {
                return false;
            }
        }
        return true;
    }
    
    // 检查矩阵维度是否匹配（用于乘法）
    public static boolean canMultiply(int[][] A, int[][] B) {
        return isRectangular(A) && isRectangular(B) && 
               A.length > 0 && B.length > 0 &&
               A[0].length == B.length;
    }
    
    // 检查矩阵维度是否匹配（用于加法）
    public static boolean canAdd(int[][] A, int[][] B) {
        return A.length == B.length && 
               A.length > 0 && A[0].length == B[0].length;
    }
    
    // 检查是否为方阵
    public static boolean isSquare(int[][] matrix) {
        return isRectangular(matrix) && 
               matrix.length > 0 && 
               matrix.length == matrix[0].length;
    }
}
```