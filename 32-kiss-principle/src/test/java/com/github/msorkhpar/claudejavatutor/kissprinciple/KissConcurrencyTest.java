package com.github.msorkhpar.claudejavatutor.kissprinciple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("KISS in Concurrent Programming Tests")
class KissConcurrencyTest {

    @Nested
    @DisplayName("Simple vs Complex Counter")
    class SimpleVsComplexCounterTest {

        @Test
        @DisplayName("Simple atomic counter should increment correctly")
        void testSimpleCounterIncrement() {
            var counter = new KissConcurrency.SimpleAtomicCounter();

            counter.increment();
            counter.increment();
            counter.increment();

            assertThat(counter.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("Simple atomic counter should start at zero")
        void testSimpleCounterStartsAtZero() {
            var counter = new KissConcurrency.SimpleAtomicCounter();

            assertThat(counter.get()).isZero();
        }

        @Test
        @DisplayName("Simple atomic counter should decrement correctly")
        void testSimpleCounterDecrement() {
            var counter = new KissConcurrency.SimpleAtomicCounter();

            counter.increment();
            counter.increment();
            counter.decrement();

            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Simple atomic counter should be thread-safe")
        void testSimpleCounterThreadSafety() throws InterruptedException {
            var counter = new KissConcurrency.SimpleAtomicCounter();
            int threads = 10;
            int incrementsPerThread = 1000;
            var latch = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                Thread.ofVirtual().start(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        counter.increment();
                    }
                    latch.countDown();
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            assertThat(counter.get()).isEqualTo(threads * incrementsPerThread);
        }

        @Test
        @DisplayName("Over-engineered counter should produce same results")
        void testOverEngineeredCounterSameResults() {
            var simple = new KissConcurrency.SimpleAtomicCounter();
            var complex = new KissConcurrency.OverEngineeredCounter();

            for (int i = 0; i < 5; i++) {
                simple.increment();
                complex.increment();
            }

            assertThat(simple.get()).isEqualTo(complex.get());
        }
    }

    @Nested
    @DisplayName("Simple vs Complex Task Execution")
    class SimpleVsComplexTaskExecutionTest {

        @Test
        @DisplayName("Simple executor should run tasks and collect results")
        void testSimpleExecutorRunsTasks() throws Exception {
            var executor = new KissConcurrency.SimpleTaskExecutor();
            List<Callable<String>> tasks = List.of(
                    () -> "result1",
                    () -> "result2",
                    () -> "result3"
            );

            List<String> results = executor.executeAll(tasks);

            assertThat(results).containsExactlyInAnyOrder("result1", "result2", "result3");
        }

        @Test
        @DisplayName("Simple executor should handle empty task list")
        void testSimpleExecutorEmptyTasks() throws Exception {
            var executor = new KissConcurrency.SimpleTaskExecutor();

            List<String> results = executor.executeAll(Collections.emptyList());

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Simple executor should propagate exceptions from tasks")
        void testSimpleExecutorPropagatesExceptions() {
            var executor = new KissConcurrency.SimpleTaskExecutor();
            List<Callable<String>> tasks = List.of(
                    () -> { throw new RuntimeException("task failed"); }
            );

            assertThatThrownBy(() -> executor.executeAll(tasks))
                    .isInstanceOf(ExecutionException.class);
        }
    }

    @Nested
    @DisplayName("Simple Producer-Consumer")
    class SimpleProducerConsumerTest {

        @Test
        @DisplayName("Should transfer items from producer to consumer")
        void testProducerConsumerTransfer() throws InterruptedException {
            var queue = new KissConcurrency.SimpleProducerConsumer<String>(10);
            var consumed = new CopyOnWriteArrayList<String>();

            Thread producer = Thread.ofVirtual().start(() -> {
                try {
                    queue.produce("item1");
                    queue.produce("item2");
                    queue.produce("item3");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            Thread consumer = Thread.ofVirtual().start(() -> {
                try {
                    for (int i = 0; i < 3; i++) {
                        consumed.add(queue.consume());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            producer.join(5000);
            consumer.join(5000);

            assertThat(consumed).containsExactly("item1", "item2", "item3");
        }

        @Test
        @DisplayName("Should respect bounded capacity")
        void testProducerConsumerBoundedCapacity() {
            var queue = new KissConcurrency.SimpleProducerConsumer<Integer>(2);

            assertThat(queue.size()).isZero();
        }
    }

    @Nested
    @DisplayName("Simple vs Complex Caching")
    class SimpleVsComplexCachingTest {

        @Test
        @DisplayName("Simple cache should store and retrieve values")
        void testSimpleCacheStoreAndRetrieve() {
            var cache = new KissConcurrency.SimpleConcurrentCache<String, Integer>();

            cache.put("one", 1);
            cache.put("two", 2);

            assertThat(cache.get("one")).isEqualTo(1);
            assertThat(cache.get("two")).isEqualTo(2);
        }

        @Test
        @DisplayName("Simple cache should return null for missing key")
        void testSimpleCacheReturnNullForMissing() {
            var cache = new KissConcurrency.SimpleConcurrentCache<String, Integer>();

            assertThat(cache.get("missing")).isNull();
        }

        @Test
        @DisplayName("Simple cache computeIfAbsent should compute and cache value")
        void testSimpleCacheComputeIfAbsent() {
            var cache = new KissConcurrency.SimpleConcurrentCache<String, Integer>();
            var computeCount = new AtomicInteger(0);

            Integer result1 = cache.computeIfAbsent("key", k -> {
                computeCount.incrementAndGet();
                return 42;
            });
            Integer result2 = cache.computeIfAbsent("key", k -> {
                computeCount.incrementAndGet();
                return 99;
            });

            assertThat(result1).isEqualTo(42);
            assertThat(result2).isEqualTo(42);
            assertThat(computeCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Simple cache should be thread-safe")
        void testSimpleCacheThreadSafety() throws InterruptedException {
            var cache = new KissConcurrency.SimpleConcurrentCache<Integer, String>();
            int threads = 10;
            var latch = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                final int idx = i;
                Thread.ofVirtual().start(() -> {
                    cache.put(idx, "value" + idx);
                    latch.countDown();
                });
            }

            latch.await(5, TimeUnit.SECONDS);

            for (int i = 0; i < threads; i++) {
                assertThat(cache.get(i)).isEqualTo("value" + i);
            }
        }

        @Test
        @DisplayName("Simple cache should support remove")
        void testSimpleCacheRemove() {
            var cache = new KissConcurrency.SimpleConcurrentCache<String, Integer>();

            cache.put("key", 42);
            assertThat(cache.get("key")).isEqualTo(42);

            cache.remove("key");
            assertThat(cache.get("key")).isNull();
        }
    }

    @Nested
    @DisplayName("Simple Readable Synchronization")
    class SimpleReadableSynchronizationTest {

        @Test
        @DisplayName("Simple synchronized list wrapper should add and get elements")
        void testSynchronizedListAddAndGet() {
            var list = new KissConcurrency.SimpleSynchronizedList<String>();

            list.add("first");
            list.add("second");

            assertThat(list.get(0)).isEqualTo("first");
            assertThat(list.get(1)).isEqualTo("second");
            assertThat(list.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("Simple synchronized list should be thread-safe for additions")
        void testSynchronizedListThreadSafe() throws InterruptedException {
            var list = new KissConcurrency.SimpleSynchronizedList<Integer>();
            int threads = 10;
            int additionsPerThread = 100;
            var latch = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                Thread.ofVirtual().start(() -> {
                    for (int j = 0; j < additionsPerThread; j++) {
                        list.add(j);
                    }
                    latch.countDown();
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            assertThat(list.size()).isEqualTo(threads * additionsPerThread);
        }

        @Test
        @DisplayName("Simple synchronized list should handle empty list")
        void testSynchronizedListEmpty() {
            var list = new KissConcurrency.SimpleSynchronizedList<String>();

            assertThat(list.size()).isZero();
            assertThat(list.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Simple synchronized list should throw on out-of-bounds index")
        void testSynchronizedListOutOfBounds() {
            var list = new KissConcurrency.SimpleSynchronizedList<String>();

            assertThatThrownBy(() -> list.get(0))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }
}
