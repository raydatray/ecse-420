package solution;

import java.util.stream.IntStream;

public class BenchmarkCLI {

    private static final int DEFAULT_MATRIX_SIZE = 4000;
    private static final int DEFAULT_MIN_THREADS = 1;
    private static final int DEFAULT_MAX_THREADS =
        Runtime.getRuntime().availableProcessors();
    private static final int DEFAULT_MIN_SIZE = 500;
    private static final int DEFAULT_MAX_SIZE = 4000;
    private static final int DEFAULT_SIZE_STEP = 500;

    public static void main(String[] args) {
        String mode = null;
        int matrixSize = DEFAULT_MATRIX_SIZE;
        int minThreads = DEFAULT_MIN_THREADS;
        int maxThreads = DEFAULT_MAX_THREADS;
        int minSize = DEFAULT_MIN_SIZE;
        int maxSize = DEFAULT_MAX_SIZE;
        int sizeStep = DEFAULT_SIZE_STEP;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--mode":
                    mode = args[++i];
                    break;
                case "--matrix-size":
                    matrixSize = Integer.parseInt(args[++i]);
                    break;
                case "--min-threads":
                    minThreads = Integer.parseInt(args[++i]);
                    break;
                case "--max-threads":
                    maxThreads = Integer.parseInt(args[++i]);
                    break;
                case "--min-size":
                    minSize = Integer.parseInt(args[++i]);
                    break;
                case "--max-size":
                    maxSize = Integer.parseInt(args[++i]);
                    break;
                case "--size-step":
                    sizeStep = Integer.parseInt(args[++i]);
                    break;
                default:
                    System.err.printf("Unknown argument: %s%n", args[i]);
                    printUsage();
                    System.exit(1);
            }
        }

        if (mode == null) {
            System.err.println("error: --mode is required");
            printUsage();
            System.exit(1);
        }

        if (!mode.equals("threads") && !mode.equals("size")) {
            System.err.println("error: --mode must be 'threads' or 'size'");
            printUsage();
            System.exit(1);
        }

        switch (mode) {
            case "threads" -> runThreadBenchmark(
                matrixSize,
                minThreads,
                maxThreads
            );
            case "size" -> runSizeBenchmark(minSize, maxSize, sizeStep);
        }
    }

    private static long captureRuntime(Runnable task) {
        long start = System.currentTimeMillis();
        task.run();
        return System.currentTimeMillis() - start;
    }

    private static void runThreadBenchmark(
        int matrixSize,
        int minThreads,
        int maxThreads
    ) {
        System.out.println("------ thread Benchmark ------");
        System.out.printf("matrix size: %dx%d%n", matrixSize, matrixSize);
        System.out.println();

        // Generate matrices once and reuse
        System.out.println("generating random matrices...");
        double[][] a = MatrixMultiplication.generateRandomMatrix(
            matrixSize,
            matrixSize
        );
        double[][] b = MatrixMultiplication.generateRandomMatrix(
            matrixSize,
            matrixSize
        );
        System.out.println("done.");
        System.out.println();

        // Run sequential benchmark
        System.out.println("running sequential multiplication...");
        long seqTime = captureRuntime(() ->
            MatrixMultiplication.sequentialMultiplyMatrix(a, b)
        );
        System.out.printf("sequential time: %d ms%n", seqTime);
        System.out.println();

        // Print header
        System.out.println("threads\tparallel(ms)\tspeedup");

        // Run parallel benchmarks
        IntStream.rangeClosed(minThreads, maxThreads).forEach(threads -> {
            long parTime = captureRuntime(() ->
                MatrixMultiplication.parallelMultiplyMatrix(a, b, threads)
            );
            double speedup = (double) seqTime / parTime;
            System.out.printf("%d\t%d\t\t%.2fx%n", threads, parTime, speedup);
        });
    }

    private static void runSizeBenchmark(
        int minSize,
        int maxSize,
        int sizeStep
    ) {
        int numThreads = Runtime.getRuntime().availableProcessors();

        System.out.println("------ size benchmark ------");
        System.out.printf("threads: %d%n", numThreads);
        System.out.println();

        // Print header
        System.out.println("size\tsequential(ms)\tparallel(ms)\tspeedup");

        // Run benchmarks for each size
        IntStream.iterate(
            minSize,
            size -> size <= maxSize,
            size -> size + sizeStep
        ).forEach(size -> {
            // Generate matrices for this size
            double[][] a = MatrixMultiplication.generateRandomMatrix(
                size,
                size
            );
            double[][] b = MatrixMultiplication.generateRandomMatrix(
                size,
                size
            );

            long seqTime = captureRuntime(() ->
                MatrixMultiplication.sequentialMultiplyMatrix(a, b)
            );
            long parTime = captureRuntime(() ->
                MatrixMultiplication.parallelMultiplyMatrix(a, b, numThreads)
            );

            double speedup = (double) seqTime / parTime;
            System.out.printf(
                "%d\t%d\t\t%d\t\t%.2fx%n",
                size,
                seqTime,
                parTime,
                speedup
            );
        });
    }

    private static void printUsage() {
        System.out.println();
        System.out.println(
            "usage: java BenchmarkCLI --mode <threads|size> [options]"
        );
        System.out.println();
        System.out.println("Options:");
        System.out.println(
            "  --mode <threads|size>  benchmark mode (required)"
        );
        System.out.println(
            "  --matrix-size <n>      matrix dimension for thread mode (default: 4000)"
        );
        System.out.println(
            "  --min-threads <n>      minimum thread count for thread mode (default: 1)"
        );
        System.out.println(
            "  --max-threads <n>      maximum thread count for thread mode (default: available processors)"
        );
        System.out.println(
            "  --min-size <n>         minimum matrix size for size mode (default: 500)"
        );
        System.out.println(
            "  --max-size <n>         maximum matrix size for size mode (default: 4000)"
        );
        System.out.println(
            "  --size-step <n>        step size for size mode (default: 500)"
        );
        System.out.println();
        System.out.println("examples:");
        System.out.println("  ./gradlew run --args=\"--mode threads\"");
        System.out.println(
            "  ./gradlew run --args=\"--mode threads --matrix-size 4000 --min-threads 1 --max-threads 16\""
        );
        System.out.println(
            "  ./gradlew run --args=\"--mode size --min-size 500 --max-size 4000 --size-step 500\""
        );
    }
}
