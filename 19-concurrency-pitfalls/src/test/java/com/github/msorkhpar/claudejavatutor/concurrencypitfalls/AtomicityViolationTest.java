package com.github.msorkhpar.claudejavatutor.concurrencypitfalls;

import org.junit.jupiter.api.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("5.3.2 Atomicity Violations and Ensuring Atomic Operations")
class AtomicityViolationTest {

    // ── NonAtomicIncrement ───────────────────────────────────────────────────

    @Nested
    @DisplayName("NonAtomicIncrement -- demonstrates atomicity violation")
    class NonAtomicIncrementTest {

        @Test
        @DisplayName("single-threaded increment works")
        void singleThreadedIncrementWorks() {
            var counter = new AtomicityViolation.NonAtomicIncrement();
            for (int i = 0; i < 100; i++) counter.increment();
            assertThat(counter.get()).isEqualTo(100);
        }

        @RepeatedTest(5)
        @DisplayName("concurrent increments likely lose updates due to atomicity violation")
        void concurrentIncrementsLikelyLoseUpdates() throws InterruptedException {
            var counter = new AtomicityViolation.NonAtomicIncrement();
            int threads = 50;
            int perThread = 1000;
            int expected = threads * perThread;

            DataRace.runConcurrentIncrements(threads, perThread, counter::increment);

            assertThat(counter.get()).isLessThanOrEqualTo(expected);
            System.out.println("NonAtomic: expected=" + expected + ", actual=" + counter.get());
        }
    }

    // ── AtomicIncrement ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("AtomicIncrement -- atomicity via AtomicInteger CAS")
    class AtomicIncrementTest {

