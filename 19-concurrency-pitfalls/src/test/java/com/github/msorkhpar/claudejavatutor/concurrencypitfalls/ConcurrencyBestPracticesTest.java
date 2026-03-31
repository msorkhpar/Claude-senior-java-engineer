package com.github.msorkhpar.claudejavatutor.concurrencypitfalls;

import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;

@DisplayName("5.3.4 Best Practices for Safe Concurrent Programming")
class ConcurrencyBestPracticesTest {

    // ── AppConfig (Immutable Record) ─────────────────────────────────────────

    @Nested
    @DisplayName("AppConfig -- immutable record for thread-safe sharing")
    class AppConfigTest {

        @Test
        @DisplayName("valid config is created successfully")
        void validConfigIsCreated() {
            var config = new ConcurrencyBestPractices.AppConfig("localhost", 8080, true, 3);
            assertThat(config.host()).isEqualTo("localhost");
            assertThat(config.port()).isEqualTo(8080);
            assertThat(config.sslEnabled()).isTrue();
            assertThat(config.maxRetries()).isEqualTo(3);
        }

        @Test
        @DisplayName("null host throws NullPointerException")
        void nullHostThrowsNpe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ConcurrencyBestPractices.AppConfig(null, 80, false, 0))
                    .withMessageContaining("host");
        }

        @Test
        @DisplayName("negative port throws IllegalArgumentException")
        void negativePortThrows() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ConcurrencyBestPractices.AppConfig("h", -1, false, 0))
                    .withMessageContaining("Invalid port");
        }

        @Test
        @DisplayName("port above 65535 throws IllegalArgumentException")
        void portAbove65535Throws() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ConcurrencyBestPractices.AppConfig("h", 70000, false, 0))
                    .withMessageContaining("Invalid port");
        }

        @Test
        @DisplayName("negative maxRetries throws IllegalArgumentException")
        void negativeMaxRetriesThrows() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ConcurrencyBestPractices.AppConfig("h", 80, false, -1))
                    .withMessageContaining("maxRetries");
        }

        @Test
        @DisplayName("withHost returns new config with updated host")
        void withHostReturnsNewConfig() {
            var original = new ConcurrencyBestPractices.AppConfig("old", 80, false, 1);
            var updated = original.withHost("new");
            assertThat(updated.host()).isEqualTo("new");
            assertThat(updated.port()).isEqualTo(80);
            assertThat(original.host()).isEqualTo("old"); // original unchanged
        }

        @Test
        @DisplayName("withPort returns new config with updated port")
        void withPortReturnsNewConfig() {
            var original = new ConcurrencyBestPractices.AppConfig("host", 80, true, 2);
            var updated = original.withPort(443);
            assertThat(updated.port()).isEqualTo(443);
            assertThat(original.port()).isEqualTo(80);
        }

        @Test
        @DisplayName("equals and hashCode are consistent for records")
        void equalsAndHashCodeConsistent() {
            var c1 = new ConcurrencyBestPractices.AppConfig("h", 80, true, 3);
            var c2 = new ConcurrencyBestPractices.AppConfig("h", 80, true, 3);
            assertThat(c1).isEqualTo(c2);
            assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
        }

        @Test
        @DisplayName("boundary port values are valid (0 and 65535)")
        void boundaryPortValuesAreValid() {
            assertThatCode(() -> new ConcurrencyBestPractices.AppConfig("h", 0, false, 0))
                    .doesNotThrowAnyException();
            assertThatCode(() -> new ConcurrencyBestPractices.AppConfig("h", 65535, false, 0))
                    .doesNotThrowAnyException();
        }
    }

    // ── ConfigManager ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ConfigManager -- volatile reference for atomic config replacement")
    class ConfigManagerTest {

        @Test
        @DisplayName("getConfig returns initial config")
        void getConfigReturnsInitialConfig() {
            var config = new ConcurrencyBestPractices.AppConfig("localhost", 8080, false, 0);
            var manager = new ConcurrencyBestPractices.ConfigManager(config);
            assertThat(manager.getConfig()).isSameAs(config);
        }

        @Test
        @DisplayName("updateConfig replaces the config")
        void updateConfigReplacesConfig() {
            var c1 = new ConcurrencyBestPractices.AppConfig("v1", 80, false, 0);
            var c2 = new ConcurrencyBestPractices.AppConfig("v2", 443, true, 5);
            var manager = new ConcurrencyBestPractices.ConfigManager(c1);
            manager.updateConfig(c2);
            assertThat(manager.getConfig()).isSameAs(c2);
        }

        @Test
        @DisplayName("null update throws NullPointerException")
        void nullUpdateThrowsNpe() {
            var manager = new ConcurrencyBestPractices.ConfigManager(
                    new ConcurrencyBestPractices.AppConfig("h", 80, false, 0));
            assertThatNullPointerException().isThrownBy(() -> manager.updateConfig(null));
        }

        @Test
        @DisplayName("concurrent readers always see a fully initialized config")
        void concurrentReadersAlwaysSeeFullConfig() throws InterruptedException {
            var initial = new ConcurrencyBestPractices.AppConfig("init", 80, false, 0);
            var manager = new ConcurrencyBestPractices.ConfigManager(initial);
            AtomicInteger errors = new AtomicInteger(0);
            int totalThreads = 30;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(totalThreads);

            // 10 writers
            for (int i = 0; i < 10; i++) {
                final int idx = i;
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < 50; j++) {
                            manager.updateConfig(new ConcurrencyBestPractices.AppConfig(
                                    "host-" + idx, 8080 + idx, idx % 2 == 0, idx));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            // 20 readers
            for (int i = 0; i < 20; i++) {
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < 200; j++) {
                            var cfg = manager.getConfig();
                            // Config should always be non-null and have a non-null host
                            if (cfg == null || cfg.host() == null) {
                                errors.incrementAndGet();
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
            assertThat(errors.get()).isZero();
        }
    }

    // ── RequestContext (ThreadLocal) ──────────────────────────────────────────

    @Nested
    @DisplayName("RequestContext -- ThreadLocal for per-thread isolation")
    class RequestContextTest {

        @AfterEach
        void cleanup() {
            ConcurrencyBestPractices.RequestContext.clear();
        }

        @Test
        @DisplayName("initial request ID is empty string")
        void initialRequestIdIsEmpty() {
            assertThat(ConcurrencyBestPractices.RequestContext.getRequestId()).isEmpty();
        }

        @Test
        @DisplayName("begin sets request ID")
        void beginSetsRequestId() {
            ConcurrencyBestPractices.RequestContext.begin("req-123");
            assertThat(ConcurrencyBestPractices.RequestContext.getRequestId()).isEqualTo("req-123");
        }

        @Test
        @DisplayName("getElapsedNanos returns a positive value after begin")
        void getElapsedNanosReturnsPositive() throws InterruptedException {
            ConcurrencyBestPractices.RequestContext.begin("req-1");
            Thread.sleep(5);
            assertThat(ConcurrencyBestPractices.RequestContext.getElapsedNanos()).isGreaterThan(0);
        }

        @Test
        @DisplayName("clear removes request context")
        void clearRemovesContext() {
            ConcurrencyBestPractices.RequestContext.begin("req-abc");
            ConcurrencyBestPractices.RequestContext.clear();
            assertThat(ConcurrencyBestPractices.RequestContext.getRequestId()).isEmpty();
        }

        @Test
        @DisplayName("each thread has its own independent request context")
        void eachThreadHasIndependentContext() throws InterruptedException {
            int threads = 10;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);
            CopyOnWriteArrayList<String> observedIds = new CopyOnWriteArrayList<>();

            for (int i = 0; i < threads; i++) {
                final String id = "thread-" + i;
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        ConcurrencyBestPractices.RequestContext.begin(id);
                        Thread.sleep(10); // overlap threads
                        observedIds.add(ConcurrencyBestPractices.RequestContext.getRequestId());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        ConcurrencyBestPractices.RequestContext.clear();
                        done.countDown();
                    }
                });
            }

            start.countDown();
            done.await(10, TimeUnit.SECONDS);

            assertThat(observedIds).hasSize(threads);
            for (int i = 0; i < threads; i++) {
                assertThat(observedIds).contains("thread-" + i);
            }
        }
    }

    // ── ServiceRegistry (ConcurrentHashMap) ──────────────────────────────────

    @Nested
    @DisplayName("ServiceRegistry -- ConcurrentHashMap for thread-safe maps")
    class ServiceRegistryTest {

        @Test
        @DisplayName("register returns true for new service")
        void registerReturnsTrueForNew() {
            var registry = new ConcurrencyBestPractices.ServiceRegistry();
            assertThat(registry.register("svc-1", "http://localhost:8080")).isTrue();
        }

        @Test
        @DisplayName("register returns false for duplicate service")
        void registerReturnsFalseForDuplicate() {
            var registry = new ConcurrencyBestPractices.ServiceRegistry();
            registry.register("svc-1", "http://localhost:8080");
            assertThat(registry.register("svc-1", "http://other:9090")).isFalse();
        }

        @Test
        @DisplayName("lookup returns the registered endpoint")
        void lookupReturnsRegisteredEndpoint() {
            var registry = new ConcurrencyBestPractices.ServiceRegistry();
            registry.register("svc-1", "http://host:8080");
            assertThat(registry.lookup("svc-1")).contains("http://host:8080");
        }

        @Test
        @DisplayName("lookup returns empty for unregistered service")
        void lookupReturnsEmptyForUnregistered() {
            var registry = new ConcurrencyBestPractices.ServiceRegistry();
            assertThat(registry.lookup("missing")).isEmpty();
        }

        @Test
        @DisplayName("unregister returns true and removes the service")
        void unregisterReturnsTrueAndRemoves() {
            var registry = new ConcurrencyBestPractices.ServiceRegistry();
            registry.register("svc-1", "endpoint");
            assertThat(registry.unregister("svc-1")).isTrue();
            assertThat(registry.lookup("svc-1")).isEmpty();
        }

        @Test
        @DisplayName("unregister returns false for missing service")
        void unregisterReturnsFalseForMissing() {
            var registry = new ConcurrencyBestPractices.ServiceRegistry();
            assertThat(registry.unregister("missing")).isFalse();
        }

        @Test
        @DisplayName("getServiceNames returns all registered names")
        void getServiceNamesReturnsAll() {
            var registry = new ConcurrencyBestPractices.ServiceRegistry();
            registry.register("a", "1");
            registry.register("b", "2");
            assertThat(registry.getServiceNames()).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("size reflects registered services")
        void sizeReflectsRegisteredServices() {
            var registry = new ConcurrencyBestPractices.ServiceRegistry();
            registry.register("a", "1");
            registry.register("b", "2");
            assertThat(registry.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("concurrent registrations do not lose entries")
        void concurrentRegistrationsDoNotLoseEntries() throws InterruptedException {
            var registry = new ConcurrencyBestPractices.ServiceRegistry();
            int threads = 50;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                final String name = "svc-" + i;
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        registry.register(name, "endpoint-" + name);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            start.countDown();
            done.await(10, TimeUnit.SECONDS);
            assertThat(registry.size()).isEqualTo(threads);
        }
    }

    // ── ProducerConsumer (BlockingQueue) ──────────────────────────────────────

    @Nested
    @DisplayName("ProducerConsumer -- BlockingQueue for thread-safe handoff")
    class ProducerConsumerTest {

        @Test
        @DisplayName("produce and consume round-trip works")
        void produceAndConsumeRoundTrip() throws InterruptedException {
            var pc = new ConcurrencyBestPractices.ProducerConsumer<String>(10);
            pc.produce("hello");
            assertThat(pc.consume()).isEqualTo("hello");
        }

        @Test
        @DisplayName("tryProduce returns false when queue is full")
        void tryProduceReturnsFalseWhenFull() throws InterruptedException {
            var pc = new ConcurrencyBestPractices.ProducerConsumer<String>(1);
            pc.produce("item");
            boolean result = pc.tryProduce("overflow", 50);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("tryConsume returns null when queue is empty")
        void tryConsumeReturnsNullWhenEmpty() throws InterruptedException {
            var pc = new ConcurrencyBestPractices.ProducerConsumer<String>(10);
            assertThat(pc.tryConsume(50)).isNull();
        }

        @Test
        @DisplayName("produced and consumed counts are tracked correctly")
        void countsAreTracked() throws InterruptedException {
            var pc = new ConcurrencyBestPractices.ProducerConsumer<Integer>(10);
            pc.produce(1);
            pc.produce(2);
            pc.consume();
            assertThat(pc.getProducedCount()).isEqualTo(2);
            assertThat(pc.getConsumedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("queueSize reflects current items")
        void queueSizeReflectsCurrentItems() throws InterruptedException {
            var pc = new ConcurrencyBestPractices.ProducerConsumer<Integer>(10);
            pc.produce(1);
            pc.produce(2);
            assertThat(pc.queueSize()).isEqualTo(2);
            pc.consume();
            assertThat(pc.queueSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("multiple producers and consumers transfer all items correctly")
        void multipleProducersAndConsumersTransferAllItems() throws InterruptedException {
            var pc = new ConcurrencyBestPractices.ProducerConsumer<Integer>(100);
            int producers = 10;
            int consumers = 10;
            int itemsPerProducer = 100;
            int totalItems = producers * itemsPerProducer;

            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch producersDone = new CountDownLatch(producers);
            CountDownLatch consumersDone = new CountDownLatch(consumers);
            AtomicInteger consumed = new AtomicInteger(0);

            // Producers
            for (int p = 0; p < producers; p++) {
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        for (int i = 0; i < itemsPerProducer; i++) {
                            pc.produce(i);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        producersDone.countDown();
                    }
                });
            }

            // Consumers
            for (int c = 0; c < consumers; c++) {
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        while (true) {
                            Integer item = pc.tryConsume(200);
                            if (item != null) {
                                consumed.incrementAndGet();
                            } else {
                                // Check if all producers are done and queue is empty
                                if (producersDone.await(0, TimeUnit.MILLISECONDS) && pc.queueSize() == 0) {
                                    break;
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        consumersDone.countDown();
                    }
                });
            }

            start.countDown();
            producersDone.await(15, TimeUnit.SECONDS);
            consumersDone.await(15, TimeUnit.SECONDS);

            assertThat(consumed.get()).isEqualTo(totalItems);
        }
    }

    // ── ReadWriteCache ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("ReadWriteCache -- ReadWriteLock for read-heavy workloads")
    class ReadWriteCacheTest {

        @Test
        @DisplayName("put and get work correctly")
        void putAndGetWork() {
            var cache = new ConcurrencyBestPractices.ReadWriteCache<String, Integer>();
            cache.put("key", 42);
            assertThat(cache.get("key")).isEqualTo(42);
        }

        @Test
        @DisplayName("get returns null for missing key")
        void getReturnsNullForMissing() {
            var cache = new ConcurrencyBestPractices.ReadWriteCache<String, Integer>();
            assertThat(cache.get("missing")).isNull();
        }

        @Test
        @DisplayName("containsKey returns true for existing key")
        void containsKeyReturnsTrueForExisting() {
            var cache = new ConcurrencyBestPractices.ReadWriteCache<String, String>();
            cache.put("k", "v");
            assertThat(cache.containsKey("k")).isTrue();
        }

        @Test
        @DisplayName("containsKey returns false for missing key")
        void containsKeyReturnsFalseForMissing() {
            var cache = new ConcurrencyBestPractices.ReadWriteCache<String, String>();
            assertThat(cache.containsKey("k")).isFalse();
        }

        @Test
        @DisplayName("remove returns the removed value")
        void removeReturnsRemovedValue() {
            var cache = new ConcurrencyBestPractices.ReadWriteCache<String, String>();
            cache.put("k", "v");
            assertThat(cache.remove("k")).isEqualTo("v");
            assertThat(cache.containsKey("k")).isFalse();
        }

        @Test
        @DisplayName("remove returns null for missing key")
        void removeReturnsNullForMissing() {
            var cache = new ConcurrencyBestPractices.ReadWriteCache<String, String>();
            assertThat(cache.remove("k")).isNull();
        }

        @Test
        @DisplayName("size reflects the number of entries")
        void sizeReflectsEntries() {
            var cache = new ConcurrencyBestPractices.ReadWriteCache<String, String>();
            cache.put("a", "1");
            cache.put("b", "2");
            assertThat(cache.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("snapshot returns a defensive copy")
        void snapshotReturnsDefensiveCopy() {
            var cache = new ConcurrencyBestPractices.ReadWriteCache<String, String>();
            cache.put("a", "1");
            var snap = cache.snapshot();
            cache.put("b", "2");
            // Snapshot should not reflect the later addition
            assertThat(snap).hasSize(1);
            assertThat(snap).containsEntry("a", "1");
        }

        @Test
        @DisplayName("concurrent reads and writes are consistent")
        void concurrentReadsAndWritesAreConsistent() throws InterruptedException {
            var cache = new ConcurrencyBestPractices.ReadWriteCache<String, String>();
            // Pre-populate
            for (int i = 0; i < 20; i++) {
                cache.put("key-" + i, "val-" + i);
            }

            AtomicInteger errors = new AtomicInteger(0);
            int totalThreads = 30;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(totalThreads);

            // Writers
            for (int i = 0; i < 10; i++) {
                final int idx = i;
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < 50; j++) {
                            cache.put("key-" + idx, "updated-" + idx + "-" + j);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            // Readers
            for (int i = 0; i < 20; i++) {
                final int idx = i % 20;
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < 100; j++) {
                            String val = cache.get("key-" + idx);
                            // val should never be null for pre-populated keys
                            if (val == null) errors.incrementAndGet();
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
            assertThat(errors.get()).isZero();
        }
    }

    // ── MinimalLockScope ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("MinimalLockScope -- computing outside the lock, storing inside")
    class MinimalLockScopeTest {

        @Test
        @DisplayName("processAndStore returns the computed result")
        void processAndStoreReturnsResult() {
            var mls = new ConcurrencyBestPractices.MinimalLockScope();
            String result = mls.processAndStore("hello");
            assertThat(result).isEqualTo("HELLO-PROCESSED");
        }

        @Test
        @DisplayName("getResults returns all stored results")
        void getResultsReturnsAllStored() {
            var mls = new ConcurrencyBestPractices.MinimalLockScope();
            mls.processAndStore("a");
            mls.processAndStore("b");
            assertThat(mls.getResults()).containsExactly("A-PROCESSED", "B-PROCESSED");
        }

        @Test
        @DisplayName("getResults returns a defensive copy")
        void getResultsReturnsDefensiveCopy() {
            var mls = new ConcurrencyBestPractices.MinimalLockScope();
            mls.processAndStore("x");
            List<String> results = mls.getResults();
            mls.processAndStore("y");
            assertThat(results).hasSize(1); // snapshot should not change
        }

        @RepeatedTest(3)
        @DisplayName("concurrent processAndStore does not lose entries")
        void concurrentProcessAndStoreDoesNotLoseEntries() throws InterruptedException {
            var mls = new ConcurrencyBestPractices.MinimalLockScope();
            int threads = 50;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                final int idx = i;
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        mls.processAndStore("item-" + idx);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            start.countDown();
            done.await(10, TimeUnit.SECONDS);
            assertThat(mls.getResults()).hasSize(threads);
        }
    }

    // ── EventListenerRegistry (CopyOnWriteArrayList) ─────────────────────────

    @Nested
    @DisplayName("EventListenerRegistry -- CopyOnWriteArrayList for read-heavy lists")
    class EventListenerRegistryTest {

        @Test
        @DisplayName("addListener increases count")
        void addListenerIncreasesCount() {
            var registry = new ConcurrencyBestPractices.EventListenerRegistry();
            registry.addListener("listener-1");
            assertThat(registry.listenerCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("removeListener returns true and removes")
        void removeListenerRemoves() {
            var registry = new ConcurrencyBestPractices.EventListenerRegistry();
            registry.addListener("listener-1");
            assertThat(registry.removeListener("listener-1")).isTrue();
            assertThat(registry.listenerCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("removeListener returns false for missing listener")
        void removeListenerReturnsFalseForMissing() {
            var registry = new ConcurrencyBestPractices.EventListenerRegistry();
            assertThat(registry.removeListener("missing")).isFalse();
        }

        @Test
        @DisplayName("hasListener returns true for registered listener")
        void hasListenerReturnsTrueForRegistered() {
            var registry = new ConcurrencyBestPractices.EventListenerRegistry();
            registry.addListener("A");
            assertThat(registry.hasListener("A")).isTrue();
        }

        @Test
        @DisplayName("hasListener returns false for unregistered listener")
        void hasListenerReturnsFalseForUnregistered() {
            var registry = new ConcurrencyBestPractices.EventListenerRegistry();
            assertThat(registry.hasListener("X")).isFalse();
        }

        @Test
        @DisplayName("getListeners returns a defensive copy")
        void getListenersReturnsDefensiveCopy() {
            var registry = new ConcurrencyBestPractices.EventListenerRegistry();
            registry.addListener("A");
            var listeners = registry.getListeners();
            registry.addListener("B");
            assertThat(listeners).hasSize(1);
        }

        @Test
        @DisplayName("concurrent add and iterate never throws ConcurrentModificationException")
        void concurrentAddAndIterateNeverThrows() throws InterruptedException {
            var registry = new ConcurrencyBestPractices.EventListenerRegistry();
            AtomicInteger errors = new AtomicInteger(0);
            int totalThreads = 30;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(totalThreads);

            // Writers
            for (int i = 0; i < 15; i++) {
                final int idx = i;
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < 20; j++) {
                            registry.addListener("listener-" + idx + "-" + j);
                        }
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    } finally {
                        done.countDown();
                    }
                });
            }

            // Readers (iterate)
            for (int i = 0; i < 15; i++) {
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < 50; j++) {
                            // This should never throw ConcurrentModificationException
                            registry.getListeners();
                        }
                    } catch (ConcurrentModificationException e) {
                        errors.incrementAndGet();
                    } catch (Exception e) {
                        // Other exceptions are not expected but do not count as CME
                    } finally {
                        done.countDown();
                    }
                });
            }

            start.countDown();
            done.await(10, TimeUnit.SECONDS);
            assertThat(errors.get()).isZero();
        }
    }
}
