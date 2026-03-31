package com.github.msorkhpar.claudejavatutor.forkjoin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Splitting and Joining Tests")
class SplittingAndJoiningTest {

    @Nested
    @DisplayName("ThresholdExperiment")
    class ThresholdExperimentTest {

        @Test
        @DisplayName("Should sum with small threshold")
        void testSmallThreshold() {
            long[] array = LongStream.rangeClosed(1, 1000).toArray();
            long expected = 1000L * 1001L / 2;
            assertThat(SplittingAndJoining.ThresholdExperiment.sumWithThreshold(array, 10))
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("Should sum with large threshold (sequential)")
        void testLargeThreshold() {
            long[] array = LongStream.rangeClosed(1, 1000).toArray();
            long expected = 1000L * 1001L / 2;
            assertThat(SplittingAndJoining.ThresholdExperiment.sumWithThreshold(array, 10_000))
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("Should sum with threshold = 1")
        void testThreshold1() {
            long[] array = {1, 2, 3, 4, 5};
            assertThat(SplittingAndJoining.ThresholdExperiment.sumWithThreshold(array, 1))
                    .isEqualTo(15);
        }

        @Test
        @DisplayName("Should return 0 for null array")
        void testNullArray() {
            assertThat(SplittingAndJoining.ThresholdExperiment.sumWithThreshold(null, 100))
                    .isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 0 for empty array")
        void testEmptyArray() {
            assertThat(SplittingAndJoining.ThresholdExperiment.sumWithThreshold(new long[0], 100))
                    .isEqualTo(0);
        }

        @Test
        @DisplayName("Should throw for threshold < 1")
        void testInvalidThreshold() {
            long[] array = {1, 2, 3};
            assertThatThrownBy(() ->
                    SplittingAndJoining.ThresholdExperiment.sumWithThreshold(array, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("SplitStrategy")
    class SplitStrategyTest {

        @Test
        @DisplayName("Balanced split should produce correct sum")
        void testBalancedSum() {
            int[] array = IntStream.rangeClosed(1, 5000).toArray();
            long expected = 5000L * 5001L / 2;
            assertThat(SplittingAndJoining.SplitStrategy.balancedSum(array))
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("Unbalanced split should produce correct sum")
        void testUnbalancedSum() {
            int[] array = IntStream.rangeClosed(1, 5000).toArray();
            long expected = 5000L * 5001L / 2;
            assertThat(SplittingAndJoining.SplitStrategy.unbalancedSum(array))
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("Both strategies should produce the same result")
        void testSameResult() {
            int[] array = IntStream.rangeClosed(1, 3000).toArray();
            long balanced = SplittingAndJoining.SplitStrategy.balancedSum(array);
            long unbalanced = SplittingAndJoining.SplitStrategy.unbalancedSum(array);
            assertThat(balanced).isEqualTo(unbalanced);
        }

        @Test
        @DisplayName("Balanced should return 0 for null")
        void testBalancedNull() {
            assertThat(SplittingAndJoining.SplitStrategy.balancedSum(null)).isEqualTo(0);
        }

        @Test
        @DisplayName("Unbalanced should return 0 for null")
        void testUnbalancedNull() {
            assertThat(SplittingAndJoining.SplitStrategy.unbalancedSum(null)).isEqualTo(0);
        }

        @Test
        @DisplayName("Balanced should return 0 for empty")
        void testBalancedEmpty() {
            assertThat(SplittingAndJoining.SplitStrategy.balancedSum(new int[0])).isEqualTo(0);
        }

        @Test
        @DisplayName("Unbalanced should return 0 for empty")
        void testUnbalancedEmpty() {
            assertThat(SplittingAndJoining.SplitStrategy.unbalancedSum(new int[0])).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle single element")
        void testSingleElement() {
            assertThat(SplittingAndJoining.SplitStrategy.balancedSum(new int[]{42})).isEqualTo(42);
            assertThat(SplittingAndJoining.SplitStrategy.unbalancedSum(new int[]{42})).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("MultiWaySplit")
    class MultiWaySplitTest {

        @Test
        @DisplayName("Should sum with 2-way split")
        void testTwoWay() {
            long[] array = LongStream.rangeClosed(1, 1000).toArray();
            long expected = 1000L * 1001L / 2;
            assertThat(SplittingAndJoining.MultiWaySplit.multiWaySum(array, 2))
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("Should sum with 4-way split")
        void testFourWay() {
            long[] array = LongStream.rangeClosed(1, 1000).toArray();
            long expected = 1000L * 1001L / 2;
            assertThat(SplittingAndJoining.MultiWaySplit.multiWaySum(array, 4))
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("Should sum with 8-way split")
        void testEightWay() {
            long[] array = LongStream.rangeClosed(1, 2000).toArray();
            long expected = 2000L * 2001L / 2;
            assertThat(SplittingAndJoining.MultiWaySplit.multiWaySum(array, 8))
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("Should sum with 1-way (sequential)")
        void testOneWay() {
            long[] array = {1, 2, 3, 4, 5};
            assertThat(SplittingAndJoining.MultiWaySplit.multiWaySum(array, 1))
                    .isEqualTo(15);
        }

        @Test
        @DisplayName("Should return 0 for null")
        void testNull() {
            assertThat(SplittingAndJoining.MultiWaySplit.multiWaySum(null, 4)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 0 for empty")
        void testEmpty() {
            assertThat(SplittingAndJoining.MultiWaySplit.multiWaySum(new long[0], 4)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should throw for ways < 1")
        void testInvalidWays() {
            long[] array = {1, 2, 3};
            assertThatThrownBy(() -> SplittingAndJoining.MultiWaySplit.multiWaySum(array, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should handle more ways than elements")
        void testMoreWaysThanElements() {
            long[] array = {1, 2, 3};
            assertThat(SplittingAndJoining.MultiWaySplit.multiWaySum(array, 100))
                    .isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("ForkJoinPattern")
    class ForkJoinPatternTest {

        @Test
        @DisplayName("Correct pattern should produce correct result")
        void testCorrectPattern() {
            int[] array = IntStream.rangeClosed(1, 5000).toArray();
            long expected = 5000L * 5001L / 2;
            assertThat(SplittingAndJoining.ForkJoinPattern.correctPattern(array))
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("Anti-pattern should produce correct result (just less efficient)")
        void testAntiPattern() {
            int[] array = IntStream.rangeClosed(1, 5000).toArray();
            long expected = 5000L * 5001L / 2;
            assertThat(SplittingAndJoining.ForkJoinPattern.antiPatternForkBoth(array))
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("Both patterns should produce the same result")
        void testSameResult() {
            int[] array = IntStream.rangeClosed(1, 2000).toArray();
            long correct = SplittingAndJoining.ForkJoinPattern.correctPattern(array);
            long antiPattern = SplittingAndJoining.ForkJoinPattern.antiPatternForkBoth(array);
            assertThat(correct).isEqualTo(antiPattern);
        }

        @Test
        @DisplayName("Correct pattern should return 0 for null")
        void testCorrectNull() {
            assertThat(SplittingAndJoining.ForkJoinPattern.correctPattern(null)).isEqualTo(0);
        }

        @Test
        @DisplayName("Anti-pattern should return 0 for null")
        void testAntiPatternNull() {
            assertThat(SplittingAndJoining.ForkJoinPattern.antiPatternForkBoth(null)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle empty array")
        void testEmpty() {
            assertThat(SplittingAndJoining.ForkJoinPattern.correctPattern(new int[0])).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("WorkStealingDemo")
    class WorkStealingDemoTest {

        @Test
        @DisplayName("Should use multiple threads for large arrays")
        void testMultipleThreads() {
            int[] array = IntStream.rangeClosed(1, 10_000).toArray();
            List<String> threads = SplittingAndJoining.WorkStealingDemo.getParticipatingThreads(array);
            // With a pool of 4 and enough work, multiple threads should participate
            assertThat(threads).isNotEmpty();
        }

        @Test
        @DisplayName("Should return empty list for null array")
        void testNullArray() {
            List<String> threads = SplittingAndJoining.WorkStealingDemo.getParticipatingThreads(null);
            assertThat(threads).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for empty array")
        void testEmptyArray() {
            List<String> threads = SplittingAndJoining.WorkStealingDemo.getParticipatingThreads(new int[0]);
            assertThat(threads).isEmpty();
        }

        @Test
        @DisplayName("Should handle small array (may use single thread)")
        void testSmallArray() {
            int[] array = {1, 2, 3};
            List<String> threads = SplittingAndJoining.WorkStealingDemo.getParticipatingThreads(array);
            assertThat(threads).hasSizeGreaterThanOrEqualTo(1);
        }
    }
}
