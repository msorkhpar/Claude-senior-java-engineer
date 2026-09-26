package practice;

import java.util.function.IntConsumer;

public final class Worker {

    private Worker() {
    }

    /** Runs step 1, 2, 3, ... until the current thread is interrupted, checking before each step; returns how many ran. */
    public static long countSteps(IntConsumer step) {
        long done = 0;
        while (!Thread.currentThread().isInterrupted()) {
            step.accept((int) (done + 1));
            done++;
        }
        return done;
    }
}
