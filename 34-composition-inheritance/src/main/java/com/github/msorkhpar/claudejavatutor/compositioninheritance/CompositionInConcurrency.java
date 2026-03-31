package com.github.msorkhpar.claudejavatutor.compositioninheritance;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Demonstrates applying Composition over Inheritance in concurrent programming (8.4.2).
 * Shows how composition favors flexibility and avoids deep inheritance hierarchies
 * in multi-threaded environments.
 */
public class CompositionInConcurrency {

    // ---- 8.4.2.1 Favoring object composition for flexibility ----

    /**
     * A task execution strategy interface. Instead of creating subclasses of Thread
     * or extending ExecutorService, compose behaviors via strategy objects.
     */
    public interface RetryStrategy {
        boolean shouldRetry(int attempt, Exception lastException);
        long delayMillis(int attempt);
    }

    /**
     * Fixed-delay retry strategy, composed into the executor rather than inherited.
     */
    public static class FixedRetryStrategy implements RetryStrategy {
        private final int maxRetries;
        private final long delayMs;

        public FixedRetryStrategy(int maxRetries, long delayMs) {
            if (maxRetries < 0) throw new IllegalArgumentException("maxRetries must be >= 0");
            if (delayMs < 0) throw new IllegalArgumentException("delayMs must be >= 0");
            this.maxRetries = maxRetries;
            this.delayMs = delayMs;
        }

        @Override
        public boolean shouldRetry(int attempt, Exception lastException) {
            return attempt <= maxRetries;
        }

        @Override
        public long delayMillis(int attempt) {
            return delayMs;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public long getDelayMs() {
            return delayMs;
        }
    }

    /**
     * Exponential backoff retry strategy.
     */
    public static class ExponentialBackoffStrategy implements RetryStrategy {
        private final int maxRetries;
        private final long initialDelayMs;
        private final long maxDelayMs;

        public ExponentialBackoffStrategy(int maxRetries, long initialDelayMs, long maxDelayMs) {
            if (maxRetries < 0) throw new IllegalArgumentException("maxRetries must be >= 0");
            if (initialDelayMs < 0) throw new IllegalArgumentException("initialDelayMs must be >= 0");
            if (maxDelayMs < 0) throw new IllegalArgumentException("maxDelayMs must be >= 0");
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
            long delay = initialDelayMs * (long) Math.pow(2, attempt - 1);
            return Math.min(delay, maxDelayMs);
        }

        public int getMaxRetries() {
            return maxRetries;
        }
    }

    /**
     * A resilient task executor that composes a RetryStrategy rather than
     * inheriting from an executor class. This makes it easy to swap retry
     * strategies without modifying the executor.
     */
    public static class ResilientExecutor {
        private final RetryStrategy retryStrategy;
        private final AtomicInteger totalAttempts = new AtomicInteger(0);

        public ResilientExecutor(RetryStrategy retryStrategy) {
            Objects.requireNonNull(retryStrategy, "RetryStrategy must not be null");
            this.retryStrategy = retryStrategy;
        }

        /**
         * Executes the given task with retry logic based on the composed strategy.
         * Returns the result or throws the last exception if all retries are exhausted.
         */
        public <T> T execute(Supplier<T> task) throws Exception {
            Objects.requireNonNull(task, "Task must not be null");
            int attempt = 0;
            Exception lastException = null;

            while (true) {
                attempt++;
                totalAttempts.incrementAndGet();
                try {
                    return task.get();
                } catch (Exception e) {
                    lastException = e;
                    if (!retryStrategy.shouldRetry(attempt, e)) {
                        throw lastException;
                    }
                    long delay = retryStrategy.delayMillis(attempt);
                    if (delay > 0) {
                        Thread.sleep(delay);
                    }
                }
            }
        }

        public int getTotalAttempts() {
            return totalAttempts.get();
        }
    }

    // ---- 8.4.2.2 Avoiding deep inheritance hierarchies ----

    /**
     * Instead of a deep hierarchy like:
     *   Thread -> WorkerThread -> RetryableWorkerThread -> LoggingRetryableWorkerThread
     * we compose behaviors as decorators around a common interface.
     */
    public interface TaskRunner {
        String run(String input) throws Exception;
    }

    /**
     * A basic task runner that transforms input to uppercase.
     */
    public static class UpperCaseRunner implements TaskRunner {
        @Override
        public String run(String input) throws Exception {
            if (input == null) {
                throw new IllegalArgumentException("Input must not be null");
            }
            return input.toUpperCase();
        }
    }

    /**
     * Logging decorator that wraps any TaskRunner, adding logging behavior
     * without requiring inheritance from the base runner.
     */
    public static class LoggingTaskRunner implements TaskRunner {
        private final TaskRunner delegate;
        private final List<String> logs = new CopyOnWriteArrayList<>();

        public LoggingTaskRunner(TaskRunner delegate) {
            Objects.requireNonNull(delegate, "Delegate must not be null");
            this.delegate = delegate;
        }

        @Override
        public String run(String input) throws Exception {
            logs.add("START: " + input);
            try {
                String result = delegate.run(input);
                logs.add("SUCCESS: " + result);
                return result;
            } catch (Exception e) {
                logs.add("ERROR: " + e.getMessage());
                throw e;
            }
        }

        public List<String> getLogs() {
            return Collections.unmodifiableList(logs);
        }
    }

    /**
     * Thread-safety decorator that wraps any TaskRunner with a lock,
     * making it safe for concurrent access without inheriting from any thread class.
     */
    public static class SynchronizedTaskRunner implements TaskRunner {
        private final TaskRunner delegate;
        private final ReentrantLock lock = new ReentrantLock();

        public SynchronizedTaskRunner(TaskRunner delegate) {
            Objects.requireNonNull(delegate, "Delegate must not be null");
            this.delegate = delegate;
        }

        @Override
        public String run(String input) throws Exception {
            lock.lock();
            try {
                return delegate.run(input);
            } finally {
                lock.unlock();
            }
        }

        public boolean isLocked() {
            return lock.isLocked();
        }
    }

    /**
     * Demonstrates composing multiple decorators to build complex behavior
     * from simple, single-responsibility components.
     */
    public static TaskRunner compose(TaskRunner base, boolean logging, boolean threadSafe) {
        TaskRunner runner = base;
        if (logging) {
            runner = new LoggingTaskRunner(runner);
        }
        if (threadSafe) {
            runner = new SynchronizedTaskRunner(runner);
        }
        return runner;
    }
}
