package com.github.msorkhpar.claudejavatutor.failfastsafe;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Provides guidance and demonstrations for choosing between fail-fast and fail-safe iterators.
 * Covers performance trade-offs, consistency guarantees, and practical decision-making criteria.
 */
public class IteratorChoiceGuide {

    /**
     * Demonstrates filtering with a standard ArrayList (fail-fast).
     * Must use safe patterns like removeIf or stream().filter().
     */
    public static <T> List<T> filterWithArrayList(List<T> source, java.util.function.Predicate<T> predicate) {
        return source.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates filtering with CopyOnWriteArrayList.
     * Reads are fast, but writes create a full copy of the internal array.
     */
    public static <T> List<T> filterWithCopyOnWrite(CopyOnWriteArrayList<T> source,
                                                     java.util.function.Predicate<T> predicate) {
        // removeIf on COW creates a new internal array
        CopyOnWriteArrayList<T> result = new CopyOnWriteArrayList<>(source);
        result.removeIf(item -> !predicate.test(item));
        return result;
    }

    /**
     * Demonstrates safe concurrent reads with CopyOnWriteArrayList.
     * Ideal for read-heavy, write-rare scenarios (e.g., listener lists, configuration).
     */
    public static int concurrentReadSize(CopyOnWriteArrayList<String> list) {
        int total = 0;
        for (String item : list) {
            total += item.length();
        }
        return total;
    }

    /**
     * Demonstrates using ConcurrentHashMap for concurrent accumulation.
     * Useful for scenarios with frequent reads and moderate writes.
     */
    public static Map<String, Integer> wordFrequency(List<String> words) {
        ConcurrentHashMap<String, Integer> freq = new ConcurrentHashMap<>();
        for (String word : words) {
            freq.merge(word, 1, Integer::sum);
        }
        return freq;
    }

    /**
     * Shows the performance impact of CopyOnWriteArrayList writes.
     * Each add() creates a new array copy -- O(n) per write.
     */
    public static long measureCopyOnWriteAddTime(int numberOfAdds) {
        CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < numberOfAdds; i++) {
            list.add(i);
        }
        return System.nanoTime() - start;
    }

    /**
     * Shows the performance of ArrayList writes for comparison.
     * Each add is amortized O(1).
     */
    public static long measureArrayListAddTime(int numberOfAdds) {
        ArrayList<Integer> list = new ArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < numberOfAdds; i++) {
            list.add(i);
        }
        return System.nanoTime() - start;
    }

    /**
     * Demonstrates using streams as a safe alternative to manual iteration + modification.
     * Streams create a pipeline that does not modify the source collection.
     */
    public static <T> List<T> safeFilterWithStream(Collection<T> source,
                                                    java.util.function.Predicate<T> predicate) {
        return source.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates using Collections.unmodifiableList to prevent accidental modification.
     * This is a defensive programming pattern that complements fail-fast behavior.
     */
    public static <T> List<T> createUnmodifiableView(List<T> source) {
        return Collections.unmodifiableList(source);
    }

    /**
     * Demonstrates when to prefer HashMap with external synchronization
     * over ConcurrentHashMap for single-threaded environments.
     */
    public static <K, V> Map<K, V> safeSingleThreadedFilter(Map<K, V> source,
                                                             java.util.function.Predicate<Map.Entry<K, V>> predicate) {
        return source.entrySet().stream()
                .filter(predicate)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Demonstrates multi-threaded safe iteration with ConcurrentHashMap.
     * Uses compute methods for atomic updates.
     */
    public static ConcurrentHashMap<String, Integer> atomicUpdate(
            ConcurrentHashMap<String, Integer> map, String key, int increment) {
        map.compute(key, (k, v) -> v == null ? increment : v + increment);
        return map;
    }

    /**
     * Shows using List.copyOf (Java 10+) to create an unmodifiable snapshot.
     * Combining with fail-fast collections for safe read-only sharing.
     */
    public static <T> List<T> createImmutableSnapshot(List<T> source) {
        return List.copyOf(source);
    }
}
