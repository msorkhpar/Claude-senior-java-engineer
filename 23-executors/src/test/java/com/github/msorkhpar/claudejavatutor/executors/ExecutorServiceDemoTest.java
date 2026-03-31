package com.github.msorkhpar.claudejavatutor.executors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ExecutorService Demo Tests")
class ExecutorServiceDemoTest {

    @Nested
    @DisplayName("Fixed Thread Pool")
    class FixedThreadPoolTest {

        @Test
        @DisplayName("Should execute all tasks successfully")
        void shouldExecuteAllTasks() {
            var example = new ExecutorServiceDemo.FixedThreadPoolExample();

            List<String> threadNames = example.executeTasksAndCollectThreadNames(10, 3);

            assertThat(threadNames).hasSize(10);
        }

        @Test
        @DisplayName("Should use at most poolSize threads")
        void shouldLimitThreadCount() {
            var example = new ExecutorServiceDemo.FixedThreadPoolExample();

            int distinctThreads = example.countDistinctThreads(20, 3);

            assertThat(distinctThreads).isLessThanOrEqualTo(3);
        }

        @Test
        @DisplayName("Should handle single task")
        void shouldHandleSingleTask() {
            var example = new ExecutorServiceDemo.FixedThreadPoolExample();

            List<String> threadNames = example.executeTasksAndCollectThreadNames(1, 3);

            assertThat(threadNames).hasSize(1);
        }

        @Test
        @DisplayName("Should handle pool size of one")
        void shouldHandlePoolSizeOfOne() {
            var example = new ExecutorServiceDemo.FixedThreadPoolExample();

            int distinctThreads = example.countDistinctThreads(5, 1);

            assertThat(distinctThreads).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle large number of tasks")
        void shouldHandleManyTasks() {
            var example = new ExecutorServiceDemo.FixedThreadPoolExample();

            List<String> threadNames = example.executeTasksAndCollectThreadNames(100, 5);

            assertThat(threadNames).hasSize(100);
            long distinctCount = threadNames.stream().distinct().count();
            assertThat(distinctCount).isLessThanOrEqualTo(5);
        }
    }

    @Nested
    @DisplayName("ThreadPoolExecutor Configuration")
    class ThreadPoolExecutorConfigTest {

        @Test
        @DisplayName("Should create executor with correct core pool size")
        void shouldCreateWithCorrectCoreSize() {
            var config = new ExecutorServiceDemo.ThreadPoolExecutorConfig();

            ThreadPoolExecutor executor = config.createCustomExecutor(2, 4, 1000L, 10);

            assertThat(executor.getCorePoolSize()).isEqualTo(2);
            assertThat(executor.getMaximumPoolSize()).isEqualTo(4);
            executor.shutdown();
        }

        @Test
        @DisplayName("Should create executor with CallerRunsPolicy")
        void shouldUseCallerRunsPolicy() {
            var config = new ExecutorServiceDemo.ThreadPoolExecutorConfig();

            ThreadPoolExecutor executor = config.createCustomExecutor(1, 2, 1000L, 5);

            assertThat(executor.getRejectedExecutionHandler())
                    .isInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class);
            executor.shutdown();
        }

        @Test
        @DisplayName("Should create executor with custom thread factory")
        void shouldCreateWithCustomThreadFactory() throws InterruptedException {
            var config = new ExecutorServiceDemo.ThreadPoolExecutorConfig();

            ThreadPoolExecutor executor = config.createWithCustomThreadFactory(2, "worker");
            CountDownLatch latch = new CountDownLatch(1);
            List<String> names = Collections.synchronizedList(new ArrayList<>());

            executor.execute(() -> {
                names.add(Thread.currentThread().getName());
                latch.countDown();
            });

            latch.await(5, TimeUnit.SECONDS);
            assertThat(names.get(0)).startsWith("worker-");
            executor.shutdown();
        }

