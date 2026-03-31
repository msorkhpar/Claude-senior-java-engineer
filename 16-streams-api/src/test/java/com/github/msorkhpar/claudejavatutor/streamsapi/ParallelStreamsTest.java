package com.github.msorkhpar.claudejavatutor.streamsapi;

import com.github.msorkhpar.claudejavatutor.base.PerformanceTestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("4.4.5 - Parallel Streams Tests")
class ParallelStreamsTest {

    private final ParallelStreams ps = new ParallelStreams();

    @Nested
    @DisplayName("Creating Parallel Streams")
    class CreationTest {

        @Test
        @DisplayName("parallelStream() should produce a parallel stream")
        void testIsParallelFromCollection() {
            assertThat(ps.isParallelFromCollection(List.of(1, 2, 3))).isTrue();
        }

        @Test
        @DisplayName("stream().parallel() should produce a parallel stream")
        void testIsParallelAfterConversion() {
            assertThat(ps.isParallelAfterConversion(List.of(1, 2, 3))).isTrue();
        }

        @Test
        @DisplayName("parallelStream().sequential() should produce a sequential stream")
        void testIsSequentialAfterConversion() {
            assertThat(ps.isSequentialAfterConversion(List.of(1, 2, 3))).isFalse();
        }
    }

    @Nested
    @DisplayName("Thread-safe Collection")
    class ThreadSafeCollectionTest {

        @Test
        @DisplayName("safeCollect should produce correct even-number list")
        void testSafeCollect() {
            List<Integer> input = IntStream.rangeClosed(1, 20).boxed().collect(Collectors.toList());
            List<Integer> result = ps.safeCollect(input);
            assertThat(result).hasSize(10);
            assertThat(result).allMatch(n -> n % 2 == 0);
        }

        @Test
        @DisplayName("safeCollect on empty list returns empty")
        void testSafeCollectEmpty() {
            assertThat(ps.safeCollect(List.of())).isEmpty();
        }

        @Test
        @DisplayName("parallelGroupCount should correctly group by last digit")
        void testParallelGroupCount() {
            List<Integer> input = IntStream.rangeClosed(1, 100).boxed().collect(Collectors.toList());
            Map<Integer, Long> result = ps.parallelGroupCount(input);
            // 10 groups of 10 each (1-10, 11-20, ...) with last digit mapping
            assertThat(result).hasSize(10);
            result.values().forEach(count -> assertThat(count).isEqualTo(10L));
        }
    }

    @Nested
    @DisplayName("Ordering: forEachOrdered vs forEach")
    class OrderingTest {

        @Test
        @DisplayName("parallelForEachOrdered should preserve encounter order")
        void testParallelForEachOrdered() {
            List<Integer> input = IntStream.rangeClosed(1, 20).boxed().collect(Collectors.toList());
            List<Integer> result = ps.parallelForEachOrdered(input);
            assertThat(result).containsExactlyElementsOf(input);
        }

        @Test
        @DisplayName("parallelForEach should contain all elements (any order)")
        void testParallelForEachContainsAll() {
            List<Integer> input = IntStream.rangeClosed(1, 20).boxed().collect(Collectors.toList());
            List<Integer> result = ps.parallelForEach(input);
            assertThat(result).containsExactlyInAnyOrder(input.toArray(Integer[]::new));
        }
    }

    @Nested
    @DisplayName("Correct Parallel Reduction (Associativity)")
    class AssociativeReductionTest {

        @Test
        @DisplayName("parallelSum should produce correct total")
        void testParallelSum() {
            List<Integer> list = IntStream.rangeClosed(1, 1000).boxed().collect(Collectors.toList());
            // Sum of 1..1000 = 500500
            assertThat(ps.parallelSum(list)).isEqualTo(500500L);
        }

        @Test
        @DisplayName("parallelSum on empty list returns 0")
        void testParallelSumEmpty() {
            assertThat(ps.parallelSum(List.of())).isEqualTo(0L);
        }

        @Test
        @DisplayName("parallelProduct should compute correct product for small list")
        void testParallelProduct() {
            List<Integer> list = List.of(1, 2, 3, 4, 5);
            assertThat(ps.parallelProduct(list)).isEqualTo(120L);
        }

        @Test
        @DisplayName("parallelMax should return maximum element")
        void testParallelMax() {
            List<Integer> list = List.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
            assertThat(ps.parallelMax(list)).contains(9);
        }

        @Test
        @DisplayName("parallelMax on empty returns Optional.empty")
        void testParallelMaxEmpty() {
            assertThat(ps.parallelMax(List.of())).isEmpty();
        }

        @Test
        @DisplayName("parallelMin should return minimum element")
        void testParallelMin() {
            List<Integer> list = List.of(5, 2, 8, 1, 9, 3);
            assertThat(ps.parallelMin(list)).contains(1);
        }

