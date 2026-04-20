package benchmark;

import implementation.ParallelMatrixVector;
import implementation.SequentialMatrixVector;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class Benchmark {

    private static final int N = 4000;
    private static final int NUM_THREADS =
        Runtime.getRuntime().availableProcessors();
    private static final int[] THRESHOLDS = {
        16,
        32,
        64,
        128,
        256,
        512,
        1024,
        2048,
        4096,
        8192,
        16384,
    };

    public static void main(String[] args) {
        System.out.println("--- matrix multiplication benchmark ---");
        System.out.println("matrix size: " + N + " x " + N);
        System.out.println("system threads: " + NUM_THREADS);

        double[][] A = new double[N][N];
        double[] x = new double[N];
        Random rand = new Random(69);

        for (int i = 0; i < N; i++) {
            x[i] = rand.nextDouble();
            for (int j = 0; j < N; j++) {
                A[i][j] = rand.nextDouble();
            }
        }

        // warm up JVM
        System.out.println("warming up JVM");
        IntStream.range(0, 5).forEach(i -> {
            ParallelMatrixVector.multiply(A, x, 1000);
            SequentialMatrixVector.multiply(A, x);
        });

        long startSeq = System.nanoTime();
        double[] ySeq = SequentialMatrixVector.multiply(A, x);
        double timeSeq = (System.nanoTime() - startSeq) / 1e6;
        System.out.printf("sequential time: %.4f ms\n\n", timeSeq);

        System.out.printf(
            "%-15s %-15s %-15s %-15s\n",
            "threshold",
            "time (ms)",
            "speedup",
            "status"
        );
        System.out.println(
            "-------------------------------------------------------------"
        );

        Arrays.stream(THRESHOLDS).forEach(threshold -> {
            long startPar = System.nanoTime();
            double[] yPar = ParallelMatrixVector.multiply(A, x, threshold);
            double timePar = (System.nanoTime() - startPar) / 1e6;

            double speedup = timeSeq / timePar;
            boolean passed = verify(ySeq, yPar);

            System.out.printf(
                "%-15d %-15.4f x%-15.2f %-15s\n",
                threshold,
                timePar,
                speedup,
                passed ? "pass" : "fail"
            );
        });
    }

    public static boolean verify(double[] expected, double[] actual) {
        double error = IntStream.range(0, expected.length)
            .mapToDouble(i -> Math.abs(expected[i] - actual[i]))
            .sum();

        return error < 1e-5;
    }
}
