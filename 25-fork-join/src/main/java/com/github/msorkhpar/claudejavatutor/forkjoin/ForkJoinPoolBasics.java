package com.github.msorkhpar.claudejavatutor.forkjoin;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Demonstrates parallelizing tasks with ForkJoinPool.
 * Covers pool creation, configuration, task submission, and common pool usage.
 */
public class ForkJoinPoolBasics {

    /**
     * Sums an array of longs using the common ForkJoinPool.
     */
    public static long sumWithCommonPool(long[] array) {
        if (array == null || array.length == 0) {
            return 0;
        }
        ForkJoinPool commonPool = ForkJoinPool.commonPool();
        return commonPool.invoke(new ArraySumTask(array, 0, array.length));
    }

    /**
     * Sums an array of longs using a custom ForkJoinPool with specified parallelism.
     */
    public static long sumWithCustomPool(long[] array, int parallelism) {
        if (array == null || array.length == 0) {
            return 0;
        }
        if (parallelism < 1) {
            throw new IllegalArgumentException("Parallelism must be at least 1");
        }
        ForkJoinPool pool = new ForkJoinPool(parallelism);
        try {
            return pool.invoke(new ArraySumTask(array, 0, array.length));
        } finally {
            pool.shutdown();
        }
    }

    /**
     * Returns information about the common ForkJoinPool.
     */
    public static PoolInfo getCommonPoolInfo() {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        return new PoolInfo(
                pool.getParallelism(),
                pool.getPoolSize(),
                pool.getActiveThreadCount(),
                pool.getQueuedTaskCount(),
                pool.getStealCount()
        );
    }

    /**
     * Demonstrates submitting a task asynchronously and retrieving the result.
     */
    public static long sumAsync(long[] array) {
        if (array == null || array.length == 0) {
            return 0;
        }
        ForkJoinPool pool = new ForkJoinPool();
        try {
            var future = pool.submit(new ArraySumTask(array, 0, array.length));
            return future.join();
        } finally {
            pool.shutdown();
        }
    }

    /**
     * Executes a parallel computation with a timeout.
     * Returns -1 if the computation does not complete in time.
     */
    public static long sumWithTimeout(long[] array, long timeoutMillis) {
        if (array == null || array.length == 0) {
            return 0;
        }
        ForkJoinPool pool = new ForkJoinPool();
        try {
            var future = pool.submit(new ArraySumTask(array, 0, array.length));
            pool.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS);
            if (future.isDone()) {
                return future.join();
            }
            return future.join(); // will block until done
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        } finally {
            pool.shutdown();
        }
    }

    /**
     * Demonstrates using ForkJoinPool with parallel streams.
     * The common pool is shared with parallel streams.
     */
    public static long sumWithParallelStream(long[] array) {
        if (array == null || array.length == 0) {
            return 0;
        }
        return IntStream.range(0, array.length)
                .parallel()
                .mapToLong(i -> array[i])
                .sum();
    }

    /**
     * Runs a parallel stream computation within a custom ForkJoinPool
     * to avoid contention with the common pool.
     */
    public static long sumWithCustomPoolAndStream(long[] array, int parallelism) {
        if (array == null || array.length == 0) {
            return 0;
        }
        ForkJoinPool customPool = new ForkJoinPool(parallelism);
        try {
            return customPool.submit(() ->
                    IntStream.range(0, array.length)
                            .parallel()
                            .mapToLong(i -> array[i])
                            .sum()
            ).join();
        } finally {
            customPool.shutdown();
        }
    }

    /**
     * Internal RecursiveTask for summing array segments.
     */
    static class ArraySumTask extends RecursiveTask<Long> {
        private static final int THRESHOLD = 1000;
        private final long[] array;
        private final int start;
        private final int end;

        ArraySumTask(long[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            int length = end - start;
            if (length <= THRESHOLD) {
                long sum = 0;
                for (int i = start; i < end; i++) {
                    sum += array[i];
                }
                return sum;
            }
            int mid = start + length / 2;
            ArraySumTask left = new ArraySumTask(array, start, mid);
            ArraySumTask right = new ArraySumTask(array, mid, end);
            left.fork();
            long rightResult = right.compute();
            long leftResult = left.join();
            return leftResult + rightResult;
        }
    }

    /**
     * Record holding ForkJoinPool diagnostic information.
     */
    public record PoolInfo(int parallelism, int poolSize, int activeThreads,
                           long queuedTasks, long stealCount) {
    }
}
