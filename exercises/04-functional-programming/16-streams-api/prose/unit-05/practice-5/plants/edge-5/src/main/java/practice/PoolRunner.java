package practice;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

public final class PoolRunner {

    private PoolRunner() {
    }

    public static <T, R> List<R> mapInPool(List<T> items, int parallelism, Function<T, R> f) {
        ForkJoinPool pool = new ForkJoinPool();
        try {
            return pool.submit(() -> items.parallelStream().map(f).toList()).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw new IllegalStateException(e.getCause());
        } finally {
            pool.shutdown();
        }
    }
}
