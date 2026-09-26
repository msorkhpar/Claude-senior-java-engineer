package practice;

import java.util.concurrent.RecursiveTask;

public final class SumTask extends RecursiveTask<Long> {

    /** Told the range of each base case, before it is summed. */
    public interface Probe {
        void leaf(int start, int end);
    }

    public SumTask(int[] array, int start, int end, int threshold, Probe probe) {
    }

    @Override
    protected Long compute() {
        throw new UnsupportedOperationException("write compute");
    }
}
