package com.github.msorkhpar.claudejavatutor.compositioninheritance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Composition in Concurrency Tests (8.4.2)")
class CompositionInConcurrencyTest {

    // ---- RetryStrategy implementations ----

    @Nested
    @DisplayName("FixedRetryStrategy")
    class FixedRetryStrategyTest {

        @Test
        @DisplayName("Should allow retry up to max retries")
        void testShouldRetryWithinLimit() {
            var strategy = new CompositionInConcurrency.FixedRetryStrategy(3, 10);

            assertThat(strategy.shouldRetry(1, new RuntimeException())).isTrue();
            assertThat(strategy.shouldRetry(2, new RuntimeException())).isTrue();
            assertThat(strategy.shouldRetry(3, new RuntimeException())).isTrue();
            assertThat(strategy.shouldRetry(4, new RuntimeException())).isFalse();
        }

        @Test
        @DisplayName("Should return fixed delay")
        void testFixedDelay() {
            var strategy = new CompositionInConcurrency.FixedRetryStrategy(3, 100);

            assertThat(strategy.delayMillis(1)).isEqualTo(100);
            assertThat(strategy.delayMillis(2)).isEqualTo(100);
            assertThat(strategy.delayMillis(3)).isEqualTo(100);
        }

        @Test
        @DisplayName("Should reject negative maxRetries")
        void testNegativeMaxRetries() {
            assertThatThrownBy(() -> new CompositionInConcurrency.FixedRetryStrategy(-1, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("maxRetries must be >= 0");
        }

        @Test
        @DisplayName("Should reject negative delay")
        void testNegativeDelay() {
            assertThatThrownBy(() -> new CompositionInConcurrency.FixedRetryStrategy(3, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("delayMs must be >= 0");
        }

        @Test
        @DisplayName("Should allow zero retries")
        void testZeroRetries() {
            var strategy = new CompositionInConcurrency.FixedRetryStrategy(0, 10);

            assertThat(strategy.shouldRetry(0, new RuntimeException())).isTrue();
            assertThat(strategy.shouldRetry(1, new RuntimeException())).isFalse();
        }

        @Test
        @DisplayName("Should expose getters")
        void testGetters() {
            var strategy = new CompositionInConcurrency.FixedRetryStrategy(5, 200);
            assertThat(strategy.getMaxRetries()).isEqualTo(5);
            assertThat(strategy.getDelayMs()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("ExponentialBackoffStrategy")
    class ExponentialBackoffStrategyTest {

        @Test
        @DisplayName("Should calculate exponential delays")
        void testExponentialDelay() {
            var strategy = new CompositionInConcurrency.ExponentialBackoffStrategy(5, 100, 10000);

            assertThat(strategy.delayMillis(1)).isEqualTo(100);
            assertThat(strategy.delayMillis(2)).isEqualTo(200);
            assertThat(strategy.delayMillis(3)).isEqualTo(400);
            assertThat(strategy.delayMillis(4)).isEqualTo(800);
        }

        @Test
        @DisplayName("Should cap delay at maxDelayMs")
        void testMaxDelayCap() {
            var strategy = new CompositionInConcurrency.ExponentialBackoffStrategy(10, 100, 500);

            assertThat(strategy.delayMillis(1)).isEqualTo(100);
            assertThat(strategy.delayMillis(4)).isEqualTo(500); // 800 capped to 500
            assertThat(strategy.delayMillis(10)).isEqualTo(500);
        }

        @Test
        @DisplayName("Should respect max retries")
        void testMaxRetries() {
            var strategy = new CompositionInConcurrency.ExponentialBackoffStrategy(2, 100, 10000);

            assertThat(strategy.shouldRetry(1, new RuntimeException())).isTrue();
            assertThat(strategy.shouldRetry(2, new RuntimeException())).isTrue();
            assertThat(strategy.shouldRetry(3, new RuntimeException())).isFalse();
        }

        @Test
        @DisplayName("Should reject negative parameters")
        void testNegativeParameters() {
            assertThatThrownBy(() -> new CompositionInConcurrency.ExponentialBackoffStrategy(-1, 100, 1000))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new CompositionInConcurrency.ExponentialBackoffStrategy(3, -1, 1000))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new CompositionInConcurrency.ExponentialBackoffStrategy(3, 100, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ---- ResilientExecutor ----

    @Nested
    @DisplayName("ResilientExecutor")
    class ResilientExecutorTest {

        @Test
        @DisplayName("Should execute task successfully on first attempt")
        void testSuccessFirstAttempt() throws Exception {
            var executor = new CompositionInConcurrency.ResilientExecutor(
                    new CompositionInConcurrency.FixedRetryStrategy(3, 0));

            String result = executor.execute(() -> "success");

            assertThat(result).isEqualTo("success");
            assertThat(executor.getTotalAttempts()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should retry and eventually succeed")
        void testRetryAndSucceed() throws Exception {
            AtomicInteger callCount = new AtomicInteger(0);
            var executor = new CompositionInConcurrency.ResilientExecutor(
                    new CompositionInConcurrency.FixedRetryStrategy(3, 0));

            String result = executor.execute(() -> {
                if (callCount.incrementAndGet() < 3) {
                    throw new RuntimeException("transient failure");
                }
                return "eventually succeeded";
            });

            assertThat(result).isEqualTo("eventually succeeded");
            assertThat(executor.getTotalAttempts()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should throw after exhausting retries")
        void testExhaustedRetries() {
            var executor = new CompositionInConcurrency.ResilientExecutor(
                    new CompositionInConcurrency.FixedRetryStrategy(2, 0));

            assertThatThrownBy(() -> executor.execute(() -> {
                throw new RuntimeException("persistent failure");
            })).isInstanceOf(RuntimeException.class)
                    .hasMessage("persistent failure");

            assertThat(executor.getTotalAttempts()).isEqualTo(3); // 1 initial + 2 retries
        }

        @Test
        @DisplayName("Should reject null strategy")
        void testNullStrategy() {
            assertThatThrownBy(() -> new CompositionInConcurrency.ResilientExecutor(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("RetryStrategy must not be null");
        }

        @Test
        @DisplayName("Should reject null task")
        void testNullTask() {
            var executor = new CompositionInConcurrency.ResilientExecutor(
                    new CompositionInConcurrency.FixedRetryStrategy(1, 0));

            assertThatThrownBy(() -> executor.execute(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Task must not be null");
        }

        @Test
        @DisplayName("Should work with exponential backoff strategy")
        void testWithExponentialBackoff() throws Exception {
            AtomicInteger callCount = new AtomicInteger(0);
            var executor = new CompositionInConcurrency.ResilientExecutor(
                    new CompositionInConcurrency.ExponentialBackoffStrategy(3, 1, 10));

            String result = executor.execute(() -> {
                if (callCount.incrementAndGet() < 2) {
                    throw new RuntimeException("fail once");
                }
                return "ok";
            });

            assertThat(result).isEqualTo("ok");
            assertThat(executor.getTotalAttempts()).isEqualTo(2);
        }
    }

    // ---- TaskRunner decorators ----

    @Nested
    @DisplayName("TaskRunner Decorators (avoiding deep hierarchies)")
    class TaskRunnerDecoratorTest {

        @Test
        @DisplayName("Should run UpperCaseRunner")
        void testUpperCaseRunner() throws Exception {
            var runner = new CompositionInConcurrency.UpperCaseRunner();

            assertThat(runner.run("hello")).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("Should reject null input in UpperCaseRunner")
        void testUpperCaseRunnerNullInput() {
            var runner = new CompositionInConcurrency.UpperCaseRunner();

            assertThatThrownBy(() -> runner.run(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Input must not be null");
        }

        @Test
        @DisplayName("Should add logging via composition")
        void testLoggingDecorator() throws Exception {
            var logging = new CompositionInConcurrency.LoggingTaskRunner(
                    new CompositionInConcurrency.UpperCaseRunner());

            String result = logging.run("test");

            assertThat(result).isEqualTo("TEST");
            assertThat(logging.getLogs()).containsExactly("START: test", "SUCCESS: TEST");
        }

        @Test
        @DisplayName("Should log errors via LoggingTaskRunner")
        void testLoggingDecoratorOnError() {
            var logging = new CompositionInConcurrency.LoggingTaskRunner(
                    new CompositionInConcurrency.UpperCaseRunner());

            assertThatThrownBy(() -> logging.run(null))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(logging.getLogs()).containsExactly("START: null", "ERROR: Input must not be null");
        }

        @Test
        @DisplayName("Should compose logging and synchronization")
        void testComposedDecorators() throws Exception {
            var runner = CompositionInConcurrency.compose(
                    new CompositionInConcurrency.UpperCaseRunner(), true, true);

            String result = runner.run("composed");

            assertThat(result).isEqualTo("COMPOSED");
        }

        @Test
        @DisplayName("Should handle concurrent access with SynchronizedTaskRunner")
        void testSynchronizedRunner() throws Exception {
            var base = new CompositionInConcurrency.UpperCaseRunner();
            var syncRunner = new CompositionInConcurrency.SynchronizedTaskRunner(base);
            int threadCount = 10;
            var latch = new CountDownLatch(threadCount);
            var results = new CopyOnWriteArrayList<String>();

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < threadCount; i++) {
                    final String input = "thread" + i;
                    executor.submit(() -> {
                        try {
                            results.add(syncRunner.run(input));
                        } catch (Exception e) {
                            // should not happen
                        } finally {
                            latch.countDown();
                        }
                    });
                }
            }

            latch.await(5, TimeUnit.SECONDS);
            assertThat(results).hasSize(threadCount);
            for (int i = 0; i < threadCount; i++) {
                assertThat(results).contains("THREAD" + i);
            }
        }

        @Test
        @DisplayName("Should reject null delegate in LoggingTaskRunner")
        void testLoggingNullDelegate() {
            assertThatThrownBy(() -> new CompositionInConcurrency.LoggingTaskRunner(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should reject null delegate in SynchronizedTaskRunner")
        void testSynchronizedNullDelegate() {
            assertThatThrownBy(() -> new CompositionInConcurrency.SynchronizedTaskRunner(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should return unmodifiable logs list")
        void testUnmodifiableLogs() throws Exception {
            var logging = new CompositionInConcurrency.LoggingTaskRunner(
                    new CompositionInConcurrency.UpperCaseRunner());
            logging.run("test");

            assertThatThrownBy(() -> logging.getLogs().add("hack"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should compose with only logging")
        void testComposeLoggingOnly() throws Exception {
            var runner = CompositionInConcurrency.compose(
                    new CompositionInConcurrency.UpperCaseRunner(), true, false);

            assertThat(runner).isInstanceOf(CompositionInConcurrency.LoggingTaskRunner.class);
            assertThat(runner.run("hello")).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("Should compose with only synchronization")
        void testComposeSyncOnly() throws Exception {
            var runner = CompositionInConcurrency.compose(
                    new CompositionInConcurrency.UpperCaseRunner(), false, true);

            assertThat(runner).isInstanceOf(CompositionInConcurrency.SynchronizedTaskRunner.class);
            assertThat(runner.run("hello")).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("Should compose with neither decorator")
        void testComposeNoDecorators() throws Exception {
            var base = new CompositionInConcurrency.UpperCaseRunner();
            var runner = CompositionInConcurrency.compose(base, false, false);

            assertThat(runner).isSameAs(base);
            assertThat(runner.run("hello")).isEqualTo("HELLO");
        }
    }
}