        @Test
        @DisplayName("Sequential and parallel sum produce same result")
        void testSequentialParallelEquivalence() {
            List<Integer> largeList = IntStream.rangeClosed(1, 100_000)
                    .boxed().collect(Collectors.toList());
            long seqSum = ps.sumOfSquaresSequential(largeList);
            long parSum = ps.sumOfSquaresParallel(largeList);
            assertThat(parSum).isEqualTo(seqSum);
        }
    }

    @Nested
    @DisplayName("Unordered hint for parallel optimization")
    class UnorderedTest {

        @Test
        @DisplayName("parallelDistinctUnordered should produce unique elements")
        void testParallelDistinctUnordered() {
            List<Integer> list = List.of(1, 2, 3, 2, 1, 4, 5, 4, 3);
            List<Integer> result = ps.parallelDistinctUnordered(list);
            assertThat(result).containsExactlyInAnyOrder(1, 2, 3, 4, 5);
            assertThat(result).doesNotHaveDuplicates();
        }
    }

    @Nested
    @DisplayName("Custom ForkJoinPool")
    class CustomPoolTest {

        @Test
        @DisplayName("runInCustomPool should correctly filter and sort evens")
        void testRunInCustomPool() throws Exception {
            List<Integer> list = IntStream.rangeClosed(1, 20).boxed().collect(Collectors.toList());
            List<Integer> result = ps.runInCustomPool(list, 2);
            assertThat(result).containsExactly(2, 4, 6, 8, 10, 12, 14, 16, 18, 20);
        }

        @Test
        @DisplayName("runInCustomPool with empty list returns empty")
        void testRunInCustomPoolEmpty() throws Exception {
            assertThat(ps.runInCustomPool(List.of(), 2)).isEmpty();
        }
    }

    @Nested
    @DisplayName("CPU-intensive Parallel Speedup")
    class PerformanceTest {

        @Test
        @DisplayName("parallelSum and sequentialSum produce same correct result")
        void testParallelVsSequentialCorrectnessLarge() {
            List<Integer> data = IntStream.rangeClosed(1, 50_000)
                    .boxed().collect(Collectors.toList());
            long seqResult = ps.sumOfSquaresSequential(data);
            long parResult = ps.sumOfSquaresParallel(data);
            assertThat(parResult).isEqualTo(seqResult);
        }

        @Test
        @DisplayName("countPrimesParallel should produce same result as sequential")
        void testCountPrimesEquivalence() {
            int limit = 10_000;
            long seqCount = ps.countPrimesSequential(limit);
            long parCount = ps.countPrimesParallel(limit);
            assertThat(parCount).isEqualTo(seqCount);
        }

        @Test
        @DisplayName("PerformanceTestUtil can measure parallel stream execution")
        void testPerformanceMeasurement() {
            List<Integer> data = IntStream.rangeClosed(1, 100_000)
                    .boxed().collect(Collectors.toList());

            var seqMeasure = PerformanceTestUtil.measureExecution(
                    () -> ps.sumOfSquaresSequential(data));
            var parMeasure = PerformanceTestUtil.measureExecution(
                    () -> ps.sumOfSquaresParallel(data));

            // Both should return the same result value
            assertThat(parMeasure.result()).isEqualTo(seqMeasure.result());
            // Execution time should be positive (greater than 0)
            assertThat(seqMeasure.executionTime()).isPositive();
            assertThat(parMeasure.executionTime()).isPositive();
        }
    }

    @Nested
    @DisplayName("Thread-safe Stateful Operations")
    class StatefulParallelTest {

        @Test
        @DisplayName("parallelDistinctWithConcurrentSet should deduplicate safely")
        void testParallelDistinctWithConcurrentSet() {
            List<String> list = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                list.add("item" + (i % 20)); // 20 unique items, each repeated 5 times
            }
            List<String> result = ps.parallelDistinctWithConcurrentSet(list);
            assertThat(result).doesNotHaveDuplicates();
            assertThat(result).hasSize(20);
        }

        @Test
        @DisplayName("parallelGroupByFirstChar should correctly group strings")
        void testParallelGroupByFirstChar() {
            List<String> list = List.of("apple", "avocado", "banana", "cherry", "blueberry", "apricot");
            Map<String, List<String>> result = ps.parallelGroupByFirstChar(list);
            assertThat(result.get("a")).containsExactlyInAnyOrder("apple", "avocado", "apricot");
            assertThat(result.get("b")).containsExactlyInAnyOrder("banana", "blueberry");
            assertThat(result.get("c")).containsExactly("cherry");
        }

        @Test
        @DisplayName("parallelGroupByFirstChar on empty list returns empty map")
        void testParallelGroupByFirstCharEmpty() {
            assertThat(ps.parallelGroupByFirstChar(List.of())).isEmpty();
        }
    }
}
