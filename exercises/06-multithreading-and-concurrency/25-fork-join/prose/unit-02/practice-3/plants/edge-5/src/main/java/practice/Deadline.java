package practice;
import java.util.Optional;
import java.util.concurrent.*;
public final class Deadline {
    private Deadline() {}
    public static <T> Optional<T> within(ForkJoinTask<T> task, int parallelism, long timeoutMillis) {
        ForkJoinPool pool = new ForkJoinPool(parallelism);
        try {
            return Optional.of(pool.submit(task).get(timeoutMillis, TimeUnit.MILLISECONDS));
        } catch (TimeoutException | InterruptedException e) {
            task.cancel(true); return Optional.empty();
        } catch (ExecutionException e) {
            throw new IllegalStateException(e.getCause());
        } finally {
            pool.shutdownNow();
        }
    }
}
