package practice;

import java.util.Objects;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

/** A retry policy: whether to try again after a failed attempt, and how long to wait first. */
interface RetryStrategy {
    boolean shouldRetry(int attempt, Exception lastException);

    long delayMillis(int attempt);
}

/** Retries up to maxRetries times, waiting initialDelayMs * 2^(attempt - 1), capped at maxDelayMs. */
class ExponentialBackoff implements RetryStrategy {
    private final int maxRetries;
    private final long initialDelayMs;
    private final long maxDelayMs;

    ExponentialBackoff(int maxRetries, long initialDelayMs, long maxDelayMs) {
        this.maxRetries = maxRetries;
        this.initialDelayMs = initialDelayMs;
        this.maxDelayMs = maxDelayMs;
    }

    @Override
    public boolean shouldRetry(int attempt, Exception lastException) {
        throw new UnsupportedOperationException("write shouldRetry");
    }

    @Override
    public long delayMillis(int attempt) {
        throw new UnsupportedOperationException("write delayMillis");
    }
}

/** Runs a task, retrying it as the composed strategy decides. */
public class ResilientExecutor {

    public ResilientExecutor(RetryStrategy strategy, LongConsumer sleeper) {
        throw new UnsupportedOperationException("write the constructor");
    }

    public <T> T execute(Supplier<T> task) throws Exception {
        throw new UnsupportedOperationException("write execute");
    }

    /** How many times this executor has called a task, over every execute call. */
    public int attempts() {
        throw new UnsupportedOperationException("write attempts");
    }
}
