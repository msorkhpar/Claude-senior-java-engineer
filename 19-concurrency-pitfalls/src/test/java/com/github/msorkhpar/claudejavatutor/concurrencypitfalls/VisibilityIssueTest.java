package com.github.msorkhpar.claudejavatutor.concurrencypitfalls;

import org.junit.jupiter.api.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;

@DisplayName("5.3.3 Visibility Issues and Proper Synchronization Techniques")
class VisibilityIssueTest {

    // ── NonVolatileFlag ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("NonVolatileFlag -- demonstrates potential visibility problem")
    class NonVolatileFlagTest {

        @Test
        @DisplayName("initial state is not stopped")
        void initialStateIsNotStopped() {
            var flag = new VisibilityIssue.NonVolatileFlag();
            assertThat(flag.isStopRequested()).isFalse();
        }

        @Test
        @DisplayName("requestStop sets the flag within the same thread")
        void requestStopSetsFlag() {
            var flag = new VisibilityIssue.NonVolatileFlag();
            flag.requestStop();
            assertThat(flag.isStopRequested()).isTrue();
        }

        @Test
        @DisplayName("spinUntilStop returns iterations when stop is pre-set")
        void spinUntilStopReturnsZeroWhenPreStopped() {
            var flag = new VisibilityIssue.NonVolatileFlag();
            flag.requestStop();
            long iterations = flag.spinUntilStop(1_000_000);
            assertThat(iterations).isEqualTo(0);
        }

        @Test
        @DisplayName("spinUntilStop respects maxIterations limit")
        void spinUntilStopRespectsMaxIterations() {
            var flag = new VisibilityIssue.NonVolatileFlag();
            long maxIter = 100;
            long iterations = flag.spinUntilStop(maxIter);
            assertThat(iterations).isEqualTo(maxIter);
        }
    }

    // ── VolatileFlag ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("VolatileFlag -- visibility fixed with volatile")
    class VolatileFlagTest {

        @Test
        @DisplayName("initial state is not stopped")
        void initialStateIsNotStopped() {
            var flag = new VisibilityIssue.VolatileFlag();
            assertThat(flag.isStopRequested()).isFalse();
        }

        @Test
        @DisplayName("requestStop makes flag visible immediately")
        void requestStopMakesFlagVisible() {
            var flag = new VisibilityIssue.VolatileFlag();
            flag.requestStop();
            assertThat(flag.isStopRequested()).isTrue();
        }

        @Test
        @DisplayName("spinUntilStop returns 0 when pre-stopped")
        void spinUntilStopReturnsZeroWhenPreStopped() {
            var flag = new VisibilityIssue.VolatileFlag();
            flag.requestStop();
            long iterations = flag.spinUntilStop(1_000_000);
            assertThat(iterations).isEqualTo(0);
        }

        @Test
        @DisplayName("cross-thread stop signal is visible due to volatile")
        void crossThreadStopSignalIsVisible() throws Exception {
            var flag = new VisibilityIssue.VolatileFlag();
            CountDownLatch started = new CountDownLatch(1);
            var resultFuture = new CompletableFuture<Long>();

            Thread worker = Thread.ofVirtual().start(() -> {
                started.countDown();
                long iterations = flag.spinUntilStop(100_000_000L);
                resultFuture.complete(iterations);
            });

            started.await(2, TimeUnit.SECONDS);
            Thread.sleep(5); // Let worker spin briefly
            flag.requestStop();

            Long iterations = resultFuture.get(5, TimeUnit.SECONDS);
            worker.join(5000);

            assertThat(iterations).isLessThan(100_000_000L);
        }
    }

    // ── VolatileCounter ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("VolatileCounter -- volatile does NOT make i++ atomic")
    class VolatileCounterTest {

        @Test
        @DisplayName("single-threaded increment works")
        void singleThreadedIncrementWorks() {
            var counter = new VisibilityIssue.VolatileCounter();
            for (int i = 0; i < 100; i++) counter.increment();
            assertThat(counter.getCount()).isEqualTo(100);
        }

