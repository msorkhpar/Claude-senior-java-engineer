package practice;

import java.util.Objects;
import java.util.concurrent.Callable;

/** One retry policy for transient failures, configured once and shared by every caller. */
public final class RetryExecutor {

    private final int maxRetries;
    private final long retryDelayMs;

    /** A policy that retries a failed action up to maxRetries times, waiting retryDelayMs between tries. */
    public RetryExecutor(int maxRetries, long retryDelayMs) {
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
    }

    /** Calls the action, retrying after a failure; returns its first result or throws its last failure. */
    public <T> T executeWithRetry(Callable<T> action) throws Exception {
        Objects.requireNonNull(action, "action must not be null");
        Exception lastException = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return action.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxRetries && retryDelayMs > 0) {
                    Thread.sleep(retryDelayMs);
                }
            }
        }
        throw lastException;
    }
}
