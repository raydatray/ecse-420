package implementation;

import java.util.stream.IntStream;

public class SequentialMatrixVector {

    public static double[] multiply(double[][] a, double[] x) {
        int n = a.length;

        return IntStream.range(0, n)
            .mapToDouble(i ->
                IntStream.range(0, n)
                    .mapToDouble(j -> a[i][j] * x[j])
                    .sum()
            )
            .toArray();
    }
}
