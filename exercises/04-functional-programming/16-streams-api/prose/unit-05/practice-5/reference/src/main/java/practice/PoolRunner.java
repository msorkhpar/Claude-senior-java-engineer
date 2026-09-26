package practice;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

public final class PoolRunner {

    private PoolRunner() {
    }

    /** Applies f to every item with a parallel stream inside a new pool; results in list order; the pool is shut down. */
    public static <T, R> List<R> mapInPool(List<T> items, int parallelism, Function<T, R> f) {
        ForkJoinPool pool = new ForkJoinPool(parallelism);
        try {
            return pool.submit(() -> items.parallelStream().map(f).toList()).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while waiting for the pool", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException unchecked) {
                throw unchecked;
            }
            if (e.getCause() instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException(e.getCause());
        } finally {
            pool.shutdown();
        }
    }
}
