package com.github.msorkhpar.claudejavatutor.concurrentcollections;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates thread safety of common data structures.
 * Covers synchronization wrappers and concurrent collections.
 */
public class ThreadSafetyExamples {

    /**
     * Demonstrates synchronization wrappers (Collections.synchronizedXXX()).
     */
    public static class SynchronizationWrappers {

        /**
         * Creates a synchronized list wrapper around an ArrayList.
         */
        public List<String> createSynchronizedList(String... elements) {
            List<String> list = new ArrayList<>(Arrays.asList(elements));
            return Collections.synchronizedList(list);
        }

        /**
         * Creates a synchronized map wrapper around a HashMap.
         */
        public Map<String, Integer> createSynchronizedMap() {
            Map<String, Integer> map = new HashMap<>();
            return Collections.synchronizedMap(map);
        }

        /**
         * Creates a synchronized set wrapper around a HashSet.
         */
        public Set<String> createSynchronizedSet(String... elements) {
            Set<String> set = new HashSet<>(Arrays.asList(elements));
            return Collections.synchronizedSet(set);
        }

        /**
         * Demonstrates safe iteration of a synchronized list (requires manual synchronization).
         */
        public List<String> safeIteration(List<String> syncList) {
            List<String> result = new ArrayList<>();
            synchronized (syncList) {
                for (String s : syncList) {
                    result.add(s.toUpperCase());
                }
            }
            return result;
        }

        /**
         * Demonstrates that synchronized wrapper iteration without explicit sync
         * can still throw ConcurrentModificationException.
         */
        public boolean unsafeIterationCanFail() {
            List<String> syncList = Collections.synchronizedList(new ArrayList<>());
            syncList.add("a");
            syncList.add("b");
            syncList.add("c");

            try {
                // Simulate concurrent modification scenario
                Iterator<String> it = syncList.iterator();
                if (it.hasNext()) {
                    it.next();
                    syncList.add("d"); // Modification outside iterator
                    it.next(); // Potential ConcurrentModificationException
                }
                return false;
            } catch (ConcurrentModificationException e) {
                return true;
            }
        }
    }

    /**
     * Demonstrates concurrent collections vs. synchronized wrappers.
     */
    public static class ConcurrentVsSynchronized {

        /**
         * Uses ConcurrentHashMap for thread-safe map operations.
         */
        public ConcurrentHashMap<String, AtomicInteger> concurrentWordCounter() {
            return new ConcurrentHashMap<>();
        }

        /**
         * Adds a word count using atomic operations on ConcurrentHashMap.
         */
        public void countWord(ConcurrentHashMap<String, AtomicInteger> counter, String word) {
            counter.computeIfAbsent(word, k -> new AtomicInteger(0)).incrementAndGet();
        }

        /**
         * Uses CopyOnWriteArrayList for a thread-safe listener registry.
         */
        public CopyOnWriteArrayList<String> createListenerRegistry(String... listeners) {
            return new CopyOnWriteArrayList<>(listeners);
        }

        /**
         * Demonstrates safe iteration during modification with CopyOnWriteArrayList.
         */
        public List<String> safeIterationDuringModification(CopyOnWriteArrayList<String> list) {
            List<String> snapshot = new ArrayList<>();
            for (String s : list) {
                snapshot.add(s);
            }
            return snapshot;
        }

        /**
         * Demonstrates that ConcurrentHashMap operations are atomic.
         */
        public int atomicPutIfAbsent(ConcurrentHashMap<String, Integer> map, String key, int value) {
            Integer existing = map.putIfAbsent(key, value);
            return existing != null ? existing : value;
        }
    }

    /**
     * Demonstrates concurrent access patterns and race condition avoidance.
     */
    public static class ConcurrentAccessPatterns {

        /**
         * Demonstrates a race condition with a non-thread-safe HashMap.
         * Multiple threads incrementing counters can lose updates.
         */
        public Map<String, Integer> unsafeCounter(int threadCount, int incrementsPerThread)
                throws InterruptedException {
            Map<String, Integer> map = new HashMap<>();
            map.put("counter", 0);

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        // Race condition: read-modify-write is not atomic
                        int current = map.get("counter");
                        map.put("counter", current + 1);
                    }
                    latch.countDown();
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();
            return map;
        }

        /**
         * Demonstrates thread-safe counter using ConcurrentHashMap.merge().
         */
        public ConcurrentHashMap<String, Integer> safeCounter(int threadCount, int incrementsPerThread)
                throws InterruptedException {
            ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
            map.put("counter", 0);

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        map.merge("counter", 1, Integer::sum);
                    }
                    latch.countDown();
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();
            return map;
        }

        /**
         * Demonstrates thread-safe list operations with CopyOnWriteArrayList.
         */
        public CopyOnWriteArrayList<Integer> safeConcurrentAdd(int threadCount, int addsPerThread)
                throws InterruptedException {
            CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int t = 0; t < threadCount; t++) {
                final int threadId = t;
                executor.submit(() -> {
                    for (int j = 0; j < addsPerThread; j++) {
                        list.add(threadId * 1000 + j);
                    }
                    latch.countDown();
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();
            return list;
        }
    }
}
