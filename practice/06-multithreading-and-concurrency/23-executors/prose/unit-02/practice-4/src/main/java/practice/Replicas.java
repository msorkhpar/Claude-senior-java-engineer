package practice;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public final class Replicas {

    private Replicas() {
    }

    /** The first successful reply; the replicas still running are cancelled. */
    public static <T> T first(ExecutorService executor, List<Callable<T>> replicas)
            throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException("write first");
    }

    /** Every task's result, in the order the tasks finished. */
    public static <T> List<T> inArrivalOrder(ExecutorService executor, List<Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException("write inArrivalOrder");
    }
}
