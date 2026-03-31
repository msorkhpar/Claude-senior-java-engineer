package com.github.msorkhpar.claudejavatutor.happensbefore;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("5.2.1 Happens-Before Definition Tests")
class HappensBeforeDefinitionTest {

    // ── Rule 1: Program Order Rule ────────────────────────────────────────────

    @Nested
    @DisplayName("Program Order Rule")
    class ProgramOrderRuleTest {

        @Test
        @DisplayName("computeWithOrdering returns deterministic result due to program order")
        void programOrderGuaranteesCorrectSum() {
            var demo = new HappensBeforeDefinition.ProgramOrderDemo();
            assertThat(demo.computeWithOrdering()).isEqualTo(3);
        }

        @Test
        @DisplayName("multiple calls return same deterministic result")
        void multipleCallsAreConsistent() {
            var demo = new HappensBeforeDefinition.ProgramOrderDemo();
            for (int i = 0; i < 100; i++) {
                assertThat(demo.computeWithOrdering()).isEqualTo(3);
            }
        }
    }

    // ── Rule 2: Monitor Lock Rule ─────────────────────────────────────────────

    @Nested
    @DisplayName("Monitor Lock Rule")
    class MonitorLockRuleTest {

        @Test
        @DisplayName("write followed by read on same monitor sees written value")
        void readSeesWrittenValue() {
            var demo = new HappensBeforeDefinition.MonitorLockDemo();
            demo.write(99);
            assertThat(demo.read()).isEqualTo(99);
        }

        @Test
        @DisplayName("concurrent writes and reads are consistent due to monitor lock rule")
        void concurrentAccessIsConsistent() throws Exception {
            var demo = new HappensBeforeDefinition.MonitorLockDemo();
            int threads = 10;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);
            List<Integer> results = new CopyOnWriteArrayList<>();

            for (int i = 0; i < threads; i++) {
                final int val = i;
                new Thread(() -> {
                    try {
                        start.await();
                        demo.write(val);
                        results.add(demo.read());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                }).start();
            }

            start.countDown();
            done.await(5, TimeUnit.SECONDS);

            // Each thread wrote a value; every read must have seen SOME valid value (0-9)
            assertThat(results).hasSize(threads);
            results.forEach(v -> assertThat(v).isBetween(0, threads - 1));
        }

        @Test
        @DisplayName("initial value is zero before any write")
        void initialValueIsZero() {
            var demo = new HappensBeforeDefinition.MonitorLockDemo();
            assertThat(demo.read()).isEqualTo(0);
        }

        @Test
        @DisplayName("write of zero is visible after write")
        void writeZeroIsVisible() {
            var demo = new HappensBeforeDefinition.MonitorLockDemo();
            demo.write(100);
            demo.write(0);
            assertThat(demo.read()).isEqualTo(0);
        }
    }

    // ── Rule 3: Volatile Variable Rule ───────────────────────────────────────

    @Nested
    @DisplayName("Volatile Variable Rule")
    class VolatileVariableRuleTest {

        @Test
        @DisplayName("consumer sees produced value when flag is set")
        void consumerSeesValueWhenFlagSet() {
            var demo = new HappensBeforeDefinition.VolatileRuleDemo();
            demo.produce(42);
            assertThat(demo.consume()).isEqualTo(42);
        }

        @Test
        @DisplayName("consumer returns -1 when flag is not set")
        void consumerReturnsNegativeOneWhenFlagNotSet() {
            var demo = new HappensBeforeDefinition.VolatileRuleDemo();
            assertThat(demo.consume()).isEqualTo(-1);
        }

