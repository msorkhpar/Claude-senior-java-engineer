package com.github.msorkhpar.claudejavatutor.executors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates ExecutorService interface, ThreadPoolExecutor configuration,
 * and the differences between submit() and execute().
 */
public class ExecutorServiceDemo {

    /**
     * Demonstrates creating a fixed thread pool and executing tasks.
     * A fixed thread pool maintains a constant number of threads.
     */
    public static class FixedThreadPoolExample {

        /**
         * Executes multiple tasks using a fixed thread pool and collects the thread names.
         *
         * @param taskCount  number of tasks to execute
         * @param poolSize   number of threads in the pool
         * @return list of thread names that executed the tasks
         */
        public List<String> executeTasksAndCollectThreadNames(int taskCount, int poolSize) {
            List<String> threadNames = Collections.synchronizedList(new ArrayList<>());
            ExecutorService executor = Executors.newFixedThreadPool(poolSize);

            try {
                CountDownLatch latch = new CountDownLatch(taskCount);
                for (int i = 0; i < taskCount; i++) {
                    executor.execute(() -> {
                        threadNames.add(Thread.currentThread().getName());
                        latch.countDown();
                    });
                }
                latch.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                executor.shutdown();
            }
            return threadNames;
        }

        /**
         * Counts distinct threads used to execute tasks in a fixed pool.
         */
        public int countDistinctThreads(int taskCount, int poolSize) {
            List<String> names = executeTasksAndCollectThreadNames(taskCount, poolSize);
            return (int) names.stream().distinct().count();
        }
    }

    /**
     * Demonstrates creating and configuring a ThreadPoolExecutor directly.
     */
    public static class ThreadPoolExecutorConfig {

        /**
         * Creates a ThreadPoolExecutor with custom configuration.
         *
         * @param corePoolSize    the number of threads to keep in the pool
         * @param maximumPoolSize the maximum number of threads to allow
         * @param keepAliveTime   time in milliseconds idle threads wait for new tasks
         * @param queueCapacity   the capacity of the work queue
         * @return the configured ThreadPoolExecutor
         */
        public ThreadPoolExecutor createCustomExecutor(int corePoolSize, int maximumPoolSize,
                                                       long keepAliveTime, int queueCapacity) {
            return new ThreadPoolExecutor(
                    corePoolSize,
                    maximumPoolSize,
                    keepAliveTime,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(queueCapacity),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );
        }

        /**
         * Creates a ThreadPoolExecutor with a custom thread factory.
         */
        public ThreadPoolExecutor createWithCustomThreadFactory(int poolSize, String threadNamePrefix) {
            AtomicInteger counter = new AtomicInteger(0);
            ThreadFactory factory = r -> {
                Thread t = new Thread(r);
                t.setName(threadNamePrefix + "-" + counter.incrementAndGet());
                t.setDaemon(true);
                return t;
            };

            return new ThreadPoolExecutor(
                    poolSize, poolSize,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(),
                    factory
            );
        }

        /**
         * Demonstrates the effect of core and max pool size.
         * When queue is full, new threads are created up to maximumPoolSize.
         */
        public int getActiveThreadCount(ThreadPoolExecutor executor, int taskCount)
                throws InterruptedException {
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch taskLatch = new CountDownLatch(taskCount);
            AtomicInteger maxActive = new AtomicInteger(0);

            for (int i = 0; i < taskCount; i++) {
                executor.execute(() -> {
                    try {
                        int active = ((ThreadPoolExecutor) Thread.currentThread()
                                .getUncaughtExceptionHandler() instanceof Thread.UncaughtExceptionHandler
                                ? executor.getActiveCount() : executor.getActiveCount());
                        maxActive.updateAndGet(current -> Math.max(current, active));
                        startLatch.await(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        taskLatch.countDown();
                    }
                });
            }

            // Give threads time to start
            Thread.sleep(200);
            int active = executor.getActiveCount();
            startLatch.countDown();
            taskLatch.await(5, TimeUnit.SECONDS);
            executor.shutdown();
            return active;
        }
    }

    /**
     * Demonstrates the differences between execute() and submit().
     */
    public static class ExecuteVsSubmit {

        /**
         * Uses execute() which takes a Runnable and returns void.
         * Exceptions thrown in the task are NOT propagated to the caller.
         *
         * @return true if task completed (tracked via side effect)
         */
        public boolean executeRunnable(ExecutorService executor) throws InterruptedException {
            AtomicInteger completionFlag = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(1);

            executor.execute(() -> {
                completionFlag.set(1);
                latch.countDown();
            });

            latch.await(5, TimeUnit.SECONDS);
            return completionFlag.get() == 1;
        }