        @Test
        @DisplayName("Custom thread factory should create daemon threads")
        void shouldCreateDaemonThreads() throws InterruptedException {
            var config = new ExecutorServiceDemo.ThreadPoolExecutorConfig();

            ThreadPoolExecutor executor = config.createWithCustomThreadFactory(1, "daemon-test");
            CountDownLatch latch = new CountDownLatch(1);
            List<Boolean> daemonFlags = Collections.synchronizedList(new ArrayList<>());

            executor.execute(() -> {
                daemonFlags.add(Thread.currentThread().isDaemon());
                latch.countDown();
            });

            latch.await(5, TimeUnit.SECONDS);
            assertThat(daemonFlags.get(0)).isTrue();
            executor.shutdown();
        }

        @Test
        @DisplayName("Should respect queue capacity")
        void shouldRespectQueueCapacity() {
            var config = new ExecutorServiceDemo.ThreadPoolExecutorConfig();

            ThreadPoolExecutor executor = config.createCustomExecutor(1, 1, 1000L, 5);

            assertThat(executor.getQueue().remainingCapacity()).isEqualTo(5);
            executor.shutdown();
        }
    }

    @Nested
    @DisplayName("Execute vs Submit")
    class ExecuteVsSubmitTest {

        @Test
        @DisplayName("Should execute runnable successfully")
        void shouldExecuteRunnable() throws InterruptedException {
            var demo = new ExecutorServiceDemo.ExecuteVsSubmit();
            ExecutorService executor = Executors.newSingleThreadExecutor();

            boolean completed = demo.executeRunnable(executor);

            assertThat(completed).isTrue();
            executor.shutdown();
        }

        @Test
        @DisplayName("Should submit runnable and return Future")
        void shouldSubmitRunnableAndReturnFuture() throws Exception {
            var demo = new ExecutorServiceDemo.ExecuteVsSubmit();
            ExecutorService executor = Executors.newSingleThreadExecutor();

            Future<?> future = demo.submitRunnable(executor);
            future.get(5, TimeUnit.SECONDS);

            assertThat(future.isDone()).isTrue();
            executor.shutdown();
        }

        @Test
        @DisplayName("Should capture exception in Future when using submit")
        void shouldCaptureExceptionInFuture() {
            var demo = new ExecutorServiceDemo.ExecuteVsSubmit();
            ExecutorService executor = Executors.newSingleThreadExecutor();

            Future<?> future = demo.submitTaskThatFails(executor);

            assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Task failed intentionally");

            executor.shutdown();
        }

        @Test
        @DisplayName("Future isDone should return true after completion")
        void shouldReturnDoneAfterCompletion() throws Exception {
            var demo = new ExecutorServiceDemo.ExecuteVsSubmit();
            ExecutorService executor = Executors.newSingleThreadExecutor();

            Future<?> future = demo.submitRunnable(executor);
            future.get(5, TimeUnit.SECONDS);

            assertThat(future.isDone()).isTrue();
            assertThat(future.isCancelled()).isFalse();
            executor.shutdown();
        }
    }

    @Nested
    @DisplayName("Executor Types")
    class ExecutorTypesTest {

        @Test
        @DisplayName("Should create single thread executor")
        void shouldCreateSingleThreadExecutor() {
            var types = new ExecutorServiceDemo.ExecutorTypes();

            ExecutorService executor = types.createSingleThreadExecutor();

            assertThat(executor).isNotNull();
            executor.shutdown();
        }

        @Test
        @DisplayName("Should create cached thread pool")
        void shouldCreateCachedThreadPool() {
            var types = new ExecutorServiceDemo.ExecutorTypes();

            ExecutorService executor = types.createCachedThreadPool();

            assertThat(executor).isNotNull();
            executor.shutdown();
        }

        @Test
        @DisplayName("Should create scheduled executor")
        void shouldCreateScheduledExecutor() {
            var types = new ExecutorServiceDemo.ExecutorTypes();

            ScheduledExecutorService executor = types.createScheduledExecutor(2);

            assertThat(executor).isNotNull();
            executor.shutdown();
        }

