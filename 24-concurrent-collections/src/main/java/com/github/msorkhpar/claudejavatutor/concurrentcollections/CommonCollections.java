package com.github.msorkhpar.claudejavatutor.concurrentcollections;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Demonstrates common collection implementations and their characteristics.
 * Covers List (ArrayList, LinkedList, CopyOnWriteArrayList),
 * Set (HashSet, ConcurrentSkipListSet), and Map (HashMap, ConcurrentHashMap).
 */
public class CommonCollections {

    /**
     * Demonstrates ArrayList characteristics: fast random access, slow middle insertion.
     */
    public static class ArrayListExamples {

        /**
         * Creates an ArrayList with initial elements demonstrating random access.
         */
        public List<String> createAndAccess(String... elements) {
            List<String> list = new ArrayList<>(Arrays.asList(elements));
            return list;
        }

        /**
         * Demonstrates that ArrayList preserves insertion order.
         */
        public List<Integer> preserveInsertionOrder(int... values) {
            List<Integer> list = new ArrayList<>();
            for (int v : values) {
                list.add(v);
            }
            return list;
        }

        /**
         * Demonstrates adding at a specific index (costly for large lists).
         */
        public List<String> insertAtIndex(List<String> original, int index, String element) {
            List<String> list = new ArrayList<>(original);
            list.add(index, element);
            return list;
        }

        /**
         * Demonstrates null handling in ArrayList.
         */
        public List<String> addNullElements() {
            List<String> list = new ArrayList<>();
            list.add("first");
            list.add(null);
            list.add("third");
            list.add(null);
            return list;
        }
    }

    /**
     * Demonstrates LinkedList characteristics: fast insertion/removal, slow random access.
     */
    public static class LinkedListExamples {

        /**
         * Uses LinkedList as a Deque (double-ended queue).
         */
        public Deque<String> useAsDeque(String... elements) {
            Deque<String> deque = new LinkedList<>();
            for (String e : elements) {
                deque.addLast(e);
            }
            return deque;
        }

        /**
         * Demonstrates efficient head/tail operations.
         */
        public String[] headAndTailOperations(List<String> elements) {
            LinkedList<String> list = new LinkedList<>(elements);
            if (list.isEmpty()) {
                return new String[0];
            }
            return new String[]{list.getFirst(), list.getLast()};
        }

        /**
         * Demonstrates LinkedList as a Queue (FIFO).
         */
        public List<String> processAsQueue(String... elements) {
            Queue<String> queue = new LinkedList<>(Arrays.asList(elements));
            List<String> processed = new ArrayList<>();
            while (!queue.isEmpty()) {
                processed.add(queue.poll());
            }
            return processed;
        }
    }

    /**
     * Demonstrates CopyOnWriteArrayList: thread-safe list for read-heavy workloads.
     */
    public static class CopyOnWriteArrayListExamples {

        /**
         * Creates a CopyOnWriteArrayList and demonstrates basic operations.
         */
        public CopyOnWriteArrayList<String> createAndModify(String... elements) {
            CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>(elements);
            return list;
        }

        /**
         * Demonstrates that iteration sees a snapshot (no ConcurrentModificationException).
         */
        public List<String> iterateWhileModifying(CopyOnWriteArrayList<String> list, String toAdd) {
            List<String> snapshot = new ArrayList<>();
            for (String s : list) {
                snapshot.add(s);
                if (s.equals(list.get(0))) {
                    list.add(toAdd); // Modification during iteration is safe
                }
            }
            return snapshot;
        }

        /**
         * Demonstrates addIfAbsent for unique element insertion.
         */
        public boolean addIfAbsent(CopyOnWriteArrayList<String> list, String element) {
            return list.addIfAbsent(element);
        }
    }

    /**
     * Demonstrates HashSet characteristics: no duplicates, no ordering guarantee.
     */
    public static class HashSetExamples {

        /**
         * Demonstrates duplicate rejection.
         */
        public Set<String> addWithDuplicates(String... elements) {
            Set<String> set = new HashSet<>();
            for (String e : elements) {
                set.add(e);
            }
            return set;
        }

        /**
         * Demonstrates that HashSet does not guarantee insertion order.
         */
        public boolean orderNotGuaranteed() {
            Set<Integer> set = new HashSet<>();
            for (int i = 100; i >= 0; i--) {
                set.add(i);
            }
            List<Integer> asList = new ArrayList<>(set);
            List<Integer> sorted = new ArrayList<>(asList);
            Collections.sort(sorted);
            // If order were guaranteed to be insertion order, asList would be reversed
            // This is intentionally non-deterministic to demonstrate the point
            return true;
        }

