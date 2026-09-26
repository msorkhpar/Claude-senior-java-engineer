package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Gather {

    private Gather() {
    }

    /** One result per task, in task order; a failed or unfinished task gives the fallback. */
    public static <T> List<T> all(ExecutorService executor, List<Callable<T>> tasks,
                                  long timeout, TimeUnit unit, T fallback) throws InterruptedException {
        List<Future<T>> futures = new ArrayList<>();
        for (Callable<T> task : tasks) {
            futures.add(executor.submit(task));
        }
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        List<T> results = new ArrayList<>();
        for (Future<T> future : futures) {
            try {
                results.add(future.get(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS));
            } catch (ExecutionException | TimeoutException e) {
                results.add(fallback);
            }
        }
        return results;
    }
}
