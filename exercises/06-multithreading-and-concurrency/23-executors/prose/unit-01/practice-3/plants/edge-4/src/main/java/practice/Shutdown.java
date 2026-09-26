package practice;
import java.util.*; import java.util.concurrent.*;
public final class Shutdown {
    private Shutdown() {}
    public static List<Runnable> close(ExecutorService executor, long timeout, TimeUnit unit) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeout, unit)) {
                List<Runnable> left = executor.shutdownNow();
                executor.awaitTermination(timeout, unit);
                return left;
            }
            return List.of();
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            return List.of(); // drops what shutdownNow() returned
        }
    }
}
