package practice;

import java.util.concurrent.RecursiveTask;

public final class SumTask extends RecursiveTask<Long> {

    /** Told the range of each base case, before it is summed. */
    public interface Probe {
        void leaf(int start, int end);
    }

    private final int[] array;
    private final int start;
    private final int end;
    private final int threshold;
    private final Probe probe;

    public SumTask(int[] array, int start, int end, int threshold, Probe probe) {
        this.array = array;
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
                sum += array[i];
            }
            return sum;
        }
        int mid = start + length / 2;
        SumTask left = new SumTask(array, start, mid, threshold, probe);
        SumTask right = new SumTask(array, mid, end, threshold, probe);
        left.fork();
        long rightSum = right.compute();
        long leftSum = left.join();
        return leftSum + rightSum;
    }
}
