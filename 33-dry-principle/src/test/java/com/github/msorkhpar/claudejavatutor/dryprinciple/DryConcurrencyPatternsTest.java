package com.github.msorkhpar.claudejavatutor.dryprinciple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DRY Concurrency Patterns Tests")
class DryConcurrencyPatternsTest {

    @Nested
    @DisplayName("Lock Violation - Duplicated Lock Pattern")
    class LockViolationTest {

        @Test
        @DisplayName("Should deposit and track balance")
        void testDeposit() {
            var account = new DryConcurrencyPatterns.LockViolation();
            assertThat(account.deposit(100)).isEqualTo(100);
            assertThat(account.deposit(50)).isEqualTo(150);
        }

        @Test
        @DisplayName("Should withdraw and track balance")
        void testWithdraw() {
            var account = new DryConcurrencyPatterns.LockViolation();
            account.deposit(200);
            assertThat(account.withdraw(50)).isEqualTo(150);
        }

        @Test
        @DisplayName("Should read balance")
        void testGetBalance() {
            var account = new DryConcurrencyPatterns.LockViolation();
            account.deposit(100);
            assertThat(account.getBalance()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("LockExecutor - Reusable Lock Pattern")
    class LockExecutorTest {

        @Test
        @DisplayName("Should execute supplier while holding lock")
        void testWithLock() {
            var executor = new DryConcurrencyPatterns.LockExecutor(new ReentrantLock());
            int result = executor.withLock(() -> 42);
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("Should execute runnable while holding lock")
        void testWithLockRun() {
            var executor = new DryConcurrencyPatterns.LockExecutor(new ReentrantLock());
            AtomicInteger counter = new AtomicInteger(0);

            executor.withLockRun(counter::incrementAndGet);

            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should release lock even when exception is thrown")
        void testLockReleasedOnException() {
            ReentrantLock lock = new ReentrantLock();
            var executor = new DryConcurrencyPatterns.LockExecutor(lock);

            assertThatRuntimeException()
                    .isThrownBy(() -> executor.withLock(() -> {
                        throw new RuntimeException("test error");
                    }));

            // Lock should be released -- we can acquire it
            assertThat(lock.tryLock()).isTrue();
            lock.unlock();
        }

        @Test
        @DisplayName("Should reject null lock")
        void testNullLock() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new DryConcurrencyPatterns.LockExecutor(null));
        }

        @Test
        @DisplayName("Should reject null action in withLock")
        void testNullActionWithLock() {
            var executor = new DryConcurrencyPatterns.LockExecutor(new ReentrantLock());
            assertThatNullPointerException()
                    .isThrownBy(() -> executor.withLock(null));
        }

        @Test
        @DisplayName("Should reject null action in withLockRun")
        void testNullActionWithLockRun() {
            var executor = new DryConcurrencyPatterns.LockExecutor(new ReentrantLock());
            assertThatNullPointerException()
                    .isThrownBy(() -> executor.withLockRun(null));
        }
    }

    @Nested
    @DisplayName("DryAccount - Thread-Safe Account Using LockExecutor")
    class DryAccountTest {

        @Test
        @DisplayName("Should deposit correctly")
        void testDeposit() {
            var account = new DryConcurrencyPatterns.DryAccount(0);
            assertThat(account.deposit(100)).isEqualTo(100);
            assertThat(account.deposit(200)).isEqualTo(300);
        }

        @Test
        @DisplayName("Should withdraw correctly")
        void testWithdraw() {
            var account = new DryConcurrencyPatterns.DryAccount(500);
            assertThat(account.withdraw(200)).isEqualTo(300);
        }

        @Test
        @DisplayName("Should read balance correctly")
        void testGetBalance() {
            var account = new DryConcurrencyPatterns.DryAccount(100);
            assertThat(account.getBalance()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should handle concurrent deposits safely")
        void testConcurrentDeposits() throws InterruptedException {
            var account = new DryConcurrencyPatterns.DryAccount(0);
            int threads = 10;
            int depositsPerThread = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threads);

            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < depositsPerThread; j++) {
                        account.deposit(1);
                    }
                });
            }

            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            assertThat(account.getBalance()).isEqualTo(threads * depositsPerThread);
        }
    }

