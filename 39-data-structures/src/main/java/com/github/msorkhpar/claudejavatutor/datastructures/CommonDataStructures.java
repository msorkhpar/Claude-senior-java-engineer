package com.github.msorkhpar.claudejavatutor.datastructures;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Demonstrates common data structures in Java: Lists, Sets, Maps, Trees, and Graphs.
 * Covers practical usage patterns, performance characteristics, and real-world scenarios.
 */
public class CommonDataStructures {

    // ========== 10.1.1.1 Lists ==========

    /**
     * Demonstrates ArrayList operations and characteristics.
     * ArrayList uses a dynamic array internally providing O(1) random access
     * and amortized O(1) append, but O(n) insertion/removal in the middle.
     */
    public static class ArrayListExamples {

        /**
         * Demonstrates ArrayList with initial capacity to avoid resizing overhead.
         */
        public List<Integer> createWithCapacity(int capacity, int count) {
            List<Integer> list = new ArrayList<>(capacity);
            for (int i = 0; i < count; i++) {
                list.add(i);
            }
            return list;
        }

        /**
         * Demonstrates random access performance advantage of ArrayList.
         */
        public int getElementAtIndex(List<Integer> list, int index) {
            if (index < 0 || index >= list.size()) {
                throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + list.size());
            }
            return list.get(index);
        }

        /**
         * Demonstrates insertion in the middle of an ArrayList (O(n) due to shifting).
         */
        public List<String> insertAtPosition(List<String> original, int position, String element) {
            List<String> copy = new ArrayList<>(original);
            if (position < 0 || position > copy.size()) {
                throw new IndexOutOfBoundsException("Position " + position + " out of bounds");
            }
            copy.add(position, element);
            return copy;
        }

        /**
         * Demonstrates removing elements by value (first occurrence).
         */
        public List<String> removeByValue(List<String> original, String value) {
            List<String> copy = new ArrayList<>(original);
            copy.remove(value);
            return copy;
        }

