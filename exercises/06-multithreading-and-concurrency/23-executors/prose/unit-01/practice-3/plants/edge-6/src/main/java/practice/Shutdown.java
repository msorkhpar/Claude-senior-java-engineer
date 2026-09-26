package practice;
import java.util.*; import java.util.concurrent.*;
public final class Shutdown {
    private Shutdown() {}
    public static List<Runnable> close(ExecutorService executor, long timeout, TimeUnit unit) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeout, unit)) {
                List<Runnable> left = executor.shutdownNow();
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); // phase 2 waits without bound
                return left;
            }
            return List.of();
        } catch (InterruptedException e) {
            List<Runnable> left = executor.shutdownNow();
            Thread.currentThread().interrupt();
            return left;
        }
    }
}
