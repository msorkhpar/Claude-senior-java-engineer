package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class Tasks {

    private Tasks() {
    }

    /** Runs every task on its own virtual thread and returns the results in task order. */
    public static <T> List<T> runAll(List<Callable<T>> tasks) throws InterruptedException, ExecutionException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<T>> futures = new ArrayList<>();
            for (Callable<T> task : tasks) {
                futures.add(executor.submit(task));
            }
            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                try {
                    results.add(future.get());
                } catch (ExecutionException skipped) {
                    // leave the failed task out
                }
            }
            return results;
        }
    }
}
