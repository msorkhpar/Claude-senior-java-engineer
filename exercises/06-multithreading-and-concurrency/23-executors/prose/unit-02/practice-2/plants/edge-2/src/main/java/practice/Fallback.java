package practice;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Fallback {

    private Fallback() {
    }

    /** The task's result if it arrives within the timeout, otherwise the fallback. */
    public static <T> T within(ExecutorService executor, Callable<T> task, long timeout, TimeUnit unit, T fallback) {
        Future<T> future = executor.submit(task);
        try {
            return future.get(timeout, unit);
        } catch (TimeoutException e) {
            future.cancel(true);
            return fallback;
        } catch (ExecutionException e) {
            throw new RuntimeException("Task failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            future.cancel(true);
            return fallback;
        }
    }
}
