package com.github.msorkhpar.claudejavatutor.forkjoin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ForkJoinPool;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ForkJoinPool Basics Tests")
class ForkJoinPoolBasicsTest {

    @Nested
    @DisplayName("Sum with Common Pool")
    class SumWithCommonPoolTest {

        @Test
        @DisplayName("Should sum a simple array")
        void testSimpleSum() {
            long[] array = {1, 2, 3, 4, 5};
            long result = ForkJoinPoolBasics.sumWithCommonPool(array);
            assertThat(result).isEqualTo(15);
        }

        @Test
        @DisplayName("Should return 0 for null array")
        void testNullArray() {
            assertThat(ForkJoinPoolBasics.sumWithCommonPool(null)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 0 for empty array")
        void testEmptyArray() {
            assertThat(ForkJoinPoolBasics.sumWithCommonPool(new long[0])).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle single element")
        void testSingleElement() {
            assertThat(ForkJoinPoolBasics.sumWithCommonPool(new long[]{42})).isEqualTo(42);
        }

        @Test
        @DisplayName("Should sum large array correctly")
        void testLargeArray() {
            long[] array = LongStream.rangeClosed(1, 10_000).toArray();
            long expected = 10_000L * 10_001L / 2;
            assertThat(ForkJoinPoolBasics.sumWithCommonPool(array)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should handle negative numbers")
        void testNegativeNumbers() {
            long[] array = {-5, -3, 10, -2};
            assertThat(ForkJoinPoolBasics.sumWithCommonPool(array)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle overflow scenario with large values")
        void testLargeValues() {
            long[] array = {Long.MAX_VALUE / 2, Long.MAX_VALUE / 2};
            long result = ForkJoinPoolBasics.sumWithCommonPool(array);
            assertThat(result).isEqualTo(Long.MAX_VALUE / 2 + Long.MAX_VALUE / 2);
        }
    }

    @Nested
    @DisplayName("Sum with Custom Pool")
    class SumWithCustomPoolTest {

        @Test
        @DisplayName("Should sum with parallelism 1")
        void testParallelism1() {
            long[] array = {1, 2, 3, 4, 5};
            assertThat(ForkJoinPoolBasics.sumWithCustomPool(array, 1)).isEqualTo(15);
        }

        @Test
        @DisplayName("Should sum with parallelism 4")
        void testParallelism4() {
            long[] array = LongStream.rangeClosed(1, 5000).toArray();
            long expected = 5000L * 5001L / 2;
            assertThat(ForkJoinPoolBasics.sumWithCustomPool(array, 4)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should throw for parallelism < 1")
        void testInvalidParallelism() {
            long[] array = {1, 2, 3};
            assertThatThrownBy(() -> ForkJoinPoolBasics.sumWithCustomPool(array, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Parallelism must be at least 1");
        }

        @Test
        @DisplayName("Should return 0 for null array")
        void testNullArray() {
            assertThat(ForkJoinPoolBasics.sumWithCustomPool(null, 2)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 0 for empty array")
        void testEmptyArray() {
            assertThat(ForkJoinPoolBasics.sumWithCustomPool(new long[0], 2)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Common Pool Info")
    class CommonPoolInfoTest {

        @Test
        @DisplayName("Should return non-null pool info")
        void testPoolInfoNotNull() {
            ForkJoinPoolBasics.PoolInfo info = ForkJoinPoolBasics.getCommonPoolInfo();
            assertThat(info).isNotNull();
        }

        @Test
        @DisplayName("Should have positive parallelism")
        void testParallelismIsPositive() {
            ForkJoinPoolBasics.PoolInfo info = ForkJoinPoolBasics.getCommonPoolInfo();
            assertThat(info.parallelism()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Pool parallelism should match common pool")
        void testMatchesCommonPool() {
            ForkJoinPoolBasics.PoolInfo info = ForkJoinPoolBasics.getCommonPoolInfo();
            assertThat(info.parallelism()).isEqualTo(ForkJoinPool.commonPool().getParallelism());
        }
    }

    @Nested
    @DisplayName("Async Sum")
    class AsyncSumTest {

        @Test
        @DisplayName("Should sum asynchronously")
        void testAsyncSum() {
            long[] array = {10, 20, 30, 40, 50};
            assertThat(ForkJoinPoolBasics.sumAsync(array)).isEqualTo(150);
        }

        @Test
        @DisplayName("Should return 0 for null array async")
        void testAsyncNull() {
            assertThat(ForkJoinPoolBasics.sumAsync(null)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 0 for empty array async")
        void testAsyncEmpty() {
            assertThat(ForkJoinPoolBasics.sumAsync(new long[0])).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Sum with Timeout")
    class SumWithTimeoutTest {

        @Test
        @DisplayName("Should complete within timeout")
        void testCompletesInTime() {
            long[] array = {1, 2, 3, 4, 5};
            long result = ForkJoinPoolBasics.sumWithTimeout(array, 5000);
            assertThat(result).isEqualTo(15);
        }

        @Test
        @DisplayName("Should return 0 for null array with timeout")
        void testNullWithTimeout() {
            assertThat(ForkJoinPoolBasics.sumWithTimeout(null, 1000)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Sum with Parallel Stream")
    class SumWithParallelStreamTest {

        @Test
        @DisplayName("Should sum using parallel stream")
        void testParallelStreamSum() {
            long[] array = LongStream.rangeClosed(1, 1000).toArray();
            long expected = 1000L * 1001L / 2;
            assertThat(ForkJoinPoolBasics.sumWithParallelStream(array)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should return 0 for null array")
        void testNullParallelStream() {
            assertThat(ForkJoinPoolBasics.sumWithParallelStream(null)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 0 for empty array")
        void testEmptyParallelStream() {
            assertThat(ForkJoinPoolBasics.sumWithParallelStream(new long[0])).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Sum with Custom Pool and Stream")
    class SumWithCustomPoolAndStreamTest {

        @Test
        @DisplayName("Should sum using custom pool and stream")
        void testCustomPoolStream() {
            long[] array = LongStream.rangeClosed(1, 100).toArray();
            long expected = 100L * 101L / 2;
            assertThat(ForkJoinPoolBasics.sumWithCustomPoolAndStream(array, 2)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should return 0 for null with custom pool stream")
        void testNullCustomPoolStream() {
            assertThat(ForkJoinPoolBasics.sumWithCustomPoolAndStream(null, 2)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 0 for empty with custom pool stream")
        void testEmptyCustomPoolStream() {
            assertThat(ForkJoinPoolBasics.sumWithCustomPoolAndStream(new long[0], 2)).isEqualTo(0);
        }
    }
}
