package com.github.msorkhpar.claudejavatutor.concurrentcollections;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Demonstrates iteration behavior and ordering guarantees in concurrent vs non-concurrent collections.
 * Covers fail-fast behavior, ConcurrentModificationException, and concurrent collection iteration.
 */
public class IterationBehavior {

    /**
     * Demonstrates fail-fast behavior of non-concurrent collections.
     */
    public static class FailFastExamples {

        /**
         * Demonstrates ConcurrentModificationException when modifying ArrayList during iteration.
         */
        public boolean arrayListFailFast() {
            List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
            try {
                for (String s : list) {
                    if ("b".equals(s)) {
                        list.remove(s); // Structural modification during iteration
                    }
                }
                return false; // No exception thrown (unlikely but possible if last element)
            } catch (ConcurrentModificationException e) {
                return true;
            }
        }

        /**
         * Demonstrates ConcurrentModificationException with HashMap.
         */
        public boolean hashMapFailFast() {
            Map<String, Integer> map = new HashMap<>();
            map.put("a", 1);
            map.put("b", 2);
            map.put("c", 3);

            try {
                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    if ("b".equals(entry.getKey())) {
                        map.remove("b"); // Structural modification during iteration
                    }
                }
                return false;
            } catch (ConcurrentModificationException e) {
                return true;
            }
        }

        /**
         * Demonstrates safe removal using Iterator.remove().
         */
        public List<String> safeRemovalWithIterator(List<String> elements, String toRemove) {
            List<String> list = new ArrayList<>(elements);
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                if (it.next().equals(toRemove)) {
                    it.remove(); // Safe removal through iterator
                }
            }
            return list;
        }

        /**
         * Demonstrates safe removal using removeIf (Java 8+).
         */
        public List<String> safeRemovalWithRemoveIf(List<String> elements, String toRemove) {
            List<String> list = new ArrayList<>(elements);
            list.removeIf(s -> s.equals(toRemove));
            return list;
        }

        /**
         * Demonstrates that HashSet also exhibits fail-fast behavior.
         */
        public boolean hashSetFailFast() {
            Set<String> set = new HashSet<>(Arrays.asList("a", "b", "c", "d"));
            try {
                for (String s : set) {
                    if ("b".equals(s)) {
                        set.add("e"); // Structural modification during iteration
                    }
                }
                return false;
            } catch (ConcurrentModificationException e) {
                return true;
            }
        }
    }

    /**
     * Demonstrates weakly consistent iteration of concurrent collections.
     */
    public static class WeaklyConsistentExamples {

        /**
         * CopyOnWriteArrayList: iterates over snapshot, no ConcurrentModificationException.
         */
        public List<String> copyOnWriteIteration() {
            CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>(
                    Arrays.asList("a", "b", "c")
            );
            List<String> result = new ArrayList<>();
            for (String s : list) {
                result.add(s);
                if ("a".equals(s)) {
                    list.add("d"); // Modification not visible to current iterator
                }
            }
            return result;
        }

        /**
         * Returns the list after modification to show the new element was added.
         */
        public CopyOnWriteArrayList<String> copyOnWriteAfterIteration() {
            CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>(
                    Arrays.asList("a", "b", "c")
            );
            for (String s : list) {
                if ("a".equals(s)) {
                    list.add("d");
                }
            }
            return list;
        }

        /**
         * ConcurrentHashMap: weakly consistent iteration, reflects some but not all changes.
         */
        public Map<String, Integer> concurrentHashMapIteration() {
            ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
            map.put("a", 1);
            map.put("b", 2);
            map.put("c", 3);

            Map<String, Integer> result = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
                // No ConcurrentModificationException
                map.put("z", 99);
            }
            return result;
        }

        /**
         * ConcurrentSkipListSet: maintains sorted order during concurrent modification.
         */
        public List<String> concurrentSkipListSetIteration() {
            ConcurrentSkipListSet<String> set = new ConcurrentSkipListSet<>(
                    Arrays.asList("b", "d", "f")
            );
            List<String> result = new ArrayList<>();
            for (String s : set) {
                result.add(s);
                // Safe to modify during iteration
                set.add("a");
            }
            return result;
        }

        /**
         * Demonstrates that CopyOnWriteArrayList iterator does NOT support remove().
         */
        public boolean cowIteratorRemoveThrows() {
            CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>(
                    Arrays.asList("a", "b", "c")
            );
            try {
                Iterator<String> it = list.iterator();
                it.next();
                it.remove(); // Throws UnsupportedOperationException
                return false;
            } catch (UnsupportedOperationException e) {
                return true;
            }
        }
    }

    /**
     * Demonstrates safe approaches to modifying collections during iteration.
     */
    public static class SafeModificationPatterns {

        /**
         * Copy-then-modify pattern: iterate a copy, modify the original.
         */
        public List<String> copyThenModify(List<String> original, String toRemove) {
            List<String> list = new ArrayList<>(original);
            List<String> toRemoveList = new ArrayList<>();
            for (String s : list) {
                if (s.equals(toRemove)) {
                    toRemoveList.add(s);
                }
            }
            list.removeAll(toRemoveList);
            return list;
        }

        /**
         * Stream-based filtering: no modification of original collection.
         */
        public List<String> streamFilter(List<String> original, String toExclude) {
            return original.stream()
                    .filter(s -> !s.equals(toExclude))
                    .toList();
        }

        /**
         * Using ConcurrentHashMap.entrySet() with removeIf for safe concurrent removal.
         */
        public ConcurrentHashMap<String, Integer> concurrentMapRemoveIf(
                ConcurrentHashMap<String, Integer> map, int threshold) {
            map.entrySet().removeIf(entry -> entry.getValue() < threshold);
            return map;
        }
    }
}