        /**
         * Demonstrates subList view (changes to subList reflect in original).
         */
        public List<Integer> getSubList(List<Integer> list, int from, int to) {
            if (from < 0 || to > list.size() || from > to) {
                throw new IndexOutOfBoundsException("Invalid range [" + from + ", " + to + ")");
            }
            return new ArrayList<>(list.subList(from, to));
        }
    }

    /**
     * Demonstrates LinkedList operations and characteristics.
     * LinkedList uses a doubly-linked list internally providing O(1) insertion/removal
     * at head/tail, but O(n) random access and search.
     */
    public static class LinkedListExamples {

        /**
         * Uses LinkedList as a Deque (double-ended queue).
         */
        public Deque<String> createDeque(List<String> elements) {
            Deque<String> deque = new LinkedList<>(elements);
            return deque;
        }

        /**
         * Demonstrates adding elements at both ends.
         */
        public LinkedList<String> addAtBothEnds(List<String> original, String first, String last) {
            LinkedList<String> list = new LinkedList<>(original);
            list.addFirst(first);
            list.addLast(last);
            return list;
        }

        /**
         * Demonstrates peek and poll operations (queue-like behavior).
         */
        public String peekFirst(LinkedList<String> list) {
            return list.peekFirst(); // Returns null if empty, doesn't throw
        }

        /**
         * Demonstrates removing from both ends.
         */
        public LinkedList<String> removeFromBothEnds(LinkedList<String> original) {
            LinkedList<String> list = new LinkedList<>(original);
            if (list.size() >= 2) {
                list.removeFirst();
                list.removeLast();
            }
            return list;
        }
    }

    // ========== 10.1.1.2 Sets ==========

    /**
     * Demonstrates HashSet operations and characteristics.
     * HashSet uses HashMap internally, providing O(1) average-case add/remove/contains.
     * Does not maintain insertion order.
     */
    public static class HashSetExamples {

        /**
         * Removes duplicates from a list while preserving no specific order.
         */
        public Set<String> removeDuplicates(List<String> list) {
            return new HashSet<>(list);
        }

        /**
         * Demonstrates set intersection (elements common to both sets).
         */
        public Set<Integer> intersection(Set<Integer> set1, Set<Integer> set2) {
            Set<Integer> result = new HashSet<>(set1);
            result.retainAll(set2);
            return result;
        }

        /**
         * Demonstrates set union (all elements from both sets).
         */
        public Set<Integer> union(Set<Integer> set1, Set<Integer> set2) {
            Set<Integer> result = new HashSet<>(set1);
            result.addAll(set2);
            return result;
        }

        /**
         * Demonstrates set difference (elements in set1 but not in set2).
         */
        public Set<Integer> difference(Set<Integer> set1, Set<Integer> set2) {
            Set<Integer> result = new HashSet<>(set1);
            result.removeAll(set2);
            return result;
        }

        /**
         * Demonstrates the importance of hashCode/equals contract for custom objects.
         */
        public int countUniquePoints(List<Point> points) {
            Set<Point> uniquePoints = new HashSet<>(points);
            return uniquePoints.size();
        }
    }

    /**
     * A simple Point record demonstrating proper hashCode/equals for use in hash-based collections.
     */
    public record Point(int x, int y) {}

    /**
     * Demonstrates TreeSet operations and characteristics.
     * TreeSet uses a Red-Black tree internally, providing O(log n) add/remove/contains.
     * Maintains elements in sorted order.
     */
    public static class TreeSetExamples {

        /**
         * Creates a sorted set from an unsorted collection.
         */
        public TreeSet<Integer> createSorted(Collection<Integer> elements) {
            return new TreeSet<>(elements);
        }

        /**
         * Demonstrates range queries using TreeSet navigation methods.
         */
        public SortedSet<Integer> getRange(TreeSet<Integer> set, int from, int to) {
            return set.subSet(from, to);
        }

        /**
         * Finds the ceiling (smallest element >= given value).
         */
        public Integer findCeiling(TreeSet<Integer> set, int value) {
            return set.ceiling(value);
        }

        /**
         * Finds the floor (largest element <= given value).
         */
        public Integer findFloor(TreeSet<Integer> set, int value) {
            return set.floor(value);
        }

        /**
         * Creates a TreeSet with a custom comparator (e.g., reverse order).
         */
        public TreeSet<String> createWithCustomOrder(Collection<String> elements) {
            TreeSet<String> set = new TreeSet<>(Comparator.reverseOrder());
            set.addAll(elements);
            return set;
        }
    }

    /**
     * Demonstrates LinkedHashSet which maintains insertion order.
     */
    public static class LinkedHashSetExamples {

        /**
         * Removes duplicates while preserving insertion order.
         */
        public List<String> removeDuplicatesPreserveOrder(List<String> list) {
            return new ArrayList<>(new LinkedHashSet<>(list));
        }
    }

    // ========== 10.1.1.3 Maps ==========

    /**
     * Demonstrates HashMap operations and characteristics.
     * HashMap provides O(1) average-case get/put operations.
     */
    public static class HashMapExamples {

        /**
         * Counts word frequency in a list.
         */
        public Map<String, Integer> countWordFrequency(List<String> words) {
            Map<String, Integer> frequency = new HashMap<>();
            for (String word : words) {
                frequency.merge(word, 1, Integer::sum);
            }
            return frequency;
        }

        /**
         * Demonstrates computeIfAbsent for lazy initialization of map values.
         */
        public Map<String, List<String>> groupByFirstLetter(List<String> words) {
            Map<String, List<String>> groups = new HashMap<>();
            for (String word : words) {
                if (word != null && !word.isEmpty()) {
                    String key = word.substring(0, 1).toUpperCase();
                    groups.computeIfAbsent(key, k -> new ArrayList<>()).add(word);
                }
            }
            return groups;
        }

        /**
         * Demonstrates getOrDefault for safe retrieval.
         */
        public int getCount(Map<String, Integer> map, String key) {
            return map.getOrDefault(key, 0);
        }

        /**
         * Demonstrates putIfAbsent for conditional insertion.
         */
        public Map<String, String> putIfNew(Map<String, String> original, String key, String value) {
            Map<String, String> copy = new HashMap<>(original);
            copy.putIfAbsent(key, value);
            return copy;
        }
    }

    /**
     * Demonstrates TreeMap operations and characteristics.
     * TreeMap uses a Red-Black tree providing O(log n) operations with sorted keys.
     */
    public static class TreeMapExamples {

        /**
         * Creates a sorted map from entries.
         */
        public TreeMap<String, Integer> createSortedMap(Map<String, Integer> unsorted) {
            return new TreeMap<>(unsorted);
        }

        /**
         * Gets entries within a key range.
         */
        public SortedMap<String, Integer> getEntriesInRange(TreeMap<String, Integer> map, String from, String to) {
            return map.subMap(from, to);
        }

        /**
         * Finds the first (lowest) key.
         */
        public String findFirstKey(TreeMap<String, Integer> map) {
            return map.isEmpty() ? null : map.firstKey();
        }

        /**
         * Finds the last (highest) key.
         */
        public String findLastKey(TreeMap<String, Integer> map) {
            return map.isEmpty() ? null : map.lastKey();
        }
    }

    // ========== 10.1.1.4 Trees (Binary Trees, BST, AVL concepts) ==========

    /**
     * A simple Binary Search Tree (BST) implementation demonstrating
     * tree data structure fundamentals: insertion, search, traversal, and deletion.
     */
    public static class BinarySearchTree<T extends Comparable<T>> {

        private Node<T> root;

        static class Node<T> {
            T value;
            Node<T> left;
            Node<T> right;

            Node(T value) {
                this.value = value;
            }
        }

        /**
         * Inserts a value into the BST. Duplicates are ignored.
         */
        public void insert(T value) {
            if (value == null) {
                throw new NullPointerException("Cannot insert null value");
            }
            root = insertRec(root, value);
        }

        private Node<T> insertRec(Node<T> node, T value) {
            if (node == null) {
                return new Node<>(value);
            }
            int cmp = value.compareTo(node.value);
            if (cmp < 0) {
                node.left = insertRec(node.left, value);
            } else if (cmp > 0) {
                node.right = insertRec(node.right, value);
            }
            // Duplicate, do nothing
            return node;
        }

        /**
         * Searches for a value in the BST.
         */
        public boolean contains(T value) {
            if (value == null) {
                return false;
            }
            return containsRec(root, value);
        }

        private boolean containsRec(Node<T> node, T value) {
            if (node == null) {
                return false;
            }
            int cmp = value.compareTo(node.value);
            if (cmp < 0) return containsRec(node.left, value);
            if (cmp > 0) return containsRec(node.right, value);
            return true;
        }

        /**
         * Returns in-order traversal (sorted order for BST).
         */
        public List<T> inOrderTraversal() {
            List<T> result = new ArrayList<>();
            inOrderRec(root, result);
            return result;
        }

        private void inOrderRec(Node<T> node, List<T> result) {
            if (node != null) {
                inOrderRec(node.left, result);
                result.add(node.value);
                inOrderRec(node.right, result);
            }
        }

        /**
         * Returns pre-order traversal (root, left, right).
         */
        public List<T> preOrderTraversal() {
            List<T> result = new ArrayList<>();
            preOrderRec(root, result);
            return result;
        }

        private void preOrderRec(Node<T> node, List<T> result) {
            if (node != null) {
                result.add(node.value);
                preOrderRec(node.left, result);
                preOrderRec(node.right, result);
            }
        }

        /**
         * Returns post-order traversal (left, right, root).
         */
        public List<T> postOrderTraversal() {
            List<T> result = new ArrayList<>();
            postOrderRec(root, result);
            return result;
        }

        private void postOrderRec(Node<T> node, List<T> result) {
            if (node != null) {
                postOrderRec(node.left, result);
                postOrderRec(node.right, result);
                result.add(node.value);
            }
        }

        /**
         * Returns the level-order (breadth-first) traversal.
         */
        public List<T> levelOrderTraversal() {
            List<T> result = new ArrayList<>();
            if (root == null) return result;
            Queue<Node<T>> queue = new LinkedList<>();
            queue.add(root);
            while (!queue.isEmpty()) {
                Node<T> current = queue.poll();
                result.add(current.value);
                if (current.left != null) queue.add(current.left);
                if (current.right != null) queue.add(current.right);
            }
            return result;
        }

        /**
         * Deletes a value from the BST.
         */
        public void delete(T value) {
            if (value == null) return;
            root = deleteRec(root, value);
        }

        private Node<T> deleteRec(Node<T> node, T value) {
            if (node == null) return null;
            int cmp = value.compareTo(node.value);
            if (cmp < 0) {
                node.left = deleteRec(node.left, value);
            } else if (cmp > 0) {
                node.right = deleteRec(node.right, value);
            } else {
                // Node found
                if (node.left == null) return node.right;
                if (node.right == null) return node.left;
                // Two children: find in-order successor (smallest in right subtree)
                Node<T> successor = findMin(node.right);
                node.value = successor.value;
                node.right = deleteRec(node.right, successor.value);
            }
            return node;
        }

        private Node<T> findMin(Node<T> node) {
            while (node.left != null) {
                node = node.left;
            }
            return node;
        }

        /**
         * Returns the height of the tree.
         */
        public int height() {
            return heightRec(root);
        }

        private int heightRec(Node<T> node) {
            if (node == null) return -1; // Empty tree has height -1
            return 1 + Math.max(heightRec(node.left), heightRec(node.right));
        }

        /**
         * Returns the number of nodes in the tree.
         */
        public int size() {
            return sizeRec(root);
        }

        private int sizeRec(Node<T> node) {
            if (node == null) return 0;
            return 1 + sizeRec(node.left) + sizeRec(node.right);
        }

        /**
         * Finds the minimum value in the BST.
         */
        public T findMinValue() {
            if (root == null) {
                throw new NoSuchElementException("Tree is empty");
            }
            return findMin(root).value;
        }

        /**
         * Finds the maximum value in the BST.
         */
        public T findMaxValue() {
            if (root == null) {
                throw new NoSuchElementException("Tree is empty");
            }
            Node<T> current = root;
            while (current.right != null) {
                current = current.right;
            }
            return current.value;
        }

        /**
         * Checks if the tree is empty.
         */
        public boolean isEmpty() {
            return root == null;
        }
    }

    // ========== 10.1.1.5 Graphs ==========

    /**
     * Graph implementation using adjacency list representation.
     * Supports both directed and undirected graphs.
     */
    public static class Graph<T> {

        private final Map<T, Set<T>> adjacencyList;
        private final boolean directed;

        public Graph(boolean directed) {
            this.adjacencyList = new LinkedHashMap<>();
            this.directed = directed;
        }

        /**
         * Adds a vertex to the graph.
         */
        public void addVertex(T vertex) {
            if (vertex == null) {
                throw new NullPointerException("Vertex cannot be null");
            }
            adjacencyList.putIfAbsent(vertex, new LinkedHashSet<>());
        }

        /**
         * Adds an edge between two vertices. Creates vertices if they don't exist.
         */
        public void addEdge(T source, T destination) {
            if (source == null || destination == null) {
                throw new NullPointerException("Vertices cannot be null");
            }
            addVertex(source);
            addVertex(destination);
            adjacencyList.get(source).add(destination);
            if (!directed) {
                adjacencyList.get(destination).add(source);
            }
        }

        /**
         * Removes an edge between two vertices.
         */
        public void removeEdge(T source, T destination) {
            Set<T> sourceNeighbors = adjacencyList.get(source);
            if (sourceNeighbors != null) {
                sourceNeighbors.remove(destination);
            }
            if (!directed) {
                Set<T> destNeighbors = adjacencyList.get(destination);
                if (destNeighbors != null) {
                    destNeighbors.remove(source);
                }
            }
        }

        /**
         * Removes a vertex and all its edges from the graph.
         */
        public void removeVertex(T vertex) {
            adjacencyList.remove(vertex);
            for (Set<T> neighbors : adjacencyList.values()) {
                neighbors.remove(vertex);
            }
        }

        /**
         * Returns the neighbors of a vertex.
         */
        public Set<T> getNeighbors(T vertex) {
            Set<T> neighbors = adjacencyList.get(vertex);
            return neighbors != null ? Collections.unmodifiableSet(neighbors) : Collections.emptySet();
        }

        /**
         * Returns all vertices in the graph.
         */
        public Set<T> getVertices() {
            return Collections.unmodifiableSet(adjacencyList.keySet());
        }

        /**
         * Checks if the graph contains a vertex.
         */
        public boolean hasVertex(T vertex) {
            return adjacencyList.containsKey(vertex);
        }

        /**
         * Checks if an edge exists between two vertices.
         */
        public boolean hasEdge(T source, T destination) {
            Set<T> neighbors = adjacencyList.get(source);
            return neighbors != null && neighbors.contains(destination);
        }

        /**
         * Returns the number of vertices.
         */
        public int vertexCount() {
            return adjacencyList.size();
        }

        /**
         * Returns the number of edges.
         */
        public int edgeCount() {
            int count = adjacencyList.values().stream()
                    .mapToInt(Set::size)
                    .sum();
            return directed ? count : count / 2;
        }

        /**
         * Performs Breadth-First Search (BFS) starting from a given vertex.
         * Returns the vertices visited in BFS order.
         */
        public List<T> bfs(T start) {
            if (!adjacencyList.containsKey(start)) {
                return Collections.emptyList();
            }
            List<T> visited = new ArrayList<>();
            Set<T> seen = new LinkedHashSet<>();
            Queue<T> queue = new LinkedList<>();
            queue.add(start);
            seen.add(start);

            while (!queue.isEmpty()) {
                T current = queue.poll();
                visited.add(current);
                for (T neighbor : adjacencyList.getOrDefault(current, Collections.emptySet())) {
                    if (!seen.contains(neighbor)) {
                        seen.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
            return visited;
        }

        /**
         * Performs Depth-First Search (DFS) starting from a given vertex.
         * Returns the vertices visited in DFS order.
         */
        public List<T> dfs(T start) {
            if (!adjacencyList.containsKey(start)) {
                return Collections.emptyList();
            }
            List<T> visited = new ArrayList<>();
            Set<T> seen = new LinkedHashSet<>();
            dfsRec(start, seen, visited);
            return visited;
        }

        private void dfsRec(T current, Set<T> seen, List<T> visited) {
            seen.add(current);
            visited.add(current);
            for (T neighbor : adjacencyList.getOrDefault(current, Collections.emptySet())) {
                if (!seen.contains(neighbor)) {
                    dfsRec(neighbor, seen, visited);
                }
            }
        }

        /**
         * Detects if the graph has a cycle (for directed graphs).
         */
        public boolean hasCycle() {
            Set<T> visited = new HashSet<>();
            Set<T> recursionStack = new HashSet<>();
            for (T vertex : adjacencyList.keySet()) {
                if (hasCycleRec(vertex, visited, recursionStack)) {
                    return true;
                }
            }
            return false;
        }

        private boolean hasCycleRec(T vertex, Set<T> visited, Set<T> recursionStack) {
            if (recursionStack.contains(vertex)) return true;
            if (visited.contains(vertex)) return false;

            visited.add(vertex);
            recursionStack.add(vertex);

            for (T neighbor : adjacencyList.getOrDefault(vertex, Collections.emptySet())) {
                if (hasCycleRec(neighbor, visited, recursionStack)) {
                    return true;
                }
            }
            recursionStack.remove(vertex);
            return false;
        }

        /**
         * Finds the shortest path between two vertices using BFS (unweighted graph).
         */
        public List<T> shortestPath(T start, T end) {
            if (!adjacencyList.containsKey(start) || !adjacencyList.containsKey(end)) {
                return Collections.emptyList();
            }
            if (start.equals(end)) {
                return List.of(start);
            }

            Map<T, T> parentMap = new HashMap<>();
            Queue<T> queue = new LinkedList<>();
            Set<T> visited = new HashSet<>();

            queue.add(start);
            visited.add(start);
            parentMap.put(start, null);

            while (!queue.isEmpty()) {
                T current = queue.poll();
                for (T neighbor : adjacencyList.getOrDefault(current, Collections.emptySet())) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        parentMap.put(neighbor, current);
                        if (neighbor.equals(end)) {
                            return buildPath(parentMap, start, end);
                        }
                        queue.add(neighbor);
                    }
                }
            }
            return Collections.emptyList(); // No path found
        }

        private List<T> buildPath(Map<T, T> parentMap, T start, T end) {
            LinkedList<T> path = new LinkedList<>();
            T current = end;
            while (current != null) {
                path.addFirst(current);
                current = parentMap.get(current);
            }
            return path;
        }

        public boolean isDirected() {
            return directed;
        }
    }
}
