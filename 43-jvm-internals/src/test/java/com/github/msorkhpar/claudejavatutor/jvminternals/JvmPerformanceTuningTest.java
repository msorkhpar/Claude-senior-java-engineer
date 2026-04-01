package com.github.msorkhpar.claudejavatutor.jvminternals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JVM Performance Tuning Tests")
class JvmPerformanceTuningTest {

    @Nested
    @DisplayName("GC Monitor")
    class GcMonitorTest {

        private final JvmPerformanceTuning.GcMonitor monitor = new JvmPerformanceTuning.GcMonitor();

        @Test
        @DisplayName("Should return at least one GC collector")
        void testGetAllGcStats() {
            List<JvmPerformanceTuning.GcMonitor.GcStats> stats = monitor.getAllGcStats();
            assertThat(stats).isNotEmpty();
        }

        @Test
        @DisplayName("Should have non-negative collection counts")
        void testGcCollectionCounts() {
            List<JvmPerformanceTuning.GcMonitor.GcStats> stats = monitor.getAllGcStats();
            for (var gc : stats) {
                assertThat(gc.collectionCount()).isGreaterThanOrEqualTo(0);
                assertThat(gc.collectionTimeMs()).isGreaterThanOrEqualTo(0);
            }
        }

        @Test
        @DisplayName("Should return non-empty GC names")
        void testGetGcNames() {
            List<String> names = monitor.getGcNames();
            assertThat(names).isNotEmpty();
            assertThat(names).allMatch(name -> name != null && !name.isEmpty());
        }

