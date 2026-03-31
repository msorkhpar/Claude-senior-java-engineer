package com.github.msorkhpar.claudejavatutor.forkjoin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * Demonstrates splitting and joining strategies for optimal fork/join performance.
 * Covers threshold selection, work-stealing, balanced splits, and multi-way forking.
 */
public class SplittingAndJoining {

    /**
     * Demonstrates the effect of threshold on performance.
     * A threshold that is too small creates excessive overhead;
     * too large underutilizes parallelism.
     */
    public static class ThresholdExperiment {

        /**
         * Sums an array with a caller-specified threshold.
         */
        public static long sumWithThreshold(long[] array, int threshold) {
            if (array == null || array.length == 0) return 0;
            if (threshold < 1) throw new IllegalArgumentException("Threshold must be >= 1");
            return ForkJoinPool.commonPool().invoke(
                    new ThresholdSumTask(array, 0, array.length, threshold));
        }

        private static class ThresholdSumTask extends RecursiveTask<Long> {
            private final long[] array;
            private final int start;
            private final int end;
            private final int threshold;

            ThresholdSumTask(long[] array, int start, int end, int threshold) {
                this.array = array;
                this.start = start;
                this.end = end;
                this.threshold = threshold;
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
                ThresholdSumTask left = new ThresholdSumTask(array, start, mid, threshold);
                ThresholdSumTask right = new ThresholdSumTask(array, mid, end, threshold);
                left.fork();
                long rightResult = right.compute();
                long leftResult = left.join();
                return leftResult + rightResult;
            }
        }
    }

    /**
     * Demonstrates balanced vs unbalanced splitting.
     */
    public static class SplitStrategy {

        /**
         * Splits the array into two equal halves (balanced).
         */
        public static long balancedSum(int[] array) {
            if (array == null || array.length == 0) return 0;
            return ForkJoinPool.commonPool().invoke(new BalancedSumTask(array, 0, array.length));
        }

        /**
         * Splits the array into unequal parts: 10% left, 90% right (unbalanced).
         * This is intentionally suboptimal for demonstration purposes.
         */
        public static long unbalancedSum(int[] array) {
            if (array == null || array.length == 0) return 0;
            return ForkJoinPool.commonPool().invoke(new UnbalancedSumTask(array, 0, array.length));
        }

        private static class BalancedSumTask extends RecursiveTask<Long> {
            private static final int THRESHOLD = 500;
            private final int[] array;
            private final int start;
            private final int end;

            BalancedSumTask(int[] array, int start, int end) {
                this.array = array;
                this.start = start;
                this.end = end;
            }

            @Override
            protected Long compute() {
                int length = end - start;
                if (length <= THRESHOLD) {
                    long sum = 0;
                    for (int i = start; i < end; i++) sum += array[i];
                    return sum;
                }
                int mid = start + length / 2; // balanced split
                BalancedSumTask left = new BalancedSumTask(array, start, mid);
                BalancedSumTask right = new BalancedSumTask(array, mid, end);
                left.fork();
                long rightResult = right.compute();
                long leftResult = left.join();
                return leftResult + rightResult;
            }
        }

        private static class UnbalancedSumTask extends RecursiveTask<Long> {
            private static final int THRESHOLD = 500;
            private final int[] array;
            private final int start;
            private final int end;

            UnbalancedSumTask(int[] array, int start, int end) {
                this.array = array;
                this.start = start;
                this.end = end;
            }

            @Override
            protected Long compute() {
                int length = end - start;
                if (length <= THRESHOLD) {
                    long sum = 0;
                    for (int i = start; i < end; i++) sum += array[i];
                    return sum;
                }
                int splitPoint = start + length / 10; // 10/90 split
                if (splitPoint <= start) splitPoint = start + 1;
                UnbalancedSumTask left = new UnbalancedSumTask(array, start, splitPoint);
                UnbalancedSumTask right = new UnbalancedSumTask(array, splitPoint, end);
                left.fork();
                long rightResult = right.compute();
                long leftResult = left.join();
                return leftResult + rightResult;
            }
        }
    }

    /**
     * Demonstrates multi-way splitting (more than 2 subtasks).
     */
    public static class MultiWaySplit {

        /**
         * Splits the array into N subtasks and joins them all.
         */
        public static long multiWaySum(long[] array, int ways) {
            if (array == null || array.length == 0) return 0;
            if (ways < 1) throw new IllegalArgumentException("ways must be >= 1");
            return ForkJoinPool.commonPool().invoke(new MultiWaySumTask(array, 0, array.length, ways));
        }

        private static class MultiWaySumTask extends RecursiveTask<Long> {
            private static final int THRESHOLD = 500;
            private final long[] array;
            private final int start;
            private final int end;
            private final int ways;

            MultiWaySumTask(long[] array, int start, int end, int ways) {
                this.array = array;
                this.start = start;
                this.end = end;
                this.ways = ways;
            }

