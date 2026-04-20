package implementation;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class ParallelMatrixVector {

    public static double[] multiply(double[][] a, double[] x, int threshold) {
        ExecutorService exc = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
        );
        try {
            List<CompletableFuture<Double>> futures = IntStream.range(
                0,
                a.length
            )
                .mapToObj(i ->
                    parallelDotProduct(a[i], x, 0, x.length, threshold, exc)
                )
                .toList();

            return futures
                .stream()
                .mapToDouble(CompletableFuture::join)
                .toArray();
        } finally {
            exc.shutdown();
        }
    }

    private static CompletableFuture<Double> parallelDotProduct(
        double[] row,
        double[] x,
        int start,
        int end,
        int threshold,
        ExecutorService exc
    ) {
        if (end - start <= threshold) {
            return CompletableFuture.supplyAsync(
                () ->
                    IntStream.range(start, end)
                        .mapToDouble(i -> row[i] * x[i])
                        .sum(),
                exc
            );
        }

        int mid = start + (end - start) / 2;
        CompletableFuture<Double> left = parallelDotProduct(
            row,
            x,
            start,
            mid,
            threshold,
            exc
        );
        CompletableFuture<Double> right = parallelDotProduct(
            row,
            x,
            mid,
            end,
            threshold,
            exc
        );
        return left.thenCombine(right, Double::sum);
    }
}
