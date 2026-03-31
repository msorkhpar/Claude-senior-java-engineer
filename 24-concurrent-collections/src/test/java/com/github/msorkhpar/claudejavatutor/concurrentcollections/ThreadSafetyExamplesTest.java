package com.github.msorkhpar.claudejavatutor.concurrentcollections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Thread Safety Examples Tests")
class ThreadSafetyExamplesTest {

    @Nested
    @DisplayName("Synchronization Wrappers")
    class SynchronizationWrappersTest {

        private final ThreadSafetyExamples.SynchronizationWrappers wrappers =
                new ThreadSafetyExamples.SynchronizationWrappers();

        @Test
        @DisplayName("Should create synchronized list with elements")
        void testCreateSynchronizedList() {
            List<String> list = wrappers.createSynchronizedList("a", "b", "c");

            assertThat(list).hasSize(3);
            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Synchronized list should support basic operations")
        void testSynchronizedListOperations() {
            List<String> list = wrappers.createSynchronizedList("a", "b");

            list.add("c");
            list.remove("a");

            assertThat(list).containsExactly("b", "c");
        }

        @Test
        @DisplayName("Should create synchronized map")
        void testCreateSynchronizedMap() {
            Map<String, Integer> map = wrappers.createSynchronizedMap();

            map.put("key", 42);
            assertThat(map.get("key")).isEqualTo(42);
        }

        @Test
        @DisplayName("Should create synchronized set")
        void testCreateSynchronizedSet() {
            Set<String> set = wrappers.createSynchronizedSet("x", "y", "z");

            assertThat(set).hasSize(3);
            assertThat(set).containsExactlyInAnyOrder("x", "y", "z");
        }

        @Test
        @DisplayName("Should iterate safely with manual synchronization")
        void testSafeIteration() {
            List<String> list = wrappers.createSynchronizedList("hello", "world");

            List<String> result = wrappers.safeIteration(list);

            assertThat(result).containsExactly("HELLO", "WORLD");
        }

        @Test
        @DisplayName("Should handle empty list in safe iteration")
        void testSafeIterationEmpty() {
            List<String> list = wrappers.createSynchronizedList();

            List<String> result = wrappers.safeIteration(list);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Unsafe iteration can throw ConcurrentModificationException")
        void testUnsafeIterationCanFail() {
            boolean exceptionThrown = wrappers.unsafeIterationCanFail();

            assertThat(exceptionThrown).isTrue();
        }
    }

    @Nested
    @DisplayName("Concurrent vs Synchronized")
    class ConcurrentVsSynchronizedTest {

        private final ThreadSafetyExamples.ConcurrentVsSynchronized concurrent =
                new ThreadSafetyExamples.ConcurrentVsSynchronized();

        @Test
        @DisplayName("Should count words using ConcurrentHashMap with AtomicInteger")
        void testConcurrentWordCounter() {
            ConcurrentHashMap<String, AtomicInteger> counter = concurrent.concurrentWordCounter();

            concurrent.countWord(counter, "hello");
            concurrent.countWord(counter, "hello");
            concurrent.countWord(counter, "world");

            assertThat(counter.get("hello").get()).isEqualTo(2);
            assertThat(counter.get("world").get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should create listener registry with CopyOnWriteArrayList")
        void testCreateListenerRegistry() {
            CopyOnWriteArrayList<String> registry = concurrent.createListenerRegistry("listener1", "listener2");

            assertThat(registry).containsExactly("listener1", "listener2");
        }

        @Test
        @DisplayName("Should safely iterate CopyOnWriteArrayList")
        void testSafeIterationDuringModification() {
            CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>(
                    Arrays.asList("a", "b", "c")
            );

            List<String> snapshot = concurrent.safeIterationDuringModification(list);

            assertThat(snapshot).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should atomically put if absent - new key")
        void testAtomicPutIfAbsentNew() {
            ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

            int result = concurrent.atomicPutIfAbsent(map, "key", 42);

            assertThat(result).isEqualTo(42);
            assertThat(map.get("key")).isEqualTo(42);
        }

        @Test
        @DisplayName("Should atomically put if absent - existing key returns existing value")
        void testAtomicPutIfAbsentExisting() {
            ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
            map.put("key", 100);

            int result = concurrent.atomicPutIfAbsent(map, "key", 42);

            assertThat(result).isEqualTo(100);
            assertThat(map.get("key")).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Concurrent Access Patterns")
    class ConcurrentAccessPatternsTest {

        private final ThreadSafetyExamples.ConcurrentAccessPatterns patterns =
                new ThreadSafetyExamples.ConcurrentAccessPatterns();

        @Test
        @DisplayName("Unsafe counter may lose updates due to race conditions")
        void testUnsafeCounter() throws InterruptedException {
            int threadCount = 4;
            int incrementsPerThread = 1000;
            int expectedTotal = threadCount * incrementsPerThread;

            Map<String, Integer> result = patterns.unsafeCounter(threadCount, incrementsPerThread);

            // Due to race conditions, the counter is likely less than expected
            // (but could occasionally equal it due to timing)
            assertThat(result.get("counter")).isLessThanOrEqualTo(expectedTotal);
        }

        @Test
        @DisplayName("Safe counter preserves all updates using ConcurrentHashMap.merge()")
        void testSafeCounter() throws InterruptedException {
            int threadCount = 4;
            int incrementsPerThread = 1000;
            int expectedTotal = threadCount * incrementsPerThread;

            ConcurrentHashMap<String, Integer> result = patterns.safeCounter(threadCount, incrementsPerThread);

            assertThat(result.get("counter")).isEqualTo(expectedTotal);
        }

        @Test
        @DisplayName("CopyOnWriteArrayList preserves all concurrent additions")
        void testSafeConcurrentAdd() throws InterruptedException {
            int threadCount = 4;
            int addsPerThread = 100;
            int expectedTotal = threadCount * addsPerThread;

            CopyOnWriteArrayList<Integer> result = patterns.safeConcurrentAdd(threadCount, addsPerThread);

            assertThat(result).hasSize(expectedTotal);
        }

        @Test
        @DisplayName("Safe counter with single thread should be exact")
        void testSafeCounterSingleThread() throws InterruptedException {
            ConcurrentHashMap<String, Integer> result = patterns.safeCounter(1, 500);

            assertThat(result.get("counter")).isEqualTo(500);
        }
    }
}
