package practice;

import java.util.concurrent.RecursiveTask;

public final class MinMaxTask extends RecursiveTask<MinMaxTask.Range> {

    public record Range(int min, int max) {
    }

    private final int[] array;
    private final int start;
    private final int end;
    private final int threshold;

    public MinMaxTask(int[] array, int start, int end, int threshold) {
        if (end <= start) {
            throw new IllegalArgumentException("an empty range has no smallest or largest value");
        }
        this.array = array;
        this.start = start;
        this.end = end;
        this.threshold = threshold;
    }

    @Override
    protected Range compute() {
        int length = end - start;
        if (length <= threshold) {
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (int i = start; i < end; i++) {
                min = Math.min(min, array[i]);
                max = Math.max(max, array[i]);
            }
            return new Range(min, max);
        }
        int mid = start + length / 2;
        MinMaxTask left = new MinMaxTask(array, start, mid, threshold);
        MinMaxTask right = new MinMaxTask(array, mid, end, threshold);
        left.fork();
        Range r = right.compute();
        Range l = left.join();
        return new Range(Math.min(l.min(), r.min()), Math.max(l.max(), r.max()));
    }
}
