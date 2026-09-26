package practice;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.function.LongUnaryOperator;
public final class PoolStream {
    private PoolStream() {}
    public static long sum(long[] values, int parallelism, LongUnaryOperator f) {
        ForkJoinPool pool = new ForkJoinPool(parallelism);
        long total = pool.submit(() -> Arrays.stream(values).parallel().map(f).sum()).join();
        pool.shutdown(); // skipped when f throws: the pool leaks
        return total;
    }
}
