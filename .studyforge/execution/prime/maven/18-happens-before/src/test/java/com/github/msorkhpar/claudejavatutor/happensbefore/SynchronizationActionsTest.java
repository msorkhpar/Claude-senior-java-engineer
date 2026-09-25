package com.github.msorkhpar.claudejavatutor.happensbefore;

import org.junit.jupiter.api.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("5.2.2 Synchronization Actions Tests")
class SynchronizationActionsTest {

    // ── Synchronized Counter ──────────────────────────────────────────────────

    @Nested
    @DisplayName("SynchronizedCounter — monitor lock rule")
    class SynchronizedCounterTest {

        private SynchronizationActions.SynchronizedCounter counter;

        @BeforeEach
        void setUp() {
            counter = new SynchronizationActions.SynchronizedCounter();
        }

        @Test
        @DisplayName("initial count is zero")
        void initialCountIsZero() {
            assertThat(counter.getCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("increment increases count by one")
        void incrementIncreasesByOne() {
            counter.increment();
            assertThat(counter.getCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("decrement decreases count by one")
        void decrementDecreasesByOne() {
            counter.increment();
            counter.increment();
            counter.decrement();
            assertThat(counter.getCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("count can go negative with decrement")
        void countCanBeNegative() {
            counter.decrement();
            assertThat(counter.getCount()).isEqualTo(-1);
        }

        @Test
        @DisplayName("concurrent increments produce correct total due to synchronization")
        void concurrentIncrementsAreCorrect() throws Exception {
            int threads = 20;
            int incrementsPerThread = 100;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                new Thread(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < incrementsPerThread; j++) {
                            counter.increment();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                }).start();
            }

            start.countDown();
            done.await(10, TimeUnit.SECONDS);

            assertThat(counter.getCount()).isEqualTo(threads * incrementsPerThread);
        }

        @Test
        @DisplayName("incrementIfLessThan respects the bound atomically")
        void incrementIfLessThanRespectsMaxAtomically() throws Exception {
            int max = 50;
            int threads = 20;
            int attemptsPerThread = 10;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);
            AtomicInteger successCount = new AtomicInteger(0);

            for (int i = 0; i < threads; i++) {
                new Thread(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < attemptsPerThread; j++) {
                            if (counter.incrementIfLessThan(max)) {
                                successCount.incrementAndGet();
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                }).start();
            }

            start.countDown();
            done.await(10, TimeUnit.SECONDS);

            // Counter must not exceed max and must match successful increments
            assertThat(counter.getCount()).isEqualTo(successCount.get());
            assertThat(counter.getCount()).isLessThanOrEqualTo(max);
        }

        @Test
        @DisplayName("reset returns count to zero")
        void resetReturnsCountToZero() {
            counter.increment();
            counter.increment();
            counter.reset();
            assertThat(counter.getCount()).isEqualTo(0);
        }
    }

    // ── Split Lock Demo ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("SplitLockDemo — separate monitors with volatile bridge")
    class SplitLockDemoTest {

        @Test
        @DisplayName("value written via write() is readable via read()")
        void writtenValueIsReadable() {
            var demo = new SynchronizationActions.SplitLockDemo();
            demo.write(77);
            assertThat(demo.read()).isEqualTo(77);
        }

        @Test
        @DisplayName("initial value is zero")
        void initialValueIsZero() {
            var demo = new SynchronizationActions.SplitLockDemo();
            assertThat(demo.read()).isEqualTo(0);
        }
    }

    // ── Static Synchronized Demo ──────────────────────────────────────────────

    @Nested
    @DisplayName("StaticSynchronizedDemo — Class object as monitor")
    class StaticSynchronizedDemoTest {

        @BeforeEach
        void reset() {
            SynchronizationActions.StaticSynchronizedDemo.resetStatic();
        }

        @Test
        @DisplayName("static synchronized increment works correctly")
        void staticIncrementWorks() {
            SynchronizationActions.StaticSynchronizedDemo.incrementStatic();
            assertThat(SynchronizationActions.StaticSynchronizedDemo.getStaticCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("concurrent static increments are correct")
        void concurrentStaticIncrementsAreCorrect() throws Exception {
            int threads = 10;
            int perThread = 50;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                new Thread(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < perThread; j++) {
                            SynchronizationActions.StaticSynchronizedDemo.incrementStatic();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                }).start();
            }

            start.countDown();
            done.await(10, TimeUnit.SECONDS);
            assertThat(SynchronizationActions.StaticSynchronizedDemo.getStaticCount())
                    .isEqualTo(threads * perThread);
        }
    }

    // ── Volatile Stop Flag ────────────────────────────────────────────────────

    @Nested
    @DisplayName("VolatileStopFlag — volatile variable rule")
    class VolatileStopFlagTest {

        @Test
        @DisplayName("isRunning is true initially")
        void isRunningTrueInitially() {
            var flag = new SynchronizationActions.VolatileStopFlag();
            assertThat(flag.isRunning()).isTrue();
        }

        @Test
        @DisplayName("stop() sets isRunning to false")
        void stopSetsIsRunningToFalse() {
            var flag = new SynchronizationActions.VolatileStopFlag();
            flag.stop();
            assertThat(flag.isRunning()).isFalse();
        }

        @Test
        @DisplayName("runLoop stops at maxIterations when flag not set")
        void runLoopStopsAtMax() {
            var flag = new SynchronizationActions.VolatileStopFlag();
            flag.runLoop(100);
            assertThat(flag.getIterationsCompleted()).isEqualTo(100);
        }

        @Test
        @DisplayName("stop() signal from another thread terminates runLoop")
        void stopFromAnotherThreadTerminatesLoop() throws Exception {
            var flag = new SynchronizationActions.VolatileStopFlag();
            CountDownLatch loopStarted = new CountDownLatch(1);
            CountDownLatch loopStopped = new CountDownLatch(1);

            Thread worker = new Thread(() -> {
                loopStarted.countDown();
                flag.runLoop(Integer.MAX_VALUE); // would run "forever" without stop
                loopStopped.countDown();
            });

            worker.start();
            loopStarted.await(2, TimeUnit.SECONDS);
            Thread.sleep(10); // let the loop run briefly
            flag.stop(); // volatile write — worker will see this

            boolean finished = loopStopped.await(3, TimeUnit.SECONDS);
            worker.join(3000);

            assertThat(finished).isTrue();
            assertThat(flag.isRunning()).isFalse();
        }
    }

    // ── Volatile Publication ──────────────────────────────────────────────────

    @Nested
    @DisplayName("VolatilePublication — safe publication via volatile reference")
    class VolatilePublicationTest {

        @Test
        @DisplayName("get() returns null before publish")
        void returnsNullBeforePublish() {
            var pub = new SynchronizationActions.VolatilePublication();
            assertThat(pub.get()).isNull();
        }

        @Test
        @DisplayName("get() returns published object with correct fields after publish")
        void returnsPublishedObjectWithCorrectFields() {
            var pub = new SynchronizationActions.VolatilePublication();
            pub.publish("Alice", 42);
            var data = pub.get();
            assertThat(data).isNotNull();
            assertThat(data.getName()).isEqualTo("Alice");
            assertThat(data.getValue()).isEqualTo(42);
        }

        @RepeatedTest(3)
        @DisplayName("cross-thread: reader sees fully initialized object after volatile publish")
        void crossThreadSafePublication() throws Exception {
            var pub = new SynchronizationActions.VolatilePublication();
            CountDownLatch published = new CountDownLatch(1);
            AtomicInteger failures = new AtomicInteger(0);

            Thread writer = new Thread(() -> {
                pub.publish("Bob", 100);
                published.countDown(); // countDown hb await
            });

            Thread reader = new Thread(() -> {
                try {
                    published.await(2, TimeUnit.SECONDS);
                    var data = pub.get(); // volatile read — sees publish
                    if (data == null || !data.getName().equals("Bob") || data.getValue() != 100) {
                        failures.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            writer.start();
            reader.start();
            writer.join(2000);
            reader.join(2000);

            assertThat(failures.get()).isZero();
        }

        @Test
        @DisplayName("ImmutableData fields are readable after construction")
        void immutableDataFieldsAreReadable() {
            var data = new SynchronizationActions.VolatilePublication.ImmutableData("test", 7);
            assertThat(data.getName()).isEqualTo("test");
            assertThat(data.getValue()).isEqualTo(7);
        }
    }

    // ── Volatile DCL ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("VolatileDCL — double-checked locking with volatile")
    class VolatileDCLTest {

        @Test
        @DisplayName("getInstance returns non-null instance")
        void getInstanceReturnsNonNull() {
            assertThat(SynchronizationActions.VolatileDCL.getInstance()).isNotNull();
        }

        @Test
        @DisplayName("getInstance returns same instance on repeated calls")
        void getInstanceReturnsSameInstance() {
            var i1 = SynchronizationActions.VolatileDCL.getInstance();
            var i2 = SynchronizationActions.VolatileDCL.getInstance();
            assertThat(i1).isSameAs(i2);
        }

        @Test
        @DisplayName("getInstance id is correctly initialized")
        void getInstanceIdIsCorrect() {
            assertThat(SynchronizationActions.VolatileDCL.getInstance().getId()).isEqualTo(99);
        }

        @Test
        @DisplayName("concurrent getInstance always returns same instance")
        void concurrentGetInstanceReturnsSameInstance() throws Exception {
            int threads = 30;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);
            var instances = new CopyOnWriteArrayList<SynchronizationActions.VolatileDCL>();

            for (int i = 0; i < threads; i++) {
                new Thread(() -> {
                    try {
                        start.await();
                        instances.add(SynchronizationActions.VolatileDCL.getInstance());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                }).start();
            }

            start.countDown();
            done.await(5, TimeUnit.SECONDS);

            assertThat(instances).hasSize(threads);
            var first = instances.get(0);
            instances.forEach(inst -> assertThat(inst).isSameAs(first));
        }
    }

    // ── Final Fields ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("FinalFieldSafePublication — final field guarantee")
    class FinalFieldSafePublicationTest {

        @Test
        @DisplayName("ImmutablePoint fields are correctly set after construction")
        void immutablePointFieldsAreCorrect() {
            var p = new SynchronizationActions.FinalFieldSafePublication.ImmutablePoint(3, 4, "origin");
            assertThat(p.x).isEqualTo(3);
            assertThat(p.y).isEqualTo(4);
            assertThat(p.label).isEqualTo("origin");
        }

        @Test
        @DisplayName("distanceTo computes Euclidean distance correctly")
        void distanceToIsCorrect() {
            var p1 = new SynchronizationActions.FinalFieldSafePublication.ImmutablePoint(0, 0, "a");
            var p2 = new SynchronizationActions.FinalFieldSafePublication.ImmutablePoint(3, 4, "b");
            assertThat(p1.distanceTo(p2)).isCloseTo(5.0, within(0.001));
        }

        @Test
        @DisplayName("PointPublisher returns null before publish")
        void pointPublisherReturnsNullBeforePublish() {
            var pub = new SynchronizationActions.FinalFieldSafePublication.PointPublisher();
            assertThat(pub.get()).isNull();
        }

        @Test
        @DisplayName("PointPublisher returns published point after publish")
        void pointPublisherReturnsPublishedPoint() {
            var pub = new SynchronizationActions.FinalFieldSafePublication.PointPublisher();
            var p = new SynchronizationActions.FinalFieldSafePublication.ImmutablePoint(5, 6, "test");
            pub.publish(p);
            var retrieved = pub.get();
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.x).isEqualTo(5);
            assertThat(retrieved.y).isEqualTo(6);
        }

        @RepeatedTest(3)
        @DisplayName("cross-thread: reader sees all final fields after volatile publish")
        void crossThreadFinalFieldVisibility() throws Exception {
            var pub = new SynchronizationActions.FinalFieldSafePublication.PointPublisher();
            CountDownLatch latch = new CountDownLatch(1);
            AtomicInteger failures = new AtomicInteger(0);

            Thread writer = new Thread(() -> {
                var p = new SynchronizationActions.FinalFieldSafePublication.ImmutablePoint(10, 20, "cross-thread");
                pub.publish(p);  // volatile write of reference
                latch.countDown();
            });

            Thread reader = new Thread(() -> {
                try {
                    latch.await(2, TimeUnit.SECONDS);
                    var p = pub.get(); // volatile read
                    if (p == null || p.x != 10 || p.y != 20 || !"cross-thread".equals(p.label)) {
                        failures.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            writer.start();
            reader.start();
            writer.join(2000);
            reader.join(2000);

            assertThat(failures.get()).isZero();
        }
    }

    // ── Proper Construction Demo ──────────────────────────────────────────────

    @Nested
    @DisplayName("ProperConstructionDemo — no this escape")
    class ProperConstructionDemoTest {

        @Test
        @DisplayName("safeValue is set correctly in properly constructed object")
        void safeValueIsSetCorrectly() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);
            var demo = new SynchronizationActions.ProperConstructionDemo(77, latch);
            // construction is complete here
            demo.publishAndSignal();
            assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
            assertThat(demo.safeValue).isEqualTo(77);
        }

        @Test
        @DisplayName("publishAndSignal triggers countdown")
        void publishAndSignalTriggersCountdown() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);
            var demo = new SynchronizationActions.ProperConstructionDemo(0, latch);
            demo.publishAndSignal();
            assertThat(latch.getCount()).isZero();
        }
    }

    // ── VarHandle RELEASE/ACQUIRE ─────────────────────────────────────────────

    @Nested
    @DisplayName("VarHandleReleaseAcquire — fine-grained memory ordering")
    class VarHandleReleaseAcquireTest {

        @Test
        @DisplayName("consume returns -1 before produce")
        void consumeReturnsMinus1BeforeProduce() {
            var demo = new SynchronizationActions.VarHandleReleaseAcquire();
            assertThat(demo.consume()).isEqualTo(-1);
        }

        @Test
        @DisplayName("produce then consume returns produced value")
        void produceThenConsumeReturnsValue() {
            var demo = new SynchronizationActions.VarHandleReleaseAcquire();
            demo.produce(42);
            assertThat(demo.consume()).isEqualTo(42);
        }

        @Test
        @DisplayName("isPublished is false before produce")
        void isPublishedFalseBeforeProduce() {
            var demo = new SynchronizationActions.VarHandleReleaseAcquire();
            assertThat(demo.isPublished()).isFalse();
        }

        @Test
        @DisplayName("isPublished is true after produce")
        void isPublishedTrueAfterProduce() {
            var demo = new SynchronizationActions.VarHandleReleaseAcquire();
            demo.produce(1);
            assertThat(demo.isPublished()).isTrue();
        }

        @RepeatedTest(3)
        @DisplayName("cross-thread: RELEASE/ACQUIRE ensures data is visible after publication")
        void crossThreadReleaseAcquireVisibility() throws Exception {
            var demo = new SynchronizationActions.VarHandleReleaseAcquire();
            CountDownLatch producedLatch = new CountDownLatch(1);
            AtomicInteger observed = new AtomicInteger(-999);

            Thread producer = new Thread(() -> {
                demo.produce(55); // RELEASE write
                producedLatch.countDown();
            });

            Thread consumer = new Thread(() -> {
                try {
                    producedLatch.await(2, TimeUnit.SECONDS);
                    observed.set(demo.consume()); // ACQUIRE read
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            producer.start();
            consumer.start();
            producer.join(2000);
            consumer.join(2000);

            assertThat(observed.get()).isEqualTo(55);
        }
    }
}
