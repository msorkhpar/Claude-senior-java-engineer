package practice;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class Shutdown {

    private Shutdown() {
    }

    /** Shuts the executor down in two phases and returns the tasks that never started. */
    public static List<Runnable> close(ExecutorService executor, long timeout, TimeUnit unit) {
        executor.shutdown();
        try {
            if (executor.awaitTermination(timeout, unit)) {
                return List.of();
            }
            List<Runnable> dropped = executor.shutdownNow();
            executor.awaitTermination(timeout, unit);
            return dropped;
        } catch (InterruptedException e) {
            List<Runnable> dropped = executor.shutdownNow();
            Thread.currentThread().interrupt();
            return dropped;
        }
    }
}
