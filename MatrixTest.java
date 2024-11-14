import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MatrixTest {
    private static final int[] MATRIX_SIZES = {
        64, 96, 128, 160, 192, 256, 320, 384, 448, 512, 640, 768, 896, 1024, 1280, 1536, 1792, 2048
    };
    
    private static final int[] TRIALS_PER_SIZE = {
        20, 18, 16, 14, 12, 10, 9, 8, 7, 6, 5, 4, 4, 3, 3, 2, 2, 2
    };
    
    private static final String RESULTS_FILE = "matrix_performance_analysis.csv";
    private static int testsPassed = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        setup();
        
        runTest("Basic Multiplication", MatrixTest::testBasicMultiplication);
        runTest("Parallel Multiplication", MatrixTest::testParallelMultiplication);
        runTest("Strassen Multiplication", MatrixTest::testStrassenMultiplication);
        
        runTest("Performance Analysis", MatrixTest::runPerformanceAnalysis);
        
        printTestSummary();
    }

    private static void setup() {
        System.out.println("Starting comprehensive matrix multiplication analysis...\n");
        try (PrintWriter writer = new PrintWriter(new FileWriter(RESULTS_FILE))) {
            writer.println("Size,Method,Trial,Time(ms)");
        } catch (IOException e) {
            System.err.println("Failed to initialize results file: " + e.getMessage());
        }
    }

    private static void runTest(String testName, Runnable test) {
        System.out.println("Running test: " + testName);
        totalTests++;
        
        try {
            test.run();
            testsPassed++;
            System.out.println("✓ " + testName + " passed\n");
        } catch (AssertionError | Exception e) {
            System.out.println("✗ " + testName + " failed");
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void testBasicMultiplication() {
        int[][] a = {{1, 2}, {3, 4}};
        int[][] b = {{5, 6}, {7, 8}};
        Matrix ma = new Matrix(a);
        Matrix mb = new Matrix(b);

        Matrix result = Matrix.multiply(ma, mb, false);

        assertTrue(result.getData()[0][0] == 19, "Position [0,0] incorrect");
        assertTrue(result.getData()[0][1] == 22, "Position [0,1] incorrect");
        assertTrue(result.getData()[1][0] == 43, "Position [1,0] incorrect");
        assertTrue(result.getData()[1][1] == 50, "Position [1,1] incorrect");
    }

    private static void testParallelMultiplication() {
        int[][] a = {{1, 2}, {3, 4}};
        int[][] b = {{5, 6}, {7, 8}};
        Matrix ma = new Matrix(a);
        Matrix mb = new Matrix(b);

        Matrix result = Matrix.multiply(ma, mb, true);

        assertTrue(result.getData()[0][0] == 19, "Position [0,0] incorrect");
        assertTrue(result.getData()[0][1] == 22, "Position [0,1] incorrect");
        assertTrue(result.getData()[1][0] == 43, "Position [1,0] incorrect");
        assertTrue(result.getData()[1][1] == 50, "Position [1,1] incorrect");
    }

    private static void testStrassenMultiplication() {
        int[][] a = {{1, 2}, {3, 4}};
        int[][] b = {{5, 6}, {7, 8}};
        Matrix ma = new Matrix(a);
        Matrix mb = new Matrix(b);

        Matrix result = Matrix.strassenMultiply(ma, mb);

        assertTrue(result.getData()[0][0] == 19, "Position [0,0] incorrect");
        assertTrue(result.getData()[0][1] == 22, "Position [0,1] incorrect");
        assertTrue(result.getData()[1][0] == 43, "Position [1,0] incorrect");
        assertTrue(result.getData()[1][1] == 50, "Position [1,1] incorrect");
    }

    private static void runPerformanceAnalysis() {
        try (PrintWriter csvWriter = new PrintWriter(new FileWriter(RESULTS_FILE, true))) {
            for (int i = 0; i < MATRIX_SIZES.length; i++) {
                int size = MATRIX_SIZES[i];
                int trials = TRIALS_PER_SIZE[i];
                
                System.out.printf("\nTesting matrices of size %dx%d (%d trials)%n", 
                                size, size, trials);
                
                runMethodTests("Standard", size, trials, csvWriter, false);
                runMethodTests("Parallel", size, trials, csvWriter, true);
                if (size >= 64) {
                    runMethodTests("Strassen", size, trials, csvWriter, null);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write performance data", e);
        }
    }

    private static void runMethodTests(String method, int size, int trials, 
                                     PrintWriter writer, Boolean isParallel) {
        Matrix ma = new Matrix(generateMatrix(size));
        Matrix mb = new Matrix(generateMatrix(size));
        
        warmupMethod(ma, mb, method, isParallel);
        
        System.out.printf("Running %s method trials...%n", method);
        
        for (int trial = 0; trial < trials; trial++) {
            System.out.printf("  Trial %d/%d%n", trial + 1, trials);
            
            long startTime = System.nanoTime();
            
            if (method.equals("Strassen")) {
                Matrix.strassenMultiply(ma, mb);
            } else {
                Matrix.multiply(ma, mb, isParallel);
            }
            
            long endTime = System.nanoTime();
            
            double timeInMs = (endTime - startTime) / 1_000_000.0;
            
            writer.printf("%d,%s,%d,%.2f%n", 
                         size, method, trial + 1, timeInMs);
            writer.flush();
            
            System.gc();
        }
    }

    private static void warmupMethod(Matrix ma, Matrix mb, String method, Boolean isParallel) {
        for (int i = 0; i < 3; i++) {
            if (method.equals("Strassen")) {
                Matrix.strassenMultiply(ma, mb);
            } else {
                Matrix.multiply(ma, mb, isParallel);
            }
        }
    }

    private static int[][] generateMatrix(int size) {
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = (i * j) % 100;
            }
        }
        return matrix;
    }

    private static void printTestSummary() {
        System.out.println("\nTest Summary:");
        System.out.println("-------------");
        System.out.printf("Passed: %d/%d tests%n", testsPassed, totalTests);
        System.out.println("Performance results written to: " + RESULTS_FILE);
    }
}
