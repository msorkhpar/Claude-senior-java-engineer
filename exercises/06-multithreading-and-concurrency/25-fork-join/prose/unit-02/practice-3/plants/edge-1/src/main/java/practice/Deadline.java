package practice;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;

public final class Deadline {

    private Deadline() {
    }

    /** Runs the task in a new pool and waits at most timeoutMillis for its result. */
    public static <T> Optional<T> within(ForkJoinTask<T> task, int parallelism, long timeoutMillis) {
        ForkJoinPool pool = new ForkJoinPool(parallelism);
        try {
            ForkJoinTask<T> running = pool.submit(task);
            try {
                return Optional.ofNullable(running.get());
            } catch (ExecutionException e) {
                throw new IllegalStateException("the task failed", e.getCause());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } finally {
            pool.shutdownNow();
        }
    }
}
