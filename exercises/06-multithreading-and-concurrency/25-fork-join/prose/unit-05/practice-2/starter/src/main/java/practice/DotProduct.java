package practice;

import java.util.concurrent.RecursiveTask;

public final class DotProduct extends RecursiveTask<Long> {

    /** Told the range of each base case, before it is computed. */
    public interface Probe {
        void leaf(int start, int end);
    }

    public DotProduct(int[] a, int[] b, int start, int end, int threshold, Probe probe) {
        throw new UnsupportedOperationException("write the constructor");
    }

    @Override
    protected Long compute() {
        throw new UnsupportedOperationException("write compute");
    }
}
