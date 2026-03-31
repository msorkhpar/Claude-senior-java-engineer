package com.github.msorkhpar.claudejavatutor.forkjoin;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Demonstrates implementing RecursiveTask for results-bearing fork/join tasks.
 * Covers various real-world scenarios: array summation, max finding,
 * Fibonacci computation, merge sort, and parallel search.
 */
public class RecursiveTaskExamples {

    /**
     * RecursiveTask that sums an array of integers with a configurable threshold.
     */
    public static class SumTask extends RecursiveTask<Long> {
        private final int[] array;
        private final int start;
        private final int end;
        private final int threshold;

        public SumTask(int[] array, int start, int end, int threshold) {
            if (array == null) {
                throw new IllegalArgumentException("Array must not be null");
            }
            if (threshold < 1) {
                throw new IllegalArgumentException("Threshold must be at least 1");
            }
            this.array = array;
            this.start = start;
            this.end = end;
            this.threshold = threshold;
        }

        public SumTask(int[] array) {
            this(array, 0, array == null ? 0 : array.length, 100);
        }

        @Override
        protected Long compute() {
            int length = end - start;
            if (length <= threshold) {
                long sum = 0;
                for (int i = start; i < end; i++) {
                    sum += array[i];
                }
                return sum;
            }
            int mid = start + length / 2;
            SumTask left = new SumTask(array, start, mid, threshold);
            SumTask right = new SumTask(array, mid, end, threshold);
            left.fork();
            long rightResult = right.compute();
            long leftResult = left.join();
            return leftResult + rightResult;
        }
    }

    /**
     * RecursiveTask that finds the maximum value in an array.
     */
    public static class MaxTask extends RecursiveTask<Integer> {
        private static final int THRESHOLD = 100;
        private final int[] array;
        private final int start;
        private final int end;

        public MaxTask(int[] array, int start, int end) {
            if (array == null || array.length == 0) {
                throw new IllegalArgumentException("Array must not be null or empty");
            }
            this.array = array;
            this.start = start;
            this.end = end;
        }

        public MaxTask(int[] array) {
            this(array, 0, array == null ? 0 : array.length);
        }

        @Override
        protected Integer compute() {
            int length = end - start;
            if (length <= THRESHOLD) {
                int max = Integer.MIN_VALUE;
                for (int i = start; i < end; i++) {
                    if (array[i] > max) {
                        max = array[i];
                    }
                }
                return max;
            }
            int mid = start + length / 2;
            MaxTask left = new MaxTask(array, start, mid);
            MaxTask right = new MaxTask(array, mid, end);
            left.fork();
            int rightMax = right.compute();
            int leftMax = left.join();
            return Math.max(leftMax, rightMax);
        }
    }

    /**
     * RecursiveTask for Fibonacci computation.
     * Demonstrates a case where fork/join is NOT ideal for small inputs
     * due to overhead, but works well for large inputs with memoization threshold.
     */
    public static class FibonacciTask extends RecursiveTask<BigInteger> {
        private static final int SEQUENTIAL_THRESHOLD = 20;
        private final int n;

        public FibonacciTask(int n) {
            if (n < 0) {
                throw new IllegalArgumentException("n must be non-negative");
            }
            this.n = n;
        }

        @Override
        protected BigInteger compute() {
            if (n <= SEQUENTIAL_THRESHOLD) {
                return sequentialFib(n);
            }
            FibonacciTask f1 = new FibonacciTask(n - 1);
            FibonacciTask f2 = new FibonacciTask(n - 2);
            f1.fork();
            BigInteger result2 = f2.compute();
            BigInteger result1 = f1.join();
            return result1.add(result2);
        }

        private BigInteger sequentialFib(int n) {
            if (n <= 1) return BigInteger.valueOf(n);
            BigInteger a = BigInteger.ZERO;
            BigInteger b = BigInteger.ONE;
            for (int i = 2; i <= n; i++) {
                BigInteger temp = b;
                b = a.add(b);
                a = temp;
            }
            return b;
        }
    }