    @Nested
    @DisplayName("ParallelComputation - Reusable Parallel Executor")
    class ParallelComputationTest {

        private final DryConcurrencyPatterns.ParallelComputation parallel =
                new DryConcurrencyPatterns.ParallelComputation();

        @Test
        @DisplayName("Should execute tasks in parallel and collect results")
        void testExecuteAll() throws InterruptedException {
            List<Callable<Integer>> tasks = List.of(() -> 1, () -> 2, () -> 3);

            List<Integer> results = parallel.executeAll(tasks, 2);

            assertThat(results).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Should handle empty task list")
        void testExecuteAllEmpty() throws InterruptedException {
            List<Integer> results = parallel.executeAll(Collections.emptyList(), 2);
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should reject null tasks")
        void testExecuteAllNullTasks() {
            assertThatNullPointerException()
                    .isThrownBy(() -> parallel.executeAll(null, 2));
        }

        @Test
        @DisplayName("Should reject non-positive thread count")
        void testExecuteAllInvalidThreadCount() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> parallel.executeAll(List.of(), 0))
                    .withMessageContaining("threadCount must be positive");
        }

        @Test
        @DisplayName("Should propagate task exceptions wrapped in RuntimeException")
        void testExecuteAllTaskException() {
            List<Callable<Integer>> tasks = List.of(() -> {
                throw new Exception("task failed");
            });

            assertThatRuntimeException()
                    .isThrownBy(() -> parallel.executeAll(tasks, 1))
                    .withMessageContaining("Task execution failed");
        }
    }

    @Nested
    @DisplayName("DryComputations - Using Reusable Parallel Executor")
    class DryComputationsTest {

        private final DryConcurrencyPatterns.DryComputations computations =
                new DryConcurrencyPatterns.DryComputations();

        @Test
        @DisplayName("Should compute squares")
        void testComputeSquares() throws InterruptedException {
            List<Integer> result = computations.computeSquares(List.of(2, 3, 4));
            assertThat(result).containsExactly(4, 9, 16);
        }

        @Test
        @DisplayName("Should compute cubes")
        void testComputeCubes() throws InterruptedException {
            List<Integer> result = computations.computeCubes(List.of(2, 3, 4));
            assertThat(result).containsExactly(8, 27, 64);
        }

        @Test
        @DisplayName("Should handle empty input for squares")
        void testComputeSquaresEmpty() throws InterruptedException {
            List<Integer> result = computations.computeSquares(List.of());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle single element")
        void testComputeSquaresSingle() throws InterruptedException {
            List<Integer> result = computations.computeSquares(List.of(5));
            assertThat(result).containsExactly(25);
        }
    }

    @Nested
    @DisplayName("RetryExecutor - Reusable Retry Mechanism")
    class RetryExecutorTest {

        @Test
        @DisplayName("Should succeed on first attempt")
        void testSuccessFirstAttempt() throws Exception {
            var retry = new DryConcurrencyPatterns.RetryExecutor(3, 0);
            int result = retry.executeWithRetry(() -> 42);
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("Should retry and succeed on later attempt")
        void testRetryAndSucceed() throws Exception {
            var retry = new DryConcurrencyPatterns.RetryExecutor(3, 0);
            AtomicInteger attempts = new AtomicInteger(0);

            int result = retry.executeWithRetry(() -> {
                if (attempts.incrementAndGet() < 3) {
                    throw new RuntimeException("transient failure");
                }
                return 99;
            });

            assertThat(result).isEqualTo(99);
            assertThat(attempts.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should throw after exhausting all retries")
        void testExhaustRetries() {
            var retry = new DryConcurrencyPatterns.RetryExecutor(2, 0);

            assertThatRuntimeException()
                    .isThrownBy(() -> retry.executeWithRetry(() -> {
                        throw new RuntimeException("permanent failure");
                    }))
                    .withMessage("permanent failure");
        }

        @Test
        @DisplayName("Should reject negative maxRetries")
        void testNegativeMaxRetries() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new DryConcurrencyPatterns.RetryExecutor(-1, 0));
        }

        @Test
        @DisplayName("Should reject negative retryDelayMs")
        void testNegativeRetryDelay() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new DryConcurrencyPatterns.RetryExecutor(1, -1));
        }

