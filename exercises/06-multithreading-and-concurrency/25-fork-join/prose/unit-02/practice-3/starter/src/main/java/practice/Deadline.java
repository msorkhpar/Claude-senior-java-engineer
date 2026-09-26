package practice;

import java.util.Optional;
import java.util.concurrent.ForkJoinTask;

public final class Deadline {

    private Deadline() {
    }

    /** Runs the task in a new pool and waits at most timeoutMillis for its result. */
    public static <T> Optional<T> within(ForkJoinTask<T> task, int parallelism, long timeoutMillis) {
        throw new UnsupportedOperationException("write within");
    }
}
