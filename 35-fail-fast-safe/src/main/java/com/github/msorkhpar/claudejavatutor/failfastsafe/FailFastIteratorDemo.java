package com.github.msorkhpar.claudejavatutor.failfastsafe;

import java.util.*;

/**
 * Demonstrates Fail-Fast iterator behavior in Java collections.
 * Fail-Fast iterators throw ConcurrentModificationException when the underlying
 * collection is structurally modified during iteration (outside of the iterator's own methods).
 *
 * <p>Fail-Fast iterators use an internal modification counter (modCount) to detect
 * structural changes. This is a "best-effort" mechanism and is not guaranteed under
 * concurrent access from multiple threads.</p>
 */
public class FailFastIteratorDemo {

    /**
     * Demonstrates that modifying an ArrayList during iteration with for-each
     * throws ConcurrentModificationException.
     */
    public static void modifyListDuringForEach(List<String> list, String toRemove) {
        for (String item : list) {
            if (item.equals(toRemove)) {
                list.remove(item); // Throws ConcurrentModificationException
            }
        }
    }

    /**
     * Demonstrates that modifying a HashMap during iteration with for-each
     * throws ConcurrentModificationException.
     */
    public static void modifyMapDuringForEach(Map<String, Integer> map, String keyToRemove) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getKey().equals(keyToRemove)) {
                map.remove(entry.getKey()); // Throws ConcurrentModificationException
            }
        }
    }

    /**
     * Demonstrates that modifying a HashSet during iteration
     * throws ConcurrentModificationException.
     */
    public static void modifySetDuringIteration(Set<Integer> set, int threshold) {
        Iterator<Integer> it = set.iterator();
        while (it.hasNext()) {
            int value = it.next();
            if (value > threshold) {
                set.add(value * 2); // Throws ConcurrentModificationException
            }
        }
    }

    /**
     * Safe removal using Iterator.remove() -- the correct way to remove during iteration.
     */
    public static <T> List<T> safeRemoveWithIterator(List<T> list, T toRemove) {
        List<T> result = new ArrayList<>(list);
        Iterator<T> it = result.iterator();
        while (it.hasNext()) {
            if (it.next().equals(toRemove)) {
                it.remove(); // Safe -- uses iterator's own remove method
            }
        }
        return result;
    }

    /**
     * Safe removal using removeIf (Java 8+) -- the modern, preferred approach.
     */
    public static <T> List<T> safeRemoveWithRemoveIf(List<T> list, T toRemove) {
        List<T> result = new ArrayList<>(list);
        result.removeIf(item -> item.equals(toRemove));
        return result;
    }

    /**
     * Safe removal by collecting items to remove first, then removing.
     */
    public static <T> List<T> safeRemoveWithSeparateCollection(List<T> list, T toRemove) {
        List<T> result = new ArrayList<>(list);
        List<T> toRemoveList = new ArrayList<>();
        for (T item : result) {
            if (item.equals(toRemove)) {
                toRemoveList.add(item);
            }
        }
        result.removeAll(toRemoveList);
        return result;
    }

    /**
     * Demonstrates fail-fast behavior with ListIterator during add operation.
     */
    public static void modifyListWhileListIterating(List<String> list) {
        ListIterator<String> listIt = list.listIterator();
        while (listIt.hasNext()) {
            listIt.next();
            list.add("new"); // Throws ConcurrentModificationException
        }
    }

    /**
     * Safe addition during iteration using ListIterator.add().
     */
    public static List<String> safeAddWithListIterator(List<String> original, String suffix) {
        List<String> result = new ArrayList<>(original);
        ListIterator<String> listIt = result.listIterator();
        while (listIt.hasNext()) {
            String current = listIt.next();
            listIt.add(current + suffix); // Safe -- uses ListIterator's own add
        }
        return result;
    }

    /**
     * Demonstrates that reading (get) operations do not trigger fail-fast behavior.
     * Only structural modifications (add, remove, clear) trigger it.
     */
    public static List<String> readDuringIteration(List<String> list) {
        List<String> results = new ArrayList<>();
        for (String item : list) {
            // Accessing elements by index is safe during iteration
            // as long as you don't structurally modify the collection
            results.add(item + "-" + list.size());
        }
        return results;
    }
}
