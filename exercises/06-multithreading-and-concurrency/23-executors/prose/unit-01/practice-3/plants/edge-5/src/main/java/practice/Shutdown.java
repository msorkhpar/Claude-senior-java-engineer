package practice;
import java.util.*; import java.util.concurrent.*;
public final class Shutdown {
    private Shutdown() {}
    public static List<Runnable> close(ExecutorService executor, long timeout, TimeUnit unit) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeout, TimeUnit.SECONDS)) { // unit ignored
                List<Runnable> left = executor.shutdownNow();
                executor.awaitTermination(timeout, TimeUnit.SECONDS);
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
