package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class Gather {

    private Gather() {
    }

    /** One result per task, in task order; a failed or unfinished task gives the fallback. */
    public static <T> List<T> all(ExecutorService executor, List<Callable<T>> tasks,
                                  long timeout, TimeUnit unit, T fallback) throws InterruptedException {
        List<T> results = new ArrayList<>();
        for (Future<T> future : executor.invokeAll(tasks, timeout, unit)) {
            try {
                results.add(future.get());
            } catch (ExecutionException | CancellationException e) {
                results.add(fallback);
            }
        }
        return results;
    }
}
