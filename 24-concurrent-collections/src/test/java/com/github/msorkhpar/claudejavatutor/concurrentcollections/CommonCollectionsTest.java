package com.github.msorkhpar.claudejavatutor.concurrentcollections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Common Collections Tests")
class CommonCollectionsTest {

    @Nested
    @DisplayName("ArrayList Examples")
    class ArrayListExamplesTest {

        private final CommonCollections.ArrayListExamples examples = new CommonCollections.ArrayListExamples();

        @Test
        @DisplayName("Should create ArrayList with elements and support random access")
        void testCreateAndAccess() {
            List<String> list = examples.createAndAccess("a", "b", "c");

            assertThat(list).hasSize(3);
            assertThat(list.get(0)).isEqualTo("a");
            assertThat(list.get(1)).isEqualTo("b");
            assertThat(list.get(2)).isEqualTo("c");
        }

        @Test
        @DisplayName("Should create empty ArrayList when no arguments given")
        void testCreateEmpty() {
            List<String> list = examples.createAndAccess();

            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("Should preserve insertion order")
        void testPreserveInsertionOrder() {
            List<Integer> list = examples.preserveInsertionOrder(5, 3, 1, 4, 2);

            assertThat(list).containsExactly(5, 3, 1, 4, 2);
        }

        @Test
        @DisplayName("Should insert element at specific index")
        void testInsertAtIndex() {
            List<String> original = Arrays.asList("a", "c", "d");
            List<String> result = examples.insertAtIndex(original, 1, "b");

            assertThat(result).containsExactly("a", "b", "c", "d");
        }

        @Test
        @DisplayName("Should insert at beginning")
        void testInsertAtBeginning() {
            List<String> original = Arrays.asList("b", "c");
            List<String> result = examples.insertAtIndex(original, 0, "a");

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should insert at end")
        void testInsertAtEnd() {
            List<String> original = Arrays.asList("a", "b");
            List<String> result = examples.insertAtIndex(original, 2, "c");

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should throw IndexOutOfBoundsException for invalid index")
        void testInsertAtInvalidIndex() {
            List<String> original = Arrays.asList("a", "b");

            assertThatThrownBy(() -> examples.insertAtIndex(original, 5, "x"))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("Should allow null elements in ArrayList")
        void testAddNullElements() {
            List<String> list = examples.addNullElements();

            assertThat(list).hasSize(4);
            assertThat(list).containsExactly("first", null, "third", null);
            assertThat(list.get(1)).isNull();
        }
    }

    @Nested
    @DisplayName("LinkedList Examples")
    class LinkedListExamplesTest {

        private final CommonCollections.LinkedListExamples examples = new CommonCollections.LinkedListExamples();

        @Test
        @DisplayName("Should create a Deque from elements")
        void testUseAsDeque() {
            Deque<String> deque = examples.useAsDeque("a", "b", "c");

            assertThat(deque).hasSize(3);
            assertThat(deque.getFirst()).isEqualTo("a");
            assertThat(deque.getLast()).isEqualTo("c");
        }

        @Test
        @DisplayName("Should get head and tail elements")
        void testHeadAndTailOperations() {
            String[] result = examples.headAndTailOperations(Arrays.asList("first", "middle", "last"));

            assertThat(result).containsExactly("first", "last");
        }

        @Test
        @DisplayName("Should return empty array for empty list")
        void testHeadAndTailEmpty() {
            String[] result = examples.headAndTailOperations(Collections.emptyList());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should process elements as FIFO queue")
        void testProcessAsQueue() {
            List<String> result = examples.processAsQueue("first", "second", "third");

            assertThat(result).containsExactly("first", "second", "third");
        }

        @Test
        @DisplayName("Should handle single element in queue")
        void testProcessSingleElementQueue() {
            List<String> result = examples.processAsQueue("only");

            assertThat(result).containsExactly("only");
        }
    }

    @Nested
    @DisplayName("CopyOnWriteArrayList Examples")
    class CopyOnWriteArrayListExamplesTest {

        private final CommonCollections.CopyOnWriteArrayListExamples examples =
                new CommonCollections.CopyOnWriteArrayListExamples();

        @Test
        @DisplayName("Should create CopyOnWriteArrayList with initial elements")
        void testCreateAndModify() {
            CopyOnWriteArrayList<String> list = examples.createAndModify("a", "b", "c");

            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should iterate over snapshot during modification")
        void testIterateWhileModifying() {
            CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>(
                    Arrays.asList("a", "b", "c")
            );

            List<String> snapshot = examples.iterateWhileModifying(list, "NEW");

            // Snapshot sees original elements (before "NEW" was added)
            assertThat(snapshot).containsExactly("a", "b", "c");
            // But the list now has the new element
            assertThat(list).contains("NEW");
        }

        @Test
        @DisplayName("Should add element if absent - returns true for new element")
        void testAddIfAbsentNew() {
            CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>(
                    Arrays.asList("a", "b")
            );

            boolean added = examples.addIfAbsent(list, "c");

            assertThat(added).isTrue();
            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should not add element if already present - returns false")
        void testAddIfAbsentExisting() {
            CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>(
                    Arrays.asList("a", "b")
            );

            boolean added = examples.addIfAbsent(list, "a");

            assertThat(added).isFalse();
            assertThat(list).containsExactly("a", "b");
        }
    }

    @Nested
    @DisplayName("HashSet Examples")
    class HashSetExamplesTest {

        private final CommonCollections.HashSetExamples examples = new CommonCollections.HashSetExamples();

        @Test
        @DisplayName("Should reject duplicate elements")
        void testAddWithDuplicates() {
            Set<String> set = examples.addWithDuplicates("a", "b", "a", "c", "b");

            assertThat(set).hasSize(3);
            assertThat(set).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("Should allow null in HashSet")
        void testAddNullToHashSet() {
            Set<String> set = examples.addNullToHashSet();

            assertThat(set).hasSize(3);
            assertThat(set).contains("one", "three");
            assertThat(set).containsNull();
        }

        @Test
        @DisplayName("HashSet does not guarantee order")
        void testOrderNotGuaranteed() {
            // Just verify the method runs without error
            assertThat(examples.orderNotGuaranteed()).isTrue();
        }

        @Test
        @DisplayName("Should handle empty set")
        void testEmptyHashSet() {
            Set<String> set = examples.addWithDuplicates();

            assertThat(set).isEmpty();
        }
    }

    @Nested
    @DisplayName("ConcurrentSkipListSet Examples")
    class ConcurrentSkipListSetExamplesTest {

        private final CommonCollections.ConcurrentSkipListSetExamples examples =
                new CommonCollections.ConcurrentSkipListSetExamples();

        @Test
        @DisplayName("Should create a sorted set")
        void testCreateSortedSet() {
            ConcurrentSkipListSet<String> set = examples.createSortedSet("c", "a", "b");

            assertThat(set).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should return elements in range")
        void testGetRange() {
            ConcurrentSkipListSet<String> set = examples.createSortedSet("a", "b", "c", "d", "e");

            NavigableSet<String> range = examples.getRange(set, "b", "d");

            assertThat(range).containsExactly("b", "c", "d");
        }

        @Test
        @DisplayName("Should throw NullPointerException when adding null")
        void testAddNullThrowsException() {
            assertThatThrownBy(examples::addNullThrowsException)
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should get first and last elements in sorted order")
        void testFirstAndLast() {
            ConcurrentSkipListSet<String> set = examples.createSortedSet("z", "a", "m");

            String[] result = examples.firstAndLast(set);

            assertThat(result).containsExactly("a", "z");
        }

        @Test
        @DisplayName("Should return empty array for empty set")
        void testFirstAndLastEmpty() {
            ConcurrentSkipListSet<String> set = new ConcurrentSkipListSet<>();

            String[] result = examples.firstAndLast(set);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should reject duplicate elements")
        void testRejectDuplicates() {
            ConcurrentSkipListSet<String> set = examples.createSortedSet("a", "b", "a", "c");

            assertThat(set).hasSize(3);
            assertThat(set).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("HashMap Examples")
    class HashMapExamplesTest {

        private final CommonCollections.HashMapExamples examples = new CommonCollections.HashMapExamples();

        @Test
        @DisplayName("Should count word occurrences")
        void testCreateWordCountMap() {
            Map<String, Integer> map = examples.createWordCountMap("hello", "world", "hello", "java", "hello");

            assertThat(map).hasSize(3);
            assertThat(map.get("hello")).isEqualTo(3);
            assertThat(map.get("world")).isEqualTo(1);
            assertThat(map.get("java")).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle empty input for word count")
        void testCreateWordCountMapEmpty() {
            Map<String, Integer> map = examples.createWordCountMap();

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("Should group by first letter")
        void testGroupByFirstLetter() {
            Map<String, Integer> input = new LinkedHashMap<>();
            input.put("apple", 1);
            input.put("avocado", 2);
            input.put("banana", 3);

            Map<String, List<Integer>> grouped = examples.groupByFirstLetter(input);

            assertThat(grouped.get("A")).containsExactlyInAnyOrder(1, 2);
            assertThat(grouped.get("B")).containsExactly(3);
        }

        @Test
        @DisplayName("Should support null key and null value")
        void testNullKeyAndValue() {
            Map<String, String> map = examples.nullKeyAndValue();

            assertThat(map.get(null)).isEqualTo("null-key-value");
            assertThat(map.get("key")).isNull();
            assertThat(map).hasSize(2);
        }
    }

    @Nested
    @DisplayName("ConcurrentHashMap Examples")
    class ConcurrentHashMapExamplesTest {

        private final CommonCollections.ConcurrentHashMapExamples examples =
                new CommonCollections.ConcurrentHashMapExamples();

        @Test
        @DisplayName("Should create ConcurrentHashMap from initial map")
        void testCreateConcurrentMap() {
            Map<String, Integer> initial = Map.of("a", 1, "b", 2);
            var map = examples.createConcurrentMap(initial);

            assertThat(map).hasSize(2);
            assertThat(map.get("a")).isEqualTo(1);
        }

        @Test
        @DisplayName("Should count words atomically")
        void testAtomicWordCount() {
            var map = examples.atomicWordCount("hello", "world", "hello", "java");

            assertThat(map.get("hello")).isEqualTo(2);
            assertThat(map.get("world")).isEqualTo(1);
            assertThat(map.get("java")).isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw NullPointerException for null key")
        void testPutNullKeyThrows() {
            assertThatThrownBy(examples::putNullKeyThrows)
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw NullPointerException for null value")
        void testPutNullValueThrows() {
            assertThatThrownBy(examples::putNullValueThrows)
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should reduce values in parallel")
        void testParallelReduce() {
            var map = new java.util.concurrent.ConcurrentHashMap<String, Integer>();
            map.put("a", 10);
            map.put("b", 20);
            map.put("c", 30);

            long sum = examples.parallelReduce(map);

            assertThat(sum).isEqualTo(60);
        }

        @Test
        @DisplayName("Should search for value and return matching key")
        void testSearchForValue() {
            var map = new java.util.concurrent.ConcurrentHashMap<String, Integer>();
            map.put("apple", 5);
            map.put("banana", 10);
            map.put("cherry", 15);

            String found = examples.searchForValue(map, 10);

            assertThat(found).isEqualTo("banana");
        }

        @Test
        @DisplayName("Should return null when search finds no match")
        void testSearchNoMatch() {
            var map = new java.util.concurrent.ConcurrentHashMap<String, Integer>();
            map.put("apple", 5);

            String found = examples.searchForValue(map, 99);

            assertThat(found).isNull();
        }
    }
}
