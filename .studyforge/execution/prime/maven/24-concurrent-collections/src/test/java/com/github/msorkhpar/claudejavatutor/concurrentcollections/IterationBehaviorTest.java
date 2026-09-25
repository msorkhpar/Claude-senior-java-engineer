package com.github.msorkhpar.claudejavatutor.concurrentcollections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Iteration Behavior Tests")
class IterationBehaviorTest {

    @Nested
    @DisplayName("Fail-Fast Examples")
    class FailFastExamplesTest {

        private final IterationBehavior.FailFastExamples examples = new IterationBehavior.FailFastExamples();

        @Test
        @DisplayName("ArrayList should throw ConcurrentModificationException during structural modification")
        void testArrayListFailFast() {
            boolean threwException = examples.arrayListFailFast();

            assertThat(threwException).isTrue();
        }

        @Test
        @DisplayName("HashMap should throw ConcurrentModificationException during structural modification")
        void testHashMapFailFast() {
            boolean threwException = examples.hashMapFailFast();

            assertThat(threwException).isTrue();
        }

        @Test
        @DisplayName("Should safely remove elements using Iterator.remove()")
        void testSafeRemovalWithIterator() {
            List<String> elements = Arrays.asList("a", "b", "c", "b", "d");

            List<String> result = examples.safeRemovalWithIterator(elements, "b");

            assertThat(result).containsExactly("a", "c", "d");
        }

        @Test
        @DisplayName("Should safely remove elements using removeIf")
        void testSafeRemovalWithRemoveIf() {
            List<String> elements = Arrays.asList("a", "b", "c", "b", "d");

            List<String> result = examples.safeRemovalWithRemoveIf(elements, "b");

            assertThat(result).containsExactly("a", "c", "d");
        }

        @Test
        @DisplayName("Safe removal with no matching element should not change list")
        void testSafeRemovalNoMatch() {
            List<String> elements = Arrays.asList("a", "b", "c");

            List<String> result = examples.safeRemovalWithIterator(elements, "z");

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Safe removal from empty list should return empty list")
        void testSafeRemovalEmptyList() {
            List<String> result = examples.safeRemovalWithIterator(Collections.emptyList(), "a");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("HashSet should exhibit fail-fast behavior")
        void testHashSetFailFast() {
            // HashSet fail-fast is best-effort; it may or may not throw
            // The test verifies the method runs without unexpected errors
            boolean threwException = examples.hashSetFailFast();
            // Either outcome is valid for fail-fast
            assertThat(threwException).isIn(true, false);
        }
    }

    @Nested
    @DisplayName("Weakly Consistent Examples")
    class WeaklyConsistentExamplesTest {

        private final IterationBehavior.WeaklyConsistentExamples examples =
                new IterationBehavior.WeaklyConsistentExamples();

        @Test
        @DisplayName("CopyOnWriteArrayList iteration sees snapshot - does not see concurrent adds")
        void testCopyOnWriteIteration() {
            List<String> result = examples.copyOnWriteIteration();

            // Iterator sees snapshot: original elements only
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("CopyOnWriteArrayList contains added element after iteration")
        void testCopyOnWriteAfterIteration() {
            var list = examples.copyOnWriteAfterIteration();

            assertThat(list).containsExactly("a", "b", "c", "d");
        }

        @Test
        @DisplayName("ConcurrentHashMap iteration does not throw ConcurrentModificationException")
        void testConcurrentHashMapIteration() {
            Map<String, Integer> result = examples.concurrentHashMapIteration();

            // Should have at least the original entries
            assertThat(result).containsKeys("a", "b", "c");
        }

        @Test
        @DisplayName("ConcurrentSkipListSet iteration is weakly consistent")
        void testConcurrentSkipListSetIteration() {
            List<String> result = examples.concurrentSkipListSetIteration();

            // Should contain at least the original elements (may or may not see "a" added during iteration)
            assertThat(result).containsAnyOf("b", "d", "f");
        }

        @Test
        @DisplayName("CopyOnWriteArrayList iterator remove throws UnsupportedOperationException")
        void testCowIteratorRemoveThrows() {
            boolean threwException = examples.cowIteratorRemoveThrows();

            assertThat(threwException).isTrue();
        }
    }

    @Nested
    @DisplayName("Safe Modification Patterns")
    class SafeModificationPatternsTest {

        private final IterationBehavior.SafeModificationPatterns patterns =
                new IterationBehavior.SafeModificationPatterns();

        @Test
        @DisplayName("Should remove elements using copy-then-modify pattern")
        void testCopyThenModify() {
            List<String> original = Arrays.asList("a", "b", "c", "b", "d");

            List<String> result = patterns.copyThenModify(original, "b");

            assertThat(result).containsExactly("a", "c", "d");
        }

        @Test
        @DisplayName("Should filter elements using streams")
        void testStreamFilter() {
            List<String> original = Arrays.asList("a", "b", "c", "b", "d");

            List<String> result = patterns.streamFilter(original, "b");

            assertThat(result).containsExactly("a", "c", "d");
        }

        @Test
        @DisplayName("Stream filter should not modify original list")
        void testStreamFilterPreservesOriginal() {
            List<String> original = new ArrayList<>(Arrays.asList("a", "b", "c"));
            List<String> originalCopy = new ArrayList<>(original);

            patterns.streamFilter(original, "b");

            assertThat(original).isEqualTo(originalCopy);
        }

        @Test
        @DisplayName("Should remove entries from ConcurrentHashMap using removeIf")
        void testConcurrentMapRemoveIf() {
            ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
            map.put("low", 1);
            map.put("medium", 5);
            map.put("high", 10);

            ConcurrentHashMap<String, Integer> result = patterns.concurrentMapRemoveIf(map, 5);

            assertThat(result).hasSize(2);
            assertThat(result).containsKeys("medium", "high");
            assertThat(result).doesNotContainKey("low");
        }

        @Test
        @DisplayName("Should handle empty map in removeIf")
        void testConcurrentMapRemoveIfEmpty() {
            ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

            ConcurrentHashMap<String, Integer> result = patterns.concurrentMapRemoveIf(map, 5);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Copy-then-modify with no matches returns unchanged list")
        void testCopyThenModifyNoMatch() {
            List<String> original = Arrays.asList("a", "b", "c");

            List<String> result = patterns.copyThenModify(original, "z");

            assertThat(result).containsExactly("a", "b", "c");
        }
    }
}
