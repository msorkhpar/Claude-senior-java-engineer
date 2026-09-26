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
        throw new UnsupportedOperationException("write executeWithRetry");
    }
}
