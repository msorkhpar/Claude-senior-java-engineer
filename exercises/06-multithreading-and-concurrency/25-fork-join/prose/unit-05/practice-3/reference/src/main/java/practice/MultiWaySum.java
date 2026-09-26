package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public final class MultiWaySum extends RecursiveTask<Long> {

    /** Told the range of each leaf, before it is summed. */
    public interface Probe {
        void leaf(int start, int end);
    }

    private final long[] array;
    private final int start;
    private final int end;
    private final int ways;
    private final int threshold;
    private final Probe probe;

    public MultiWaySum(long[] array, int start, int end, int ways, int threshold, Probe probe) {
        this.array = array;
        this.start = start;
        this.end = end;
        this.ways = ways;
        this.threshold = threshold;
        this.probe = probe;
    }

    @Override
    protected Long compute() {
        int length = end - start;
        if (length <= threshold || ways <= 1) {
            probe.leaf(start, end);
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }
            return sum;
        }
        int effectiveWays = Math.min(ways, length);
        int chunk = length / effectiveWays;
        List<MultiWaySum> tasks = new ArrayList<>();
        int from = start;
        for (int i = 0; i < effectiveWays; i++) {
            int to = i == effectiveWays - 1 ? end : from + chunk;
            tasks.add(new MultiWaySum(array, from, to, 1, threshold, probe));
            from = to;
        }
        for (int i = 0; i < tasks.size() - 1; i++) {
            tasks.get(i).fork();
        }
        long total = tasks.get(tasks.size() - 1).compute();
        for (int i = tasks.size() - 2; i >= 0; i--) {
            total += tasks.get(i).join();
        }
        return total;
    }
}
