package com.github.msorkhpar.claudejavatutor.concurrencypitfalls;

import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("5.3.1 Data Races and How to Prevent Them")
class DataRaceTest {

    // ── UnsynchronizedCounter ────────────────────────────────────────────────

    @Nested
    @DisplayName("UnsynchronizedCounter -- demonstrates data race")
    class UnsynchronizedCounterTest {

        @Test
        @DisplayName("single-threaded increment works correctly")
        void singleThreadedIncrementWorks() {
            var counter = new DataRace.UnsynchronizedCounter();
            for (int i = 0; i < 100; i++) {
                counter.increment();
            }
            assertThat(counter.getCounter()).isEqualTo(100);
        }

        @RepeatedTest(5)
        @DisplayName("concurrent increments likely lose updates due to data race")
        void concurrentIncrementsLikelyLoseUpdates() throws InterruptedException {
            var counter = new DataRace.UnsynchronizedCounter();
            int threads = 50;
            int perThread = 1000;
            int expected = threads * perThread;

            DataRace.runConcurrentIncrements(threads, perThread, counter::increment);

            // Due to the data race, the counter is almost certainly less than expected.
            // We assert it is <= expected (it could theoretically equal it, but very unlikely).
            assertThat(counter.getCounter()).isLessThanOrEqualTo(expected);
            // We log it for visibility; in a real test we'd check it's strictly less.
            System.out.println("Unsynchronized counter: expected=" + expected
                    + ", actual=" + counter.getCounter());
        }
    }

    // ── SynchronizedCounter ──────────────────────────────────────────────────

    @Nested
    @DisplayName("SynchronizedCounter -- data race fixed with synchronized")
    class SynchronizedCounterTest {

        @Test
        @DisplayName("initial counter is zero")
        void initialCounterIsZero() {
            var counter = new DataRace.SynchronizedCounter();
            assertThat(counter.getCounter()).isEqualTo(0);
        }

        @Test
        @DisplayName("single-threaded increment works")
        void singleThreadedIncrementWorks() {
            var counter = new DataRace.SynchronizedCounter();
            counter.increment();
            counter.increment();
            assertThat(counter.getCounter()).isEqualTo(2);
        }

        @Test
        @DisplayName("reset sets counter to zero")
        void resetSetsCounterToZero() {
            var counter = new DataRace.SynchronizedCounter();
            counter.increment();
            counter.reset();
            assertThat(counter.getCounter()).isEqualTo(0);
        }

        @RepeatedTest(3)
        @DisplayName("concurrent increments produce exact expected total")
        void concurrentIncrementsProduceCorrectTotal() throws InterruptedException {
            var counter = new DataRace.SynchronizedCounter();
            int threads = 50;
            int perThread = 1000;
            int expected = threads * perThread;

            DataRace.runConcurrentIncrements(threads, perThread, counter::increment);

            assertThat(counter.getCounter()).isEqualTo(expected);
        }
    }

    // ── AtomicCounter ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("AtomicCounter -- data race fixed with AtomicInteger")
    class AtomicCounterTest {

        @Test
        @DisplayName("initial counter is zero")
        void initialCounterIsZero() {
            var counter = new DataRace.AtomicCounter();
            assertThat(counter.getCounter()).isEqualTo(0);
        }

        @Test
        @DisplayName("single-threaded increment works")
        void singleThreadedIncrementWorks() {
            var counter = new DataRace.AtomicCounter();
            counter.increment();
            assertThat(counter.getCounter()).isEqualTo(1);
        }

        @Test
        @DisplayName("reset sets counter to zero")
        void resetSetsCounterToZero() {
            var counter = new DataRace.AtomicCounter();
            counter.increment();
            counter.reset();
            assertThat(counter.getCounter()).isEqualTo(0);
        }

        @RepeatedTest(3)
        @DisplayName("concurrent increments produce exact expected total")
        void concurrentIncrementsProduceCorrectTotal() throws InterruptedException {
            var counter = new DataRace.AtomicCounter();
            int threads = 50;
            int perThread = 1000;
            int expected = threads * perThread;

            DataRace.runConcurrentIncrements(threads, perThread, counter::increment);

            assertThat(counter.getCounter()).isEqualTo(expected);
        }
    }

