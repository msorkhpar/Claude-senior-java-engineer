package com.github.msorkhpar.claudejavatutor.failfastsafe;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Fail-Fast Iterator Demo Tests")
class FailFastIteratorDemoTest {

    @Nested
    @DisplayName("ConcurrentModificationException Scenarios")
    class ConcurrentModificationTests {

        @Test
        @DisplayName("Should throw ConcurrentModificationException when removing from ArrayList during for-each")
        void testModifyListDuringForEachThrows() {
            List<String> list = new ArrayList<>(List.of("a", "b", "c"));

            assertThatThrownBy(() -> FailFastIteratorDemo.modifyListDuringForEach(list, "a"))
                    .isInstanceOf(ConcurrentModificationException.class);
        }

        @Test
        @DisplayName("Should throw ConcurrentModificationException when removing from HashMap during for-each")
        void testModifyMapDuringForEachThrows() {
            Map<String, Integer> map = new HashMap<>(Map.of("a", 1, "b", 2, "c", 3));

            assertThatThrownBy(() -> FailFastIteratorDemo.modifyMapDuringForEach(map, "a"))
                    .isInstanceOf(ConcurrentModificationException.class);
        }

        @Test
        @DisplayName("Should throw ConcurrentModificationException when adding to HashSet during iteration")
        void testModifySetDuringIterationThrows() {
            Set<Integer> set = new HashSet<>(Set.of(1, 2, 3, 10, 20));

            assertThatThrownBy(() -> FailFastIteratorDemo.modifySetDuringIteration(set, 5))
                    .isInstanceOf(ConcurrentModificationException.class);
        }

        @Test
        @DisplayName("Should throw ConcurrentModificationException when adding via list while using ListIterator backed by list")
        void testModifyListWhileListIterating() {
            List<String> list = new ArrayList<>(List.of("a", "b"));

            assertThatThrownBy(() -> FailFastIteratorDemo.modifyListWhileListIterating(list))
                    .isInstanceOf(ConcurrentModificationException.class);
        }

