package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class Totals {

    private Totals() {
    }

    /** Submits every part, then sums their results; a failed part surfaces as IllegalStateException caused by its own exception. */
    public static long total(ExecutorService pool, List<Callable<Integer>> parts) throws InterruptedException {
        long sum = 0;
        try {
            for (Callable<Integer> part : parts) {
                Future<Integer> future = pool.submit(part);
                sum += future.get();
            }
        } catch (ExecutionException e) {
            throw new IllegalStateException("a part failed", e.getCause());
        }
        return sum;
    }
}