    /**
     * RecursiveTask for counting occurrences of a target in an array.
     */
    public static class CountTask extends RecursiveTask<Integer> {
        private static final int THRESHOLD = 100;
        private final int[] array;
        private final int start;
        private final int end;
        private final int target;

        public CountTask(int[] array, int target) {
            this(array, 0, array == null ? 0 : array.length, target);
        }

        public CountTask(int[] array, int start, int end, int target) {
            if (array == null) {
                throw new IllegalArgumentException("Array must not be null");
            }
            this.array = array;
            this.start = start;
            this.end = end;
            this.target = target;
        }

        @Override
        protected Integer compute() {
            int length = end - start;
            if (length <= THRESHOLD) {
                int count = 0;
                for (int i = start; i < end; i++) {
                    if (array[i] == target) {
                        count++;
                    }
                }
                return count;
            }
            int mid = start + length / 2;
            CountTask left = new CountTask(array, start, mid, target);
            CountTask right = new CountTask(array, mid, end, target);
            left.fork();
            int rightCount = right.compute();
            int leftCount = left.join();
            return leftCount + rightCount;
        }
    }

    /**
     * RecursiveTask that performs parallel merge sort and returns the sorted array.
     */
    public static class MergeSortTask extends RecursiveTask<int[]> {
        private static final int THRESHOLD = 64;
        private final int[] array;

        public MergeSortTask(int[] array) {
            if (array == null) {
                throw new IllegalArgumentException("Array must not be null");
            }
            this.array = array;
        }

        @Override
        protected int[] compute() {
            if (array.length <= THRESHOLD) {
                int[] copy = Arrays.copyOf(array, array.length);
                Arrays.sort(copy);
                return copy;
            }
            int mid = array.length / 2;
            int[] leftArray = Arrays.copyOfRange(array, 0, mid);
            int[] rightArray = Arrays.copyOfRange(array, mid, array.length);

            MergeSortTask leftTask = new MergeSortTask(leftArray);
            MergeSortTask rightTask = new MergeSortTask(rightArray);
            leftTask.fork();
            int[] rightSorted = rightTask.compute();
            int[] leftSorted = leftTask.join();
            return merge(leftSorted, rightSorted);
        }

        private int[] merge(int[] left, int[] right) {
            int[] result = new int[left.length + right.length];
            int i = 0, j = 0, k = 0;
            while (i < left.length && j < right.length) {
                if (left[i] <= right[j]) {
                    result[k++] = left[i++];
                } else {
                    result[k++] = right[j++];
                }
            }
            while (i < left.length) {
                result[k++] = left[i++];
            }
            while (j < right.length) {
                result[k++] = right[j++];
            }
            return result;
        }
    }

    // Convenience methods

    /**
     * Sums an int array using ForkJoinPool.
     */
    public static long parallelSum(int[] array) {
        if (array == null || array.length == 0) return 0;
        ForkJoinPool pool = ForkJoinPool.commonPool();
        return pool.invoke(new SumTask(array));
    }

    /**
     * Finds the maximum value in an int array using ForkJoinPool.
     */
    public static int parallelMax(int[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array must not be null or empty");
        }
        return ForkJoinPool.commonPool().invoke(new MaxTask(array));
    }

    /**
     * Sorts an int array using parallel merge sort.
     */
    public static int[] parallelMergeSort(int[] array) {
        if (array == null) {
            throw new IllegalArgumentException("Array must not be null");
        }
        if (array.length <= 1) return Arrays.copyOf(array, array.length);
        return ForkJoinPool.commonPool().invoke(new MergeSortTask(array));
    }

    /**
     * Counts occurrences of target in array using ForkJoinPool.
     */
    public static int parallelCount(int[] array, int target) {
        if (array == null || array.length == 0) return 0;
        return ForkJoinPool.commonPool().invoke(new CountTask(array, target));
    }
}
