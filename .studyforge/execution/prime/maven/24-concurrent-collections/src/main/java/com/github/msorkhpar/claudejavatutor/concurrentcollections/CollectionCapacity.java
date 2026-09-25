package com.github.msorkhpar.claudejavatutor.concurrentcollections;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Demonstrates collection capacity, resizing, and load factor concepts.
 * Covers HashMap internals, resizing/rehashing, and capacity considerations
 * in other collections.
 */
public class CollectionCapacity {

    /**
     * Demonstrates HashMap initial capacity and load factor.
     */
    public static class HashMapCapacity {

        /**
         * Creates a HashMap with default initial capacity (16) and load factor (0.75).
         */
        public Map<String, Integer> createWithDefaults() {
            return new HashMap<>();
        }

        /**
         * Creates a HashMap with specified initial capacity.
         */
        public Map<String, Integer> createWithCapacity(int initialCapacity) {
            return new HashMap<>(initialCapacity);
        }

        /**
         * Creates a HashMap with specified initial capacity and load factor.
         */
        public Map<String, Integer> createWithCapacityAndLoadFactor(int initialCapacity, float loadFactor) {
            return new HashMap<>(initialCapacity, loadFactor);
        }

        /**
         * Demonstrates how specifying initial capacity avoids unnecessary resizing.
         * Returns the number of elements added.
         */
        public int addElementsEfficiently(int expectedSize) {
            // Best practice: set initial capacity to expectedSize / loadFactor + 1
            Map<String, Integer> map = new HashMap<>((int) (expectedSize / 0.75f) + 1);
            for (int i = 0; i < expectedSize; i++) {
                map.put("key" + i, i);
            }
            return map.size();
        }

        /**
         * Calculates the next power of 2 >= the given capacity.
         * This mirrors HashMap's internal tableSizeFor() method behavior.
         * HashMap always rounds up capacity to the nearest power of 2.
         */
        public int nextPowerOfTwo(int requestedCapacity) {
            if (requestedCapacity <= 0) {
                return 1;
            }
            int n = requestedCapacity - 1;
            n |= n >>> 1;
            n |= n >>> 2;
            n |= n >>> 4;
            n |= n >>> 8;
            n |= n >>> 16;
            return n + 1;
        }
    }

    /**
     * Demonstrates HashMap resizing and rehashing behavior.
     */
    public static class HashMapResizing {

        /**
         * Demonstrates HashMap capacity doubling behavior conceptually.
         * Given an initial capacity of 4 and a load factor of 0.75:
         * - threshold = 4 * 0.75 = 3, so resize at 4th element -> capacity 8
         * - threshold = 8 * 0.75 = 6, so resize at 7th element -> capacity 16
         * Returns the expected capacity thresholds for a given number of elements.
         */
        public List<Integer> expectedCapacityThresholds(int initialCapacity, float loadFactor, int elementsToAdd) {
            List<Integer> thresholds = new ArrayList<>();
            int capacity = initialCapacity;
            thresholds.add(capacity);

            while (elementsToAdd > (int) (capacity * loadFactor)) {
                capacity *= 2;
                thresholds.add(capacity);
            }
            return thresholds;
        }

        /**
         * Demonstrates that a high load factor increases density but may degrade performance.
         */
        public Map<String, Integer> createHighLoadFactor() {
            // High load factor = more entries before resizing, but more collisions
            return new HashMap<>(16, 0.95f);
        }

        /**
         * Demonstrates that a low load factor wastes space but improves lookup speed.
         */
        public Map<String, Integer> createLowLoadFactor() {
            // Low load factor = fewer entries before resizing, less collisions
            return new HashMap<>(16, 0.5f);
        }

        /**
         * Calculates the optimal initial capacity for a known number of elements.
         */
        public int optimalInitialCapacity(int expectedElements, float loadFactor) {
            return (int) (expectedElements / loadFactor) + 1;
        }
    }

    /**
     * Demonstrates capacity considerations in various collection types.
     */
    public static class OtherCollectionCapacity {

        /**
         * Creates an ArrayList with specified initial capacity.
         */
        public List<String> createArrayListWithCapacity(int capacity) {
            return new ArrayList<>(capacity);
        }

        /**
         * Demonstrates ArrayList growth: typically 50% increase (oldCapacity + oldCapacity >> 1).
         * Returns the element count after adding elements.
         */
        public int arrayListGrowth(int elementsToAdd) {
            List<Integer> list = new ArrayList<>(1); // Start very small
            for (int i = 0; i < elementsToAdd; i++) {
                list.add(i);
            }
            return list.size();
        }

        /**
         * Demonstrates trimToSize() to reduce ArrayList capacity to match size.
         */
        public ArrayList<String> trimArrayList(String... elements) {
            ArrayList<String> list = new ArrayList<>(100);
            Collections.addAll(list, elements);
            list.trimToSize();
            return list;
        }

        /**
         * Demonstrates ConcurrentHashMap initial capacity with concurrency level.
         */
        public ConcurrentHashMap<String, Integer> createConcurrentMapWithParams(
                int initialCapacity, float loadFactor, int concurrencyLevel) {
            return new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
        }

        /**
         * Demonstrates HashSet capacity (backed by HashMap internally).
         */
        public Set<String> createHashSetWithCapacity(int capacity) {
            return new HashSet<>(capacity);
        }

        /**
         * Demonstrates pre-sizing a collection for known data to avoid resizing overhead.
         */
        public Map<String, Integer> preSizedVsDefault(List<String> keys) {
            // Pre-sized to avoid resizing
            Map<String, Integer> map = new HashMap<>((int) (keys.size() / 0.75f) + 1);
            for (int i = 0; i < keys.size(); i++) {
                map.put(keys.get(i), i);
            }
            return map;
        }

        /**
         * LinkedList has no initial capacity concept (nodes are allocated individually).
         */
        public LinkedList<String> linkedListNoCapacity(String... elements) {
            return new LinkedList<>(Arrays.asList(elements));
        }

        /**
         * CopyOnWriteArrayList - the internal array is always exactly the size of the list.
         */
        public CopyOnWriteArrayList<String> cowAlwaysExactSize(String... elements) {
            return new CopyOnWriteArrayList<>(elements);
        }
    }
}