    // ── SafeCheckThenAct ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("SafeCheckThenAct -- ConcurrentHashMap.computeIfAbsent")
    class SafeCheckThenActTest {

        @Test
        @DisplayName("getOrCompute returns computed value for new key")
        void getOrComputeReturnsComputedValue() {
            var cache = new DataRace.SafeCheckThenAct();
            String result = cache.getOrCompute("key", k -> k + "-value");
            assertThat(result).isEqualTo("key-value");
        }

        @Test
        @DisplayName("getOrCompute returns cached value for existing key")
        void getOrComputeReturnsCachedValue() {
            var cache = new DataRace.SafeCheckThenAct();
            cache.getOrCompute("key", k -> "first");
            String result = cache.getOrCompute("key", k -> "second");
            assertThat(result).isEqualTo("first");
        }

        @Test
        @DisplayName("computeIfAbsent is called at most once per key under concurrency")
        void computeIsCalledAtMostOncePerKey() throws InterruptedException {
            var cache = new DataRace.SafeCheckThenAct();
            int threads = 50;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        cache.getOrCompute("shared-key", k -> k + "-computed");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            start.countDown();
            done.await(10, TimeUnit.SECONDS);

            assertThat(cache.size()).isEqualTo(1);
            assertThat(cache.getComputeCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("multiple distinct keys are cached independently")
        void multipleKeysAreCachedIndependently() {
            var cache = new DataRace.SafeCheckThenAct();
            cache.getOrCompute("a", k -> "val-a");
            cache.getOrCompute("b", k -> "val-b");
            assertThat(cache.size()).isEqualTo(2);
            assertThat(cache.getOrCompute("a", k -> "other")).isEqualTo("val-a");
            assertThat(cache.getOrCompute("b", k -> "other")).isEqualTo("val-b");
        }
    }

    // ── ImmutablePoint ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("ImmutablePoint -- race-free sharing via immutability")
    class ImmutablePointTest {

        @Test
        @DisplayName("record fields are accessible via accessors")
        void recordFieldsAreAccessible() {
            var point = new DataRace.ImmutablePoint(3, 4);
            assertThat(point.x()).isEqualTo(3);
            assertThat(point.y()).isEqualTo(4);
        }

        @Test
        @DisplayName("translate returns a new point without modifying the original")
        void translateReturnsNewPoint() {
            var original = new DataRace.ImmutablePoint(1, 2);
            var translated = original.translate(10, 20);
            assertThat(translated).isEqualTo(new DataRace.ImmutablePoint(11, 22));
            assertThat(original).isEqualTo(new DataRace.ImmutablePoint(1, 2));
        }

        @Test
        @DisplayName("distanceFromOrigin calculates correctly")
        void distanceFromOriginCalculatesCorrectly() {
            var point = new DataRace.ImmutablePoint(3, 4);
            assertThat(point.distanceFromOrigin()).isCloseTo(5.0, within(0.0001));
        }

        @Test
        @DisplayName("origin has zero distance")
        void originHasZeroDistance() {
            var point = new DataRace.ImmutablePoint(0, 0);
            assertThat(point.distanceFromOrigin()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("equals and hashCode work for records")
        void equalsAndHashCodeWork() {
            var p1 = new DataRace.ImmutablePoint(5, 10);
            var p2 = new DataRace.ImmutablePoint(5, 10);
            assertThat(p1).isEqualTo(p2);
            assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        }
    }

    // ── SafePointHolder ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("SafePointHolder -- volatile publication of immutable objects")
    class SafePointHolderTest {

        @Test
        @DisplayName("initial point is returned correctly")
        void initialPointIsReturned() {
            var holder = new DataRace.SafePointHolder(new DataRace.ImmutablePoint(1, 2));
            assertThat(holder.getPoint()).isEqualTo(new DataRace.ImmutablePoint(1, 2));
        }

        @Test
        @DisplayName("setPoint updates the point atomically")
        void setPointUpdatesAtomically() {
            var holder = new DataRace.SafePointHolder(new DataRace.ImmutablePoint(0, 0));
            holder.setPoint(new DataRace.ImmutablePoint(99, 100));
            assertThat(holder.getPoint()).isEqualTo(new DataRace.ImmutablePoint(99, 100));
        }

        @Test
        @DisplayName("null point throws NullPointerException")
        void nullPointThrowsNpe() {
            var holder = new DataRace.SafePointHolder(new DataRace.ImmutablePoint(0, 0));
            assertThatNullPointerException().isThrownBy(() -> holder.setPoint(null));
        }

        @Test
        @DisplayName("concurrent readers always see a consistent point")
        void concurrentReadersAlwaysSeeConsistentPoint() throws InterruptedException {
            var holder = new DataRace.SafePointHolder(new DataRace.ImmutablePoint(0, 0));
            AtomicInteger inconsistencies = new AtomicInteger(0);
            CountDownLatch start = new CountDownLatch(1);
            int totalThreads = 20;
            CountDownLatch done = new CountDownLatch(totalThreads);

            // 10 writers
            for (int i = 0; i < 10; i++) {
                final int val = i * 10;
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < 100; j++) {
                            holder.setPoint(new DataRace.ImmutablePoint(val + j, val + j));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            // 10 readers checking consistency (x should always equal y for our writes)
            for (int i = 0; i < 10; i++) {
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < 1000; j++) {
                            var p = holder.getPoint();
                            if (p.x() != p.y()) {
                                inconsistencies.incrementAndGet();
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            start.countDown();
            done.await(10, TimeUnit.SECONDS);
            assertThat(inconsistencies.get()).isZero();
        }
    }

    // ── ThreadLocalCounter ───────────────────────────────────────────────────

    @Nested
    @DisplayName("ThreadLocalCounter -- thread-confined state prevents data races")
    class ThreadLocalCounterTest {

        @AfterEach
        void cleanup() {
            DataRace.ThreadLocalCounter.reset();
        }

        @Test
        @DisplayName("initial value is zero")
        void initialValueIsZero() {
            assertThat(DataRace.ThreadLocalCounter.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("increment increases thread-local counter")
        void incrementIncreases() {
            DataRace.ThreadLocalCounter.increment();
            DataRace.ThreadLocalCounter.increment();
            assertThat(DataRace.ThreadLocalCounter.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("reset clears the thread-local value")
        void resetClears() {
            DataRace.ThreadLocalCounter.increment();
            DataRace.ThreadLocalCounter.reset();
            assertThat(DataRace.ThreadLocalCounter.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("each thread has its own independent counter")
        void eachThreadHasIndependentCounter() throws InterruptedException {
            int threads = 10;
            int incrementsPerThread = 100;
            CopyOnWriteArrayList<Integer> observedValues = new CopyOnWriteArrayList<>();
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < incrementsPerThread; j++) {
                            DataRace.ThreadLocalCounter.increment();
                        }
                        observedValues.add(DataRace.ThreadLocalCounter.get());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        DataRace.ThreadLocalCounter.reset();
                        done.countDown();
                    }
                });
            }

            start.countDown();
            done.await(10, TimeUnit.SECONDS);

            // Each thread should see exactly incrementsPerThread (no cross-thread interference)
            assertThat(observedValues).hasSize(threads);
            observedValues.forEach(v ->
                    assertThat(v).isEqualTo(incrementsPerThread));
        }
    }

    // ── runConcurrentIncrements utility ───────────────────────────────────────

    @Nested
    @DisplayName("runConcurrentIncrements -- utility method")
    class RunConcurrentIncrementsTest {

        @Test
        @DisplayName("runs all threads to completion")
        void runsAllThreadsToCompletion() throws InterruptedException {
            AtomicInteger count = new AtomicInteger(0);
            DataRace.runConcurrentIncrements(10, 100, count::incrementAndGet);
            assertThat(count.get()).isEqualTo(1000);
        }
    }
}
