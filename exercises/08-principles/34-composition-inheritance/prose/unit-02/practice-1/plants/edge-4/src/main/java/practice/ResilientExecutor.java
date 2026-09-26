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
        return attempt <= maxRetries;
    }

    @Override
    public long delayMillis(int attempt) {
        if (attempt <= 1 || initialDelayMs == 0) {
            return Math.min(initialDelayMs, maxDelayMs);
        }
        return Math.min(initialDelayMs << (attempt - 1), maxDelayMs);
    }
}

/** Runs a task, retrying it as the composed strategy decides. */
public class ResilientExecutor {
    private final RetryStrategy strategy;
    private final LongConsumer sleeper;
    private int attempts;

    public ResilientExecutor(RetryStrategy strategy, LongConsumer sleeper) {
        this.strategy = Objects.requireNonNull(strategy, "strategy");
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
    }

    public <T> T execute(Supplier<T> task) throws Exception {
        int attempt = 0;
        while (true) {
            attempt++;
            attempts++;
            try {
                return task.get();
            } catch (Exception e) {
                if (!strategy.shouldRetry(attempt, e)) {
                    throw e;
                }
                long delay = strategy.delayMillis(attempt);
                if (delay > 0) {
                    sleeper.accept(delay);
                }
            }
        }
    }

    /** How many times this executor has called a task, over every execute call. */
    public int attempts() {
        return attempts;
    }
}
