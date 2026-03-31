package com.github.msorkhpar.claudejavatutor.executors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates Callable and Future for returning results from asynchronous tasks,
 * including exception handling and timeouts.
 */
public class CallableFutureDemo {

    /**
     * Demonstrates the differences between Callable and Runnable.
     */
    public static class CallableVsRunnable {

        /**
         * A Runnable cannot return a result or throw checked exceptions.
         * This method uses a side-effect to communicate the result.
         */
        public int computeWithRunnable(ExecutorService executor, int input)
                throws InterruptedException {
            AtomicInteger result = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(1);

            executor.execute(() -> {
                result.set(input * input);
                latch.countDown();
            });

            latch.await(5, TimeUnit.SECONDS);
            return result.get();
        }

        /**
         * A Callable can return a result and throw checked exceptions.
         */
        public int computeWithCallable(ExecutorService executor, int input)
                throws ExecutionException, InterruptedException {
            Future<Integer> future = executor.submit(() -> input * input);
            return future.get();
        }

        /**
         * Demonstrates that Callable can throw checked exceptions,
         * which are wrapped in ExecutionException.
         */
        public Future<String> submitCallableThatThrowsChecked(ExecutorService executor) {
            return executor.submit(() -> {
                throw new Exception("Checked exception from Callable");
            });
        }

        /**
         * Demonstrates Callable with a complex return type.
         */
        public Future<List<Integer>> computeRange(ExecutorService executor, int start, int end) {
            return executor.submit(() -> {
                List<Integer> result = new ArrayList<>();
                for (int i = start; i <= end; i++) {
                    result.add(i * i);
                }
                return result;
            });
        }
    }

    /**
     * Demonstrates Future interface methods for retrieving task results.
     */
    public static class FutureOperations {

        /**
         * Demonstrates Future.get() which blocks until the result is available.
         */
        public <T> T getResult(Future<T> future) throws ExecutionException, InterruptedException {
            return future.get();
        }

        /**
         * Demonstrates Future.get(timeout, unit) which blocks for at most the given time.
         */
        public <T> T getResultWithTimeout(Future<T> future, long timeoutMs)
                throws ExecutionException, InterruptedException, TimeoutException {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        }

        /**
         * Checks if a Future is done (completed normally, exceptionally, or cancelled).
         */
        public boolean isDone(Future<?> future) {
            return future.isDone();
        }

        /**
         * Attempts to cancel a running task.
         *
         * @param mayInterruptIfRunning if true, the thread executing the task is interrupted
         * @return true if the task was successfully cancelled
         */
        public boolean cancelTask(Future<?> future, boolean mayInterruptIfRunning) {
            return future.cancel(mayInterruptIfRunning);
        }

        /**
         * Checks if a task was cancelled.
         */
        public boolean isCancelled(Future<?> future) {
            return future.isCancelled();
        }

