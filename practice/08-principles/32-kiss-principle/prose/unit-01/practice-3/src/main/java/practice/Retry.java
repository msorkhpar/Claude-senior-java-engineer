package practice;

import java.util.function.Supplier;

public final class Retry {

    private Retry() {
    }

    /** Calls the action until it succeeds, at most maxAttempts times. */
    public static <T> T executeWithRetry(Supplier<T> action, int maxAttempts) {
        throw new UnsupportedOperationException("write executeWithRetry");
    }
}
