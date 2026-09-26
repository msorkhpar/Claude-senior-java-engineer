package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class Gather {

    private Gather() {
    }

    /** One result per task, in task order; a failed or unfinished task gives the fallback. */
    public static <T> List<T> all(ExecutorService executor, List<Callable<T>> tasks,
                                  long timeout, TimeUnit unit, T fallback) throws InterruptedException {
        CompletionService<T> done = new ExecutorCompletionService<>(executor);
        List<Future<T>> futures = new ArrayList<>();
        for (Callable<T> task : tasks) {
            futures.add(done.submit(task));
        }
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        List<T> results = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            Future<T> next = done.poll(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
            if (next == null) {
                results.add(fallback);
                continue;
            }
            try {
                results.add(next.get());
            } catch (ExecutionException e) {
                results.add(fallback);
            }
        }
        for (Future<T> future : futures) {
            future.cancel(true);
        }
        return results;
    }
}