        @Test
        @DisplayName("initial value is zero")
        void initialValueIsZero() {
            var counter = new AtomicityViolation.AtomicIncrement();
            assertThat(counter.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("single-threaded increment works")
        void singleThreadedIncrementWorks() {
            var counter = new AtomicityViolation.AtomicIncrement();
            counter.increment();
            counter.increment();
            assertThat(counter.get()).isEqualTo(2);
        }

        @RepeatedTest(3)
        @DisplayName("concurrent increments produce exact expected total")
        void concurrentIncrementsProduceCorrectTotal() throws InterruptedException {
            var counter = new AtomicityViolation.AtomicIncrement();
            int threads = 50;
            int perThread = 1000;
            DataRace.runConcurrentIncrements(threads, perThread, counter::increment);
            assertThat(counter.get()).isEqualTo(threads * perThread);
        }

        @Test
        @DisplayName("incrementIfBelow returns true and increments when below max")
        void incrementIfBelowReturnsTrueWhenBelow() {
            var counter = new AtomicityViolation.AtomicIncrement();
            assertThat(counter.incrementIfBelow(5)).isTrue();
            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("incrementIfBelow returns false when at max")
        void incrementIfBelowReturnsFalseWhenAtMax() {
            var counter = new AtomicityViolation.AtomicIncrement();
            for (int i = 0; i < 5; i++) counter.increment();
            assertThat(counter.incrementIfBelow(5)).isFalse();
            assertThat(counter.get()).isEqualTo(5);
        }

        @Test
        @DisplayName("incrementIfBelow is atomic under concurrent access")
        void incrementIfBelowIsAtomicUnderConcurrency() throws InterruptedException {
            var counter = new AtomicityViolation.AtomicIncrement();
            int max = 100;
            int threads = 200;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        counter.incrementIfBelow(max);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            start.countDown();
            done.await(10, TimeUnit.SECONDS);

            // Counter should be exactly max (200 threads tried, only 100 succeeded)
            assertThat(counter.get()).isEqualTo(max);
        }
    }

    // ── SynchronizedCoordinate ───────────────────────────────────────────────

    @Nested
    @DisplayName("SynchronizedCoordinate -- atomic multi-variable update")
    class SynchronizedCoordinateTest {

        @Test
        @DisplayName("initial position is (0, 0)")
        void initialPositionIsOrigin() {
            var coord = new AtomicityViolation.SynchronizedCoordinate();
            assertThat(coord.getPosition()).containsExactly(0, 0);
        }

        @Test
        @DisplayName("moveTo updates both coordinates atomically")
        void moveToUpdatesBothCoordinates() {
            var coord = new AtomicityViolation.SynchronizedCoordinate();
            coord.moveTo(10, 20);
            assertThat(coord.getPosition()).containsExactly(10, 20);
        }

        @Test
        @DisplayName("concurrent moveTo always produces consistent (x,y) pairs")
        void concurrentMoveToProducesConsistentPairs() throws InterruptedException {
            var coord = new AtomicityViolation.SynchronizedCoordinate();
            AtomicInteger inconsistencies = new AtomicInteger(0);
            CountDownLatch start = new CountDownLatch(1);
            int totalThreads = 30;
            CountDownLatch done = new CountDownLatch(totalThreads);

            // 10 writers: always write pairs where x == y
            for (int i = 0; i < 10; i++) {
                final int base = i * 100;
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < 100; j++) {
                            coord.moveTo(base + j, base + j);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            // 20 readers: check that x always equals y
            for (int i = 0; i < 20; i++) {
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < 500; j++) {
                            int[] pos = coord.getPosition();
                            if (pos[0] != pos[1]) {
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

    // ── ImmutableCoordinate ──────────────────────────────────────────────────

    @Nested
    @DisplayName("ImmutableCoordinate -- atomic via immutable snapshot + volatile")
    class ImmutableCoordinateTest {

        @Test
        @DisplayName("initial position is (0, 0)")
        void initialPositionIsOrigin() {
            var coord = new AtomicityViolation.ImmutableCoordinate();
            assertThat(coord.getPosition()).isEqualTo(new AtomicityViolation.ImmutableCoordinate.Point(0, 0));
        }

        @Test
        @DisplayName("moveTo creates a new consistent snapshot")
        void moveToCreatesNewSnapshot() {
            var coord = new AtomicityViolation.ImmutableCoordinate();
            coord.moveTo(42, 99);
            var pos = coord.getPosition();
            assertThat(pos.x()).isEqualTo(42);
            assertThat(pos.y()).isEqualTo(99);
        }

        @Test
        @DisplayName("concurrent reads always see a consistent snapshot")
        void concurrentReadsAlwaysSeeConsistentSnapshot() throws InterruptedException {
            var coord = new AtomicityViolation.ImmutableCoordinate();
            AtomicInteger inconsistencies = new AtomicInteger(0);
            CountDownLatch start = new CountDownLatch(1);
            int totalThreads = 30;
            CountDownLatch done = new CountDownLatch(totalThreads);

            for (int i = 0; i < 10; i++) {
                final int base = i * 100;
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < 100; j++) {
                            coord.moveTo(base + j, base + j);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            for (int i = 0; i < 20; i++) {
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < 500; j++) {
                            var pos = coord.getPosition();
                            if (pos.x() != pos.y()) {
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

    // ── SafeBankAccount ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("SafeBankAccount -- ReentrantLock for multi-field atomicity")
    class SafeBankAccountTest {

        @Test
        @DisplayName("initial balance and transaction count are zero")
        void initialStateIsZero() {
            var account = new AtomicityViolation.SafeBankAccount();
            assertThat(account.getBalance()).isEqualTo(0);
            assertThat(account.getTransactionCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("deposit increases balance and transaction count")
        void depositIncreasesBalanceAndCount() {
            var account = new AtomicityViolation.SafeBankAccount();
            account.deposit(100);
            assertThat(account.getBalance()).isEqualTo(100);
            assertThat(account.getTransactionCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("withdraw decreases balance and increases transaction count")
        void withdrawDecreasesBalanceAndIncreasesCount() {
            var account = new AtomicityViolation.SafeBankAccount();
            account.deposit(200);
            account.withdraw(50);
            assertThat(account.getBalance()).isEqualTo(150);
            assertThat(account.getTransactionCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("withdraw with insufficient funds throws exception")
        void withdrawWithInsufficientFundsThrows() {
            var account = new AtomicityViolation.SafeBankAccount();
            account.deposit(50);
            assertThatIllegalStateException()
                    .isThrownBy(() -> account.withdraw(100))
                    .withMessageContaining("Insufficient funds");
        }

        @Test
        @DisplayName("getSnapshot returns consistent balance and transaction count")
        void getSnapshotReturnsConsistentView() {
            var account = new AtomicityViolation.SafeBankAccount();
            account.deposit(100);
            account.deposit(200);
            int[] snapshot = account.getSnapshot();
            assertThat(snapshot[0]).isEqualTo(300);  // balance
            assertThat(snapshot[1]).isEqualTo(2);    // transaction count
        }

        @RepeatedTest(3)
        @DisplayName("concurrent deposits produce correct total balance and transaction count")
        void concurrentDepositsProduceCorrectTotals() throws InterruptedException {
            var account = new AtomicityViolation.SafeBankAccount();
            int threads = 50;
            int depositsPerThread = 100;
            int amountPerDeposit = 10;

            DataRace.runConcurrentIncrements(threads, depositsPerThread,
                    () -> account.deposit(amountPerDeposit));

            int[] snapshot = account.getSnapshot();
            assertThat(snapshot[0]).isEqualTo(threads * depositsPerThread * amountPerDeposit);
            assertThat(snapshot[1]).isEqualTo(threads * depositsPerThread);
        }
    }

    // ── CASRetryCounter ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("CASRetryCounter -- CAS retry loop patterns")
    class CASRetryCounterTest {

        @Test
        @DisplayName("addN adds the correct amount")
        void addNAddsCorrectAmount() {
            var counter = new AtomicityViolation.CASRetryCounter();
            counter.addN(5);
            counter.addN(3);
            assertThat(counter.get()).isEqualTo(8);
        }

        @Test
        @DisplayName("addN with negative value works")
        void addNWithNegativeWorks() {
            var counter = new AtomicityViolation.CASRetryCounter();
            counter.addN(10);
            counter.addN(-3);
            assertThat(counter.get()).isEqualTo(7);
        }

        @Test
        @DisplayName("incrementIfPositive returns true and increments when positive")
        void incrementIfPositiveReturnsTrueWhenPositive() {
            var counter = new AtomicityViolation.CASRetryCounter();
            counter.set(5);
            assertThat(counter.incrementIfPositive()).isTrue();
            assertThat(counter.get()).isEqualTo(6);
        }

        @Test
        @DisplayName("incrementIfPositive returns false when zero")
        void incrementIfPositiveReturnsFalseWhenZero() {
            var counter = new AtomicityViolation.CASRetryCounter();
            assertThat(counter.incrementIfPositive()).isFalse();
            assertThat(counter.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("incrementIfPositive returns false when negative")
        void incrementIfPositiveReturnsFalseWhenNegative() {
            var counter = new AtomicityViolation.CASRetryCounter();
            counter.set(-1);
            assertThat(counter.incrementIfPositive()).isFalse();
            assertThat(counter.get()).isEqualTo(-1);
        }

        @RepeatedTest(3)
        @DisplayName("concurrent addN produces correct total")
        void concurrentAddNProducesCorrectTotal() throws InterruptedException {
            var counter = new AtomicityViolation.CASRetryCounter();
            int threads = 50;
            int perThread = 1000;
            DataRace.runConcurrentIncrements(threads, perThread, () -> counter.addN(1));
            assertThat(counter.get()).isEqualTo(threads * perThread);
        }
    }

    // ── ABADemo ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ABADemo -- AtomicStampedReference prevents ABA problem")
    class ABADemoTest {

        @Test
        @DisplayName("initial value and stamp are correct")
        void initialValueAndStampCorrect() {
            var demo = new AtomicityViolation.ABADemo("A");
            assertThat(demo.getValue()).isEqualTo("A");
            assertThat(demo.getStamp()).isEqualTo(0);
        }

        @Test
        @DisplayName("compareAndSet succeeds with correct expected value and stamp")
        void compareAndSetSucceedsWithCorrectExpected() {
            var demo = new AtomicityViolation.ABADemo("A");
            boolean result = demo.compareAndSet("A", 0, "B");
            assertThat(result).isTrue();
            assertThat(demo.getValue()).isEqualTo("B");
            assertThat(demo.getStamp()).isEqualTo(1);
        }

        @Test
        @DisplayName("compareAndSet fails with wrong expected value")
        void compareAndSetFailsWithWrongValue() {
            var demo = new AtomicityViolation.ABADemo("A");
            boolean result = demo.compareAndSet("B", 0, "C");
            assertThat(result).isFalse();
            assertThat(demo.getValue()).isEqualTo("A");
        }

        @Test
        @DisplayName("compareAndSet fails with wrong stamp (detects ABA)")
        void compareAndSetFailsWithWrongStamp() {
            var demo = new AtomicityViolation.ABADemo("A");
            // Simulate A -> B -> A
            demo.compareAndSet("A", 0, "B");  // stamp becomes 1
            demo.compareAndSet("B", 1, "A");  // stamp becomes 2

            // Value is "A" again, but stamp is 2 (not 0)
            boolean result = demo.compareAndSet("A", 0, "C");
            assertThat(result).isFalse();
            assertThat(demo.getValue()).isEqualTo("A");
        }

        @Test
        @DisplayName("getStampedValue returns both value and stamp atomically")
        void getStampedValueReturnsAtomicSnapshot() {
            var demo = new AtomicityViolation.ABADemo("X");
            demo.compareAndSet("X", 0, "Y");
            var stamped = demo.getStampedValue();
            assertThat(stamped.value()).isEqualTo("Y");
            assertThat(stamped.stamp()).isEqualTo(1);
        }
    }

    // ── HighContentionCounter ────────────────────────────────────────────────

    @Nested
    @DisplayName("HighContentionCounter -- LongAdder for high-contention scenarios")
    class HighContentionCounterTest {

        @Test
        @DisplayName("initial sum is zero")
        void initialSumIsZero() {
            var counter = new AtomicityViolation.HighContentionCounter();
            assertThat(counter.sum()).isEqualTo(0);
        }

        @Test
        @DisplayName("increment and add work correctly single-threaded")
        void incrementAndAddWorkSingleThreaded() {
            var counter = new AtomicityViolation.HighContentionCounter();
            counter.increment();
            counter.increment();
            counter.add(10);
            assertThat(counter.sum()).isEqualTo(12);
        }

        @Test
        @DisplayName("reset clears the sum")
        void resetClearsSum() {
            var counter = new AtomicityViolation.HighContentionCounter();
            counter.increment();
            counter.reset();
            assertThat(counter.sum()).isEqualTo(0);
        }

        @Test
        @DisplayName("sumThenReset returns sum and resets to zero")
        void sumThenResetReturnsSumAndResets() {
            var counter = new AtomicityViolation.HighContentionCounter();
            counter.add(42);
            long result = counter.sumThenReset();
            assertThat(result).isEqualTo(42);
            assertThat(counter.sum()).isEqualTo(0);
        }

        @RepeatedTest(3)
        @DisplayName("concurrent increments produce correct total under high contention")
        void concurrentIncrementsProduceCorrectTotal() throws InterruptedException {
            var counter = new AtomicityViolation.HighContentionCounter();
            int threads = 100;
            int perThread = 1000;
            DataRace.runConcurrentIncrements(threads, perThread, counter::increment);
            assertThat(counter.sum()).isEqualTo((long) threads * perThread);
        }
    }

    // ── MaxAccumulator ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("MaxAccumulator -- LongAccumulator for tracking maximum")
    class MaxAccumulatorTest {

        @Test
        @DisplayName("initial max is Long.MIN_VALUE")
        void initialMaxIsMinValue() {
            var acc = new AtomicityViolation.MaxAccumulator();
            assertThat(acc.getMax()).isEqualTo(Long.MIN_VALUE);
        }

        @Test
        @DisplayName("observe updates the max correctly")
        void observeUpdatesMax() {
            var acc = new AtomicityViolation.MaxAccumulator();
            acc.observe(10);
            acc.observe(5);
            acc.observe(20);
            acc.observe(15);
            assertThat(acc.getMax()).isEqualTo(20);
        }

        @Test
        @DisplayName("observe handles negative values")
        void observeHandlesNegativeValues() {
            var acc = new AtomicityViolation.MaxAccumulator();
            acc.observe(-5);
            acc.observe(-1);
            acc.observe(-10);
            assertThat(acc.getMax()).isEqualTo(-1);
        }

        @Test
        @DisplayName("reset resets to identity (Long.MIN_VALUE)")
        void resetResetsToIdentity() {
            var acc = new AtomicityViolation.MaxAccumulator();
            acc.observe(100);
            acc.reset();
            assertThat(acc.getMax()).isEqualTo(Long.MIN_VALUE);
        }

        @RepeatedTest(3)
        @DisplayName("concurrent observers find correct max")
        void concurrentObserversCorrectMax() throws InterruptedException {
            var acc = new AtomicityViolation.MaxAccumulator();
            int threads = 50;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                final int val = i;
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        acc.observe(val);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            start.countDown();
            done.await(10, TimeUnit.SECONDS);
            assertThat(acc.getMax()).isEqualTo(threads - 1);
        }
    }
}