        @Test
        @DisplayName("Should return non-negative total GC time")
        void testGetTotalGcTime() {
            assertThat(monitor.getTotalGcTimeMs()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should return non-negative total GC count")
        void testGetTotalGcCount() {
            assertThat(monitor.getTotalGcCount()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should have GcStats with memory pool names")
        void testGcStatsHavePoolNames() {
            List<JvmPerformanceTuning.GcMonitor.GcStats> stats = monitor.getAllGcStats();
            for (var gc : stats) {
                assertThat(gc.name()).isNotEmpty();
                assertThat(gc.memoryPoolNames()).isNotNull();
            }
        }

        @Test
        @DisplayName("Should execute requestGc without error")
        void testRequestGc() {
            assertThatCode(() -> monitor.requestGc()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Memory Monitor")
    class MemoryMonitorTest {

        private final JvmPerformanceTuning.MemoryMonitor monitor = new JvmPerformanceTuning.MemoryMonitor();

        @Test
        @DisplayName("Should return positive heap usage")
        void testGetHeapUsage() {
            var usage = monitor.getHeapUsage();
            assertThat(usage.usedBytes()).isPositive();
            assertThat(usage.committedBytes()).isPositive();
            assertThat(usage.maxBytes()).isPositive();
            assertThat(usage.poolName()).isEqualTo("Heap");
            assertThat(usage.type()).isEqualTo("HEAP");
        }

        @Test
        @DisplayName("Should return positive non-heap usage")
        void testGetNonHeapUsage() {
            var usage = monitor.getNonHeapUsage();
            assertThat(usage.usedBytes()).isPositive();
            assertThat(usage.poolName()).isEqualTo("Non-Heap");
        }

        @Test
        @DisplayName("Should return non-empty memory pool list")
        void testGetAllMemoryPools() {
            var pools = monitor.getAllMemoryPools();
            assertThat(pools).isNotEmpty();
            for (var pool : pools) {
                assertThat(pool.poolName()).isNotEmpty();
                assertThat(pool.type()).isNotEmpty();
                assertThat(pool.usedBytes()).isGreaterThanOrEqualTo(0);
            }
        }

        @Test
        @DisplayName("Should return positive max heap bytes")
        void testGetMaxHeapBytes() {
            assertThat(monitor.getMaxHeapBytes()).isPositive();
        }

        @Test
        @DisplayName("Should return non-negative free heap bytes")
        void testGetFreeHeapBytes() {
            assertThat(monitor.getFreeHeapBytes()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should return positive total heap bytes")
        void testGetTotalHeapBytes() {
            assertThat(monitor.getTotalHeapBytes()).isPositive();
        }

        @Test
        @DisplayName("Should have used + free = total heap")
        void testHeapBytesAddUp() {
            long used = monitor.getUsedHeapBytes();
            long free = monitor.getFreeHeapBytes();
            long total = monitor.getTotalHeapBytes();
            assertThat(used + free).isEqualTo(total);
        }

        @Test
        @DisplayName("Should return heap usage percentage between 0 and 100")
        void testGetHeapUsagePercent() {
            double percent = monitor.getHeapUsagePercent();
            assertThat(percent).isBetween(0.0, 100.0);
        }

        @Test
        @DisplayName("Should calculate MB correctly")
        void testUsageMBConversion() {
            var usage = monitor.getHeapUsage();
            assertThat(usage.usedMB()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should calculate usage percentage correctly")
        void testUsagePercentCalculation() {
            var usage = monitor.getHeapUsage();
            if (usage.maxBytes() > 0) {
                assertThat(usage.usagePercent()).isBetween(0.0, 100.0);
            }
        }
    }

    @Nested
    @DisplayName("Thread Monitor")
    class ThreadMonitorTest {

        private final JvmPerformanceTuning.ThreadMonitor monitor = new JvmPerformanceTuning.ThreadMonitor();

        @Test
        @DisplayName("Should return positive thread count")
        void testGetCurrentThreadCount() {
            assertThat(monitor.getCurrentThreadCount()).isPositive();
        }

        @Test
        @DisplayName("Should return positive peak thread count")
        void testGetPeakThreadCount() {
            assertThat(monitor.getPeakThreadCount()).isPositive();
        }

        @Test
        @DisplayName("Should have peak >= current thread count")
        void testPeakGreaterThanOrEqualCurrent() {
            assertThat(monitor.getPeakThreadCount())
                    .isGreaterThanOrEqualTo(monitor.getCurrentThreadCount());
        }

        @Test
        @DisplayName("Should return positive total started thread count")
        void testGetTotalStartedThreadCount() {
            assertThat(monitor.getTotalStartedThreadCount()).isPositive();
        }

        @Test
        @DisplayName("Should return non-negative daemon thread count")
        void testGetDaemonThreadCount() {
            assertThat(monitor.getDaemonThreadCount()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should detect no deadlocks in normal operation")
        void testDetectDeadlocks() {
            long[] deadlocked = monitor.detectDeadlocks();
            assertThat(deadlocked).isEmpty();
        }

        @Test
        @DisplayName("Should return non-empty thread summaries")
        void testGetAllThreadSummaries() {
            var summaries = monitor.getAllThreadSummaries();
            assertThat(summaries).isNotEmpty();
            // Main thread should be present
            assertThat(summaries).anyMatch(t -> t.name().contains("main"));
        }

        @Test
        @DisplayName("Should have thread states in distribution")
        void testGetThreadStateDistribution() {
            Map<Thread.State, Long> distribution = monitor.getThreadStateDistribution();
            assertThat(distribution).isNotEmpty();
            // There must be at least one RUNNABLE thread (the current thread)
            assertThat(distribution).containsKey(Thread.State.RUNNABLE);
        }

        @Test
        @DisplayName("Should reflect new threads in count")
        void testThreadCountAfterCreation() throws InterruptedException {
            int before = monitor.getCurrentThreadCount();
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            t.start();
            // Allow thread to start
            Thread.sleep(50);
            int during = monitor.getCurrentThreadCount();
            t.join();
            assertThat(during).isGreaterThanOrEqualTo(before);
        }
    }

    @Nested
    @DisplayName("Runtime Info")
    class RuntimeInfoTest {

        private final JvmPerformanceTuning.RuntimeInfo info = new JvmPerformanceTuning.RuntimeInfo();

        @Test
        @DisplayName("Should return complete JVM info")
        void testGetJvmInfo() {
            var jvmInfo = info.getJvmInfo();
            assertThat(jvmInfo.vmName()).isNotEmpty();
            assertThat(jvmInfo.vmVersion()).isNotEmpty();
            assertThat(jvmInfo.vmVendor()).isNotEmpty();
            assertThat(jvmInfo.javaVersion()).isNotEmpty();
            assertThat(jvmInfo.uptimeMs()).isPositive();
            assertThat(jvmInfo.availableProcessors()).isGreaterThanOrEqualTo(1);
            assertThat(jvmInfo.inputArguments()).isNotNull();
        }

        @Test
        @DisplayName("Should return JVM system properties")
        void testGetJvmSystemProperties() {
            Map<String, String> props = info.getJvmSystemProperties();
            assertThat(props).containsKeys("java.version", "java.vendor", "java.home",
                    "os.name", "os.arch", "java.vm.name", "java.vm.version");
            assertThat(props.get("java.version")).isNotEmpty();
        }

        @Test
        @DisplayName("Should return boolean for container detection")
        void testIsLikelyContainer() {
            // Just verify it doesn't throw -- result depends on environment
            assertThatCode(() -> info.isLikelyContainer()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Allocation Patterns")
    class AllocationPatternsTest {

        private final JvmPerformanceTuning.AllocationPatterns patterns =
                new JvmPerformanceTuning.AllocationPatterns();

        @Test
        @DisplayName("Should compute total length from short-lived allocations")
        void testShortLivedAllocations() {
            int total = patterns.shortLivedAllocations(100);
            assertThat(total).isPositive();
        }

        @Test
        @DisplayName("Should handle zero short-lived allocations")
        void testShortLivedAllocationsZero() {
            assertThat(patterns.shortLivedAllocations(0)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should create long-lived allocations of correct size")
        void testLongLivedAllocations() {
            List<byte[]> data = patterns.longLivedAllocations(5, 1024);
            assertThat(data).hasSize(5);
            for (byte[] arr : data) {
                assertThat(arr).hasSize(1024);
            }
        }

        @Test
        @DisplayName("Should handle empty long-lived allocations")
        void testLongLivedAllocationsEmpty() {
            assertThat(patterns.longLivedAllocations(0, 100)).isEmpty();
        }

        @Test
        @DisplayName("Should return non-negative GC impact count")
        void testMeasureAllocationImpact() {
            long gcDelta = patterns.measureAllocationImpact(100, 64);
            assertThat(gcDelta).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Object Pool")
    class ObjectPoolTest {

        @Test
        @DisplayName("Should acquire objects from factory when pool is empty")
        void testAcquireFromEmptyPool() {
            var pool = new JvmPerformanceTuning.AllocationPatterns.SimpleObjectPool<>(
                    StringBuilder::new, 5);
            StringBuilder sb = pool.acquire();
            assertThat(sb).isNotNull();
            assertThat(pool.poolSize()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should release and re-acquire objects")
        void testReleaseAndReacquire() {
            var pool = new JvmPerformanceTuning.AllocationPatterns.SimpleObjectPool<>(
                    StringBuilder::new, 5);
            StringBuilder sb = pool.acquire();
            sb.append("test");
            pool.release(sb);
            assertThat(pool.poolSize()).isEqualTo(1);

            StringBuilder reacquired = pool.acquire();
            assertThat(reacquired).isSameAs(sb);
            assertThat(pool.poolSize()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should respect max pool size")
        void testMaxPoolSize() {
            var pool = new JvmPerformanceTuning.AllocationPatterns.SimpleObjectPool<>(
                    StringBuilder::new, 2);
            pool.release(new StringBuilder());
            pool.release(new StringBuilder());
            pool.release(new StringBuilder()); // Exceeds max size

            assertThat(pool.poolSize()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should start with empty pool")
        void testInitialPoolSize() {
            var pool = new JvmPerformanceTuning.AllocationPatterns.SimpleObjectPool<>(
                    StringBuilder::new, 10);
            assertThat(pool.poolSize()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Memory Leak Patterns")
    class MemoryLeakPatternsTest {

        @Test
        @DisplayName("Should demonstrate unbounded cache growth")
        void testUnboundedCacheGrowth() {
            var cache = new JvmPerformanceTuning.MemoryLeakPatterns.UnboundedCache();
            for (int i = 0; i < 1000; i++) {
                cache.put("key-" + i, "value-" + i);
            }
            assertThat(cache.size()).isEqualTo(1000);
            assertThat(cache.get("key-0")).isEqualTo("value-0");
            assertThat(cache.get("key-999")).isEqualTo("value-999");
        }

        @Test
        @DisplayName("Should clear unbounded cache")
        void testUnboundedCacheClear() {
            var cache = new JvmPerformanceTuning.MemoryLeakPatterns.UnboundedCache();
            cache.put("key", "value");
            cache.clear();
            assertThat(cache.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should bound LRU cache to max size")
        void testBoundedLruCache() {
            var cache = new JvmPerformanceTuning.MemoryLeakPatterns.BoundedLruCache(5);
            for (int i = 0; i < 10; i++) {
                cache.put("key-" + i, "value-" + i);
            }
            assertThat(cache.size()).isEqualTo(5);
            assertThat(cache.maxSize()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should evict oldest entries in LRU cache")
        void testLruEviction() {
            var cache = new JvmPerformanceTuning.MemoryLeakPatterns.BoundedLruCache(3);
            cache.put("a", "1");
            cache.put("b", "2");
            cache.put("c", "3");
            cache.put("d", "4"); // Should evict "a"

            assertThat(cache.get("a")).isNull();
            assertThat(cache.get("b")).isEqualTo("2");
            assertThat(cache.get("d")).isEqualTo("4");
        }

        @Test
        @DisplayName("Should handle LRU cache access order update")
        void testLruAccessOrder() {
            var cache = new JvmPerformanceTuning.MemoryLeakPatterns.BoundedLruCache(3);
            cache.put("a", "1");
            cache.put("b", "2");
            cache.put("c", "3");
            cache.get("a"); // Access "a" -- makes it most recently used
            cache.put("d", "4"); // Should evict "b" (least recently used)

            assertThat(cache.get("a")).isEqualTo("1"); // Still present
            assertThat(cache.get("b")).isNull();        // Evicted
            assertThat(cache.get("c")).isEqualTo("3");
            assertThat(cache.get("d")).isEqualTo("4");
        }

        @Test
        @DisplayName("Should demonstrate safe ThreadLocal usage")
        void testSafeThreadLocal() {
            try {
                List<String> data = new ArrayList<>();
                data.add("test");
                JvmPerformanceTuning.MemoryLeakPatterns.SafeThreadLocal.set(data);

                assertThat(JvmPerformanceTuning.MemoryLeakPatterns.SafeThreadLocal.get())
                        .containsExactly("test");
            } finally {
                JvmPerformanceTuning.MemoryLeakPatterns.SafeThreadLocal.cleanup();
            }

            assertThat(JvmPerformanceTuning.MemoryLeakPatterns.SafeThreadLocal.get()).isNull();
        }

        @Test
        @DisplayName("Should return null for unset ThreadLocal")
        void testThreadLocalDefault() {
            JvmPerformanceTuning.MemoryLeakPatterns.SafeThreadLocal.cleanup();
            assertThat(JvmPerformanceTuning.MemoryLeakPatterns.SafeThreadLocal.get()).isNull();
        }
    }
}
