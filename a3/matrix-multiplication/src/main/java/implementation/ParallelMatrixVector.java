package implementation;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class ParallelMatrixVector {

    public static double[] multiply(double[][] a, double[] x, int threshold) {
        ExecutorService exc = Executors.newCachedThreadPool();
    }
}
