public class MatrixTest {
    public static void main(String[] args) {
        runTests();
    }
    
    private static void runTests() {
        testBasicMultiplication();
        testParallelMultiplication();
        testStrassenMultiplication();
        testLargeMatrix();
        System.out.println("All tests passed!");
    }
    
    private static void testBasicMultiplication() {
        int[][] a = {{1, 2}, {3, 4}};
        int[][] b = {{5, 6}, {7, 8}};
        Matrix ma = new Matrix(a);
        Matrix mb = new Matrix(b);
        
        Matrix result = Matrix.multiply(ma, mb, false);
        
        assert result.getData()[0][0] == 19 : "Basic multiplication failed";
        assert result.getData()[0][1] == 22 : "Basic multiplication failed";
        assert result.getData()[1][0] == 43 : "Basic multiplication failed";
        assert result.getData()[1][1] == 50 : "Basic multiplication failed";
        
        System.out.println("Basic multiplication test passed");
    }
    
    private static void testParallelMultiplication() {
        int[][] a = {{1, 2}, {3, 4}};
        int[][] b = {{5, 6}, {7, 8}};
        Matrix ma = new Matrix(a);
        Matrix mb = new Matrix(b);
        
        Matrix result = Matrix.multiply(ma, mb, true);
        
        assert result.getData()[0][0] == 19 : "Parallel multiplication failed";
        assert result.getData()[0][1] == 22 : "Parallel multiplication failed";
        assert result.getData()[1][0] == 43 : "Parallel multiplication failed";
        assert result.getData()[1][1] == 50 : "Parallel multiplication failed";
        
        System.out.println("Parallel multiplication test passed");
    }
    
    private static void testStrassenMultiplication() {
        int[][] a = {{1, 2}, {3, 4}};
        int[][] b = {{5, 6}, {7, 8}};
        Matrix ma = new Matrix(a);
        Matrix mb = new Matrix(b);
        
        Matrix result = Matrix.strassenMultiply(ma, mb);
        
        assert result.getData()[0][0] == 19 : "Strassen multiplication failed";
        assert result.getData()[0][1] == 22 : "Strassen multiplication failed";
        assert result.getData()[1][0] == 43 : "Strassen multiplication failed";
        assert result.getData()[1][1] == 50 : "Strassen multiplication failed";
        
        System.out.println("Strassen multiplication test passed");
    }
    
    private static void testLargeMatrix() {
        int size = 128;
        int[][] a = new int[size][size];
        int[][] b = new int[size][size];
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                a[i][j] = i + j;
                b[i][j] = i - j;
            }
        }
        
        Matrix ma = new Matrix(a);
        Matrix mb = new Matrix(b);
        
        // Both methods should produce the same result
        Matrix result1 = Matrix.multiply(ma, mb, true);
        Matrix result2 = Matrix.strassenMultiply(ma, mb);
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                assert result1.getData()[i][j] == result2.getData()[i][j] : 
                    "Large matrix multiplication mismatch at [" + i + "][" + j + "]";
            }
        }
        
        System.out.println("Large matrix test passed");
    }
}
