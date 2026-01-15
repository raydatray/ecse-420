package solution;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static solution.MatrixMultiplication.sequentialMultiplyMatrix;

import org.junit.jupiter.api.Test;

class MatrixMultiplicationTest {

    @Test
    void testSimple2x2Multiplication() {
        double[][] a = { { 1.0, 2.0 }, { 3.0, 4.0 } };
        double[][] b = { { 5.0, 6.0 }, { 7.0, 8.0 } };
        double[][] expected = { { 19.0, 22.0 }, { 43.0, 50.0 } };

        double[][] result = sequentialMultiplyMatrix(a, b);

        assertArrayEquals(expected[0], result[0], "First row mismatch");
        assertArrayEquals(expected[1], result[1], "Second row mismatch");
    }

    @Test
    void testIdentityMatrix() {
        double[][] identity = { { 1.0, 0.0 }, { 0.0, 1.0 } };
        double[][] a = { { 3.0, 5.0 }, { 2.0, 7.0 } };

        double[][] result = sequentialMultiplyMatrix(identity, a);

        assertArrayEquals(
            a[0],
            result[0],
            "Identity multiplication should return original matrix - row 0"
        );
        assertArrayEquals(
            a[1],
            result[1],
            "Identity multiplication should return original matrix - row 1"
        );
    }

    @Test
    void testZeroMatrix() {
        double[][] a = { { 1.0, 2.0 }, { 3.0, 4.0 } };
        double[][] zero = { { 0.0, 0.0 }, { 0.0, 0.0 } };
        double[][] expected = { { 0.0, 0.0 }, { 0.0, 0.0 } };

        double[][] result = sequentialMultiplyMatrix(a, zero);

        assertArrayEquals(
            expected[0],
            result[0],
            "Multiplying by zero matrix should give zero - row 0"
        );
        assertArrayEquals(
            expected[1],
            result[1],
            "Multiplying by zero matrix should give zero - row 1"
        );
    }

    @Test
    void testSingleElementMatrix() {
        double[][] a = { { 3.0 } };
        double[][] b = { { 4.0 } };
        double[][] expected = { { 12.0 } };

        double[][] result = sequentialMultiplyMatrix(a, b);

        assertArrayEquals(expected[0], result[0], "1x1 matrix multiplication");
    }

    @Test
    void testRectangularMatrices2x3Times3x2() {
        double[][] a = { { 1.0, 2.0, 3.0 }, { 4.0, 5.0, 6.0 } };
        double[][] b = { { 7.0, 8.0 }, { 9.0, 10.0 }, { 11.0, 12.0 } };
        // Result should be 2x2
        // [1*7+2*9+3*11, 1*8+2*10+3*12] = [58, 64]
        // [4*7+5*9+6*11, 4*8+5*10+6*12] = [139, 154]
        double[][] expected = { { 58.0, 64.0 }, { 139.0, 154.0 } };

        double[][] result = sequentialMultiplyMatrix(a, b);

        assertArrayEquals(
            expected[0],
            result[0],
            "Rectangular 2x3 * 3x2 - row 0"
        );
        assertArrayEquals(
            expected[1],
            result[1],
            "Rectangular 2x3 * 3x2 - row 1"
        );
    }

    @Test
    void testRectangularMatrices1x3Times3x1() {
        double[][] a = { { 1.0, 2.0, 3.0 } };
        double[][] b = { { 4.0 }, { 5.0 }, { 6.0 } };
        // Result should be 1x1: 1*4 + 2*5 + 3*6 = 32
        double[][] expected = { { 32.0 } };

        double[][] result = sequentialMultiplyMatrix(a, b);

        assertArrayEquals(expected[0], result[0], "Rectangular 1x3 * 3x1");
    }

    @Test
    void test3x3Multiplication() {
        double[][] a = {
            { 1.0, 2.0, 3.0 },
            { 4.0, 5.0, 6.0 },
            { 7.0, 8.0, 9.0 },
        };
        double[][] b = {
            { 9.0, 8.0, 7.0 },
            { 6.0, 5.0, 4.0 },
            { 3.0, 2.0, 1.0 },
        };
        // Row 0: [1*9+2*6+3*3, 1*8+2*5+3*2, 1*7+2*4+3*1] = [30, 24, 18]
        // Row 1: [4*9+5*6+6*3, 4*8+5*5+6*2, 4*7+5*4+6*1] = [84, 69, 54]
        // Row 2: [7*9+8*6+9*3, 7*8+8*5+9*2, 7*7+8*4+9*1] = [138, 114, 90]
        double[][] expected = {
            { 30.0, 24.0, 18.0 },
            { 84.0, 69.0, 54.0 },
            { 138.0, 114.0, 90.0 },
        };

        double[][] result = sequentialMultiplyMatrix(a, b);

        assertArrayEquals(expected[0], result[0], "3x3 multiplication - row 0");
        assertArrayEquals(expected[1], result[1], "3x3 multiplication - row 1");
        assertArrayEquals(expected[2], result[2], "3x3 multiplication - row 2");
    }

    @Test
    void testWithNegativeNumbers() {
        double[][] a = { { -1.0, 2.0 }, { 3.0, -4.0 } };
        double[][] b = { { 5.0, -6.0 }, { -7.0, 8.0 } };
        // Row 0: [-1*5+2*(-7), -1*(-6)+2*8] = [-19, 22]
        // Row 1: [3*5+(-4)*(-7), 3*(-6)+(-4)*8] = [43, -50]
        double[][] expected = { { -19.0, 22.0 }, { 43.0, -50.0 } };

        double[][] result = sequentialMultiplyMatrix(a, b);

        assertArrayEquals(expected[0], result[0], "Negative numbers - row 0");
        assertArrayEquals(expected[1], result[1], "Negative numbers - row 1");
    }

    @Test
    void testWithDecimalNumbers() {
        double[][] a = { { 1.5, 2.5 }, { 3.5, 4.5 } };
        double[][] b = { { 0.5, 1.5 }, { 2.5, 3.5 } };
        // Row 0: [1.5*0.5+2.5*2.5, 1.5*1.5+2.5*3.5] = [7.0, 11.0]
        // Row 1: [3.5*0.5+4.5*2.5, 3.5*1.5+4.5*3.5] = [13.0, 21.0]
        double[][] expected = { { 7.0, 11.0 }, { 13.0, 21.0 } };

        double[][] result = sequentialMultiplyMatrix(a, b);

        assertArrayEquals(
            expected[0],
            result[0],
            0.0001,
            "Decimal numbers - row 0"
        );
        assertArrayEquals(
            expected[1],
            result[1],
            0.0001,
            "Decimal numbers - row 1"
        );
    }
}
