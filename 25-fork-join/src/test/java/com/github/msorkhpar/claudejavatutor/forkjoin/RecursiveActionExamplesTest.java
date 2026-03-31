package com.github.msorkhpar.claudejavatutor.forkjoin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RecursiveAction Examples Tests")
class RecursiveActionExamplesTest {

    @Nested
    @DisplayName("IncrementAction")
    class IncrementActionTest {

        @Test
        @DisplayName("Should increment all elements by 1")
        void testIncrementBy1() {
            int[] array = {1, 2, 3, 4, 5};
            RecursiveActionExamples.parallelIncrement(array, 1);
            assertThat(array).containsExactly(2, 3, 4, 5, 6);
        }

        @Test
        @DisplayName("Should increment all elements by negative value")
        void testIncrementByNegative() {
            int[] array = {10, 20, 30};
            RecursiveActionExamples.parallelIncrement(array, -5);
            assertThat(array).containsExactly(5, 15, 25);
        }

        @Test
        @DisplayName("Should increment by 0 (no change)")
        void testIncrementByZero() {
            int[] array = {1, 2, 3};
            RecursiveActionExamples.parallelIncrement(array, 0);
            assertThat(array).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Should handle null array gracefully")
        void testNullArray() {
            assertThatCode(() -> RecursiveActionExamples.parallelIncrement(null, 1))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle empty array gracefully")
        void testEmptyArray() {
            int[] array = new int[0];
            assertThatCode(() -> RecursiveActionExamples.parallelIncrement(array, 1))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should increment large array correctly")
        void testLargeArray() {
            int[] array = new int[5000];
            Arrays.fill(array, 1);
            RecursiveActionExamples.parallelIncrement(array, 10);
            assertThat(array).containsOnly(11);
        }

        @Test
        @DisplayName("Should throw for null array in constructor")
        void testConstructorNull() {
            assertThatThrownBy(() -> new RecursiveActionExamples.IncrementAction(null, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("TransformAction")
    class TransformActionTest {

        @Test
        @DisplayName("Should apply linear transformation")
        void testLinearTransform() {
            double[] array = {1.0, 2.0, 3.0};
            RecursiveActionExamples.parallelTransform(array, 2.0, 1.0);
            assertThat(array).containsExactly(3.0, 5.0, 7.0);
        }

        @Test
        @DisplayName("Should apply identity transformation")
        void testIdentityTransform() {
            double[] array = {5.0, 10.0};
            RecursiveActionExamples.parallelTransform(array, 1.0, 0.0);
            assertThat(array).containsExactly(5.0, 10.0);
        }

        @Test
        @DisplayName("Should apply zero-out transformation")
        void testZeroTransform() {
            double[] array = {5.0, 10.0, 15.0};
            RecursiveActionExamples.parallelTransform(array, 0.0, 0.0);
            assertThat(array).containsExactly(0.0, 0.0, 0.0);
        }

        @Test
        @DisplayName("Should handle null array gracefully")
        void testNullArray() {
            assertThatCode(() -> RecursiveActionExamples.parallelTransform(null, 2.0, 1.0))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle empty array gracefully")
        void testEmptyArray() {
            assertThatCode(() -> RecursiveActionExamples.parallelTransform(new double[0], 2.0, 1.0))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should transform large array")
        void testLargeArray() {
            double[] array = new double[2000];
            Arrays.fill(array, 10.0);
            RecursiveActionExamples.parallelTransform(array, 3.0, -5.0);
            for (double v : array) {
                assertThat(v).isEqualTo(25.0);
            }
        }

        @Test
        @DisplayName("Should throw for null array in constructor")
        void testConstructorNull() {
            assertThatThrownBy(() -> new RecursiveActionExamples.TransformAction(null, 1.0, 0.0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("FillAction")
    class FillActionTest {

        @Test
        @DisplayName("Should fill array with value")
        void testFill() {
            int[] array = new int[10];
            RecursiveActionExamples.parallelFill(array, 42);
            assertThat(array).containsOnly(42);
        }

        @Test
        @DisplayName("Should fill large array")
        void testFillLarge() {
            int[] array = new int[5000];
            RecursiveActionExamples.parallelFill(array, -1);
            assertThat(array).containsOnly(-1);
        }

        @Test
        @DisplayName("Should handle null array gracefully")
        void testNullArray() {
            assertThatCode(() -> RecursiveActionExamples.parallelFill(null, 0))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle empty array gracefully")
        void testEmptyArray() {
            assertThatCode(() -> RecursiveActionExamples.parallelFill(new int[0], 0))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw for null in constructor")
        void testConstructorNull() {
            assertThatThrownBy(() -> new RecursiveActionExamples.FillAction(null, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("SquareAction")
    class SquareActionTest {

        @Test
        @DisplayName("Should square all elements")
        void testSquare() {
            int[] array = {1, 2, 3, 4, 5};
            RecursiveActionExamples.parallelSquare(array);
            assertThat(array).containsExactly(1, 4, 9, 16, 25);
        }

        @Test
        @DisplayName("Should handle zeros")
        void testSquareZeros() {
            int[] array = {0, 0, 0};
            RecursiveActionExamples.parallelSquare(array);
            assertThat(array).containsExactly(0, 0, 0);
        }

        @Test
        @DisplayName("Should handle negative numbers")
        void testSquareNegative() {
            int[] array = {-2, -3};
            RecursiveActionExamples.parallelSquare(array);
            assertThat(array).containsExactly(4, 9);
        }

        @Test
        @DisplayName("Should handle null array gracefully")
        void testNullArray() {
            assertThatCode(() -> RecursiveActionExamples.parallelSquare(null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle empty array gracefully")
        void testEmptyArray() {
            assertThatCode(() -> RecursiveActionExamples.parallelSquare(new int[0]))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should square large array")
        void testLargeArray() {
            int[] array = new int[5000];
            Arrays.fill(array, 3);
            RecursiveActionExamples.parallelSquare(array);
            assertThat(array).containsOnly(9);
        }

        @Test
        @DisplayName("Should throw for null in constructor")
        void testConstructorNull() {
            assertThatThrownBy(() -> new RecursiveActionExamples.SquareAction(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("CountMatchingAction")
    class CountMatchingActionTest {

        @Test
        @DisplayName("Should count matching elements")
        void testCountMatching() {
            int[] array = {1, 2, 3, 2, 1, 2};
            assertThat(RecursiveActionExamples.parallelCountMatching(array, 2)).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return 0 when no match")
        void testNoMatch() {
            int[] array = {1, 2, 3};
            assertThat(RecursiveActionExamples.parallelCountMatching(array, 99)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 0 for null array")
        void testNullArray() {
            assertThat(RecursiveActionExamples.parallelCountMatching(null, 1)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 0 for empty array")
        void testEmptyArray() {
            assertThat(RecursiveActionExamples.parallelCountMatching(new int[0], 1)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should count in large array")
        void testLargeArray() {
            int[] array = new int[5000];
            Arrays.fill(array, 7);
            assertThat(RecursiveActionExamples.parallelCountMatching(array, 7)).isEqualTo(5000);
        }

        @Test
        @DisplayName("Should throw for null array in constructor")
        void testConstructorNullArray() {
            assertThatThrownBy(() ->
                    new RecursiveActionExamples.CountMatchingAction(null, 1, new AtomicInteger()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw for null counter in constructor")
        void testConstructorNullCounter() {
            assertThatThrownBy(() ->
                    new RecursiveActionExamples.CountMatchingAction(new int[]{1}, 1, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
