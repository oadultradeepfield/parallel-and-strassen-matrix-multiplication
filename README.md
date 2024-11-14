# 🧮 Parallel Matrix Multiplication and Strassen's Algorithm

A Java implementation of matrix multiplication algorithms, featuring both standard and Strassen's algorithm with parallel processing capabilities.

![Performance analysis of matrix multiplication algorithms. Note that Strassen's algorithm requires adjusting matrix dimensions to the nearest power of two. For any matrix of size n, where 2^k-1 < n ≤ 2^k, the runtime will be identical to a matrix of size 2^k, as the matrix must be padded to the next power of two.](https://github.com/oadultradeepfield/parallel-and-strassen-matrix-multiplication/blob/150c9e6a2f6fdcd8091dbe78297230a4a3aca370/performance_analysis.png?raw=true)

## ✨ Features

- Standard matrix multiplication with parallel processing
- Strassen's algorithm for large matrices
- Automatic optimization between standard and Strassen's algorithm
- Support for matrices of any size
- Parallel processing using Java's Fork/Join framework

## ⚙️ Implementation Details

The implementation includes two main multiplication approaches:

1. **Standard Multiplication**: Traditional matrix multiplication algorithm with parallel processing support
2. **Strassen's Algorithm**: Optimized multiplication for large matrices using Strassen's divide-and-conquer approach

**Key optimizations:**

- Automatic selection between standard and Strassen's algorithm based on matrix size
- Parallel processing for improved performance on multi-core systems
- Memory-efficient implementation for large matrices

## 🚀 Usage Example

```java
int[][] a = {{1, 2}, {3, 4}};
int[][] b = {{5, 6}, {7, 8}};
Matrix ma = new Matrix(a);
Matrix mb = new Matrix(b);

Matrix result1 = Matrix.multiply(ma, mb, true);
result1.display();

Matrix result2 = Matrix.strassenMultiply(ma, mb);
result2.display();
```

## ⚡ Performance

The implementation automatically optimizes performance by:

- Using parallel processing for large matrices
- Switching between standard and Strassen's algorithm based on matrix size
- Using efficient array operations

## ⚙️ Running the Tests

Compile and run the test file:

```bash
javac Matrix.java MatrixTest.java
java MatrixTest
```
