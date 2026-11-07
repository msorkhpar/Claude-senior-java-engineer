package practice;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/** Stops the invoice service's worker pool when the service is redeployed. */
public class Drain {

    /**
     * Stops the pool within the deploy's grace period and returns the tasks that never started,
     * so the caller can put them back on the message broker. Called by the deploy's shutdown hook.
     */
    public static List<Runnable> stop(ExecutorService pool, long grace, TimeUnit unit) {
        pool.shutdown();
        try {
            if (pool.awaitTermination(grace, unit)) return List.of();
            return List.of();
        } catch (InterruptedException e) {
            List<Runnable> neverStarted = pool.shutdownNow();
            Thread.currentThread().interrupt();
            return neverStarted;
        }
    }
}
