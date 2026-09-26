package practice;

import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

public final class CountMatching extends RecursiveAction {

    private final int[] array;
    private final int start;
    private final int end;
    private final int threshold;
    private final int target;
    private final AtomicInteger counter;

    public CountMatching(int[] array, int start, int end, int threshold, int target, AtomicInteger counter) {
        this.array = array;
        this.start = start;
        this.end = end;
        this.threshold = threshold;
        this.target = target;
        this.counter = counter;
    }

    @Override
    protected void compute() {
        int length = end - start;
        if (length <= threshold) {
            int localCount = 0;
            for (int i = start; i < end; i++) {
                if (array[i] == target) {
                    localCount++;
                }
            }
            counter.addAndGet(localCount);
            return;
        }
        int mid = start + length / 2;
        invokeAll(new CountMatching(array, start, mid, threshold, target, counter),
                new CountMatching(array, mid, end, threshold, target, counter));
    }
}
