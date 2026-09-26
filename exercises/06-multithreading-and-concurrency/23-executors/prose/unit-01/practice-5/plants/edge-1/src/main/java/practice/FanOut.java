package practice;

import java.util.ArrayList;
import java.util.Collections;
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
        List<Future<?>> futures = new ArrayList<>();
        List<T> results = Collections.synchronizedList(new ArrayList<>());
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Callable<T> task : tasks) {
                futures.add(executor.submit(() -> results.add(task.call())));
            }
        }
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof Exception cause) {
                    throw cause;
                }
                throw e;
            }
        }
        return results;
    }
}