        /**
         * Demonstrates null handling in HashSet.
         */
        public Set<String> addNullToHashSet() {
            Set<String> set = new HashSet<>();
            set.add("one");
            set.add(null);
            set.add("three");
            return set;
        }
    }

    /**
     * Demonstrates ConcurrentSkipListSet: thread-safe sorted set.
     */
    public static class ConcurrentSkipListSetExamples {

        /**
         * Creates a sorted, thread-safe set.
         */
        public ConcurrentSkipListSet<String> createSortedSet(String... elements) {
            ConcurrentSkipListSet<String> set = new ConcurrentSkipListSet<>();
            Collections.addAll(set, elements);
            return set;
        }

        /**
         * Demonstrates range operations (headSet, tailSet, subSet).
         */
        public NavigableSet<String> getRange(ConcurrentSkipListSet<String> set, String from, String to) {
            return set.subSet(from, true, to, true);
        }

        /**
         * Demonstrates that ConcurrentSkipListSet does not allow null.
         */
        public void addNullThrowsException() {
            ConcurrentSkipListSet<String> set = new ConcurrentSkipListSet<>();
            set.add(null); // Throws NullPointerException
        }

        /**
         * Demonstrates first/last element retrieval in sorted order.
         */
        public String[] firstAndLast(ConcurrentSkipListSet<String> set) {
            if (set.isEmpty()) {
                return new String[0];
            }
            return new String[]{set.first(), set.last()};
        }
    }

    /**
     * Demonstrates HashMap characteristics: fast O(1) lookup, not thread-safe.
     */
    public static class HashMapExamples {

        /**
         * Demonstrates basic put/get operations.
         */
        public Map<String, Integer> createWordCountMap(String... words) {
            Map<String, Integer> map = new HashMap<>();
            for (String word : words) {
                map.merge(word, 1, Integer::sum);
            }
            return map;
        }

        /**
         * Demonstrates compute/computeIfAbsent/computeIfPresent.
         */
        public Map<String, List<Integer>> groupByFirstLetter(Map<String, Integer> input) {
            Map<String, List<Integer>> grouped = new HashMap<>();
            input.forEach((key, value) ->
                    grouped.computeIfAbsent(
                            key.substring(0, 1).toUpperCase(),
                            k -> new ArrayList<>()
                    ).add(value)
            );
            return grouped;
        }

        /**
         * Demonstrates null key and null value support in HashMap.
         */
        public Map<String, String> nullKeyAndValue() {
            Map<String, String> map = new HashMap<>();
            map.put(null, "null-key-value");
            map.put("key", null);
            return map;
        }
    }

    /**
     * Demonstrates ConcurrentHashMap: thread-safe map with high concurrency.
     */
    public static class ConcurrentHashMapExamples {

        /**
         * Creates a ConcurrentHashMap with initial entries.
         */
        public ConcurrentHashMap<String, Integer> createConcurrentMap(Map<String, Integer> initial) {
            return new ConcurrentHashMap<>(initial);
        }

        /**
         * Demonstrates atomic compute operations.
         */
        public ConcurrentHashMap<String, Integer> atomicWordCount(String... words) {
            ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
            for (String word : words) {
                map.merge(word, 1, Integer::sum);
            }
            return map;
        }

        /**
         * Demonstrates that ConcurrentHashMap does not allow null keys or values.
         */
        public void putNullKeyThrows() {
            ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
            map.put(null, "value"); // Throws NullPointerException
        }

        /**
         * Demonstrates that ConcurrentHashMap does not allow null values.
         */
        public void putNullValueThrows() {
            ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
            map.put("key", null); // Throws NullPointerException
        }

        /**
         * Demonstrates bulk operations: search, reduce, forEach with parallelism threshold.
         */
        public long parallelReduce(ConcurrentHashMap<String, Integer> map) {
            return map.reduceValuesToLong(
                    1, // parallelism threshold
                    Integer::longValue,
                    0L,
                    Long::sum
            );
        }

        /**
         * Demonstrates search operation.
         */
        public String searchForValue(ConcurrentHashMap<String, Integer> map, int target) {
            return map.search(1, (key, value) -> value == target ? key : null);
        }
    }
}