        /**
         * Submits a long-running task and cancels it after a timeout.
         *
         * @return true if cancellation was requested before completion
         */
        public boolean submitAndCancel(ExecutorService executor, long taskDurationMs, long cancelAfterMs)
                throws InterruptedException {
            Future<?> future = executor.submit(() -> {
                try {
                    Thread.sleep(taskDurationMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            });

            Thread.sleep(cancelAfterMs);
            return future.cancel(true);
        }
    }

    /**
     * Demonstrates exception handling with Future.
     */
    public static class FutureExceptionHandling {

        /**
         * Demonstrates that runtime exceptions thrown in Callable are wrapped
         * in ExecutionException.
         */
        public Future<String> submitTaskWithRuntimeException(ExecutorService executor) {
            return executor.submit(() -> {
                throw new IllegalArgumentException("Invalid argument in task");
            });
        }

        /**
         * Demonstrates that checked exceptions thrown in Callable are wrapped
         * in ExecutionException.
         */
        public Future<String> submitTaskWithCheckedException(ExecutorService executor) {
            return executor.submit(() -> {
                throw new Exception("Checked exception in task");
            });
        }

        /**
         * Demonstrates safe result retrieval with proper exception handling.
         *
         * @return the result, or the default value if the task failed
         */
        public <T> T safeGet(Future<T> future, T defaultValue) {
            try {
                return future.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return defaultValue;
            } catch (ExecutionException e) {
                // Log the root cause
                Throwable cause = e.getCause();
                System.err.println("Task failed with: " + cause.getMessage());
                return defaultValue;
            } catch (TimeoutException e) {
                future.cancel(true);
                return defaultValue;
            }
        }

        /**
         * Demonstrates extracting the root cause from an ExecutionException.
         */
        public Throwable extractCause(Future<?> future) {
            try {
                future.get(5, TimeUnit.SECONDS);
                return null;
            } catch (ExecutionException e) {
                return e.getCause();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return e;
            } catch (TimeoutException e) {
                return e;
            }
        }
    }

    /**
     * Demonstrates invokeAll() and invokeAny() for batch task execution.
     */
    public static class BatchExecution {

        /**
         * Uses invokeAll() to submit a batch of Callable tasks and wait for all to complete.
         *
         * @return list of results from all completed tasks
         */
        public List<Integer> computeAllSquares(ExecutorService executor, List<Integer> values)
                throws InterruptedException {
            List<Callable<Integer>> tasks = new ArrayList<>();
            for (int value : values) {
                tasks.add(() -> value * value);
            }

            List<Future<Integer>> futures = executor.invokeAll(tasks);
            List<Integer> results = new ArrayList<>();
            for (Future<Integer> future : futures) {
                try {
                    results.add(future.get());
                } catch (ExecutionException e) {
                    results.add(-1); // sentinel for failed tasks
                }
            }
            return results;
        }

        /**
         * Uses invokeAll() with a timeout. Tasks not completed within the timeout
         * are cancelled.
         */
        public List<Integer> computeWithTimeout(ExecutorService executor,
                                                List<Integer> values, long timeoutMs)
                throws InterruptedException {
            List<Callable<Integer>> tasks = new ArrayList<>();
            for (int value : values) {
                tasks.add(() -> {
                    Thread.sleep(value * 10L); // simulate varying computation time
                    return value * value;
                });
            }

            List<Future<Integer>> futures = executor.invokeAll(tasks, timeoutMs, TimeUnit.MILLISECONDS);
            List<Integer> results = new ArrayList<>();
            for (Future<Integer> future : futures) {
                try {
                    if (!future.isCancelled()) {
                        results.add(future.get());
                    } else {
                        results.add(-1); // cancelled
                    }
                } catch (ExecutionException | CancellationException e) {
                    results.add(-1);
                }
            }
            return results;
        }

        /**
         * Uses invokeAny() to return the result of the first task to complete successfully.
         */
        public int computeFastest(ExecutorService executor, List<Integer> values)
                throws InterruptedException, ExecutionException {
            List<Callable<Integer>> tasks = new ArrayList<>();
            for (int value : values) {
                tasks.add(() -> {
                    Thread.sleep(value * 10L); // lower values complete faster
                    return value * value;
                });
            }
            return executor.invokeAny(tasks);
        }

        /**
         * Uses invokeAny() with a timeout.
         */
        public int computeFastestWithTimeout(ExecutorService executor,
                                             List<Integer> values, long timeoutMs)
                throws InterruptedException, ExecutionException, TimeoutException {
            List<Callable<Integer>> tasks = new ArrayList<>();
            for (int value : values) {
                tasks.add(() -> {
                    Thread.sleep(value * 100L);
                    return value * value;
                });
            }
            return executor.invokeAny(tasks, timeoutMs, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Demonstrates practical patterns using Callable and Future.
     */
    public static class PracticalPatterns {

        /**
         * Demonstrates parallel computation with result aggregation.
         * Splits work across multiple threads and combines results.
         */
        public long parallelSum(ExecutorService executor, int from, int to, int numPartitions)
                throws InterruptedException {
            int rangeSize = (to - from + 1) / numPartitions;
            List<Callable<Long>> tasks = new ArrayList<>();

            for (int i = 0; i < numPartitions; i++) {
                int start = from + (i * rangeSize);
                int end = (i == numPartitions - 1) ? to : start + rangeSize - 1;
                tasks.add(() -> {
                    long sum = 0;
                    for (int n = start; n <= end; n++) {
                        sum += n;
                    }
                    return sum;
                });
            }

            List<Future<Long>> futures = executor.invokeAll(tasks);
            long totalSum = 0;
            for (Future<Long> future : futures) {
                try {
                    totalSum += future.get();
                } catch (ExecutionException e) {
                    throw new RuntimeException("Parallel sum failed", e);
                }
            }
            return totalSum;
        }

        /**
         * Demonstrates the retry pattern with Callable and Future.
         *
         * @param maxRetries the maximum number of retry attempts
         * @return the result of the task, or throws if all retries fail
         */
        public <T> T executeWithRetry(ExecutorService executor, Callable<T> task, int maxRetries)
                throws Exception {
            Exception lastException = null;
            for (int attempt = 0; attempt <= maxRetries; attempt++) {
                try {
                    Future<T> future = executor.submit(task);
                    return future.get(5, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    lastException = (Exception) e.getCause();
                } catch (TimeoutException e) {
                    lastException = e;
                }
            }
            throw lastException;
        }

        /**
         * Demonstrates CompletionService for processing results as they become available.
         */
        public List<Integer> processInCompletionOrder(ExecutorService executor, List<Integer> values)
                throws InterruptedException {
            CompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

            for (int value : values) {
                completionService.submit(() -> {
                    Thread.sleep(value * 10L); // varying computation times
                    return value * value;
                });
            }

            List<Integer> results = new ArrayList<>();
            for (int i = 0; i < values.size(); i++) {
                try {
                    Future<Integer> completed = completionService.take();
                    results.add(completed.get());
                } catch (ExecutionException e) {
                    results.add(-1);
                }
            }
            return results;
        }
    }
}