        @Test
        @DisplayName("Should reject null action")
        void testNullAction() {
            var retry = new DryConcurrencyPatterns.RetryExecutor(1, 0);
            assertThatNullPointerException()
                    .isThrownBy(() -> retry.executeWithRetry(null));
        }

        @Test
        @DisplayName("Should succeed with zero retries allowed")
        void testZeroRetriesSuccess() throws Exception {
            var retry = new DryConcurrencyPatterns.RetryExecutor(0, 0);
            assertThat(retry.executeWithRetry(() -> 1)).isEqualTo(1);
        }

        @Test
        @DisplayName("Should fail immediately with zero retries on failure")
        void testZeroRetriesFailure() {
            var retry = new DryConcurrencyPatterns.RetryExecutor(0, 0);
            assertThatRuntimeException()
                    .isThrownBy(() -> retry.executeWithRetry(() -> {
                        throw new RuntimeException("fail");
                    }));
        }
    }

    @Nested
    @DisplayName("ThreadSafeCache - Reusable Cache Pattern")
    class ThreadSafeCacheTest {

        @Test
        @DisplayName("Should compute and cache value")
        void testGetOrCompute() {
            var cache = new DryConcurrencyPatterns.ThreadSafeCache<String, Integer>();
            int result = cache.getOrCompute("hello", String::length);
            assertThat(result).isEqualTo(5);
            assertThat(cache.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return cached value on subsequent calls")
        void testGetOrComputeCached() {
            var cache = new DryConcurrencyPatterns.ThreadSafeCache<String, Integer>();
            AtomicInteger computeCount = new AtomicInteger(0);

            cache.getOrCompute("key", k -> {
                computeCount.incrementAndGet();
                return 42;
            });
            cache.getOrCompute("key", k -> {
                computeCount.incrementAndGet();
                return 99;
            });

            assertThat(cache.getOrCompute("key", k -> 0)).isEqualTo(42);
            assertThat(computeCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should get existing value")
        void testGet() {
            var cache = new DryConcurrencyPatterns.ThreadSafeCache<String, String>();
            cache.getOrCompute("key", k -> "value");

            assertThat(cache.get("key")).isPresent().contains("value");
            assertThat(cache.get("missing")).isEmpty();
        }

        @Test
        @DisplayName("Should invalidate cached value")
        void testInvalidate() {
            var cache = new DryConcurrencyPatterns.ThreadSafeCache<String, String>();
            cache.getOrCompute("key", k -> "value");
            cache.invalidate("key");

            assertThat(cache.get("key")).isEmpty();
            assertThat(cache.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should clear all cached values")
        void testClear() {
            var cache = new DryConcurrencyPatterns.ThreadSafeCache<String, String>();
            cache.getOrCompute("a", k -> "1");
            cache.getOrCompute("b", k -> "2");
            cache.clear();

            assertThat(cache.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should reject null key")
        void testNullKey() {
            var cache = new DryConcurrencyPatterns.ThreadSafeCache<String, String>();
            assertThatNullPointerException()
                    .isThrownBy(() -> cache.getOrCompute(null, k -> "value"));
        }

        @Test
        @DisplayName("Should reject null compute function")
        void testNullComputeFunction() {
            var cache = new DryConcurrencyPatterns.ThreadSafeCache<String, String>();
            assertThatNullPointerException()
                    .isThrownBy(() -> cache.getOrCompute("key", null));
        }

        @Test
        @DisplayName("Should handle concurrent access safely")
        void testConcurrentAccess() throws InterruptedException {
            var cache = new DryConcurrencyPatterns.ThreadSafeCache<Integer, Integer>();
            int threads = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threads);

            for (int i = 0; i < threads; i++) {
                final int key = i;
                executor.submit(() -> cache.getOrCompute(key, k -> k * k));
            }

            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            assertThat(cache.size()).isEqualTo(threads);
            assertThat(cache.get(3)).isPresent().contains(9);
        }
    }
}
