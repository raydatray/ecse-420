package solution;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public class MatrixMultiplication {

    private static final int NUMBER_THREADS = 1;
    private static final int MATRIX_SIZE = 2000;

    public static void main(String[] args) {
        // Generate two random matrices, same size
        double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
        double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
        sequentialMultiplyMatrix(a, b);
        parallelMultiplyMatrix(a, b);
    }

    /**
     * Returns the result of a sequential matrix multiplication
     * The two matrices are randomly generated
     * @param a is the first matrix
     * @param b is the second matrix
     * @return the result of the multiplication
     * */
    public static double[][] sequentialMultiplyMatrix(
        double[][] a,
        double[][] b
    ) {
        int rows = a.length;
        int common = a[0].length;
        int cols = b[0].length;

        double[][] bT = transposeMatrix(b);
        double[][] res = new double[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double s = 0.0;

                for (int i = 0; i < common; i++) {
                    s += a[r][i] * bT[c][i];
                }

                res[r][c] = s;
            }
        }

        return res;
    }

    /**
     * Returns the result of a concurrent matrix multiplication
     * The two matrices are randomly generated
     * @param a is the first matrix
     * @param b is the second matrix
     * @return the result of the multiplication
     * */
    public static double[][] parallelMultiplyMatrix(
        double[][] a,
        double[][] b
    ) {
        int rows = a.length;
        int cols = b[0].length;

        double[][] bT = transposeMatrix(b);
        double[][] res = new double[rows][cols];

        CompletableFuture<?>[] futures = IntStream.range(0, rows)
            .mapToObj(aRowIdx ->
                CompletableFuture.runAsync(
                    new MatrixMultiplicationTask(aRowIdx, a, bT, res)
                )
            )
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        return res;
    }

    private static double[][] generateRandomMatrix(int rows, int cols) {
        return IntStream.range(0, rows)
            .mapToObj(r ->
                IntStream.range(0, cols)
                    .mapToDouble(c -> (double) ((int) (Math.random() * 10.0)))
                    .toArray()
            )
            .toArray(double[][]::new);
    }

    private static double[][] transposeMatrix(double[][] m) {
        return IntStream.range(0, m[0].length)
            .mapToObj(c ->
                IntStream.range(0, m.length)
                    .mapToDouble(r -> m[r][c])
                    .toArray()
            )
            .toArray(double[][]::new);
    }
}
