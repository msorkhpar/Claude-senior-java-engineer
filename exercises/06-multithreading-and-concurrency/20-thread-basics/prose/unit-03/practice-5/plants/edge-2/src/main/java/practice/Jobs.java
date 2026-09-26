package practice;

import java.util.concurrent.Callable;
import java.util.function.IntConsumer;

public final class Jobs {

    private Jobs() {
    }

    /** A task that runs step 1..limit, pausing after each, and stops early once its thread is interrupted. */
    public static Callable<Integer> countingTask(int limit, long pauseMillis, IntConsumer step) {
        return () -> {
            int done = 0;
            for (int i = 1; i <= limit && !Thread.currentThread().isInterrupted(); i++) {
                step.accept(i);
                done++;
                if (pauseMillis > 0) {
                    try {
                        Thread.sleep(pauseMillis);
                    } catch (InterruptedException e) {
                        // interrupted while sleeping: carry on with the next step
                    }
                }
            }
            return done;
        };
    }
}
