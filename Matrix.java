import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class Matrix {
    private final int[][] data;
    private final int rows;
    private final int cols;
    
    // Threshold for Strassen's algorithm. It's only beneficial for larger matrices.
    private static final int MINIMUM_STRASSEN_THRESHOLD = 64;
    
    public Matrix(int[][] data) {
        this.data = deepCopy(data);
        this.rows = data.length;
        this.cols = data[0].length;
    }
    
    // Creating a deep copy to avoid side-effects due to references
    private static int[][] deepCopy(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return copy;
    }
    
    public int[][] getData() {
        return deepCopy(data); // Return a deep copy to maintain encapsulation
    }

    public int getRows() {
        return rows;
    }
    
    public int getCols() {
        return cols;
    }
    
    public void display() {
        Arrays.stream(data).map(Arrays::toString).forEach(System.out::println);
    }
    
    public static Matrix multiply(Matrix a, Matrix b, boolean parallel) {
        if (a.getCols() != b.getRows()) {
            throw new IllegalArgumentException("Matrix dimensions incompatible for multiplication");
        }
        
        int[][] result = new int[a.getRows()][b.getCols()];
        
        if (parallel) {
            // Parallelizing initialization of result matrix to make it thread-safe
            Arrays.stream(result).parallel().forEach(row -> Arrays.fill(row, 0));
            
            // Start parallel multiplication using ForkJoinPool for better parallelism
            ForkJoinPool.commonPool().invoke(new ParallelMultiplication(a.data, b.data, result, 0, a.getRows()));
        } else {
            for (int i = 0; i < a.getRows(); i++) {
                for (int j = 0; j < b.getCols(); j++) {
                    int sum = 0;
                    for (int k = 0; k < a.getCols(); k++) {
                        sum += a.data[i][k] * b.data[k][j];
                    }
                    result[i][j] = sum;
                }
            }
        }
        
        return new Matrix(result);
    }
    
    private static class ParallelMultiplication extends RecursiveTask<Void> {
        private final int[][] a;
        private final int[][] b;
        private final int[][] result;
        private final int startRow;
        private final int endRow;
        private static final int THRESHOLD = 64; // If the submatrix is small enough, do direct multiplication to avoid overhead
        
        ParallelMultiplication(int[][] a, int[][] b, int[][] result, int startRow, int endRow) {
            this.a = a;
            this.b = b;
            this.result = result;
            this.startRow = startRow;
            this.endRow = endRow;
        }
        
        @Override
        protected Void compute() {
            if (endRow - startRow <= THRESHOLD) {
                computeDirectly();
                return null;
            }
            
            int midRow = (startRow + endRow) / 2;
            ParallelMultiplication left = new ParallelMultiplication(a, b, result, startRow, midRow);
            ParallelMultiplication right = new ParallelMultiplication(a, b, result, midRow, endRow);
            
            right.fork();
            left.compute();
            right.join();
            
            return null;
        }
        
        private void computeDirectly() {
            for (int i = startRow; i < endRow; i++) {
                for (int j = 0; j < b[0].length; j++) {
                    int sum = 0;
                    for (int k = 0; k < b.length; k++) {
                        sum += a[i][k] * b[k][j];
                    }
                    result[i][j] = sum;
                }
            }
        }
    }
    
    public static Matrix strassenMultiply(Matrix a, Matrix b) {
        if (a.getCols() != b.getRows()) {
            throw new IllegalArgumentException("Matrix dimensions incompatible for multiplication");
        }
        
        int maxDim = Math.max(Math.max(a.getRows(), a.getCols()), Math.max(b.getRows(), b.getCols()));
        int paddedSize = nextPowerOfTwo(maxDim); // Padding to the next power of two to optimize Strassen's method
        
        int[][] paddedA = padMatrix(a.data, paddedSize);
        int[][] paddedB = padMatrix(b.data, paddedSize);
        int[][] paddedResult = strassenMultiplyRecursive(paddedA, paddedB);
        
        return new Matrix(extractResult(paddedResult, a.getRows(), b.getCols()));
    }
    
    private static int[][] strassenMultiplyRecursive(int[][] a, int[][] b) {
        int n = a.length;
        
        // Base case: standard multiplication for small matrices
        if (n <= MINIMUM_STRASSEN_THRESHOLD) {
            return standardMultiply(a, b);
        }
        
        int newSize = n / 2;
        
        int[][] a11 = new int[newSize][newSize];
        int[][] a12 = new int[newSize][newSize];
        int[][] a21 = new int[newSize][newSize];
        int[][] a22 = new int[newSize][newSize];
        int[][] b11 = new int[newSize][newSize];
        int[][] b12 = new int[newSize][newSize];
        int[][] b21 = new int[newSize][newSize];
        int[][] b22 = new int[newSize][newSize];
        
        divideMatrix(a, a11, a12, a21, a22);
        divideMatrix(b, b11, b12, b21, b22);
        
        // Calculate the 7 matrix multiplications (M1 to M7) using Strassen's method
        int[][] m1 = strassenMultiplyRecursive(addMatrices(a11, a22), addMatrices(b11, b22));
        int[][] m2 = strassenMultiplyRecursive(addMatrices(a21, a22), b11);
        int[][] m3 = strassenMultiplyRecursive(a11, subtractMatrices(b12, b22));
        int[][] m4 = strassenMultiplyRecursive(a22, subtractMatrices(b21, b11));
        int[][] m5 = strassenMultiplyRecursive(addMatrices(a11, a12), b22);
        int[][] m6 = strassenMultiplyRecursive(subtractMatrices(a21, a11), addMatrices(b11, b12));
        int[][] m7 = strassenMultiplyRecursive(subtractMatrices(a12, a22), addMatrices(b21, b22));
        
        int[][] c11 = addMatrices(subtractMatrices(addMatrices(m1, m4), m5), m7);
        int[][] c12 = addMatrices(m3, m5);
        int[][] c21 = addMatrices(m2, m4);
        int[][] c22 = addMatrices(subtractMatrices(addMatrices(m1, m3), m2), m6);
        
        return combineMatrices(c11, c12, c21, c22);
    }
    
    private static int nextPowerOfTwo(int n) {
        return (int) Math.pow(2, Math.ceil(Math.log(n) / Math.log(2)));
    }
    
    private static int[][] padMatrix(int[][] matrix, int size) {
        int[][] padded = new int[size][size];
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, padded[i], 0, matrix[i].length);
        }
        return padded;
    }
    
    private static int[][] extractResult(int[][] padded, int rows, int cols) {
        int[][] result = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(padded[i], 0, result[i], 0, cols);
        }
        return result;
    }
    
    private static int[][] standardMultiply(int[][] a, int[][] b) {
        int n = a.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }
    
    private static void divideMatrix(int[][] matrix, int[][] a11, int[][] a12, int[][] a21, int[][] a22) {
        int size = matrix.length / 2;
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                a11[i][j] = matrix[i][j];
                a12[i][j] = matrix[i][j + size];
                a21[i][j] = matrix[i + size][j];
                a22[i][j] = matrix[i + size][j + size];
            }
        }
    }
    
    private static int[][] addMatrices(int[][] a, int[][] b) {
        int n = a.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = a[i][j] + b[i][j];
            }
        }
        return result;
    }
    
    private static int[][] subtractMatrices(int[][] a, int[][] b) {
        int n = a.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = a[i][j] - b[i][j];
            }
        }
        return result;
    }
    
    private static int[][] combineMatrices(int[][] c11, int[][] c12, int[][] c21, int[][] c22) {
        int n = c11.length * 2;
        int[][] result = new int[n][n];
        
        for (int i = 0; i < c11.length; i++) {
            for (int j = 0; j < c11.length; j++) {
                result[i][j] = c11[i][j];
                result[i][j + c11.length] = c12[i][j];
                result[i + c11.length][j] = c21[i][j];
                result[i + c11.length][j + c11.length] = c22[i][j];
            }
        }
        return result;
    }
}