        @RepeatedTest(5)
        @DisplayName("concurrent increments likely lose updates despite volatile")
        void concurrentIncrementsLikelyLoseUpdates() throws InterruptedException {
            var counter = new VisibilityIssue.VolatileCounter();
            int threads = 50;
            int perThread = 1000;
            int expected = threads * perThread;

            DataRace.runConcurrentIncrements(threads, perThread, counter::increment);

            assertThat(counter.getCount()).isLessThanOrEqualTo(expected);
            System.out.println("VolatileCounter: expected=" + expected
                    + ", actual=" + counter.getCount());
        }
    }

    // ── SynchronizedVisibility ───────────────────────────────────────────────

    @Nested
    @DisplayName("SynchronizedVisibility -- synchronized provides both atomicity and visibility")
    class SynchronizedVisibilityTest {

        @Test
        @DisplayName("readIfReady returns -1 when not yet published")
        void readIfReadyReturnsMinusOneWhenNotReady() {
            var sv = new VisibilityIssue.SynchronizedVisibility();
            assertThat(sv.readIfReady()).isEqualTo(-1);
        }

        @Test
        @DisplayName("isReady returns false initially")
        void isReadyReturnsFalseInitially() {
            var sv = new VisibilityIssue.SynchronizedVisibility();
            assertThat(sv.isReady()).isFalse();
        }

        @Test
        @DisplayName("publish makes data visible via readIfReady")
        void publishMakesDataVisible() {
            var sv = new VisibilityIssue.SynchronizedVisibility();
            sv.publish(42);
            assertThat(sv.isReady()).isTrue();
            assertThat(sv.readIfReady()).isEqualTo(42);
        }

        @Test
        @DisplayName("cross-thread: reader sees published value after writer publishes")
        void crossThreadPublishIsVisible() throws Exception {
            var sv = new VisibilityIssue.SynchronizedVisibility();
            CountDownLatch writerDone = new CountDownLatch(1);
            AtomicInteger readValue = new AtomicInteger(-999);

            Thread writer = Thread.ofVirtual().start(() -> {
                sv.publish(99);
                writerDone.countDown();
            });

            Thread reader = Thread.ofVirtual().start(() -> {
                try {
                    writerDone.await(2, TimeUnit.SECONDS);
                    readValue.set(sv.readIfReady());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            writer.join(3000);
            reader.join(3000);

            assertThat(readValue.get()).isEqualTo(99);
        }
    }

    // ── DoubleCheckedLocking ─────────────────────────────────────────────────

    @Nested
    @DisplayName("DoubleCheckedLocking -- correct lazy initialization with volatile")
    class DoubleCheckedLockingTest {

        @Test
        @DisplayName("isInitialized returns false before first access")
        void isInitializedReturnsFalseBeforeAccess() {
            var dcl = new VisibilityIssue.DoubleCheckedLocking<>(() -> "hello");
            assertThat(dcl.isInitialized()).isFalse();
        }

        @Test
        @DisplayName("getInstance returns the value created by the factory")
        void getInstanceReturnsFactoryValue() {
            var dcl = new VisibilityIssue.DoubleCheckedLocking<>(() -> "hello");
            assertThat(dcl.getInstance()).isEqualTo("hello");
            assertThat(dcl.isInitialized()).isTrue();
        }

        @Test
        @DisplayName("getInstance returns the same instance on repeated calls")
        void getInstanceReturnsSameInstance() {
            var dcl = new VisibilityIssue.DoubleCheckedLocking<>(Object::new);
            Object first = dcl.getInstance();
            Object second = dcl.getInstance();
            assertThat(first).isSameAs(second);
        }

        @Test
        @DisplayName("factory is called exactly once even under concurrent access")
        void factoryCalledExactlyOnce() throws InterruptedException {
            AtomicInteger factoryCallCount = new AtomicInteger(0);
            var dcl = new VisibilityIssue.DoubleCheckedLocking<>(() -> {
                factoryCallCount.incrementAndGet();
                return "value";
            });

            int threads = 100;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);
            CopyOnWriteArrayList<String> results = new CopyOnWriteArrayList<>();

            for (int i = 0; i < threads; i++) {
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        results.add(dcl.getInstance());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            start.countDown();
            done.await(10, TimeUnit.SECONDS);

            assertThat(factoryCallCount.get()).isEqualTo(1);
            assertThat(results).hasSize(threads);
            results.forEach(r -> assertThat(r).isEqualTo("value"));
        }

        @Test
        @DisplayName("constructor rejects null factory")
        void constructorRejectsNullFactory() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new VisibilityIssue.DoubleCheckedLocking<>(null));
        }
    }

