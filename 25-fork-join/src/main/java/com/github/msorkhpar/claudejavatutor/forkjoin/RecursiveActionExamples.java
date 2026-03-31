package com.github.msorkhpar.claudejavatutor.forkjoin;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates implementing RecursiveAction for resultless (side-effect) fork/join tasks.
 * Covers in-place transformations, parallel array operations, and void computations.
 */
public class RecursiveActionExamples {

    /**
     * RecursiveAction that increments every element of an array in-place.
     */
    public static class IncrementAction extends RecursiveAction {
        private static final int THRESHOLD = 1000;
        private final int[] array;
        private final int start;
        private final int end;
        private final int incrementBy;

        public IncrementAction(int[] array, int start, int end, int incrementBy) {
            if (array == null) {
                throw new IllegalArgumentException("Array must not be null");
            }
            this.array = array;
            this.start = start;
            this.end = end;
            this.incrementBy = incrementBy;
        }

        public IncrementAction(int[] array, int incrementBy) {
            this(array, 0, array == null ? 0 : array.length, incrementBy);
        }

        @Override
        protected void compute() {
            int length = end - start;
            if (length <= THRESHOLD) {
                for (int i = start; i < end; i++) {
                    array[i] += incrementBy;
                }
                return;
            }
            int mid = start + length / 2;
            IncrementAction left = new IncrementAction(array, start, mid, incrementBy);
            IncrementAction right = new IncrementAction(array, mid, end, incrementBy);
            invokeAll(left, right);
        }
    }

    /**
     * RecursiveAction that applies a transformation: each element becomes element * factor + offset.
     */
    public static class TransformAction extends RecursiveAction {
        private static final int THRESHOLD = 500;
        private final double[] array;
        private final int start;
        private final int end;
        private final double factor;
        private final double offset;

        public TransformAction(double[] array, int start, int end, double factor, double offset) {
            if (array == null) {
                throw new IllegalArgumentException("Array must not be null");
            }
            this.array = array;
            this.start = start;
            this.end = end;
            this.factor = factor;
            this.offset = offset;
        }

        public TransformAction(double[] array, double factor, double offset) {
            this(array, 0, array == null ? 0 : array.length, factor, offset);
        }

        @Override
        protected void compute() {
            int length = end - start;
            if (length <= THRESHOLD) {
                for (int i = start; i < end; i++) {
                    array[i] = array[i] * factor + offset;
                }
                return;
            }
            int mid = start + length / 2;
            TransformAction left = new TransformAction(array, start, mid, factor, offset);
            TransformAction right = new TransformAction(array, mid, end, factor, offset);
            invokeAll(left, right);
        }
    }

    /**
     * RecursiveAction that fills a range of an array with a given value.
     */
    public static class FillAction extends RecursiveAction {
        private static final int THRESHOLD = 1000;
        private final int[] array;
        private final int start;
        private final int end;
        private final int value;

        public FillAction(int[] array, int start, int end, int value) {
            if (array == null) {
                throw new IllegalArgumentException("Array must not be null");
            }
            this.array = array;
            this.start = start;
            this.end = end;
            this.value = value;
        }

        public FillAction(int[] array, int value) {
            this(array, 0, array == null ? 0 : array.length, value);
        }

        @Override
        protected void compute() {
            int length = end - start;
            if (length <= THRESHOLD) {
                Arrays.fill(array, start, end, value);
                return;
            }
            int mid = start + length / 2;
            FillAction left = new FillAction(array, start, mid, value);
            FillAction right = new FillAction(array, mid, end, value);
            invokeAll(left, right);
        }
    }

    /**
     * RecursiveAction that counts elements matching a predicate using AtomicInteger
     * (demonstrating side-effect accumulation in RecursiveAction).
     */
    public static class CountMatchingAction extends RecursiveAction {
        private static final int THRESHOLD = 500;
        private final int[] array;
        private final int start;
        private final int end;
        private final int target;
        private final AtomicInteger counter;

        public CountMatchingAction(int[] array, int start, int end, int target, AtomicInteger counter) {
            if (array == null) {
                throw new IllegalArgumentException("Array must not be null");
            }
            if (counter == null) {
                throw new IllegalArgumentException("Counter must not be null");
            }
            this.array = array;
            this.start = start;
            this.end = end;
            this.target = target;
            this.counter = counter;
        }

        public CountMatchingAction(int[] array, int target, AtomicInteger counter) {
            this(array, 0, array == null ? 0 : array.length, target, counter);
        }

        @Override
        protected void compute() {
            int length = end - start;
            if (length <= THRESHOLD) {
                int localCount = 0;
                for (int i = start; i < end; i++) {
                    if (array[i] == target) {
                        localCount++;
                    }
                }
                counter.addAndGet(localCount);
                return;
            }
            int mid = start + length / 2;
            CountMatchingAction left = new CountMatchingAction(array, start, mid, target, counter);
            CountMatchingAction right = new CountMatchingAction(array, mid, end, target, counter);
            invokeAll(left, right);
        }
    }

    /**
     * RecursiveAction that squares every element in an int array in-place.
     */
    public static class SquareAction extends RecursiveAction {
        private static final int THRESHOLD = 1000;
        private final int[] array;
        private final int start;
        private final int end;

        public SquareAction(int[] array, int start, int end) {
            if (array == null) {
                throw new IllegalArgumentException("Array must not be null");
            }
            this.array = array;
            this.start = start;
            this.end = end;
        }

        public SquareAction(int[] array) {
            this(array, 0, array == null ? 0 : array.length);
        }

        @Override
        protected void compute() {
            int length = end - start;
            if (length <= THRESHOLD) {
                for (int i = start; i < end; i++) {
                    array[i] = array[i] * array[i];
                }
                return;
            }
            int mid = start + length / 2;
            SquareAction left = new SquareAction(array, start, mid);
            SquareAction right = new SquareAction(array, mid, end);
            invokeAll(left, right);
        }
    }

    // Convenience methods

    /**
     * Increments all elements of the array in-place using ForkJoinPool.
     */
    public static void parallelIncrement(int[] array, int incrementBy) {
        if (array == null || array.length == 0) return;
        ForkJoinPool.commonPool().invoke(new IncrementAction(array, incrementBy));
    }

    /**
     * Applies linear transformation (element * factor + offset) to all elements in-place.
     */
    public static void parallelTransform(double[] array, double factor, double offset) {
        if (array == null || array.length == 0) return;
        ForkJoinPool.commonPool().invoke(new TransformAction(array, factor, offset));
    }

    /**
     * Fills the entire array with the given value using ForkJoinPool.
     */
    public static void parallelFill(int[] array, int value) {
        if (array == null || array.length == 0) return;
        ForkJoinPool.commonPool().invoke(new FillAction(array, value));
    }

    /**
     * Squares every element in-place using ForkJoinPool.
     */
    public static void parallelSquare(int[] array) {
        if (array == null || array.length == 0) return;
        ForkJoinPool.commonPool().invoke(new SquareAction(array));
    }

    /**
     * Counts elements matching a target using RecursiveAction with AtomicInteger.
     */
    public static int parallelCountMatching(int[] array, int target) {
        if (array == null || array.length == 0) return 0;
        AtomicInteger counter = new AtomicInteger(0);
        ForkJoinPool.commonPool().invoke(new CountMatchingAction(array, target, counter));
        return counter.get();
    }
}