        @Test
        @DisplayName("Single thread executor should process tasks sequentially")
        void shouldProcessSequentially() throws InterruptedException {
            var types = new ExecutorServiceDemo.ExecutorTypes();

            List<Integer> result = types.executeSingleThreadSequentially(List.of(1, 2, 3, 4, 5));

            assertThat(result).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Should handle empty list in single thread executor")
        void shouldHandleEmptyList() throws InterruptedException {
            var types = new ExecutorServiceDemo.ExecutorTypes();

            List<Integer> result = types.executeSingleThreadSequentially(List.of());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Virtual thread executor should complete all tasks")
        void shouldCompleteAllVirtualThreadTasks() throws InterruptedException {
            var types = new ExecutorServiceDemo.ExecutorTypes();

            int completed = types.executeWithVirtualThreads(50);

            assertThat(completed).isEqualTo(50);
        }

        @Test
        @DisplayName("Virtual thread executor should handle many concurrent tasks")
        void shouldHandleManyConcurrentVirtualTasks() throws InterruptedException {
            var types = new ExecutorServiceDemo.ExecutorTypes();

            int completed = types.executeWithVirtualThreads(500);

            assertThat(completed).isEqualTo(500);
        }

        @Test
        @DisplayName("Should create virtual thread executor")
        void shouldCreateVirtualThreadExecutor() {
            var types = new ExecutorServiceDemo.ExecutorTypes();

            ExecutorService executor = types.createVirtualThreadExecutor();

            assertThat(executor).isNotNull();
            executor.shutdown();
        }
    }

    @Nested
    @DisplayName("Shutdown Patterns")
    class ShutdownPatternsTest {

        @Test
        @DisplayName("Should gracefully shutdown idle executor")
        void shouldGracefullyShutdownIdleExecutor() throws InterruptedException {
            var shutdown = new ExecutorServiceDemo.ShutdownPatterns();
            ExecutorService executor = Executors.newFixedThreadPool(2);

            boolean terminated = shutdown.gracefulShutdown(executor, 1000);

            assertThat(terminated).isTrue();
            assertThat(executor.isShutdown()).isTrue();
            assertThat(executor.isTerminated()).isTrue();
        }

        @Test
        @DisplayName("Should report shutdown status correctly")
        void shouldReportShutdownStatus() {
            var shutdown = new ExecutorServiceDemo.ShutdownPatterns();
            ExecutorService executor = Executors.newFixedThreadPool(2);

            assertThat(shutdown.isShutdown(executor)).isFalse();
            executor.shutdown();
            assertThat(shutdown.isShutdown(executor)).isTrue();
        }

        @Test
        @DisplayName("Two-phase shutdown should return empty list for idle executor")
        void twoPhaseShutdownIdleExecutor() throws InterruptedException {
            var shutdown = new ExecutorServiceDemo.ShutdownPatterns();
            ExecutorService executor = Executors.newFixedThreadPool(2);

            List<Runnable> unfinished = shutdown.twoPhaseShutdown(executor, 1000);

            assertThat(unfinished).isEmpty();
        }

        @Test
        @DisplayName("Force shutdown should shut down executor")
        void shouldForceShutdown() {
            var shutdown = new ExecutorServiceDemo.ShutdownPatterns();
            ExecutorService executor = Executors.newFixedThreadPool(2);

            List<Runnable> unfinished = shutdown.forceShutdown(executor);

            assertThat(executor.isShutdown()).isTrue();
            assertThat(unfinished).isNotNull();
        }

        @Test
        @DisplayName("Should reject tasks after shutdown")
        void shouldRejectTasksAfterShutdown() throws InterruptedException {
            var shutdown = new ExecutorServiceDemo.ShutdownPatterns();
            ExecutorService executor = Executors.newFixedThreadPool(2);

            shutdown.gracefulShutdown(executor, 1000);

            assertThatThrownBy(() -> executor.execute(() -> {}))
                    .isInstanceOf(RejectedExecutionException.class);
        }

        @Test
        @DisplayName("Should gracefully shutdown with pending tasks")
        void shouldShutdownWithPendingTasks() throws InterruptedException {
            var shutdown = new ExecutorServiceDemo.ShutdownPatterns();
            ExecutorService executor = Executors.newFixedThreadPool(2);
            AtomicInteger completed = new AtomicInteger(0);

            for (int i = 0; i < 5; i++) {
                executor.submit(() -> {
                    Thread.sleep(10);
                    completed.incrementAndGet();
                    return null;
                });
            }

            boolean terminated = shutdown.gracefulShutdown(executor, 5000);

            assertThat(terminated).isTrue();
            assertThat(completed.get()).isEqualTo(5);
        }
    }
}
