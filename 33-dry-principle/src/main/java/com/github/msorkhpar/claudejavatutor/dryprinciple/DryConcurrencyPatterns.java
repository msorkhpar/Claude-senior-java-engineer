package com.github.msorkhpar.claudejavatutor.dryprinciple;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Demonstrates applying DRY in concurrent programming.
 * Covers extracting reusable concurrency patterns and avoiding duplication of synchronization logic.
 */
public class DryConcurrencyPatterns {

    // ========== 8.3.2.1: Extracting Reusable Concurrency Patterns ==========

    /**
     * VIOLATION: Duplicated try-finally lock pattern in every method.
     */
    public static class LockViolation {
        private final Lock lock = new ReentrantLock();
        private int balance = 0;

        public int deposit(int amount) {
            lock.lock();
            try {
                balance += amount;
                return balance;
            } finally {
                lock.unlock();
            }
        }

        public int withdraw(int amount) {
            lock.lock();
            try {
                balance -= amount;
                return balance;
            } finally {
                lock.unlock();
            }
        }

        public int getBalance() {
            lock.lock();
            try {
                return balance;
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * DRY: Reusable lock execution utility that extracts the lock/try/finally pattern.
     */
    public static class LockExecutor {

        private final Lock lock;

        public LockExecutor(Lock lock) {
            this.lock = Objects.requireNonNull(lock, "lock must not be null");
        }

        /**
         * Executes a supplier while holding the lock.
         */
        public <T> T withLock(Supplier<T> action) {
            Objects.requireNonNull(action, "action must not be null");
            lock.lock();
            try {
                return action.get();
            } finally {
                lock.unlock();
            }
        }

        /**
         * Executes a runnable while holding the lock.
         */
        public void withLockRun(Runnable action) {
            Objects.requireNonNull(action, "action must not be null");
            lock.lock();
            try {
                action.run();
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * DRY: Account using the reusable LockExecutor -- no repeated lock patterns.
     */
    public static class DryAccount {
        private final LockExecutor lockExecutor = new LockExecutor(new ReentrantLock());
        private int balance;

        public DryAccount(int initialBalance) {
            this.balance = initialBalance;
        }

        public int deposit(int amount) {
            return lockExecutor.withLock(() -> {
                balance += amount;
                return balance;
            });
        }

        public int withdraw(int amount) {
            return lockExecutor.withLock(() -> {
                balance -= amount;
                return balance;
            });
        }

        public int getBalance() {
            return lockExecutor.withLock(() -> balance);
        }
    }

    // ========== 8.3.2.2: Avoiding Duplication of Synchronization Logic ==========

    /**
     * VIOLATION: Duplicated ExecutorService lifecycle management.
     */
    public static class ExecutorViolation {

        public List<Integer> computeSquares(List<Integer> numbers) throws InterruptedException {
            ExecutorService executor = Executors.newFixedThreadPool(4);
            List<Future<Integer>> futures = new ArrayList<>();
            for (int n : numbers) {
                futures.add(executor.submit(() -> n * n));
            }
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            List<Integer> results = new ArrayList<>();
            for (Future<Integer> f : futures) {
                try {
                    results.add(f.get());
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            return results;
        }

        public List<Integer> computeCubes(List<Integer> numbers) throws InterruptedException {
            // Duplicated executor lifecycle!
            ExecutorService executor = Executors.newFixedThreadPool(4);
            List<Future<Integer>> futures = new ArrayList<>();
            for (int n : numbers) {
                futures.add(executor.submit(() -> n * n * n));
            }
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            List<Integer> results = new ArrayList<>();
            for (Future<Integer> f : futures) {
                try {
                    results.add(f.get());
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            return results;
        }
    }

    /**
     * DRY: Reusable parallel computation utility.
     */
    public static class ParallelComputation {

        /**
         * Executes a list of callables in parallel using a thread pool and collects results.
         */
        public <T> List<T> executeAll(List<Callable<T>> tasks, int threadCount)
                throws InterruptedException {
            Objects.requireNonNull(tasks, "tasks must not be null");
            if (threadCount <= 0) {
                throw new IllegalArgumentException("threadCount must be positive");
            }

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            try {
                List<Future<T>> futures = executor.invokeAll(tasks);
                List<T> results = new ArrayList<>();
                for (Future<T> f : futures) {
                    try {
                        results.add(f.get());
                    } catch (ExecutionException e) {
                        throw new RuntimeException("Task execution failed", e);
                    }
                }
                return results;
            } finally {
                executor.shutdown();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * DRY: Computations using the reusable parallel executor.
     */
    public static class DryComputations {

        private final ParallelComputation parallelComputation = new ParallelComputation();

        public List<Integer> computeSquares(List<Integer> numbers) throws InterruptedException {
            List<Callable<Integer>> tasks = numbers.stream()
                    .<Callable<Integer>>map(n -> () -> n * n)
                    .toList();
            return parallelComputation.executeAll(tasks, 4);
        }

        public List<Integer> computeCubes(List<Integer> numbers) throws InterruptedException {
            List<Callable<Integer>> tasks = numbers.stream()
                    .<Callable<Integer>>map(n -> () -> n * n * n)
                    .toList();
            return parallelComputation.executeAll(tasks, 4);
        }
    }

    // ========== Reusable Retry Pattern ==========

    /**
     * DRY: A reusable retry mechanism for transient failures in concurrent systems.
     */
    public static class RetryExecutor {

        private final int maxRetries;
        private final long retryDelayMs;

        public RetryExecutor(int maxRetries, long retryDelayMs) {
            if (maxRetries < 0) {
                throw new IllegalArgumentException("maxRetries must not be negative");
            }
            if (retryDelayMs < 0) {
                throw new IllegalArgumentException("retryDelayMs must not be negative");
            }
            this.maxRetries = maxRetries;
            this.retryDelayMs = retryDelayMs;
        }

        /**
         * Executes an action with retries on failure.
         */
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

    // ========== Thread-Safe Singleton Cache Pattern ==========

    /**
     * DRY: Reusable thread-safe lazy cache pattern using ConcurrentHashMap.
     */
    public static class ThreadSafeCache<K, V> {

        private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();

        public V getOrCompute(K key, java.util.function.Function<K, V> computeFunction) {
            Objects.requireNonNull(key, "key must not be null");
            Objects.requireNonNull(computeFunction, "computeFunction must not be null");
            return cache.computeIfAbsent(key, computeFunction);
        }

        public Optional<V> get(K key) {
            return Optional.ofNullable(cache.get(key));
        }

        public void invalidate(K key) {
            cache.remove(key);
        }

        public int size() {
            return cache.size();
        }

        public void clear() {
            cache.clear();
        }
    }
}
