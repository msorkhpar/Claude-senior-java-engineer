package practice;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

public final class PoolRunner {

    private PoolRunner() {
    }

    /** Applies f to every item with a parallel stream inside a new pool; results in list order; the pool is shut down. */
    public static <T, R> List<R> mapInPool(List<T> items, int parallelism, Function<T, R> f) {
        ForkJoinPool pool = new ForkJoinPool(parallelism);
        try {
            return items.parallelStream().map(f).toList();
        } finally {
            pool.shutdown();
        }
    }
}