    // ── VolatilePublisher ────────────────────────────────────────────────────

    @Nested
    @DisplayName("VolatilePublisher -- piggybacking non-volatile data on volatile flag")
    class VolatilePublisherTest {

        @Test
        @DisplayName("isPublished returns false initially")
        void isPublishedReturnsFalseInitially() {
            var pub = new VisibilityIssue.VolatilePublisher();
            assertThat(pub.isPublished()).isFalse();
        }

        @Test
        @DisplayName("publish makes all data fields visible")
        void publishMakesAllFieldsVisible() {
            var pub = new VisibilityIssue.VolatilePublisher();
            pub.publish(10, 20, "hello");
            assertThat(pub.isPublished()).isTrue();
            assertThat(pub.getValue1()).isEqualTo(10);
            assertThat(pub.getValue2()).isEqualTo(20);
            assertThat(pub.getLabel()).isEqualTo("hello");
        }

        @RepeatedTest(3)
        @DisplayName("cross-thread: reader sees all published data once flag is true")
        void crossThreadReaderSeesAllData() throws Exception {
            var pub = new VisibilityIssue.VolatilePublisher();
            AtomicInteger errors = new AtomicInteger(0);
            CountDownLatch writerDone = new CountDownLatch(1);
            CountDownLatch readerDone = new CountDownLatch(1);

            Thread writer = Thread.ofVirtual().start(() -> {
                pub.publish(42, 99, "test-label");
                writerDone.countDown();
            });

            Thread reader = Thread.ofVirtual().start(() -> {
                try {
                    writerDone.await(2, TimeUnit.SECONDS);
                    // After volatile read of 'published' returns true,
                    // all prior writes are visible (happens-before)
                    if (pub.isPublished()) {
                        if (pub.getValue1() != 42) errors.incrementAndGet();
                        if (pub.getValue2() != 99) errors.incrementAndGet();
                        if (!"test-label".equals(pub.getLabel())) errors.incrementAndGet();
                    } else {
                        errors.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    readerDone.countDown();
                }
            });

            writer.join(3000);
            reader.join(3000);
            assertThat(errors.get()).isZero();
        }
    }

    // ── HolderIdiom ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("HolderIdiom -- Initialization-on-Demand Holder pattern")
    class HolderIdiomTest {

        @Test
        @DisplayName("getInstance returns a non-null singleton")
        void getInstanceReturnsNonNull() {
            assertThat(VisibilityIssue.HolderIdiom.getInstance()).isNotNull();
        }

        @Test
        @DisplayName("getInstance always returns the same instance")
        void getInstanceReturnsSameInstance() {
            var a = VisibilityIssue.HolderIdiom.getInstance();
            var b = VisibilityIssue.HolderIdiom.getInstance();
            assertThat(a).isSameAs(b);
        }

        @Test
        @DisplayName("concurrent access always returns the same instance")
        void concurrentAccessReturnsSameInstance() throws InterruptedException {
            int threads = 50;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);
            CopyOnWriteArrayList<VisibilityIssue.HolderIdiom> instances = new CopyOnWriteArrayList<>();

            for (int i = 0; i < threads; i++) {
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        instances.add(VisibilityIssue.HolderIdiom.getInstance());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            start.countDown();
            done.await(10, TimeUnit.SECONDS);

            assertThat(instances).hasSize(threads);
            var first = instances.getFirst();
            instances.forEach(inst -> assertThat(inst).isSameAs(first));
        }
    }
}
