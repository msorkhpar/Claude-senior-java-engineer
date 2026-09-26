package practice;

import java.util.List;
import java.util.function.Function;

public final class PoolRunner {

    private PoolRunner() {
    }

    /** Applies f to every item with a parallel stream inside a new pool; results in list order; the pool is shut down. */
    public static <T, R> List<R> mapInPool(List<T> items, int parallelism, Function<T, R> f) {
        throw new UnsupportedOperationException("write mapInPool");
    }
}
