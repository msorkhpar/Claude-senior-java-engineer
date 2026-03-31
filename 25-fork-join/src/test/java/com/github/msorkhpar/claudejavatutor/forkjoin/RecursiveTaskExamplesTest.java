package com.github.msorkhpar.claudejavatutor.forkjoin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RecursiveTask Examples Tests")
class RecursiveTaskExamplesTest {

    @Nested
    @DisplayName("SumTask")
    class SumTaskTest {

        @Test
        @DisplayName("Should sum a simple array")
        void testSimpleSum() {
            assertThat(RecursiveTaskExamples.parallelSum(new int[]{1, 2, 3, 4, 5}))
                    .isEqualTo(15);
        }

        @Test
        @DisplayName("Should return 0 for null array")
        void testNullArray() {
            assertThat(RecursiveTaskExamples.parallelSum(null)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 0 for empty array")
        void testEmptyArray() {
            assertThat(RecursiveTaskExamples.parallelSum(new int[0])).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle single element")
        void testSingleElement() {
            assertThat(RecursiveTaskExamples.parallelSum(new int[]{42})).isEqualTo(42);
        }

        @Test
        @DisplayName("Should sum large array correctly")
        void testLargeArray() {
            int[] array = IntStream.rangeClosed(1, 10_000).toArray();
            long expected = 10_000L * 10_001L / 2;
            assertThat(RecursiveTaskExamples.parallelSum(array)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should handle negative numbers")
        void testNegativeNumbers() {
            assertThat(RecursiveTaskExamples.parallelSum(new int[]{-1, -2, -3})).isEqualTo(-6);
        }

        @Test
        @DisplayName("Should handle mixed positive and negative")
        void testMixedNumbers() {
            assertThat(RecursiveTaskExamples.parallelSum(new int[]{-5, 5, -10, 10})).isEqualTo(0);
        }

        @Test
        @DisplayName("Should throw for null array in SumTask constructor")
        void testSumTaskNullConstructor() {
            assertThatThrownBy(() -> new RecursiveTaskExamples.SumTask(null, 0, 0, 100))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw for threshold < 1")
        void testInvalidThreshold() {
            assertThatThrownBy(() -> new RecursiveTaskExamples.SumTask(new int[]{1}, 0, 1, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should work with custom threshold")
        void testCustomThreshold() {
            int[] array = IntStream.rangeClosed(1, 500).toArray();
            RecursiveTaskExamples.SumTask task = new RecursiveTaskExamples.SumTask(array, 0, array.length, 50);
            long result = ForkJoinPool.commonPool().invoke(task);
            assertThat(result).isEqualTo(500L * 501L / 2);
        }
    }

    @Nested
    @DisplayName("MaxTask")
    class MaxTaskTest {

        @Test
        @DisplayName("Should find max in simple array")
        void testSimpleMax() {
            assertThat(RecursiveTaskExamples.parallelMax(new int[]{3, 1, 4, 1, 5, 9, 2, 6}))
                    .isEqualTo(9);
        }

        @Test
        @DisplayName("Should throw for null array")
        void testNullArray() {
            assertThatThrownBy(() -> RecursiveTaskExamples.parallelMax(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw for empty array")
        void testEmptyArray() {
            assertThatThrownBy(() -> RecursiveTaskExamples.parallelMax(new int[0]))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should find max in single element array")
        void testSingleElement() {
            assertThat(RecursiveTaskExamples.parallelMax(new int[]{7})).isEqualTo(7);
        }

        @Test
        @DisplayName("Should find max in all-negative array")
        void testAllNegative() {
            assertThat(RecursiveTaskExamples.parallelMax(new int[]{-5, -3, -8, -1}))
                    .isEqualTo(-1);
        }

        @Test
        @DisplayName("Should find max in large array")
        void testLargeArray() {
            int[] array = IntStream.rangeClosed(1, 5000).toArray();
            assertThat(RecursiveTaskExamples.parallelMax(array)).isEqualTo(5000);
        }

        @Test
        @DisplayName("Should find max when max is at the beginning")
        void testMaxAtBeginning() {
            assertThat(RecursiveTaskExamples.parallelMax(new int[]{100, 1, 2, 3})).isEqualTo(100);
        }

        @Test
        @DisplayName("Should find max when max is at the end")
        void testMaxAtEnd() {
            assertThat(RecursiveTaskExamples.parallelMax(new int[]{1, 2, 3, 100})).isEqualTo(100);
        }

        @Test
        @DisplayName("Should handle all equal elements")
        void testAllEqual() {
            assertThat(RecursiveTaskExamples.parallelMax(new int[]{5, 5, 5, 5})).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("FibonacciTask")
    class FibonacciTaskTest {

        @Test
        @DisplayName("Should compute fib(0) = 0")
        void testFib0() {
            ForkJoinPool pool = ForkJoinPool.commonPool();
            assertThat(pool.invoke(new RecursiveTaskExamples.FibonacciTask(0)))
                    .isEqualTo(BigInteger.ZERO);
        }

        @Test
        @DisplayName("Should compute fib(1) = 1")
        void testFib1() {
            ForkJoinPool pool = ForkJoinPool.commonPool();
            assertThat(pool.invoke(new RecursiveTaskExamples.FibonacciTask(1)))
                    .isEqualTo(BigInteger.ONE);
        }

        @Test
        @DisplayName("Should compute fib(10) = 55")
        void testFib10() {
            ForkJoinPool pool = ForkJoinPool.commonPool();
            assertThat(pool.invoke(new RecursiveTaskExamples.FibonacciTask(10)))
                    .isEqualTo(BigInteger.valueOf(55));
        }

        @Test
        @DisplayName("Should compute fib(30) correctly")
        void testFib30() {
            ForkJoinPool pool = ForkJoinPool.commonPool();
            assertThat(pool.invoke(new RecursiveTaskExamples.FibonacciTask(30)))
                    .isEqualTo(BigInteger.valueOf(832040));
        }

        @Test
        @DisplayName("Should throw for negative input")
        void testNegativeInput() {
            assertThatThrownBy(() -> new RecursiveTaskExamples.FibonacciTask(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("CountTask")
    class CountTaskTest {

        @Test
        @DisplayName("Should count occurrences of target")
        void testCountOccurrences() {
            int[] array = {1, 2, 3, 2, 1, 2, 3, 2};
            assertThat(RecursiveTaskExamples.parallelCount(array, 2)).isEqualTo(4);
        }

        @Test
        @DisplayName("Should return 0 when target not found")
        void testTargetNotFound() {
            int[] array = {1, 2, 3};
            assertThat(RecursiveTaskExamples.parallelCount(array, 99)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 0 for null array")
        void testNullArray() {
            assertThat(RecursiveTaskExamples.parallelCount(null, 1)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 0 for empty array")
        void testEmptyArray() {
            assertThat(RecursiveTaskExamples.parallelCount(new int[0], 1)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should count in large array")
        void testLargeArray() {
            int[] array = new int[5000];
            Arrays.fill(array, 7);
            array[0] = 1;
            array[2500] = 1;
            assertThat(RecursiveTaskExamples.parallelCount(array, 7)).isEqualTo(4998);
        }

        @Test
        @DisplayName("Should throw for null in CountTask constructor")
        void testNullConstructor() {
            assertThatThrownBy(() -> new RecursiveTaskExamples.CountTask(null, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("MergeSortTask")
    class MergeSortTaskTest {

        @Test
        @DisplayName("Should sort a simple array")
        void testSimpleSort() {
            int[] result = RecursiveTaskExamples.parallelMergeSort(new int[]{5, 3, 1, 4, 2});
            assertThat(result).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Should sort already sorted array")
        void testAlreadySorted() {
            int[] result = RecursiveTaskExamples.parallelMergeSort(new int[]{1, 2, 3, 4, 5});
            assertThat(result).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Should sort reverse-sorted array")
        void testReverseSorted() {
            int[] result = RecursiveTaskExamples.parallelMergeSort(new int[]{5, 4, 3, 2, 1});
            assertThat(result).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Should handle single element array")
        void testSingleElement() {
            int[] result = RecursiveTaskExamples.parallelMergeSort(new int[]{42});
            assertThat(result).containsExactly(42);
        }

        @Test
        @DisplayName("Should handle empty array")
        void testEmptyArray() {
            int[] result = RecursiveTaskExamples.parallelMergeSort(new int[0]);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should throw for null array")
        void testNullArray() {
            assertThatThrownBy(() -> RecursiveTaskExamples.parallelMergeSort(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should sort large array correctly")
        void testLargeArray() {
            int[] array = IntStream.range(0, 5000).map(i -> 5000 - i).toArray();
            int[] result = RecursiveTaskExamples.parallelMergeSort(array);
            int[] expected = IntStream.rangeClosed(1, 5000).toArray();
            assertThat(result).containsExactly(expected);
        }

        @Test
        @DisplayName("Should handle duplicates")
        void testDuplicates() {
            int[] result = RecursiveTaskExamples.parallelMergeSort(new int[]{3, 1, 3, 1, 2});
            assertThat(result).containsExactly(1, 1, 2, 3, 3);
        }

        @Test
        @DisplayName("Should not modify original array")
        void testOriginalUnmodified() {
            int[] original = {5, 3, 1, 4, 2};
            int[] copy = Arrays.copyOf(original, original.length);
            RecursiveTaskExamples.parallelMergeSort(original);
            assertThat(original).containsExactly(copy);
        }

        @Test
        @DisplayName("Should handle negative numbers")
        void testNegativeNumbers() {
            int[] result = RecursiveTaskExamples.parallelMergeSort(new int[]{-3, 1, -1, 0, 2});
            assertThat(result).containsExactly(-3, -1, 0, 1, 2);
        }
    }
}
