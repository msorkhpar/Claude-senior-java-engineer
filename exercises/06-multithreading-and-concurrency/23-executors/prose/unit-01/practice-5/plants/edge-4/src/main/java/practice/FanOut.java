package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class FanOut {

    private FanOut() {
    }

    /** Runs every task on its own virtual thread and returns the results in task order. */
    public static <T> List<T> all(List<Callable<T>> tasks) throws Exception {
        List<Future<T>> futures = new ArrayList<>();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Callable<T> task : tasks) {
                futures.add(executor.submit(task));
            }
        }
        List<T> results = new ArrayList<>();
        for (Future<T> future : futures) {
            try {
                results.add(future.get());
            } catch (ExecutionException e) {
                throw e;
            }
        }
        return results;
    }
}
