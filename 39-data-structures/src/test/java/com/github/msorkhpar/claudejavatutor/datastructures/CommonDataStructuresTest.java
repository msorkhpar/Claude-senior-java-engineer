package com.github.msorkhpar.claudejavatutor.datastructures;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Common Data Structures Tests")
class CommonDataStructuresTest {

    // ========== ArrayList Tests ==========

    @Nested
    @DisplayName("ArrayList Examples")
    class ArrayListExamplesTest {

        private final CommonDataStructures.ArrayListExamples examples = new CommonDataStructures.ArrayListExamples();

        @Test
        @DisplayName("Should create list with initial capacity and populate it")
        void testCreateWithCapacity() {
            List<Integer> list = examples.createWithCapacity(100, 50);
            assertThat(list).hasSize(50);
            assertThat(list.get(0)).isEqualTo(0);
            assertThat(list.get(49)).isEqualTo(49);
        }

        @Test
        @DisplayName("Should create empty list when count is zero")
        void testCreateWithCapacityZeroCount() {
            List<Integer> list = examples.createWithCapacity(10, 0);
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("Should get element at valid index")
        void testGetElementAtIndex() {
            List<Integer> list = List.of(10, 20, 30, 40, 50);
            assertThat(examples.getElementAtIndex(new ArrayList<>(list), 2)).isEqualTo(30);
        }

        @Test
        @DisplayName("Should throw on negative index")
        void testGetElementAtNegativeIndex() {
            List<Integer> list = new ArrayList<>(List.of(1, 2, 3));
            assertThatThrownBy(() -> examples.getElementAtIndex(list, -1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("Should throw on index equal to size")
        void testGetElementAtOutOfBoundsIndex() {
            List<Integer> list = new ArrayList<>(List.of(1, 2, 3));
            assertThatThrownBy(() -> examples.getElementAtIndex(list, 3))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("Should insert at beginning")
        void testInsertAtBeginning() {
            List<String> result = examples.insertAtPosition(List.of("b", "c"), 0, "a");
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should insert at end")
        void testInsertAtEnd() {
            List<String> result = examples.insertAtPosition(List.of("a", "b"), 2, "c");
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should insert in the middle")
        void testInsertAtMiddle() {
            List<String> result = examples.insertAtPosition(List.of("a", "c"), 1, "b");
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should throw on invalid insert position")
        void testInsertAtInvalidPosition() {
            assertThatThrownBy(() -> examples.insertAtPosition(List.of("a"), -1, "x"))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("Should remove first occurrence by value")
        void testRemoveByValue() {
            List<String> result = examples.removeByValue(List.of("a", "b", "a", "c"), "a");
            assertThat(result).containsExactly("b", "a", "c");
        }

        @Test
        @DisplayName("Should return unchanged list when value not found")
        void testRemoveByValueNotFound() {
            List<String> result = examples.removeByValue(List.of("a", "b"), "x");
            assertThat(result).containsExactly("a", "b");
        }

        @Test
        @DisplayName("Should get correct subList")
        void testGetSubList() {
            List<Integer> list = new ArrayList<>(List.of(0, 1, 2, 3, 4));
            List<Integer> sub = examples.getSubList(list, 1, 4);
            assertThat(sub).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Should return empty subList for equal from and to")
        void testGetSubListEmpty() {
            List<Integer> list = new ArrayList<>(List.of(0, 1, 2));
            List<Integer> sub = examples.getSubList(list, 1, 1);
            assertThat(sub).isEmpty();
        }

        @Test
        @DisplayName("Should throw on invalid subList range")
        void testGetSubListInvalidRange() {
            List<Integer> list = new ArrayList<>(List.of(0, 1, 2));
            assertThatThrownBy(() -> examples.getSubList(list, 2, 1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    // ========== LinkedList Tests ==========

    @Nested
    @DisplayName("LinkedList Examples")
    class LinkedListExamplesTest {

        private final CommonDataStructures.LinkedListExamples examples = new CommonDataStructures.LinkedListExamples();

        @Test
        @DisplayName("Should create deque from elements")
        void testCreateDeque() {
            Deque<String> deque = examples.createDeque(List.of("a", "b", "c"));
            assertThat(deque).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should add elements at both ends")
        void testAddAtBothEnds() {
            LinkedList<String> result = examples.addAtBothEnds(List.of("b", "c"), "a", "d");
            assertThat(result).containsExactly("a", "b", "c", "d");
        }

        @Test
        @DisplayName("Should add to empty list at both ends")
        void testAddAtBothEndsEmpty() {
            LinkedList<String> result = examples.addAtBothEnds(Collections.emptyList(), "first", "last");
            assertThat(result).containsExactly("first", "last");
        }

        @Test
        @DisplayName("Should peek first element without removing")
        void testPeekFirst() {
            LinkedList<String> list = new LinkedList<>(List.of("a", "b"));
            String first = examples.peekFirst(list);
            assertThat(first).isEqualTo("a");
            assertThat(list).hasSize(2); // Not removed
        }

        @Test
        @DisplayName("Should return null when peeking empty list")
        void testPeekFirstEmpty() {
            LinkedList<String> list = new LinkedList<>();
            assertThat(examples.peekFirst(list)).isNull();
        }

        @Test
        @DisplayName("Should remove from both ends")
        void testRemoveFromBothEnds() {
            LinkedList<String> list = new LinkedList<>(List.of("a", "b", "c", "d"));
            LinkedList<String> result = examples.removeFromBothEnds(list);
            assertThat(result).containsExactly("b", "c");
        }

        @Test
        @DisplayName("Should not remove from single element list")
        void testRemoveFromBothEndsSingleElement() {
            LinkedList<String> list = new LinkedList<>(List.of("a"));
            LinkedList<String> result = examples.removeFromBothEnds(list);
            assertThat(result).containsExactly("a");
        }
    }

    // ========== HashSet Tests ==========

    @Nested
    @DisplayName("HashSet Examples")
    class HashSetExamplesTest {

        private final CommonDataStructures.HashSetExamples examples = new CommonDataStructures.HashSetExamples();

        @Test
        @DisplayName("Should remove duplicates")
        void testRemoveDuplicates() {
            Set<String> result = examples.removeDuplicates(List.of("a", "b", "a", "c", "b"));
            assertThat(result).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("Should handle empty list for duplicates")
        void testRemoveDuplicatesEmpty() {
            Set<String> result = examples.removeDuplicates(Collections.emptyList());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should compute intersection of two sets")
        void testIntersection() {
            Set<Integer> set1 = Set.of(1, 2, 3, 4);
            Set<Integer> set2 = Set.of(3, 4, 5, 6);
            Set<Integer> result = examples.intersection(set1, set2);
            assertThat(result).containsExactlyInAnyOrder(3, 4);
        }

        @Test
        @DisplayName("Should return empty intersection for disjoint sets")
        void testIntersectionDisjoint() {
            Set<Integer> set1 = Set.of(1, 2);
            Set<Integer> set2 = Set.of(3, 4);
            Set<Integer> result = examples.intersection(set1, set2);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should compute union of two sets")
        void testUnion() {
            Set<Integer> set1 = Set.of(1, 2, 3);
            Set<Integer> set2 = Set.of(3, 4, 5);
            Set<Integer> result = examples.union(set1, set2);
            assertThat(result).containsExactlyInAnyOrder(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Should compute difference of two sets")
        void testDifference() {
            Set<Integer> set1 = Set.of(1, 2, 3, 4);
            Set<Integer> set2 = Set.of(3, 4, 5);
            Set<Integer> result = examples.difference(set1, set2);
            assertThat(result).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("Should count unique points using hashCode/equals from record")
        void testCountUniquePoints() {
            List<CommonDataStructures.Point> points = List.of(
                    new CommonDataStructures.Point(1, 2),
                    new CommonDataStructures.Point(3, 4),
                    new CommonDataStructures.Point(1, 2), // duplicate
                    new CommonDataStructures.Point(5, 6)
            );
            assertThat(examples.countUniquePoints(points)).isEqualTo(3);
        }

        @Test
        @DisplayName("Should count zero unique points for empty list")
        void testCountUniquePointsEmpty() {
            assertThat(examples.countUniquePoints(Collections.emptyList())).isEqualTo(0);
        }
    }

    // ========== TreeSet Tests ==========

    @Nested
    @DisplayName("TreeSet Examples")
    class TreeSetExamplesTest {

        private final CommonDataStructures.TreeSetExamples examples = new CommonDataStructures.TreeSetExamples();

        @Test
        @DisplayName("Should create sorted set from unsorted collection")
        void testCreateSorted() {
            TreeSet<Integer> result = examples.createSorted(List.of(5, 3, 1, 4, 2));
            assertThat(result).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Should remove duplicates when creating sorted set")
        void testCreateSortedWithDuplicates() {
            TreeSet<Integer> result = examples.createSorted(List.of(3, 1, 3, 2, 1));
            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Should get elements in range")
        void testGetRange() {
            TreeSet<Integer> set = new TreeSet<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
            SortedSet<Integer> range = examples.getRange(set, 3, 7);
            assertThat(range).containsExactly(3, 4, 5, 6);
        }

        @Test
        @DisplayName("Should find ceiling value")
        void testFindCeiling() {
            TreeSet<Integer> set = new TreeSet<>(List.of(10, 20, 30, 40, 50));
            assertThat(examples.findCeiling(set, 25)).isEqualTo(30);
            assertThat(examples.findCeiling(set, 30)).isEqualTo(30);
        }

        @Test
        @DisplayName("Should return null when no ceiling exists")
        void testFindCeilingNull() {
            TreeSet<Integer> set = new TreeSet<>(List.of(10, 20, 30));
            assertThat(examples.findCeiling(set, 31)).isNull();
        }

        @Test
        @DisplayName("Should find floor value")
        void testFindFloor() {
            TreeSet<Integer> set = new TreeSet<>(List.of(10, 20, 30, 40, 50));
            assertThat(examples.findFloor(set, 25)).isEqualTo(20);
            assertThat(examples.findFloor(set, 20)).isEqualTo(20);
        }

        @Test
        @DisplayName("Should return null when no floor exists")
        void testFindFloorNull() {
            TreeSet<Integer> set = new TreeSet<>(List.of(10, 20, 30));
            assertThat(examples.findFloor(set, 9)).isNull();
        }

        @Test
        @DisplayName("Should create TreeSet with reverse order")
        void testCreateWithCustomOrder() {
            TreeSet<String> result = examples.createWithCustomOrder(List.of("banana", "apple", "cherry"));
            assertThat(new ArrayList<>(result)).containsExactly("cherry", "banana", "apple");
        }
    }

    // ========== LinkedHashSet Tests ==========

    @Nested
    @DisplayName("LinkedHashSet Examples")
    class LinkedHashSetExamplesTest {

        private final CommonDataStructures.LinkedHashSetExamples examples = new CommonDataStructures.LinkedHashSetExamples();

        @Test
        @DisplayName("Should remove duplicates preserving insertion order")
        void testRemoveDuplicatesPreserveOrder() {
            List<String> result = examples.removeDuplicatesPreserveOrder(
                    List.of("c", "a", "b", "a", "c", "d"));
            assertThat(result).containsExactly("c", "a", "b", "d");
        }

        @Test
        @DisplayName("Should handle empty list")
        void testRemoveDuplicatesPreserveOrderEmpty() {
            List<String> result = examples.removeDuplicatesPreserveOrder(Collections.emptyList());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle list with no duplicates")
        void testRemoveDuplicatesPreserveOrderNoDuplicates() {
            List<String> result = examples.removeDuplicatesPreserveOrder(List.of("a", "b", "c"));
            assertThat(result).containsExactly("a", "b", "c");
        }
    }

    // ========== HashMap Tests ==========

    @Nested
    @DisplayName("HashMap Examples")
    class HashMapExamplesTest {

        private final CommonDataStructures.HashMapExamples examples = new CommonDataStructures.HashMapExamples();

        @Test
        @DisplayName("Should count word frequency")
        void testCountWordFrequency() {
            Map<String, Integer> freq = examples.countWordFrequency(
                    List.of("hello", "world", "hello", "java", "world", "hello"));
            assertThat(freq).containsEntry("hello", 3)
                    .containsEntry("world", 2)
                    .containsEntry("java", 1);
        }

        @Test
        @DisplayName("Should return empty map for empty list")
        void testCountWordFrequencyEmpty() {
            Map<String, Integer> freq = examples.countWordFrequency(Collections.emptyList());
            assertThat(freq).isEmpty();
        }

        @Test
        @DisplayName("Should group words by first letter")
        void testGroupByFirstLetter() {
            Map<String, List<String>> groups = examples.groupByFirstLetter(
                    List.of("apple", "banana", "avocado", "cherry", "blueberry"));
            assertThat(groups.get("A")).containsExactly("apple", "avocado");
            assertThat(groups.get("B")).containsExactly("banana", "blueberry");
            assertThat(groups.get("C")).containsExactly("cherry");
        }

        @Test
        @DisplayName("Should skip null and empty strings when grouping")
        void testGroupByFirstLetterSkipsNullAndEmpty() {
            Map<String, List<String>> groups = examples.groupByFirstLetter(
                    Arrays.asList("apple", null, "", "banana"));
            assertThat(groups).hasSize(2);
            assertThat(groups.get("A")).containsExactly("apple");
        }

        @Test
        @DisplayName("Should return default value for missing key")
        void testGetCount() {
            Map<String, Integer> map = Map.of("a", 5, "b", 10);
            assertThat(examples.getCount(map, "a")).isEqualTo(5);
            assertThat(examples.getCount(map, "missing")).isEqualTo(0);
        }

        @Test
        @DisplayName("Should put only if key is absent")
        void testPutIfNew() {
            Map<String, String> original = new HashMap<>(Map.of("key1", "value1"));
            Map<String, String> result = examples.putIfNew(original, "key1", "newValue");
            assertThat(result.get("key1")).isEqualTo("value1"); // Not overwritten

            result = examples.putIfNew(original, "key2", "value2");
            assertThat(result.get("key2")).isEqualTo("value2"); // Added
        }
    }

    // ========== TreeMap Tests ==========

    @Nested
    @DisplayName("TreeMap Examples")
    class TreeMapExamplesTest {

        private final CommonDataStructures.TreeMapExamples examples = new CommonDataStructures.TreeMapExamples();

        @Test
        @DisplayName("Should create sorted map")
        void testCreateSortedMap() {
            Map<String, Integer> unsorted = Map.of("banana", 2, "apple", 1, "cherry", 3);
            TreeMap<String, Integer> sorted = examples.createSortedMap(unsorted);
            assertThat(new ArrayList<>(sorted.keySet())).containsExactly("apple", "banana", "cherry");
        }

        @Test
        @DisplayName("Should get entries in range")
        void testGetEntriesInRange() {
            TreeMap<String, Integer> map = new TreeMap<>(Map.of("a", 1, "b", 2, "c", 3, "d", 4, "e", 5));
            SortedMap<String, Integer> range = examples.getEntriesInRange(map, "b", "d");
            assertThat(range.keySet()).containsExactly("b", "c");
        }

        @Test
        @DisplayName("Should find first and last key")
        void testFindFirstAndLastKey() {
            TreeMap<String, Integer> map = new TreeMap<>(Map.of("banana", 2, "apple", 1, "cherry", 3));
            assertThat(examples.findFirstKey(map)).isEqualTo("apple");
            assertThat(examples.findLastKey(map)).isEqualTo("cherry");
        }

        @Test
        @DisplayName("Should return null for empty map first/last key")
        void testFindFirstLastKeyEmpty() {
            TreeMap<String, Integer> map = new TreeMap<>();
            assertThat(examples.findFirstKey(map)).isNull();
            assertThat(examples.findLastKey(map)).isNull();
        }
    }

    // ========== BST Tests ==========

    @Nested
    @DisplayName("Binary Search Tree")
    class BinarySearchTreeTest {

        @Test
        @DisplayName("Should insert and retrieve elements in sorted order")
        void testInsertAndInOrder() {
            var bst = new CommonDataStructures.BinarySearchTree<Integer>();
            bst.insert(5);
            bst.insert(3);
            bst.insert(7);
            bst.insert(1);
            bst.insert(4);

            assertThat(bst.inOrderTraversal()).containsExactly(1, 3, 4, 5, 7);
        }

        @Test
        @DisplayName("Should handle duplicate insertions")
        void testInsertDuplicate() {
            var bst = new CommonDataStructures.BinarySearchTree<Integer>();
            bst.insert(5);
            bst.insert(5);
            bst.insert(5);

            assertThat(bst.inOrderTraversal()).containsExactly(5);
            assertThat(bst.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw on null insertion")
        void testInsertNull() {
            var bst = new CommonDataStructures.BinarySearchTree<Integer>();
            assertThatThrownBy(() -> bst.insert(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should search for existing and non-existing values")
        void testContains() {
            var bst = new CommonDataStructures.BinarySearchTree<Integer>();
            bst.insert(5);
            bst.insert(3);
            bst.insert(7);

            assertThat(bst.contains(5)).isTrue();
            assertThat(bst.contains(3)).isTrue();
            assertThat(bst.contains(99)).isFalse();
            assertThat(bst.contains(null)).isFalse();
        }

        @Test
        @DisplayName("Should perform pre-order traversal correctly")
        void testPreOrderTraversal() {
            var bst = new CommonDataStructures.BinarySearchTree<Integer>();
            bst.insert(5);
            bst.insert(3);
            bst.insert(7);
            bst.insert(1);
            bst.insert(4);

            assertThat(bst.preOrderTraversal()).containsExactly(5, 3, 1, 4, 7);
        }

        @Test
        @DisplayName("Should perform post-order traversal correctly")
        void testPostOrderTraversal() {
            var bst = new CommonDataStructures.BinarySearchTree<Integer>();
            bst.insert(5);
            bst.insert(3);
            bst.insert(7);
            bst.insert(1);
            bst.insert(4);

            assertThat(bst.postOrderTraversal()).containsExactly(1, 4, 3, 7, 5);
        }

        @Test
        @DisplayName("Should perform level-order (BFS) traversal")
        void testLevelOrderTraversal() {
            var bst = new CommonDataStructures.BinarySearchTree<Integer>();
            bst.insert(5);
            bst.insert(3);
            bst.insert(7);
            bst.insert(1);
            bst.insert(4);

            assertThat(bst.levelOrderTraversal()).containsExactly(5, 3, 7, 1, 4);
        }

        @Test
        @DisplayName("Should return empty list for level-order traversal on empty tree")
        void testLevelOrderTraversalEmpty() {
            var bst = new CommonDataStructures.BinarySearchTree<Integer>();
            assertThat(bst.levelOrderTraversal()).isEmpty();
        }

        @Test
        @DisplayName("Should delete leaf node")
        void testDeleteLeafNode() {
            var bst = new CommonDataStructures.BinarySearchTree<Integer>();
            bst.insert(5);
            bst.insert(3);
            bst.insert(7);

            bst.delete(3);
            assertThat(bst.inOrderTraversal()).containsExactly(5, 7);
        }

        @Test
        @DisplayName("Should delete node with one child")
        void testDeleteNodeOneChild() {
            var bst = new CommonDataStructures.BinarySearchTree<Integer>();
            bst.insert(5);
            bst.insert(3);
            bst.insert(7);
            bst.insert(1);

            bst.delete(3);
            assertThat(bst.inOrderTraversal()).containsExactly(1, 5, 7);
        }

        @Test
        @DisplayName("Should delete node with two children")
        void testDeleteNodeTwoChildren() {
            var bst = new CommonDataStructures.BinarySearchTree<Integer>();
            bst.insert(5);
            bst.insert(3);
            bst.insert(7);
            bst.insert(1);
            bst.insert(4);
            bst.insert(6);
            bst.insert(8);

            bst.delete(5); // Root with two children
            assertThat(bst.inOrderTraversal()).containsExactly(1, 3, 4, 6, 7, 8);
        }

        @Test
        @DisplayName("Should handle deleting non-existing value")
        void testDeleteNonExisting() {
            var bst = new CommonDataStructures.BinarySearchTree<Integer>();
            bst.insert(5);
            bst.delete(99); // Should not throw
            assertThat(bst.inOrderTraversal()).containsExactly(5);
        }

        @Test
        @DisplayName("Should handle deleting null value")
        void testDeleteNull() {
            var bst = new CommonDataStructures.BinarySearchTree<Integer>();
            bst.insert(5);
            bst.delete(null); // Should not throw
            assertThat(bst.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should calculate height correctly")
        void testHeight() {
            var bst = new CommonDataStructures.BinarySearchTree<Integer>();
            assertThat(bst.height()).isEqualTo(-1); // Empty tree

            bst.insert(5);
            assertThat(bst.height()).isEqualTo(0); // Single node

            bst.insert(3);
            bst.insert(7);
            assertThat(bst.height()).isEqualTo(1);

            bst.insert(1);
            assertThat(bst.height()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should calculate size correctly")
        void testSize() {
            var bst = new CommonDataStructures.BinarySearchTree<Integer>();
            assertThat(bst.size()).isEqualTo(0);

            bst.insert(5);
            assertThat(bst.size()).isEqualTo(1);

            bst.insert(3);
            bst.insert(7);
            assertThat(bst.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should find min and max values")
        void testFindMinMax() {
            var bst = new CommonDataStructures.BinarySearchTree<Integer>();
            bst.insert(5);
            bst.insert(3);
            bst.insert(7);
            bst.insert(1);
            bst.insert(9);

            assertThat(bst.findMinValue()).isEqualTo(1);
            assertThat(bst.findMaxValue()).isEqualTo(9);
        }

        @Test
        @DisplayName("Should throw when finding min/max on empty tree")
        void testFindMinMaxEmpty() {
            var bst = new CommonDataStructures.BinarySearchTree<Integer>();
            assertThatThrownBy(bst::findMinValue).isInstanceOf(NoSuchElementException.class);
            assertThatThrownBy(bst::findMaxValue).isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("Should report isEmpty correctly")
        void testIsEmpty() {
            var bst = new CommonDataStructures.BinarySearchTree<Integer>();
            assertThat(bst.isEmpty()).isTrue();

            bst.insert(5);
            assertThat(bst.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("Should work with String type")
        void testWithStrings() {
            var bst = new CommonDataStructures.BinarySearchTree<String>();
            bst.insert("banana");
            bst.insert("apple");
            bst.insert("cherry");

            assertThat(bst.inOrderTraversal()).containsExactly("apple", "banana", "cherry");
        }
    }

    // ========== Graph Tests ==========

    @Nested
    @DisplayName("Graph")
    class GraphTest {

        @Test
        @DisplayName("Should create undirected graph and add edges")
        void testUndirectedGraph() {
            var graph = new CommonDataStructures.Graph<String>(false);
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            assertThat(graph.hasEdge("A", "B")).isTrue();
            assertThat(graph.hasEdge("B", "A")).isTrue(); // Undirected
            assertThat(graph.hasEdge("A", "C")).isFalse();
        }

        @Test
        @DisplayName("Should create directed graph and respect direction")
        void testDirectedGraph() {
            var graph = new CommonDataStructures.Graph<String>(true);
            graph.addEdge("A", "B");

            assertThat(graph.hasEdge("A", "B")).isTrue();
            assertThat(graph.hasEdge("B", "A")).isFalse(); // Directed
        }

        @Test
        @DisplayName("Should throw on null vertex")
        void testAddNullVertex() {
            var graph = new CommonDataStructures.Graph<String>(false);
            assertThatThrownBy(() -> graph.addVertex(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw on null edge vertices")
        void testAddNullEdge() {
            var graph = new CommonDataStructures.Graph<String>(false);
            assertThatThrownBy(() -> graph.addEdge(null, "B"))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> graph.addEdge("A", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should add vertex and auto-create vertices for edges")
        void testAddVertex() {
            var graph = new CommonDataStructures.Graph<String>(false);
            graph.addVertex("A");
            graph.addEdge("B", "C"); // Auto-creates B and C

            assertThat(graph.hasVertex("A")).isTrue();
            assertThat(graph.hasVertex("B")).isTrue();
            assertThat(graph.hasVertex("C")).isTrue();
            assertThat(graph.vertexCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should remove edge")
        void testRemoveEdge() {
            var graph = new CommonDataStructures.Graph<String>(false);
            graph.addEdge("A", "B");
            graph.removeEdge("A", "B");

            assertThat(graph.hasEdge("A", "B")).isFalse();
            assertThat(graph.hasEdge("B", "A")).isFalse();
        }

        @Test
        @DisplayName("Should remove vertex and its edges")
        void testRemoveVertex() {
            var graph = new CommonDataStructures.Graph<String>(false);
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("B", "C");

            graph.removeVertex("A");

            assertThat(graph.hasVertex("A")).isFalse();
            assertThat(graph.getNeighbors("B")).doesNotContain("A");
            assertThat(graph.getNeighbors("C")).doesNotContain("A");
        }

        @Test
        @DisplayName("Should return empty neighbors for non-existing vertex")
        void testGetNeighborsNonExisting() {
            var graph = new CommonDataStructures.Graph<String>(false);
            assertThat(graph.getNeighbors("X")).isEmpty();
        }

        @Test
        @DisplayName("Should count vertices and edges correctly")
        void testCounts() {
            var graph = new CommonDataStructures.Graph<String>(false);
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("A", "C");

            assertThat(graph.vertexCount()).isEqualTo(3);
            assertThat(graph.edgeCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should count edges correctly for directed graph")
        void testEdgeCountDirected() {
            var graph = new CommonDataStructures.Graph<String>(true);
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            assertThat(graph.edgeCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should perform BFS traversal")
        void testBfs() {
            var graph = new CommonDataStructures.Graph<Integer>(false);
            graph.addEdge(1, 2);
            graph.addEdge(1, 3);
            graph.addEdge(2, 4);
            graph.addEdge(3, 5);

            List<Integer> bfsOrder = graph.bfs(1);
            assertThat(bfsOrder).startsWith(1);
            assertThat(bfsOrder).hasSize(5);
            // BFS: 1 should come before 2 and 3, which should come before 4 and 5
            assertThat(bfsOrder.indexOf(1)).isLessThan(bfsOrder.indexOf(2));
            assertThat(bfsOrder.indexOf(1)).isLessThan(bfsOrder.indexOf(3));
            assertThat(bfsOrder.indexOf(2)).isLessThan(bfsOrder.indexOf(4));
        }

        @Test
        @DisplayName("Should return empty list for BFS on non-existing vertex")
        void testBfsNonExisting() {
            var graph = new CommonDataStructures.Graph<Integer>(false);
            assertThat(graph.bfs(1)).isEmpty();
        }

        @Test
        @DisplayName("Should perform DFS traversal")
        void testDfs() {
            var graph = new CommonDataStructures.Graph<Integer>(false);
            graph.addEdge(1, 2);
            graph.addEdge(1, 3);
            graph.addEdge(2, 4);
            graph.addEdge(3, 5);

            List<Integer> dfsOrder = graph.dfs(1);
            assertThat(dfsOrder).startsWith(1);
            assertThat(dfsOrder).hasSize(5);
        }

        @Test
        @DisplayName("Should return empty list for DFS on non-existing vertex")
        void testDfsNonExisting() {
            var graph = new CommonDataStructures.Graph<Integer>(false);
            assertThat(graph.dfs(1)).isEmpty();
        }

        @Test
        @DisplayName("Should detect cycle in directed graph")
        void testHasCycleDirected() {
            var graph = new CommonDataStructures.Graph<String>(true);
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A"); // Creates cycle

            assertThat(graph.hasCycle()).isTrue();
        }

        @Test
        @DisplayName("Should detect no cycle in acyclic directed graph")
        void testNoCycleDirected() {
            var graph = new CommonDataStructures.Graph<String>(true);
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("A", "C");

            assertThat(graph.hasCycle()).isFalse();
        }

        @Test
        @DisplayName("Should find shortest path using BFS")
        void testShortestPath() {
            var graph = new CommonDataStructures.Graph<String>(false);
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("A", "C"); // Direct shorter path
            graph.addEdge("C", "D");

            List<String> path = graph.shortestPath("A", "D");
            assertThat(path).containsExactly("A", "C", "D");
        }

        @Test
        @DisplayName("Should return single element path for same start and end")
        void testShortestPathSameVertex() {
            var graph = new CommonDataStructures.Graph<String>(false);
            graph.addVertex("A");

            List<String> path = graph.shortestPath("A", "A");
            assertThat(path).containsExactly("A");
        }

        @Test
        @DisplayName("Should return empty path when no path exists")
        void testShortestPathNoPath() {
            var graph = new CommonDataStructures.Graph<String>(false);
            graph.addVertex("A");
            graph.addVertex("B"); // Disconnected

            List<String> path = graph.shortestPath("A", "B");
            assertThat(path).isEmpty();
        }

        @Test
        @DisplayName("Should return empty path for non-existing vertices")
        void testShortestPathNonExisting() {
            var graph = new CommonDataStructures.Graph<String>(false);
            assertThat(graph.shortestPath("X", "Y")).isEmpty();
        }

        @Test
        @DisplayName("Should report directed/undirected status")
        void testIsDirected() {
            var directed = new CommonDataStructures.Graph<String>(true);
            var undirected = new CommonDataStructures.Graph<String>(false);

            assertThat(directed.isDirected()).isTrue();
            assertThat(undirected.isDirected()).isFalse();
        }
    }
}
