package com.github.msorkhpar.claudejavatutor.failfastsafe;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Iterator Choice Guide Tests")
class IteratorChoiceGuideTest {

    @Nested
    @DisplayName("Filtering with ArrayList")
    class ArrayListFilterTests {

        @Test
        @DisplayName("Should filter elements using stream on ArrayList")
        void testFilterWithArrayList() {
            List<Integer> source = List.of(1, 2, 3, 4, 5, 6);

            List<Integer> result = IteratorChoiceGuide.filterWithArrayList(source, n -> n % 2 == 0);

            assertThat(result).containsExactly(2, 4, 6);
        }

        @Test
        @DisplayName("Should return empty list when no elements match")
        void testFilterWithNoMatch() {
            List<Integer> source = List.of(1, 3, 5);

            List<Integer> result = IteratorChoiceGuide.filterWithArrayList(source, n -> n % 2 == 0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return all elements when all match")
        void testFilterWithAllMatch() {
            List<Integer> source = List.of(2, 4, 6);

            List<Integer> result = IteratorChoiceGuide.filterWithArrayList(source, n -> n % 2 == 0);

            assertThat(result).containsExactly(2, 4, 6);
        }

        @Test
        @DisplayName("Should handle empty source list")
        void testFilterEmptyList() {
            List<Integer> result = IteratorChoiceGuide.filterWithArrayList(List.of(), n -> true);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Filtering with CopyOnWriteArrayList")
    class CopyOnWriteFilterTests {

        @Test
        @DisplayName("Should filter elements from CopyOnWriteArrayList")
        void testFilterWithCopyOnWrite() {
            CopyOnWriteArrayList<String> source = new CopyOnWriteArrayList<>(List.of("apple", "banana", "avocado", "cherry"));

            List<String> result = IteratorChoiceGuide.filterWithCopyOnWrite(source, s -> s.startsWith("a"));

            assertThat(result).containsExactly("apple", "avocado");
        }

        @Test
        @DisplayName("Should not modify original CopyOnWriteArrayList")
        void testFilterDoesNotModifyOriginal() {
            CopyOnWriteArrayList<String> source = new CopyOnWriteArrayList<>(List.of("a", "b", "c"));

            IteratorChoiceGuide.filterWithCopyOnWrite(source, s -> s.equals("a"));

            assertThat(source).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("Concurrent Read with CopyOnWriteArrayList")
    class ConcurrentReadTests {

        @Test
        @DisplayName("Should compute total length of elements")
        void testConcurrentReadSize() {
            CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>(List.of("hi", "hello", "hey"));

            int total = IteratorChoiceGuide.concurrentReadSize(list);

            assertThat(total).isEqualTo(2 + 5 + 3);
        }

        @Test
        @DisplayName("Should return zero for empty list")
        void testConcurrentReadSizeEmpty() {
            CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();

            int total = IteratorChoiceGuide.concurrentReadSize(list);

            assertThat(total).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Word Frequency with ConcurrentHashMap")
    class WordFrequencyTests {

        @Test
        @DisplayName("Should count word frequencies correctly")
        void testWordFrequency() {
            List<String> words = List.of("hello", "world", "hello", "java", "world", "hello");

            Map<String, Integer> freq = IteratorChoiceGuide.wordFrequency(words);

            assertThat(freq)
                    .containsEntry("hello", 3)
                    .containsEntry("world", 2)
                    .containsEntry("java", 1);
        }

        @Test
        @DisplayName("Should handle empty word list")
        void testWordFrequencyEmpty() {
            Map<String, Integer> freq = IteratorChoiceGuide.wordFrequency(List.of());

            assertThat(freq).isEmpty();
        }

        @Test
        @DisplayName("Should handle single word")
        void testWordFrequencySingle() {
            Map<String, Integer> freq = IteratorChoiceGuide.wordFrequency(List.of("only"));

            assertThat(freq).containsExactly(entry("only", 1));
        }

        @Test
        @DisplayName("Should handle all unique words")
        void testWordFrequencyAllUnique() {
            Map<String, Integer> freq = IteratorChoiceGuide.wordFrequency(List.of("a", "b", "c"));

            assertThat(freq).allSatisfy((key, value) -> assertThat(value).isEqualTo(1));
        }
    }

    @Nested
    @DisplayName("Performance Comparison")
    class PerformanceTests {

        @Test
        @DisplayName("CopyOnWriteArrayList write should be slower than ArrayList for bulk adds")
        void testCopyOnWriteSlowerThanArrayList() {
            int count = 1000;

            long cowTime = IteratorChoiceGuide.measureCopyOnWriteAddTime(count);
            long alTime = IteratorChoiceGuide.measureArrayListAddTime(count);

            // COW should be significantly slower due to array copy on every add
            assertThat(cowTime).isGreaterThan(alTime);
        }

        @Test
        @DisplayName("Measurement methods should return positive times")
        void testMeasurementReturnsPositive() {
            assertThat(IteratorChoiceGuide.measureCopyOnWriteAddTime(10)).isPositive();
            assertThat(IteratorChoiceGuide.measureArrayListAddTime(10)).isPositive();
        }
    }

    @Nested
    @DisplayName("Stream-based Safe Filtering")
    class StreamFilterTests {

        @Test
        @DisplayName("Should safely filter using streams")
        void testSafeFilterWithStream() {
            Collection<String> source = List.of("alpha", "beta", "gamma", "delta");

            List<String> result = IteratorChoiceGuide.safeFilterWithStream(source, s -> s.length() > 4);

            assertThat(result).containsExactly("alpha", "gamma", "delta");
        }

        @Test
        @DisplayName("Should work with sets as source")
        void testSafeFilterWithSet() {
            Collection<Integer> source = new TreeSet<>(List.of(1, 2, 3, 4, 5));

            List<Integer> result = IteratorChoiceGuide.safeFilterWithStream(source, n -> n > 3);

            assertThat(result).containsExactly(4, 5);
        }
    }

    @Nested
    @DisplayName("Unmodifiable Views")
    class UnmodifiableViewTests {

        @Test
        @DisplayName("Should create unmodifiable view that throws on modification")
        void testUnmodifiableView() {
            List<String> source = new ArrayList<>(List.of("a", "b", "c"));
            List<String> view = IteratorChoiceGuide.createUnmodifiableView(source);

            assertThatThrownBy(() -> view.add("d"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Unmodifiable view should reflect changes to source")
        void testUnmodifiableViewReflectsSourceChanges() {
            List<String> source = new ArrayList<>(List.of("a", "b"));
            List<String> view = IteratorChoiceGuide.createUnmodifiableView(source);

            source.add("c");

            assertThat(view).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Immutable snapshot should NOT reflect changes to source")
        void testImmutableSnapshotDoesNotReflectChanges() {
            List<String> source = new ArrayList<>(List.of("a", "b"));
            List<String> snapshot = IteratorChoiceGuide.createImmutableSnapshot(source);

            source.add("c");

            assertThat(snapshot).containsExactly("a", "b");
        }

        @Test
        @DisplayName("Immutable snapshot should throw on modification")
        void testImmutableSnapshotThrowsOnModification() {
            List<String> snapshot = IteratorChoiceGuide.createImmutableSnapshot(List.of("a", "b"));

            assertThatThrownBy(() -> snapshot.add("c"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Single-Threaded Map Filtering")
    class SingleThreadedMapTests {

        @Test
        @DisplayName("Should filter map entries by predicate")
        void testSafeSingleThreadedFilter() {
            Map<String, Integer> source = Map.of("a", 1, "b", 5, "c", 10);

            Map<String, Integer> result = IteratorChoiceGuide.safeSingleThreadedFilter(
                    source, entry -> entry.getValue() > 3);

            assertThat(result)
                    .containsEntry("b", 5)
                    .containsEntry("c", 10)
                    .doesNotContainKey("a");
        }

        @Test
        @DisplayName("Should handle empty map")
        void testFilterEmptyMap() {
            Map<String, Integer> result = IteratorChoiceGuide.safeSingleThreadedFilter(
                    Map.of(), entry -> true);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Atomic Update on ConcurrentHashMap")
    class AtomicUpdateTests {

        @Test
        @DisplayName("Should atomically increment existing key")
        void testAtomicUpdateExisting() {
            ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
            map.put("counter", 10);

            IteratorChoiceGuide.atomicUpdate(map, "counter", 5);

            assertThat(map.get("counter")).isEqualTo(15);
        }

        @Test
        @DisplayName("Should atomically create new key")
        void testAtomicUpdateNew() {
            ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

            IteratorChoiceGuide.atomicUpdate(map, "counter", 5);

            assertThat(map.get("counter")).isEqualTo(5);
        }

        @Test
        @DisplayName("Should handle multiple atomic updates")
        void testMultipleAtomicUpdates() {
            ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

            IteratorChoiceGuide.atomicUpdate(map, "a", 1);
            IteratorChoiceGuide.atomicUpdate(map, "a", 2);
            IteratorChoiceGuide.atomicUpdate(map, "a", 3);

            assertThat(map.get("a")).isEqualTo(6);
        }
    }
}
