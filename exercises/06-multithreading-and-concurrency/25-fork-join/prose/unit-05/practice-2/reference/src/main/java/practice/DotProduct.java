package practice;

import java.util.concurrent.RecursiveTask;

public final class DotProduct extends RecursiveTask<Long> {

    /** Told the range of each base case, before it is computed. */
    public interface Probe {
        void leaf(int start, int end);
    }

    private final int[] a;
    private final int[] b;
    private final int start;
    private final int end;
    private final int threshold;
    private final Probe probe;

    public DotProduct(int[] a, int[] b, int start, int end, int threshold, Probe probe) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("the arrays must have the same length");
        }
        this.a = a;
        this.b = b;
        this.start = start;
        this.end = end;
        this.threshold = threshold;
        this.probe = probe;
    }

    @Override
    protected Long compute() {
        int length = end - start;
        if (length <= threshold) {
            probe.leaf(start, end);
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += (long) a[i] * b[i];
            }
            return sum;
        }
        int mid = start + length / 2;
        DotProduct left = new DotProduct(a, b, start, mid, threshold, probe);
        DotProduct right = new DotProduct(a, b, mid, end, threshold, probe);
        left.fork();
        long rightResult = right.compute();
        long leftResult = left.join();
        return leftResult + rightResult;
    }
}
