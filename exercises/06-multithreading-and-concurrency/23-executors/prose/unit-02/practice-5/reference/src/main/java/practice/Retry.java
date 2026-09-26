package practice;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Retry {

    private Retry() {
    }

    /** Submits the task up to maxRetries + 1 times and returns the first success. */
    public static <T> T call(ExecutorService executor, Callable<T> task, int maxRetries) throws Exception {
        Exception last = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            Future<T> future = executor.submit(task);
            try {
                return future.get(5, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                last = e.getCause() instanceof Exception cause ? cause : e;
            } catch (TimeoutException e) {
                future.cancel(true);
                last = e;
            }
        }
        throw last;
    }
}
