package com.github.msorkhpar.claudejavatutor.happensbefore;

import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("5.2.3 Memory Visibility Patterns Tests")
class MemoryVisibilityTest {

    // ── Cooperative Cancellation ──────────────────────────────────────────────

    @Nested
    @DisplayName("CooperativeCancellation — volatile stop flag")
    class CooperativeCancellationTest {

        @Test
        @DisplayName("isCancelled is false initially")
        void isCancelledFalseInitially() {
            var cc = new MemoryVisibility.CooperativeCancellation();
            assertThat(cc.isCancelled()).isFalse();
        }

        @Test
        @DisplayName("cancel() sets isCancelled to true")
        void cancelSetsCancelledToTrue() {
            var cc = new MemoryVisibility.CooperativeCancellation();
            cc.cancel();
            assertThat(cc.isCancelled()).isTrue();
        }

        @Test
        @DisplayName("processUntilCancelled processes all items when not cancelled")
        void processesAllItemsWhenNotCancelled() {
            var cc = new MemoryVisibility.CooperativeCancellation();
            List<Integer> items = List.of(1, 2, 3, 4, 5);
            int processed = cc.processUntilCancelled(items);
            assertThat(processed).isEqualTo(5);
            assertThat(cc.getWorkCompleted()).isEqualTo(5);
        }

        @Test
        @DisplayName("processUntilCancelled processes zero items when pre-cancelled")
        void processesZeroItemsWhenPreCancelled() {
            var cc = new MemoryVisibility.CooperativeCancellation();
            cc.cancel();
            List<Integer> items = List.of(1, 2, 3);
            int processed = cc.processUntilCancelled(items);
            assertThat(processed).isEqualTo(0);
            assertThat(cc.getWorkCompleted()).isEqualTo(0);
        }

        @Test
        @DisplayName("processUntilCancelled on empty list returns 0")
        void processesEmptyList() {
            var cc = new MemoryVisibility.CooperativeCancellation();
            int processed = cc.processUntilCancelled(Collections.emptyList());
            assertThat(processed).isEqualTo(0);
        }

        @Test
        @DisplayName("cancel signal from another thread stops processing")
        void cancelFromAnotherThreadStopsProcessing() throws Exception {
            var cc = new MemoryVisibility.CooperativeCancellation();
            List<Integer> largeList = new ArrayList<>();
            for (int i = 0; i < 100_000; i++) largeList.add(i);

            CountDownLatch started = new CountDownLatch(1);
            CountDownLatch workerDone = new CountDownLatch(1);
            AtomicInteger processedCount = new AtomicInteger(0);

            Thread worker = new Thread(() -> {
                started.countDown();
                int count = cc.processUntilCancelled(largeList);
                processedCount.set(count);
                workerDone.countDown();
            });

            worker.start();
            started.await(2, TimeUnit.SECONDS);
            Thread.sleep(5); // let worker run briefly
            cc.cancel(); // volatile write — visible to worker

            boolean finished = workerDone.await(3, TimeUnit.SECONDS);
            worker.join(3000);

            assertThat(finished).isTrue();
            assertThat(processedCount.get()).isLessThan(100_000);
        }
    }

    // ── Synchronized Counter ──────────────────────────────────────────────────

    @Nested
    @DisplayName("SynchronizedSharedCounter — monitor lock rule")
    class SynchronizedSharedCounterTest {

        private MemoryVisibility.SynchronizedSharedCounter counter;

        @BeforeEach
        void setUp() {
            counter = new MemoryVisibility.SynchronizedSharedCounter();
        }

        @Test
        @DisplayName("initial value is zero")
        void initialValueIsZero() {
            assertThat(counter.getValue()).isEqualTo(0L);
        }

        @Test
        @DisplayName("increment increases value by one")
        void incrementIncreasesByOne() {
            counter.increment();
            assertThat(counter.getValue()).isEqualTo(1L);
        }

        @Test
        @DisplayName("add(delta) adds the correct delta")
        void addDeltaWorks() {
            counter.add(10);
            counter.add(-3);
            assertThat(counter.getValue()).isEqualTo(7L);
        }

        @Test
        @DisplayName("incrementIfBelow returns true and increments when below max")
        void incrementIfBelowReturnsTrueWhenBelow() {
            boolean result = counter.incrementIfBelow(5);
            assertThat(result).isTrue();
            assertThat(counter.getValue()).isEqualTo(1L);
        }

