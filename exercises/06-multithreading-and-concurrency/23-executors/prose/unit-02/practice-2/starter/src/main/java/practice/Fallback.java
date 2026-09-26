package practice;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class Fallback {

    private Fallback() {
    }

    /** The task's result if it arrives within the timeout, otherwise the fallback. */
    public static <T> T within(ExecutorService executor, Callable<T> task, long timeout, TimeUnit unit, T fallback) {
        throw new UnsupportedOperationException("write within");
    }
}
