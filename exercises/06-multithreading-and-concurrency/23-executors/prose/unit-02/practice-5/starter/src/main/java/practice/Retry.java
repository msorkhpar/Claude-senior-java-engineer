package practice;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public final class Retry {

    private Retry() {
    }

    /** Submits the task up to maxRetries + 1 times and returns the first success. */
    public static <T> T call(ExecutorService executor, Callable<T> task, int maxRetries) throws Exception {
        throw new UnsupportedOperationException("write call");
    }
}
