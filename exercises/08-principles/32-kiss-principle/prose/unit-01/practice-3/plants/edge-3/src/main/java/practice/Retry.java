package practice;

import java.util.function.Supplier;

public final class Retry {

    private Retry() {
    }

    /** Calls the action until it succeeds, at most maxAttempts times. */
    public static <T> T executeWithRetry(Supplier<T> action, int maxAttempts) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be positive");
        }
        Throwable lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.get();
            } catch (Throwable e) {
                lastException = e;
            }
        }
        if (lastException instanceof RuntimeException r) {
            throw r;
        }
        throw (Error) lastException;
    }
}