        /**
         * Uses submit() with a Runnable. Returns a Future<?> that can be used
         * to check completion or retrieve exceptions.
         */
        public Future<?> submitRunnable(ExecutorService executor) {
            return executor.submit(() -> {
                Thread.sleep(50);
                return null;
            });
        }

        /**
         * Demonstrates that exceptions in execute() are handled by the thread's
         * UncaughtExceptionHandler, while exceptions in submit() are captured
         * in the Future.
         */
        public Future<?> submitTaskThatFails(ExecutorService executor) {
            return executor.submit(() -> {
                throw new IllegalStateException("Task failed intentionally");
            });
        }

        /**
         * Demonstrates executing a task that throws, via execute().
         * The exception is NOT accessible to the caller directly.
         */
        public boolean executeFailing(ExecutorService executor) throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicInteger exceptionCaught = new AtomicInteger(0);

            Thread.UncaughtExceptionHandler originalHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                exceptionCaught.set(1);
                latch.countDown();
            });

            try {
                executor.execute(() -> {
                    throw new RuntimeException("Boom!");
                });
                latch.await(5, TimeUnit.SECONDS);
            } finally {
                Thread.setDefaultUncaughtExceptionHandler(originalHandler);
            }

            return exceptionCaught.get() == 1;
        }
    }

    /**
     * Demonstrates different types of ExecutorService implementations.
     */
    public static class ExecutorTypes {

        /**
         * Creates a single-thread executor - guarantees sequential execution.
         */
        public ExecutorService createSingleThreadExecutor() {
            return Executors.newSingleThreadExecutor();
        }

        /**
         * Creates a cached thread pool - creates threads as needed and
         * reuses previously constructed threads when available.
         */
        public ExecutorService createCachedThreadPool() {
            return Executors.newCachedThreadPool();
        }

        /**
         * Creates a scheduled thread pool for delayed and periodic task execution.
         */
        public ScheduledExecutorService createScheduledExecutor(int poolSize) {
            return Executors.newScheduledThreadPool(poolSize);
        }

        /**
         * Creates a virtual thread per-task executor (Java 21+).
         * Virtual threads are lightweight and ideal for I/O-bound tasks.
         */
        public ExecutorService createVirtualThreadExecutor() {
            return Executors.newVirtualThreadPerTaskExecutor();
        }

        /**
         * Demonstrates that a single-thread executor processes tasks sequentially.
         */
        public List<Integer> executeSingleThreadSequentially(List<Integer> values)
                throws InterruptedException {
            List<Integer> results = Collections.synchronizedList(new ArrayList<>());
            ExecutorService executor = Executors.newSingleThreadExecutor();
            CountDownLatch latch = new CountDownLatch(values.size());

            for (int value : values) {
                executor.execute(() -> {
                    results.add(value);
                    latch.countDown();
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();
            return results;
        }

        /**
         * Demonstrates virtual threads handling many concurrent I/O-bound tasks.
         */
        public int executeWithVirtualThreads(int taskCount) throws InterruptedException {
            AtomicInteger completedTasks = new AtomicInteger(0);

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                CountDownLatch latch = new CountDownLatch(taskCount);
                for (int i = 0; i < taskCount; i++) {
                    executor.submit(() -> {
                        try {
                            Thread.sleep(10); // simulate I/O
                            completedTasks.incrementAndGet();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            latch.countDown();
                        }
                    });
                }
                latch.await(30, TimeUnit.SECONDS);
            }
            return completedTasks.get();
        }
    }

    /**
     * Demonstrates proper shutdown patterns for ExecutorService.
     */
    public static class ShutdownPatterns {

        /**
         * Demonstrates a graceful shutdown: complete pending tasks, then shut down.
         */
        public boolean gracefulShutdown(ExecutorService executor, long timeoutMs)
                throws InterruptedException {
            executor.shutdown();
            return executor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
        }

        /**
         * Demonstrates the two-phase shutdown recommended by the javadoc.
         */
        public List<Runnable> twoPhaseShutdown(ExecutorService executor, long timeoutMs)
                throws InterruptedException {
            executor.shutdown();
            if (!executor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS)) {
                return executor.shutdownNow();
            }
            return Collections.emptyList();
        }

        /**
         * Demonstrates shutdownNow() which attempts to cancel running tasks.
         */
        public List<Runnable> forceShutdown(ExecutorService executor) {
            return executor.shutdownNow();
        }

        /**
         * Checks if an executor is shut down.
         */
        public boolean isShutdown(ExecutorService executor) {
            return executor.isShutdown();
        }

        /**
         * Checks if all tasks completed after shutdown.
         */
        public boolean isTerminated(ExecutorService executor) {
            return executor.isTerminated();
        }
    }
}
