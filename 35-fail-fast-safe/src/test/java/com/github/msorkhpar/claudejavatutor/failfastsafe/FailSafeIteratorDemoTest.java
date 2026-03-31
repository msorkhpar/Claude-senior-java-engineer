package com.github.msorkhpar.claudejavatutor.failfastsafe;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Fail-Safe Iterator Demo Tests")
class FailSafeIteratorDemoTest {

    @Nested
    @DisplayName("CopyOnWriteArrayList Behavior")
    class CopyOnWriteArrayListTests {

        @Test
        @DisplayName("Should iterate over snapshot and not see modifications during iteration")
        void testIterateAndModifyCopyOnWriteList() {
            List<String> initial = List.of("a", "b", "c");

            List<String> iterated = FailSafeIteratorDemo.iterateAndModifyCopyOnWriteList(initial);

            // Iterator sees only the original snapshot
            assertThat(iterated).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should throw UnsupportedOperationException when calling remove on COW iterator")
        void testRemoveOnCopyOnWriteIteratorThrows() {
            assertThatThrownBy(FailSafeIteratorDemo::attemptRemoveOnCopyOnWriteIterator)
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("CopyOnWriteArrayList should contain added element after iteration completes")
        void testCopyOnWriteListContainsAddedElement() {
            CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>(List.of("a", "b"));

            // Iterate and add during iteration
            for (String item : cowList) {
                if (item.equals("a")) {
                    cowList.add("c");
                }
            }

            // After iteration, the added element is in the list
            assertThat(cowList).contains("c");
        }

        @Test
        @DisplayName("Should handle empty CopyOnWriteArrayList")
        void testEmptyCopyOnWriteList() {
            List<String> iterated = FailSafeIteratorDemo.iterateAndModifyCopyOnWriteList(List.of());

            assertThat(iterated).isEmpty();
        }

        @Test
        @DisplayName("Should handle single element CopyOnWriteArrayList")
        void testSingleElementCopyOnWriteList() {
            List<String> iterated = FailSafeIteratorDemo.iterateAndModifyCopyOnWriteList(List.of("only"));

            assertThat(iterated).containsExactly("only");
        }
    }

    @Nested
    @DisplayName("CopyOnWriteArraySet Behavior")
    class CopyOnWriteArraySetTests {

        @Test
        @DisplayName("Should iterate snapshot without seeing concurrent modifications")
        void testIterateAndModifyCopyOnWriteSet() {
            Set<String> initial = new LinkedHashSet<>(List.of("x", "y", "z"));

            Set<String> iterated = FailSafeIteratorDemo.iterateAndModifyCopyOnWriteSet(initial);

            // Should only see original elements (snapshot semantics)
            assertThat(iterated).containsExactlyInAnyOrder("x", "y", "z");
        }

        @Test
        @DisplayName("CopyOnWriteArraySet should contain new elements after iteration")
        void testCopyOnWriteSetContainsNewElements() {
            CopyOnWriteArraySet<String> cowSet = new CopyOnWriteArraySet<>(List.of("a", "b"));

            for (String item : cowSet) {
                cowSet.add("NEW_" + item);
            }

            assertThat(cowSet).contains("NEW_a", "NEW_b");
        }
    }

    @Nested
    @DisplayName("ConcurrentHashMap Behavior")
    class ConcurrentHashMapTests {

        @Test
        @DisplayName("Should not throw exception when modifying ConcurrentHashMap during iteration")
        void testIterateAndModifyConcurrentMap() {
            Map<String, String> initial = Map.of("k1", "v1", "k2", "v2");

            assertThatCode(() -> FailSafeIteratorDemo.iterateAndModifyConcurrentMap(initial))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should iterate at least original entries from ConcurrentHashMap")
        void testConcurrentMapIteratesOriginalEntries() {
            Map<String, String> initial = Map.of("k1", "v1", "k2", "v2");

            Map<String, String> iterated = FailSafeIteratorDemo.iterateAndModifyConcurrentMap(initial);

            assertThat(iterated).containsKeys("k1", "k2");
        }

        @Test
        @DisplayName("Should safely remove entries below threshold from ConcurrentHashMap")
        void testSafeRemoveFromConcurrentMap() {
            Map<String, Integer> initial = Map.of("a", 1, "b", 5, "c", 10, "d", 3);

            Map<String, Integer> result = FailSafeIteratorDemo.safeRemoveFromConcurrentMap(initial, 5);

            assertThat(result).doesNotContainKeys("a", "d");
            assertThat(result).containsKeys("b", "c");
        }

        @Test
        @DisplayName("Should handle empty ConcurrentHashMap")
        void testEmptyConcurrentMap() {
            Map<String, Integer> result = FailSafeIteratorDemo.safeRemoveFromConcurrentMap(Map.of(), 5);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle all entries below threshold in ConcurrentHashMap")
        void testAllBelowThreshold() {
            Map<String, Integer> initial = Map.of("a", 1, "b", 2);

            Map<String, Integer> result = FailSafeIteratorDemo.safeRemoveFromConcurrentMap(initial, 10);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle no entries below threshold in ConcurrentHashMap")
        void testNoneBelowThreshold() {
            Map<String, Integer> initial = Map.of("a", 10, "b", 20);

            Map<String, Integer> result = FailSafeIteratorDemo.safeRemoveFromConcurrentMap(initial, 5);

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("ConcurrentSkipListSet Behavior")
    class ConcurrentSkipListSetTests {

        @Test
        @DisplayName("Should not throw exception when modifying ConcurrentSkipListSet during iteration")
        void testIterateAndModifyConcurrentSkipListSet() {
            assertThatCode(() ->
                    FailSafeIteratorDemo.iterateAndModifyConcurrentSkipListSet(List.of(1, 2, 3))
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should iterate original elements from ConcurrentSkipListSet")
        void testSkipListSetOriginalElements() {
            List<Integer> iterated = FailSafeIteratorDemo.iterateAndModifyConcurrentSkipListSet(List.of(1, 2, 3));

            // Should contain at minimum the original elements (may also contain some added ones)
            assertThat(iterated).contains(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("ConcurrentLinkedQueue Behavior")
    class ConcurrentLinkedQueueTests {

        @Test
        @DisplayName("Should not throw exception when modifying ConcurrentLinkedQueue during iteration")
        void testIterateAndModifyConcurrentQueue() {
            assertThatCode(() ->
                    FailSafeIteratorDemo.iterateAndModifyConcurrentQueue(List.of("a", "b", "c"))
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should iterate at least original elements from ConcurrentLinkedQueue")
        void testQueueOriginalElements() {
            List<String> iterated = FailSafeIteratorDemo.iterateAndModifyConcurrentQueue(List.of("a", "b", "c"));

            assertThat(iterated).contains("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("SynchronizedList vs ConcurrentCollections")
    class SynchronizedListTests {

        @Test
        @DisplayName("Collections.synchronizedList should still throw ConcurrentModificationException")
        void testSynchronizedListStillFailFast() {
            List<String> elements = List.of("a", "b", "c");

            assertThatThrownBy(() ->
                    FailSafeIteratorDemo.modifySynchronizedListDuringIteration(elements)
            ).isInstanceOf(ConcurrentModificationException.class);
        }
    }

    @Nested
    @DisplayName("Null and Edge Case Handling")
    class NullAndEdgeCaseTests {

        @Test
        @DisplayName("ConcurrentHashMap should not accept null keys")
        void testConcurrentHashMapRejectsNullKey() {
            ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

            assertThatThrownBy(() -> map.put(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("ConcurrentHashMap should not accept null values")
        void testConcurrentHashMapRejectsNullValue() {
            ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

            assertThatThrownBy(() -> map.put("key", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("CopyOnWriteArrayList should accept null elements")
        void testCopyOnWriteAcceptsNull() {
            CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
            list.add(null);
            list.add("a");

            assertThat(list).containsExactly(null, "a");
        }
    }
}
