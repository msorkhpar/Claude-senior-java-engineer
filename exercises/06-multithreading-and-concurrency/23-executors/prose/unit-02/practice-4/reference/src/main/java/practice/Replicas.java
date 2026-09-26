package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

public final class Replicas {

    private Replicas() {
    }

    /** The first successful reply; the replicas still running are cancelled. */
    public static <T> T first(ExecutorService executor, List<Callable<T>> replicas)
            throws InterruptedException, ExecutionException {
        return executor.invokeAny(replicas);
    }

    /** Every task's result, in the order the tasks finished. */
    public static <T> List<T> inArrivalOrder(ExecutorService executor, List<Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        CompletionService<T> done = new ExecutorCompletionService<>(executor);
        for (Callable<T> task : tasks) {
            done.submit(task);
        }
        List<T> results = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            results.add(done.take().get());
        }
        return results;
    }
}