        @RepeatedTest(5)
        @DisplayName("cross-thread: consumer thread always sees the produced value")
        void crossThreadVisibilityViaTransitivity() throws Exception {
            var demo = new HappensBeforeDefinition.VolatileRuleDemo();
            CountDownLatch produced = new CountDownLatch(1);
            AtomicInteger observed = new AtomicInteger(-1);

            Thread producer = new Thread(() -> {
                demo.produce(77);
                produced.countDown();
            });

            Thread consumer = new Thread(() -> {
                try {
                    produced.await(2, TimeUnit.SECONDS);
                    // produced.await() returns after produce() — transitivity:
                    // data write hb volatile write hb countDown hb await hb consume()
                    observed.set(demo.consume());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            producer.start();
            consumer.start();
            producer.join(2000);
            consumer.join(2000);

            assertThat(observed.get()).isEqualTo(77);
        }
    }

    // ── Rule 4: Thread Start Rule ─────────────────────────────────────────────

    @Nested
    @DisplayName("Thread Start Rule")
    class ThreadStartRuleTest {

        @Test
        @DisplayName("thread sees value written before Thread.start()")
        void threadSeesValueWrittenBeforeStart() throws InterruptedException {
            var demo = new HappensBeforeDefinition.ThreadStartRuleDemo();
            AtomicInteger result = demo.demonstrateStartRule(55);
            assertThat(result.get()).isEqualTo(55);
        }

        @Test
        @DisplayName("thread start rule applies for various values")
        void threadStartRuleForVariousValues() throws InterruptedException {
            var demo = new HappensBeforeDefinition.ThreadStartRuleDemo();
            int[] testValues = {0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE, 42};
            for (int v : testValues) {
                var newDemo = new HappensBeforeDefinition.ThreadStartRuleDemo();
                assertThat(newDemo.demonstrateStartRule(v).get())
                        .as("Thread should see value %d written before start()", v)
                        .isEqualTo(v);
            }
        }
    }

    // ── Rule 5: Thread Join Rule ──────────────────────────────────────────────

    @Nested
    @DisplayName("Thread Join Rule")
    class ThreadJoinRuleTest {

        @Test
        @DisplayName("joining thread sees all writes made by the joined thread")
        void joiningThreadSeesAllWrites() throws InterruptedException {
            var demo = new HappensBeforeDefinition.ThreadJoinRuleDemo();
            int result = demo.demonstrateJoinRule();
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("join rule allows safe aggregation of worker results")
        void joinRuleAllowsSafeAggregation() throws InterruptedException {
            int numWorkers = 5;
            int[] results = new int[numWorkers];
            Thread[] workers = new Thread[numWorkers];

            for (int i = 0; i < numWorkers; i++) {
                final int idx = i;
                workers[i] = new Thread(() -> results[idx] = idx * 10);
                workers[i].start();
            }

            for (Thread w : workers) {
                w.join(); // join() hb reads of results after this point
            }

            // All joins completed — by join rule, all worker writes are visible
            for (int i = 0; i < numWorkers; i++) {
                assertThat(results[i]).isEqualTo(i * 10);
            }
        }
    }

    // ── Rule 6: Transitivity ──────────────────────────────────────────────────

    @Nested
    @DisplayName("Transitivity")
    class TransitivityTest {

        @Test
        @DisplayName("non-volatile write is visible via volatile transitivity chain")
        void nonVolatileWriteVisibleViaTransitivity() {
            var demo = new HappensBeforeDefinition.TransitivityDemo();
            demo.produce(123);
            assertThat(demo.consume()).isEqualTo(123);
        }

        @Test
        @DisplayName("consume returns -1 when flag not set")
        void consumeReturnsNegativeOneWhenFlagNotSet() {
            var demo = new HappensBeforeDefinition.TransitivityDemo();
            assertThat(demo.consume()).isEqualTo(-1);
        }

        @RepeatedTest(5)
        @DisplayName("cross-thread: non-volatile data visible via volatile chain")
        void crossThreadTransitivity() throws Exception {
            var demo = new HappensBeforeDefinition.TransitivityDemo();
            CountDownLatch producerDone = new CountDownLatch(1);
            AtomicInteger observed = new AtomicInteger(-999);

            Thread producer = new Thread(() -> {
                demo.produce(999);
                producerDone.countDown(); // countDown hb await
            });

            Thread consumer = new Thread(() -> {
                try {
                    producerDone.await(2, TimeUnit.SECONDS); // await hb code after
                    observed.set(demo.consume());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            producer.start();
            consumer.start();
            producer.join(2000);
            consumer.join(2000);

            assertThat(observed.get()).isEqualTo(999);
        }
    }

    // ── Initialization-on-Demand Holder ──────────────────────────────────────

    @Nested
    @DisplayName("Initialization-on-Demand Holder")
    class IoDHSingletonTest {

        @Test
        @DisplayName("getInstance returns non-null singleton")
        void getInstanceReturnsNonNull() {
            assertThat(HappensBeforeDefinition.IoDHSingleton.getInstance()).isNotNull();
        }

        @Test
        @DisplayName("getInstance always returns the same instance")
        void getInstanceAlwaysReturnsSameInstance() {
            var s1 = HappensBeforeDefinition.IoDHSingleton.getInstance();
            var s2 = HappensBeforeDefinition.IoDHSingleton.getInstance();
            assertThat(s1).isSameAs(s2);
        }

        @Test
        @DisplayName("singleton id is initialized correctly")
        void singletonIdIsInitialized() {
            assertThat(HappensBeforeDefinition.IoDHSingleton.getInstance().getId()).isEqualTo(42);
        }

        @Test
        @DisplayName("concurrent access always returns the same singleton instance")
        void concurrentAccessReturnsSameInstance() throws Exception {
            int threads = 20;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);
            List<HappensBeforeDefinition.IoDHSingleton> instances = new CopyOnWriteArrayList<>();

            for (int i = 0; i < threads; i++) {
                new Thread(() -> {
                    try {
                        start.await();
                        instances.add(HappensBeforeDefinition.IoDHSingleton.getInstance());
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
            HappensBeforeDefinition.IoDHSingleton first = instances.get(0);
            instances.forEach(inst -> assertThat(inst).isSameAs(first));
        }
    }

    // ── Interrupt Happens-before ───────────────────────────────────────────────

    @Nested
    @DisplayName("Interrupt Happens-Before")
    class InterruptHappensBeforeTest {

        @Test
        @DisplayName("interrupt() hb interrupted thread detecting interruption — message is visible")
        void interruptHappensBeforeDetection() throws Exception {
            var demo = new HappensBeforeDefinition.InterruptHappensBefore();
            demo.setMessageBeforeInterrupt("visible-after-interrupt");

            Thread t = demo.createInterruptibleThread();
            t.start();
            Thread.sleep(50); // give the thread time to enter sleep
            t.interrupt(); // interrupt() hb InterruptedException detection

            boolean detected = demo.getInterruptDetected().await(2, TimeUnit.SECONDS);
            t.join(2000);

            assertThat(detected).isTrue();
            assertThat(demo.getObservedMessage().get()).isEqualTo("visible-after-interrupt");
        }
    }

    // ── Volatile long atomicity ────────────────────────────────────────────────

    @Nested
    @DisplayName("Volatile Long Atomicity")
    class VolatileLongAtomicityTest {

        @Test
        @DisplayName("volatile long write is visible to other threads")
        void volatileLongWriteIsVisible() throws Exception {
            var demo = new HappensBeforeDefinition.LongAtomicityDemo();
            long expectedValue = 0xDEADBEEFCAFEBABEL;
            CountDownLatch written = new CountDownLatch(1);
            AtomicInteger failureCount = new AtomicInteger(0);

            Thread writer = new Thread(() -> {
                demo.writeSafe(expectedValue);
                written.countDown();
            });

            Thread reader = new Thread(() -> {
                try {
                    written.await(2, TimeUnit.SECONDS);
                    if (demo.readSafe() != expectedValue) {
                        failureCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            writer.start();
            reader.start();
            writer.join(2000);
            reader.join(2000);

            assertThat(failureCount.get()).isZero();
        }

        @Test
        @DisplayName("volatile long initial value is zero")
        void volatileLongInitialValueIsZero() {
            var demo = new HappensBeforeDefinition.LongAtomicityDemo();
            assertThat(demo.readSafe()).isEqualTo(0L);
        }
    }
}