        @Test
        @DisplayName("Should not throw when element to remove is not found in the list")
        void testNoModificationNoException() {
            List<String> list = new ArrayList<>(List.of("a", "b", "c"));

            assertThatCode(() -> FailFastIteratorDemo.modifyListDuringForEach(list, "z"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Safe Removal Patterns")
    class SafeRemovalTests {

        @Test
        @DisplayName("Should safely remove using Iterator.remove()")
        void testSafeRemoveWithIterator() {
            List<String> list = List.of("a", "b", "c", "b", "d");

            List<String> result = FailFastIteratorDemo.safeRemoveWithIterator(list, "b");

            assertThat(result).containsExactly("a", "c", "d");
        }

        @Test
        @DisplayName("Should safely remove using removeIf")
        void testSafeRemoveWithRemoveIf() {
            List<String> list = List.of("a", "b", "c", "b", "d");

            List<String> result = FailFastIteratorDemo.safeRemoveWithRemoveIf(list, "b");

            assertThat(result).containsExactly("a", "c", "d");
        }

        @Test
        @DisplayName("Should safely remove using separate collection")
        void testSafeRemoveWithSeparateCollection() {
            List<String> list = List.of("a", "b", "c", "b", "d");

            List<String> result = FailFastIteratorDemo.safeRemoveWithSeparateCollection(list, "b");

            assertThat(result).containsExactly("a", "c", "d");
        }

        @Test
        @DisplayName("All safe removal methods should produce the same result")
        void testAllSafeRemovalMethodsConsistent() {
            List<String> list = List.of("x", "y", "z", "y");

            List<String> result1 = FailFastIteratorDemo.safeRemoveWithIterator(list, "y");
            List<String> result2 = FailFastIteratorDemo.safeRemoveWithRemoveIf(list, "y");
            List<String> result3 = FailFastIteratorDemo.safeRemoveWithSeparateCollection(list, "y");

            assertThat(result1).isEqualTo(result2).isEqualTo(result3);
        }

        @Test
        @DisplayName("Should handle removing non-existing element")
        void testRemoveNonExisting() {
            List<String> list = List.of("a", "b", "c");

            List<String> result = FailFastIteratorDemo.safeRemoveWithIterator(list, "z");

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should handle empty list")
        void testRemoveFromEmptyList() {
            List<String> list = List.of();

            List<String> result = FailFastIteratorDemo.safeRemoveWithIterator(list, "a");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle removing all elements")
        void testRemoveAllElements() {
            List<String> list = List.of("a", "a", "a");

            List<String> result = FailFastIteratorDemo.safeRemoveWithRemoveIf(list, "a");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle single element list")
        void testRemoveFromSingleElementList() {
            List<String> list = List.of("only");

            List<String> result = FailFastIteratorDemo.safeRemoveWithIterator(list, "only");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Safe Addition with ListIterator")
    class SafeAdditionTests {

        @Test
        @DisplayName("Should safely add elements using ListIterator.add()")
        void testSafeAddWithListIterator() {
            List<String> original = List.of("a", "b", "c");

            List<String> result = FailFastIteratorDemo.safeAddWithListIterator(original, "_copy");

            assertThat(result).containsExactly("a", "a_copy", "b", "b_copy", "c", "c_copy");
        }

        @Test
        @DisplayName("Should handle empty list for safe add")
        void testSafeAddWithEmptyList() {
            List<String> original = List.of();

            List<String> result = FailFastIteratorDemo.safeAddWithListIterator(original, "_copy");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should not modify original list during safe add")
        void testOriginalNotModified() {
            List<String> original = new ArrayList<>(List.of("a", "b"));
            List<String> snapshot = new ArrayList<>(original);

            FailFastIteratorDemo.safeAddWithListIterator(original, "_x");

            assertThat(original).isEqualTo(snapshot);
        }
    }

    @Nested
    @DisplayName("Read Operations During Iteration")
    class ReadDuringIterationTests {

        @Test
        @DisplayName("Should safely read collection size during iteration")
        void testReadDuringIteration() {
            List<String> list = List.of("hello", "world");

            List<String> result = FailFastIteratorDemo.readDuringIteration(list);

            assertThat(result).containsExactly("hello-2", "world-2");
        }

        @Test
        @DisplayName("Should handle empty list for read during iteration")
        void testReadDuringIterationEmptyList() {
            List<String> list = List.of();

            List<String> result = FailFastIteratorDemo.readDuringIteration(list);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Fail-Fast Behavior with Different Collection Types")
    class CollectionTypeTests {

        @Test
        @DisplayName("LinkedList should also exhibit fail-fast behavior")
        void testLinkedListFailFast() {
            List<String> linkedList = new LinkedList<>(List.of("a", "b", "c"));

            assertThatThrownBy(() -> FailFastIteratorDemo.modifyListDuringForEach(linkedList, "a"))
                    .isInstanceOf(ConcurrentModificationException.class);
        }

        @Test
        @DisplayName("TreeMap should exhibit fail-fast behavior")
        void testTreeMapFailFast() {
            Map<String, Integer> treeMap = new TreeMap<>(Map.of("a", 1, "b", 2, "c", 3));

            assertThatThrownBy(() -> FailFastIteratorDemo.modifyMapDuringForEach(treeMap, "a"))
                    .isInstanceOf(ConcurrentModificationException.class);
        }

        @Test
        @DisplayName("TreeSet should exhibit fail-fast behavior")
        void testTreeSetFailFast() {
            // TreeSet iterates in sorted order: 1, 2, 3, 10, 20
            // threshold=0 ensures the first element triggers the add
            Set<Integer> treeSet = new TreeSet<>(List.of(1, 2, 3, 10, 20));

            assertThatThrownBy(() -> FailFastIteratorDemo.modifySetDuringIteration(treeSet, 0))
                    .isInstanceOf(ConcurrentModificationException.class);
        }

        @Test
        @DisplayName("LinkedHashMap should exhibit fail-fast behavior")
        void testLinkedHashMapFailFast() {
            Map<String, Integer> map = new LinkedHashMap<>();
            map.put("a", 1);
            map.put("b", 2);

            assertThatThrownBy(() -> FailFastIteratorDemo.modifyMapDuringForEach(map, "a"))
                    .isInstanceOf(ConcurrentModificationException.class);
        }
    }
}
