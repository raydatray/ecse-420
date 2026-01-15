package solution;

public class MatrixMultiplicationTask implements Runnable {

    int aRowIdx;
    double[][] a;
    double[][] b;
    double[][] res;

    public MatrixMultiplicationTask(
        int aRowIdx,
        double[][] a,
        double[][] b,
        double[][] res
    ) {
        this.aRowIdx = aRowIdx;
        this.a = a;
        this.b = b;
        this.res = res;
    }

    public void run() {
        int common = a[0].length;
        int cols = res[0].length;

        for (int c = 0; c < cols; c++) {
            double s = 0.0;

            for (int i = 0; i < common; i++) {
                s += a[aRowIdx][i] * b[c][i];
            }

            res[aRowIdx][c] = s;
        }
    }
}
