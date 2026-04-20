package implementation;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.stream.IntStream;

public final class ParallelMatrixVector {

    private static final ForkJoinPool POOL = new ForkJoinPool(
        Runtime.getRuntime().availableProcessors()
    );

    private static final int ROW_LEAF_SIZE = 64;

    private ParallelMatrixVector() {}

    public static double[] multiply(double[][] a, double[] x, int threshold) {
        double[] y = new double[a.length];
        POOL.invoke(new RowTask(a, x, y, 0, a.length, threshold));
        return y;
    }

    private static final class RowTask extends RecursiveAction {

        private final double[][] a;
        private final double[] x;
        private final double[] y;
        private final int start;
        private final int end;
        private final int threshold;

        RowTask(
            double[][] a,
            double[] x,
            double[] y,
            int start,
            int end,
            int threshold
        ) {
            this.a = a;
            this.x = x;
            this.y = y;
            this.start = start;
            this.end = end;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            if (end - start <= ROW_LEAF_SIZE) {
                IntStream.range(start, end).forEach(i ->
                    y[i] = new DotProductTask(
                        a[i],
                        x,
                        0,
                        x.length,
                        threshold
                    ).compute()
                );
                return;
            }

            int mid = start + (end - start) / 2;
            invokeAll(
                new RowTask(a, x, y, start, mid, threshold),
                new RowTask(a, x, y, mid, end, threshold)
            );
        }
    }

    private static final class DotProductTask extends RecursiveTask<Double> {

        private final double[] row;
        private final double[] x;
        private final int start;
        private final int end;
        private final int threshold;

        DotProductTask(
            double[] row,
            double[] x,
            int start,
            int end,
            int threshold
        ) {
            this.row = row;
            this.x = x;
            this.start = start;
            this.end = end;
            this.threshold = threshold;
        }

        @Override
        protected Double compute() {
            if (end - start <= threshold) {
                return IntStream.range(start, end)
                    .mapToDouble(i -> row[i] * x[i])
                    .sum();
            }

            int mid = start + (end - start) / 2;
            DotProductTask right = new DotProductTask(
                row,
                x,
                mid,
                end,
                threshold
            );
            right.fork();
            double leftSum = new DotProductTask(
                row,
                x,
                start,
                mid,
                threshold
            ).compute();
            return leftSum + right.join();
        }
    }
}