        @Test
        @DisplayName("incrementIfBelow returns false and does not increment when at max")
        void incrementIfBelowReturnsFalseWhenAtMax() {
            counter.add(5);
            boolean result = counter.incrementIfBelow(5);
            assertThat(result).isFalse();
            assertThat(counter.getValue()).isEqualTo(5L);
        }

        @Test
        @DisplayName("history grows with each increment")
        void historyGrowsWithEachIncrement() {
            counter.increment();
            counter.increment();
            counter.increment();
            assertThat(counter.getHistory()).containsExactly(1L, 2L, 3L);
        }

        @Test
        @DisplayName("reset clears value and history")
        void resetClearsValueAndHistory() {
            counter.increment();
            counter.reset();
            assertThat(counter.getValue()).isEqualTo(0L);
            assertThat(counter.getHistory()).isEmpty();
        }

        @Test
        @DisplayName("concurrent increments produce correct total")
        void concurrentIncrementsProduceCorrectTotal() throws Exception {
            int threads = 20;
            int perThread = 500;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                new Thread(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < perThread; j++) counter.increment();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                }).start();
            }

            start.countDown();
            done.await(15, TimeUnit.SECONDS);

            assertThat(counter.getValue()).isEqualTo((long) threads * perThread);
        }
    }

    // ── Atomic Counter ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("AtomicSharedCounter — lock-free atomic operations")
    class AtomicSharedCounterTest {

        @Test
        @DisplayName("initial value is zero")
        void initialValueIsZero() {
            var c = new MemoryVisibility.AtomicSharedCounter();
            assertThat(c.getValue()).isEqualTo(0L);
        }

        @Test
        @DisplayName("increment returns and stores new value")
        void incrementReturnsNewValue() {
            var c = new MemoryVisibility.AtomicSharedCounter();
            assertThat(c.increment()).isEqualTo(1L);
            assertThat(c.getValue()).isEqualTo(1L);
        }

        @Test
        @DisplayName("addAndGet returns correct new value")
        void addAndGetReturnsCorrectValue() {
            var c = new MemoryVisibility.AtomicSharedCounter();
            assertThat(c.addAndGet(10)).isEqualTo(10L);
            assertThat(c.addAndGet(-3)).isEqualTo(7L);
        }

        @Test
        @DisplayName("updateMax tracks maximum value seen")
        void updateMaxTracksMaximum() {
            var c = new MemoryVisibility.AtomicSharedCounter();
            c.updateMax(5);
            c.updateMax(3);
            c.updateMax(10);
            c.updateMax(7);
            assertThat(c.getMaxSeen()).isEqualTo(10L);
        }

        @Test
        @DisplayName("updateMax ignores smaller values")
        void updateMaxIgnoresSmallerValues() {
            var c = new MemoryVisibility.AtomicSharedCounter();
            c.updateMax(100);
            c.updateMax(50);
            assertThat(c.getMaxSeen()).isEqualTo(100L);
        }

        @Test
        @DisplayName("concurrent increments produce correct total (lock-free)")
        void concurrentIncrementsCorrect() throws Exception {
            var c = new MemoryVisibility.AtomicSharedCounter();
            int threads = 20;
            int perThread = 1000;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                new Thread(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < perThread; j++) c.increment();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                }).start();
            }

            start.countDown();
            done.await(10, TimeUnit.SECONDS);
            assertThat(c.getValue()).isEqualTo((long) threads * perThread);
        }
    }

    // ── Immutable Config ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("ImmutableConfig — final field guarantee")
    class ImmutableConfigTest {

        @Test
        @DisplayName("fields are set correctly at construction")
        void fieldsAreSetAtConstruction() {
            var cfg = new MemoryVisibility.ImmutableConfig("localhost", 8080, 5000, true);
            assertThat(cfg.getHost()).isEqualTo("localhost");
            assertThat(cfg.getPort()).isEqualTo(8080);
            assertThat(cfg.getTimeoutMs()).isEqualTo(5000);
            assertThat(cfg.isSslEnabled()).isTrue();
        }

        @Test
        @DisplayName("null host throws NullPointerException")
        void nullHostThrowsNpe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new MemoryVisibility.ImmutableConfig(null, 80, 1000, false))
                    .withMessageContaining("host");
        }

        @Test
        @DisplayName("withHost returns new config with updated host")
        void withHostReturnsNewConfig() {
            var original = new MemoryVisibility.ImmutableConfig("old", 80, 1000, false);
            var updated = original.withHost("new");
            assertThat(updated.getHost()).isEqualTo("new");
            assertThat(updated.getPort()).isEqualTo(80);
            assertThat(original.getHost()).isEqualTo("old"); // original unchanged
        }

        @Test
        @DisplayName("equals and hashCode are consistent")
        void equalsAndHashCodeConsistent() {
            var c1 = new MemoryVisibility.ImmutableConfig("host", 80, 1000, false);
            var c2 = new MemoryVisibility.ImmutableConfig("host", 80, 1000, false);
            assertThat(c1).isEqualTo(c2);
            assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
        }

        @Test
        @DisplayName("different configs are not equal")
        void differentConfigsNotEqual() {
            var c1 = new MemoryVisibility.ImmutableConfig("host1", 80, 1000, false);
            var c2 = new MemoryVisibility.ImmutableConfig("host2", 80, 1000, false);
            assertThat(c1).isNotEqualTo(c2);
        }
    }

    // ── ConfigHolder ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ConfigHolder — volatile safe publication of immutable objects")
    class ConfigHolderTest {

        @Test
        @DisplayName("initial config is returned correctly")
        void initialConfigReturned() {
            var cfg = new MemoryVisibility.ImmutableConfig("initial", 80, 1000, false);
            var holder = new MemoryVisibility.ConfigHolder(cfg);
            assertThat(holder.getConfig()).isSameAs(cfg);
        }

        @Test
        @DisplayName("updateConfig replaces the config atomically")
        void updateConfigReplacesConfig() {
            var cfg1 = new MemoryVisibility.ImmutableConfig("v1", 80, 1000, false);
            var cfg2 = new MemoryVisibility.ImmutableConfig("v2", 443, 2000, true);
            var holder = new MemoryVisibility.ConfigHolder(cfg1);
            holder.updateConfig(cfg2);
            assertThat(holder.getConfig()).isSameAs(cfg2);
        }

        @Test
        @DisplayName("null config throws NullPointerException")
        void nullConfigThrowsNpe() {
            var holder = new MemoryVisibility.ConfigHolder(
                    new MemoryVisibility.ImmutableConfig("h", 80, 1000, false));
            assertThatNullPointerException()
                    .isThrownBy(() -> holder.updateConfig(null));
        }

        @RepeatedTest(3)
        @DisplayName("cross-thread: reader always sees fully initialized immutable config")
        void crossThreadImmutableConfigVisibility() throws Exception {
            var initial = new MemoryVisibility.ImmutableConfig("init", 80, 1000, false);
            var holder = new MemoryVisibility.ConfigHolder(initial);
            CountDownLatch updated = new CountDownLatch(1);
            AtomicInteger failures = new AtomicInteger(0);

            Thread writer = new Thread(() -> {
                var newCfg = new MemoryVisibility.ImmutableConfig("updated", 443, 3000, true);
                holder.updateConfig(newCfg); // volatile write
                updated.countDown();
            });

            Thread reader = new Thread(() -> {
                try {
                    updated.await(2, TimeUnit.SECONDS);
                    var cfg = holder.getConfig(); // volatile read
                    // Due to final field guarantee + volatile publication:
                    // all final fields of cfg are guaranteed visible
                    if (!"updated".equals(cfg.getHost()) || cfg.getPort() != 443
                            || cfg.getTimeoutMs() != 3000 || !cfg.isSslEnabled()) {
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

    // ── Stack Confinement ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("StackConfinementDemo — local variable safety")
    class StackConfinementDemoTest {

        @Test
        @DisplayName("sumSquaresOfOdds returns correct result for mixed list")
        void sumSquaresOfOddsCorrect() {
            var demo = new MemoryVisibility.StackConfinementDemo();
            // odds: 1, 3, 5 → 1 + 9 + 25 = 35
            assertThat(demo.sumSquaresOfOdds(List.of(1, 2, 3, 4, 5))).isEqualTo(35);
        }

        @Test
        @DisplayName("sumSquaresOfOdds returns 0 for empty list")
        void sumSquaresOfOddsEmptyList() {
            var demo = new MemoryVisibility.StackConfinementDemo();
            assertThat(demo.sumSquaresOfOdds(Collections.emptyList())).isEqualTo(0);
        }

        @Test
        @DisplayName("sumSquaresOfOdds returns 0 for all-even list")
        void sumSquaresOfOddsAllEvens() {
            var demo = new MemoryVisibility.StackConfinementDemo();
            assertThat(demo.sumSquaresOfOdds(List.of(2, 4, 6))).isEqualTo(0);
        }

        @Test
        @DisplayName("sumSquaresOfOdds handles negative odd numbers")
        void sumSquaresOfOddsNegativeOdds() {
            var demo = new MemoryVisibility.StackConfinementDemo();
            // -1, -3 → 1 + 9 = 10
            assertThat(demo.sumSquaresOfOdds(List.of(-1, -2, -3))).isEqualTo(10);
        }

        @Test
        @DisplayName("multiple concurrent calls are safe (stack confinement)")
        void concurrentCallsAreSafe() throws Exception {
            var demo = new MemoryVisibility.StackConfinementDemo();
            List<Integer> input = List.of(1, 2, 3, 4, 5); // expected: 35
            int threads = 10;
            CountDownLatch start = new CountDownLatch(1);
            List<Integer> results = new CopyOnWriteArrayList<>();
            CountDownLatch done = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                new Thread(() -> {
                    try {
                        start.await();
                        results.add(demo.sumSquaresOfOdds(input));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                }).start();
            }

            start.countDown();
            done.await(5, TimeUnit.SECONDS);

            assertThat(results).hasSize(threads);
            results.forEach(r -> assertThat(r).isEqualTo(35));
        }
    }

    // ── ThreadLocal ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("RequestContextHolder — ThreadLocal per-thread isolation")
    class RequestContextHolderTest {

        @AfterEach
        void cleanup() {
            MemoryVisibility.RequestContextHolder.clear();
        }

        @Test
        @DisplayName("default userId is 'anonymous'")
        void defaultUserIdIsAnonymous() {
            assertThat(MemoryVisibility.RequestContextHolder.getUserId()).isEqualTo("anonymous");
        }

        @Test
        @DisplayName("setUserId persists within the same thread")
        void setUserIdPersistsInSameThread() {
            MemoryVisibility.RequestContextHolder.setUserId("alice");
            assertThat(MemoryVisibility.RequestContextHolder.getUserId()).isEqualTo("alice");
        }

        @Test
        @DisplayName("audit log is empty initially")
        void auditLogEmptyInitially() {
            assertThat(MemoryVisibility.RequestContextHolder.getAuditLog()).isEmpty();
        }

        @Test
        @DisplayName("addAuditEntry adds entries to thread-local log")
        void addAuditEntryAddsEntries() {
            MemoryVisibility.RequestContextHolder.addAuditEntry("login");
            MemoryVisibility.RequestContextHolder.addAuditEntry("view-account");
            assertThat(MemoryVisibility.RequestContextHolder.getAuditLog())
                    .containsExactly("login", "view-account");
        }

        @Test
        @DisplayName("clear removes userId and audit log")
        void clearRemovesContext() {
            MemoryVisibility.RequestContextHolder.setUserId("bob");
            MemoryVisibility.RequestContextHolder.addAuditEntry("e1");
            MemoryVisibility.RequestContextHolder.clear();
            assertThat(MemoryVisibility.RequestContextHolder.getUserId()).isEqualTo("anonymous");
            assertThat(MemoryVisibility.RequestContextHolder.getAuditLog()).isEmpty();
        }

        @Test
        @DisplayName("each thread sees its own independent ThreadLocal values")
        void eachThreadHasIndependentValues() throws Exception {
            int threads = 5;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);
            List<String> observedIds = new CopyOnWriteArrayList<>();

            for (int i = 0; i < threads; i++) {
                final String userId = "user-" + i;
                new Thread(() -> {
                    try {
                        start.await();
                        MemoryVisibility.RequestContextHolder.setUserId(userId);
                        Thread.sleep(10); // ensure threads overlap
                        // Each thread reads its OWN userId — not another thread's
                        observedIds.add(MemoryVisibility.RequestContextHolder.getUserId());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        MemoryVisibility.RequestContextHolder.clear();
                        done.countDown();
                    }
                }).start();
            }

            start.countDown();
            done.await(5, TimeUnit.SECONDS);

            assertThat(observedIds).hasSize(threads);
            // Each thread saw its own userId (no cross-thread contamination)
            for (int i = 0; i < threads; i++) {
                assertThat(observedIds).contains("user-" + i);
            }
        }
    }

    // ── Staged Initializer ────────────────────────────────────────────────────

    @Nested
    @DisplayName("StagedInitializer — CountDownLatch happens-before")
    class StagedInitializerTest {

        @Test
        @DisplayName("awaitAndGetResults returns all worker results")
        void awaitAndGetResultsReturnsAllResults() throws Exception {
            int size = 5;
            var init = new MemoryVisibility.StagedInitializer(size);
            ExecutorService executor = Executors.newFixedThreadPool(size);

            for (int i = 0; i < size; i++) {
                executor.submit(init.createWorker(i, i * 10));
            }

            int[] results = init.awaitAndGetResults();
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            assertThat(results).containsExactly(0, 10, 20, 30, 40);
        }

        @Test
        @DisplayName("awaitWithTimeout returns true when all workers complete in time")
        void awaitWithTimeoutReturnsTrueOnTime() throws Exception {
            var init = new MemoryVisibility.StagedInitializer(1);
            new Thread(init.createWorker(0, 42)).start();
            boolean completed = init.awaitWithTimeout(2, TimeUnit.SECONDS);
            assertThat(completed).isTrue();
        }

        @Test
        @DisplayName("workers' writes are all visible after await returns")
        void workerWritesVisibleAfterAwait() throws Exception {
            int workers = 10;
            var init = new MemoryVisibility.StagedInitializer(workers);
            CountDownLatch start = new CountDownLatch(1);

            for (int i = 0; i < workers; i++) {
                final int idx = i;
                final int val = (idx + 1) * 100;
                new Thread(() -> {
                    try {
                        start.await();
                        init.createWorker(idx, val).run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }

            start.countDown();
            int[] results = init.awaitAndGetResults();

            for (int i = 0; i < workers; i++) {
                assertThat(results[i]).isEqualTo((i + 1) * 100);
            }
        }
    }

    // ── ReadHeavyRegistry ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("ReadHeavyRegistry — ReadWriteLock for read-heavy workloads")
    class ReadHeavyRegistryTest {

        private MemoryVisibility.ReadHeavyRegistry registry;

        @BeforeEach
        void setUp() {
            registry = new MemoryVisibility.ReadHeavyRegistry();
        }

        @Test
        @DisplayName("lookup returns null for missing key")
        void lookupReturnNullForMissing() {
            assertThat(registry.lookup("absent")).isNull();
        }

        @Test
        @DisplayName("register then lookup returns the value")
        void registerThenLookupReturnsValue() {
            registry.register("key", "value");
            assertThat(registry.lookup("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("contains returns true for registered key")
        void containsReturnsTrueForRegistered() {
            registry.register("k", "v");
            assertThat(registry.contains("k")).isTrue();
        }

        @Test
        @DisplayName("contains returns false for unregistered key")
        void containsReturnsFalseForUnregistered() {
            assertThat(registry.contains("missing")).isFalse();
        }

        @Test
        @DisplayName("remove returns true and removes the key")
        void removeReturnsTrueAndRemovesKey() {
            registry.register("k", "v");
            assertThat(registry.remove("k")).isTrue();
            assertThat(registry.contains("k")).isFalse();
        }

        @Test
        @DisplayName("remove returns false for absent key")
        void removeReturnsFalseForAbsent() {
            assertThat(registry.remove("absent")).isFalse();
        }

        @Test
        @DisplayName("size reflects number of registered entries")
        void sizeReflectsEntries() {
            registry.register("a", "1");
            registry.register("b", "2");
            assertThat(registry.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("concurrent reads and writes are consistent")
        void concurrentReadsAndWritesAreConsistent() throws Exception {
            int writers = 5;
            int readers = 10;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(writers + readers);
            AtomicInteger readErrors = new AtomicInteger(0);

            // Pre-populate
            for (int i = 0; i < 20; i++) {
                registry.register("key-" + i, "value-" + i);
            }

            // Writers: update existing keys
            for (int i = 0; i < writers; i++) {
                final int idx = i;
                new Thread(() -> {
                    try {
                        start.await();
                        registry.register("key-" + idx, "updated-" + idx);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                }).start();
            }

            // Readers: look up keys — should never see null for pre-populated keys
            for (int i = 0; i < readers; i++) {
                final int idx = i % 20;
                new Thread(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < 50; j++) {
                            // key-idx was pre-populated — value may have been updated but not null
                            String val = registry.lookup("key-" + idx);
                            if (val == null) readErrors.incrementAndGet();
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

            assertThat(readErrors.get()).isZero();
        }
    }

    // ── VirtualThreadFriendlyCounter ──────────────────────────────────────────

    @Nested
    @DisplayName("VirtualThreadFriendlyCounter — ReentrantLock")
    class VirtualThreadFriendlyCounterTest {

        @Test
        @DisplayName("initial count is zero")
        void initialCountIsZero() {
            var c = new MemoryVisibility.VirtualThreadFriendlyCounter();
            assertThat(c.get()).isEqualTo(0L);
        }

        @Test
        @DisplayName("increment increases count")
        void incrementIncreasesCount() {
            var c = new MemoryVisibility.VirtualThreadFriendlyCounter();
            c.increment();
            assertThat(c.get()).isEqualTo(1L);
        }

        @Test
        @DisplayName("tryIncrement returns true and increments within timeout")
        void tryIncrementReturnsTrueWithinTimeout() throws InterruptedException {
            var c = new MemoryVisibility.VirtualThreadFriendlyCounter();
            assertThat(c.tryIncrement(1000)).isTrue();
            assertThat(c.get()).isEqualTo(1L);
        }

        @Test
        @DisplayName("concurrent increments are correct")
        void concurrentIncrementsAreCorrect() throws Exception {
            var c = new MemoryVisibility.VirtualThreadFriendlyCounter();
            int threads = 15;
            int perThread = 300;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                new Thread(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < perThread; j++) c.increment();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                }).start();
            }

            start.countDown();
            done.await(10, TimeUnit.SECONDS);
            assertThat(c.get()).isEqualTo((long) threads * perThread);
        }
    }

    // ── SharedWordCounter ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("SharedWordCounter — ConcurrentHashMap safe shared access")
    class SharedWordCounterTest {

        @Test
        @DisplayName("getCount returns 0 for unseen word")
        void getCountReturnsZeroForUnseenWord() {
            var wc = new MemoryVisibility.SharedWordCounter();
            assertThat(wc.getCount("hello")).isEqualTo(0);
        }

        @Test
        @DisplayName("countWord increments count for that word")
        void countWordIncrements() {
            var wc = new MemoryVisibility.SharedWordCounter();
            wc.countWord("java");
            wc.countWord("java");
            wc.countWord("java");
            assertThat(wc.getCount("java")).isEqualTo(3);
        }

        @Test
        @DisplayName("getTotalCount sums all word counts")
        void getTotalCountSumsAll() {
            var wc = new MemoryVisibility.SharedWordCounter();
            wc.countWord("a");
            wc.countWord("b");
            wc.countWord("a");
            assertThat(wc.getTotalCount()).isEqualTo(3L);
        }

        @Test
        @DisplayName("getMostFrequent returns the highest-count word")
        void getMostFrequentReturnsHighestCount() {
            var wc = new MemoryVisibility.SharedWordCounter();
            wc.countWord("hello");
            wc.countWord("hello");
            wc.countWord("world");
            assertThat(wc.getMostFrequent()).contains("hello");
        }

        @Test
        @DisplayName("getMostFrequent returns empty when no words counted")
        void getMostFrequentEmptyWhenNoWords() {
            var wc = new MemoryVisibility.SharedWordCounter();
            assertThat(wc.getMostFrequent()).isEmpty();
        }

        @Test
        @DisplayName("distinctWordCount returns number of unique words")
        void distinctWordCountIsCorrect() {
            var wc = new MemoryVisibility.SharedWordCounter();
            wc.countWord("a");
            wc.countWord("b");
            wc.countWord("a");
            assertThat(wc.distinctWordCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("concurrent word counting produces correct totals")
        void concurrentWordCountingIsCorrect() throws Exception {
            var wc = new MemoryVisibility.SharedWordCounter();
            String[] words = {"java", "concurrency", "happens-before", "volatile", "synchronized"};
            int threadsPerWord = 10;
            int countsPerThread = 100;
            int totalThreads = words.length * threadsPerWord;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(totalThreads);

            for (String word : words) {
                for (int t = 0; t < threadsPerWord; t++) {
                    new Thread(() -> {
                        try {
                            start.await();
                            for (int j = 0; j < countsPerThread; j++) wc.countWord(word);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            done.countDown();
                        }
                    }).start();
                }
            }

            start.countDown();
            done.await(15, TimeUnit.SECONDS);

            int expectedPerWord = threadsPerWord * countsPerThread;
            for (String word : words) {
                assertThat(wc.getCount(word))
                        .as("Count for '%s' should be %d", word, expectedPerWord)
                        .isEqualTo(expectedPerWord);
            }
            assertThat(wc.getTotalCount()).isEqualTo((long) words.length * expectedPerWord);
        }
    }
}
