package practice;

import java.util.concurrent.RecursiveTask;

public final class MultiWaySum extends RecursiveTask<Long> {

    /** Told the range of each leaf, before it is summed. */
    public interface Probe {
        void leaf(int start, int end);
    }

    public MultiWaySum(long[] array, int start, int end, int ways, int threshold, Probe probe) {
    }

    @Override
    protected Long compute() {
        throw new UnsupportedOperationException("write compute");
    }
}
