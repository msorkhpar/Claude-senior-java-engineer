package practice;

import java.util.concurrent.Callable;
import java.util.function.IntConsumer;

public final class Jobs {

    private Jobs() {
    }

    /** A task that runs step 1..limit, pausing after each, and stops early once its thread is interrupted. */
    public static Callable<Integer> countingTask(int limit, long pauseMillis, IntConsumer step) {
        throw new UnsupportedOperationException("write countingTask");
    }
}
