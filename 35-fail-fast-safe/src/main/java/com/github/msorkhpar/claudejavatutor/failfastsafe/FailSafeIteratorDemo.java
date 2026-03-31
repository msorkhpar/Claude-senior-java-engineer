package com.github.msorkhpar.claudejavatutor.failfastsafe;

import java.util.*;
import java.util.concurrent.*;

/**
 * Demonstrates Fail-Safe (weakly consistent) iterator behavior in Java's concurrent collections.
 *
 * <p>Fail-Safe iterators operate on a snapshot or a weakly consistent view of the collection.
 * They do NOT throw ConcurrentModificationException when the collection is modified during
 * iteration, but they may or may not reflect modifications made after the iterator was created.</p>
 *
 * <p>Key concurrent collections with fail-safe iterators:</p>
 * <ul>
 *   <li>{@link CopyOnWriteArrayList} - creates a snapshot on every write</li>
 *   <li>{@link CopyOnWriteArraySet} - backed by CopyOnWriteArrayList</li>
 *   <li>{@link ConcurrentHashMap} - uses weakly consistent iterators</li>
 *   <li>{@link ConcurrentSkipListMap} - uses weakly consistent iterators</li>
 *   <li>{@link ConcurrentSkipListSet} - uses weakly consistent iterators</li>
 *   <li>{@link ConcurrentLinkedQueue} - uses weakly consistent iterators</li>
 * </ul>
 */
public class FailSafeIteratorDemo {

    /**
     * Demonstrates CopyOnWriteArrayList snapshot semantics.
     * The iterator works on a snapshot taken at the time of iterator creation.
     * Modifications after iterator creation are NOT visible to the iterator.
     */
    public static List<String> iterateAndModifyCopyOnWriteList(List<String> initialElements) {
        CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>(initialElements);
        List<String> iteratedElements = new ArrayList<>();

        for (String item : cowList) {
            iteratedElements.add(item);
            if (item.equals(initialElements.get(0))) {
                cowList.add("ADDED_DURING_ITERATION");
            }
        }
        return iteratedElements;
    }

    /**
     * Shows that CopyOnWriteArrayList iterator does not support remove().
     */
    public static void attemptRemoveOnCopyOnWriteIterator() {
        CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>(List.of("a", "b", "c"));
        Iterator<String> it = cowList.iterator();
        it.next();
        it.remove(); // Throws UnsupportedOperationException
    }

    /**
     * Demonstrates ConcurrentHashMap weakly consistent iteration.
     * Unlike CopyOnWriteArrayList, ConcurrentHashMap may reflect some modifications
     * made during iteration (weakly consistent, not snapshot-based).
     */
    public static Map<String, String> iterateAndModifyConcurrentMap(Map<String, String> initialEntries) {
        ConcurrentHashMap<String, String> concMap = new ConcurrentHashMap<>(initialEntries);
        Map<String, String> iteratedEntries = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : concMap.entrySet()) {
            iteratedEntries.put(entry.getKey(), entry.getValue());
            // This does NOT throw ConcurrentModificationException
            concMap.put("addedKey_" + entry.getKey(), "addedValue");
        }
        return iteratedEntries;
    }

    /**
     * Demonstrates ConcurrentSkipListSet safe iteration.
     */
    public static List<Integer> iterateAndModifyConcurrentSkipListSet(Collection<Integer> initialElements) {
        ConcurrentSkipListSet<Integer> skipSet = new ConcurrentSkipListSet<>(initialElements);
        List<Integer> iteratedElements = new ArrayList<>();

        for (Integer item : skipSet) {
            iteratedElements.add(item);
            skipSet.add(item + 1000); // Safe - no exception
        }
        return iteratedElements;
    }

    /**
     * Demonstrates ConcurrentLinkedQueue safe iteration.
     */
    public static List<String> iterateAndModifyConcurrentQueue(Collection<String> initialElements) {
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>(initialElements);
        List<String> iteratedElements = new ArrayList<>();

        for (String item : queue) {
            iteratedElements.add(item);
            if (iteratedElements.size() == 1) {
                queue.add("ADDED");
            }
        }
        return iteratedElements;
    }

    /**
     * Demonstrates that Collections.synchronizedList still uses fail-fast iterators.
     * Synchronization wrapper does NOT make iterators fail-safe.
     */
    public static void modifySynchronizedListDuringIteration(List<String> elements) {
        List<String> syncList = Collections.synchronizedList(new ArrayList<>(elements));
        for (String item : syncList) {
            if (item.equals(elements.get(0))) {
                syncList.remove(item); // Still throws ConcurrentModificationException!
            }
        }
    }

    /**
     * Compares the behavior of HashMap (fail-fast) vs ConcurrentHashMap (fail-safe)
     * when removing during iteration.
     */
    public static Map<String, Integer> safeRemoveFromConcurrentMap(Map<String, Integer> initial, int threshold) {
        ConcurrentHashMap<String, Integer> concMap = new ConcurrentHashMap<>(initial);
        // Safe to remove during iteration with ConcurrentHashMap
        for (Map.Entry<String, Integer> entry : concMap.entrySet()) {
            if (entry.getValue() < threshold) {
                concMap.remove(entry.getKey()); // No exception
            }
        }
        return new HashMap<>(concMap);
    }

    /**
     * Demonstrates CopyOnWriteArraySet behavior during iteration.
     */
    public static Set<String> iterateAndModifyCopyOnWriteSet(Set<String> initialElements) {
        CopyOnWriteArraySet<String> cowSet = new CopyOnWriteArraySet<>(initialElements);
        Set<String> iteratedElements = new LinkedHashSet<>();

        for (String item : cowSet) {
            iteratedElements.add(item);
            cowSet.add("NEW_" + item); // No exception; not visible to current iterator
        }
        return iteratedElements;
    }
}
