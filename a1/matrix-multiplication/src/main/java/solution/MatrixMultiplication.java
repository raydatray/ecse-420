package solution;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    public static double[][] sequentialMultiplyMatrix(
        double[][] a,
        double[][] b
    ) {
        double[][] bT = transposeMatrix(b);

        return IntStream.range(0, a.length)
            .mapToObj(r ->
                IntStream.range(0, b[0].length)
                    .mapToDouble(c -> dotProduct(a[r], bT[c]))
                    .toArray()
            )
            .toArray(double[][]::new);
    }

    public static double[][] parallelMultiplyMatrix(
        double[][] a,
        double[][] b
    ) {
        double[][] bT = transposeMatrix(b);
        ExecutorService exc = Executors.newFixedThreadPool(NUMBER_THREADS);

        try {
            List<CompletableFuture<double[]>> futures = IntStream.range(
                0,
                a.length
            )
                .mapToObj(r ->
                    CompletableFuture.supplyAsync(
                        () ->
                            IntStream.range(0, b[0].length)
                                .mapToDouble(c -> dotProduct(a[r], bT[c]))
                                .toArray(),
                        exc
                    )
                )
                .toList();

            return futures
                .stream()
                .map(CompletableFuture::join)
                .toArray(double[][]::new);
        } finally {
            exc.shutdown();
        }
    }

    private static double dotProduct(double[] a, double[] b) {
        return IntStream.range(0, a.length)
            .mapToDouble(i -> a[i] * b[i])
            .sum();
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