            @Override
            protected Long compute() {
                int length = end - start;
                if (length <= THRESHOLD || ways <= 1) {
                    long sum = 0;
                    for (int i = start; i < end; i++) {
                        sum += array[i];
                    }
                    return sum;
                }
                int effectiveWays = Math.min(ways, length);
                int chunkSize = length / effectiveWays;
                List<MultiWaySumTask> tasks = new ArrayList<>();
                int current = start;
                for (int i = 0; i < effectiveWays; i++) {
                    int chunkEnd = (i == effectiveWays - 1) ? end : current + chunkSize;
                    tasks.add(new MultiWaySumTask(array, current, chunkEnd, 1));
                    current = chunkEnd;
                }
                // Fork all but the last, compute the last in current thread
                for (int i = 0; i < tasks.size() - 1; i++) {
                    tasks.get(i).fork();
                }
                long total = tasks.get(tasks.size() - 1).compute();
                for (int i = tasks.size() - 2; i >= 0; i--) {
                    total += tasks.get(i).join();
                }
                return total;
            }
        }
    }

    /**
     * Demonstrates the correct fork/compute/join pattern vs. anti-patterns.
     */
    public static class ForkJoinPattern {

        /**
         * Correct pattern: fork left, compute right, join left.
         * This is optimal because the current thread stays busy computing the right subtask
         * while the left subtask runs on a stolen thread.
         */
        public static long correctPattern(int[] array) {
            if (array == null || array.length == 0) return 0;
            return ForkJoinPool.commonPool().invoke(new CorrectPatternTask(array, 0, array.length));
        }

        /**
         * Anti-pattern: fork both, join both.
         * The current thread does no computation work itself, wasting a thread.
         */
        public static long antiPatternForkBoth(int[] array) {
            if (array == null || array.length == 0) return 0;
            return ForkJoinPool.commonPool().invoke(new ForkBothTask(array, 0, array.length));
        }

        private static class CorrectPatternTask extends RecursiveTask<Long> {
            private static final int THRESHOLD = 500;
            private final int[] array;
            private final int start;
            private final int end;

            CorrectPatternTask(int[] array, int start, int end) {
                this.array = array;
                this.start = start;
                this.end = end;
            }

            @Override
            protected Long compute() {
                int length = end - start;
                if (length <= THRESHOLD) {
                    long sum = 0;
                    for (int i = start; i < end; i++) sum += array[i];
                    return sum;
                }
                int mid = start + length / 2;
                CorrectPatternTask left = new CorrectPatternTask(array, start, mid);
                CorrectPatternTask right = new CorrectPatternTask(array, mid, end);
                left.fork();           // fork left
                long rightResult = right.compute();  // compute right in current thread
                long leftResult = left.join();       // join left
                return leftResult + rightResult;
            }
        }

        private static class ForkBothTask extends RecursiveTask<Long> {
            private static final int THRESHOLD = 500;
            private final int[] array;
            private final int start;
            private final int end;

            ForkBothTask(int[] array, int start, int end) {
                this.array = array;
                this.start = start;
                this.end = end;
            }

            @Override
            protected Long compute() {
                int length = end - start;
                if (length <= THRESHOLD) {
                    long sum = 0;
                    for (int i = start; i < end; i++) sum += array[i];
                    return sum;
                }
                int mid = start + length / 2;
                ForkBothTask left = new ForkBothTask(array, start, mid);
                ForkBothTask right = new ForkBothTask(array, mid, end);
                left.fork();   // fork left
                right.fork();  // fork right (anti-pattern: current thread is idle)
                return left.join() + right.join();
            }
        }
    }

    /**
     * Demonstrates work-stealing by showing that ForkJoinPool
     * re-distributes work across idle threads.
     */
    public static class WorkStealingDemo {

        /**
         * Returns the names of threads that participated in the computation.
         * This demonstrates work-stealing: multiple threads process subtasks.
         */
        public static List<String> getParticipatingThreads(int[] array) {
            if (array == null || array.length == 0) return List.of();
            List<String> threads = java.util.Collections.synchronizedList(new ArrayList<>());
            ForkJoinPool pool = new ForkJoinPool(4);
            try {
                pool.invoke(new ThreadRecordingAction(array, 0, array.length, threads));
            } finally {
                pool.shutdown();
            }
            return threads.stream().distinct().toList();
        }

        private static class ThreadRecordingAction extends RecursiveTask<Long> {
            private static final int THRESHOLD = 50;
            private final int[] array;
            private final int start;
            private final int end;
            private final List<String> threads;

            ThreadRecordingAction(int[] array, int start, int end, List<String> threads) {
                this.array = array;
                this.start = start;
                this.end = end;
                this.threads = threads;
            }

            @Override
            protected Long compute() {
                int length = end - start;
                if (length <= THRESHOLD) {
                    threads.add(Thread.currentThread().getName());
                    long sum = 0;
                    for (int i = start; i < end; i++) sum += array[i];
                    return sum;
                }
                int mid = start + length / 2;
                ThreadRecordingAction left = new ThreadRecordingAction(array, start, mid, threads);
                ThreadRecordingAction right = new ThreadRecordingAction(array, mid, end, threads);
                left.fork();
                long rightResult = right.compute();
                long leftResult = left.join();
                return leftResult + rightResult;
            }
        }
    }
}
