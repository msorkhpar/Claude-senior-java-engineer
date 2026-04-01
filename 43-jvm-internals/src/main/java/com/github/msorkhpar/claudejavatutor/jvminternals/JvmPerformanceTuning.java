package com.github.msorkhpar.claudejavatutor.jvminternals;

import java.lang.management.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Demonstrates JVM performance tuning and monitoring concepts including
 * GC monitoring, memory analysis, thread diagnostics, and runtime information.
 */
public class JvmPerformanceTuning {

    /**
     * Provides garbage collection monitoring capabilities.
     */
    public static class GcMonitor {

        /**
         * Record containing GC statistics for a single collector.
         */
        public record GcStats(String name, long collectionCount, long collectionTimeMs,
                              List<String> memoryPoolNames) {
        }

        /**
         * Returns statistics for all garbage collectors.
         */
        public List<GcStats> getAllGcStats() {
            return ManagementFactory.getGarbageCollectorMXBeans().stream()
                    .map(gc -> new GcStats(
                            gc.getName(),
                            gc.getCollectionCount(),
                            gc.getCollectionTime(),
                            gc.getMemoryPoolNames() != null
                                    ? Arrays.asList(gc.getMemoryPoolNames())
                                    : Collections.emptyList()
                    ))
                    .collect(Collectors.toList());
        }

        /**
         * Returns the names of all registered garbage collectors.
         */
        public List<String> getGcNames() {
            return ManagementFactory.getGarbageCollectorMXBeans().stream()
                    .map(GarbageCollectorMXBean::getName)
                    .collect(Collectors.toList());
        }

        /**
         * Returns the total GC time across all collectors.
         */
        public long getTotalGcTimeMs() {
            return ManagementFactory.getGarbageCollectorMXBeans().stream()
                    .mapToLong(GarbageCollectorMXBean::getCollectionTime)
                    .sum();
        }

        /**
         * Returns the total number of GC collections across all collectors.
         */
        public long getTotalGcCount() {
            return ManagementFactory.getGarbageCollectorMXBeans().stream()
                    .mapToLong(GarbageCollectorMXBean::getCollectionCount)
                    .sum();
        }

        /**
         * Triggers a GC request (non-deterministic -- JVM may ignore it).
         */
        public void requestGc() {
            System.gc();
        }
    }

    /**
     * Provides memory monitoring capabilities.
     */
    public static class MemoryMonitor {

        /**
         * Record for memory usage details.
         */
        public record MemoryUsageInfo(String poolName, String type, long usedBytes,
                                      long committedBytes, long maxBytes) {
            public long usedMB() {
                return usedBytes / (1024 * 1024);
            }

            public double usagePercent() {
                return maxBytes > 0 ? 100.0 * usedBytes / maxBytes : -1;
            }
        }

        /**
         * Returns heap memory usage.
         */
        public MemoryUsageInfo getHeapUsage() {
            MemoryUsage usage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            return new MemoryUsageInfo("Heap", "HEAP",
                    usage.getUsed(), usage.getCommitted(), usage.getMax());
        }

        /**
         * Returns non-heap memory usage (Metaspace, code cache, etc.).
         */
        public MemoryUsageInfo getNonHeapUsage() {
            MemoryUsage usage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
            return new MemoryUsageInfo("Non-Heap", "NON_HEAP",
                    usage.getUsed(), usage.getCommitted(),
                    usage.getMax() > 0 ? usage.getMax() : -1);
        }

        /**
         * Returns detailed memory pool information for all pools.
         */
        public List<MemoryUsageInfo> getAllMemoryPools() {
            return ManagementFactory.getMemoryPoolMXBeans().stream()
                    .map(pool -> {
                        MemoryUsage usage = pool.getUsage();
                        return new MemoryUsageInfo(
                                pool.getName(),
                                pool.getType().name(),
                                usage.getUsed(),
                                usage.getCommitted(),
                                usage.getMax() > 0 ? usage.getMax() : -1
                        );
                    })
                    .collect(Collectors.toList());
        }

        /**
         * Returns the maximum heap size in bytes.
         */
        public long getMaxHeapBytes() {
            return Runtime.getRuntime().maxMemory();
        }

        /**
         * Returns the current free heap memory in bytes.
         */
        public long getFreeHeapBytes() {
            return Runtime.getRuntime().freeMemory();
        }

        /**
         * Returns the total currently allocated heap in bytes.
         */
        public long getTotalHeapBytes() {
            return Runtime.getRuntime().totalMemory();
        }

        /**
         * Returns used heap memory (total - free).
         */
        public long getUsedHeapBytes() {
            Runtime rt = Runtime.getRuntime();
            return rt.totalMemory() - rt.freeMemory();
        }

        /**
         * Calculates heap usage percentage.
         */
        public double getHeapUsagePercent() {
            Runtime rt = Runtime.getRuntime();
            long used = rt.totalMemory() - rt.freeMemory();
            long max = rt.maxMemory();
            return 100.0 * used / max;
        }
    }

    /**
     * Provides thread monitoring and diagnostics.
     */
    public static class ThreadMonitor {

        /**
         * Record containing thread information.
         */
        public record ThreadSummary(String name, Thread.State state, boolean isDaemon,
                                    long threadId) {
        }

        /**
         * Returns the current thread count.
         */
        public int getCurrentThreadCount() {
            return ManagementFactory.getThreadMXBean().getThreadCount();
        }

        /**
         * Returns the peak thread count since JVM start.
         */
        public int getPeakThreadCount() {
            return ManagementFactory.getThreadMXBean().getPeakThreadCount();
        }

        /**
         * Returns the total number of threads created since JVM start.
         */
        public long getTotalStartedThreadCount() {
            return ManagementFactory.getThreadMXBean().getTotalStartedThreadCount();
        }

        /**
         * Returns the number of daemon threads.
         */
        public int getDaemonThreadCount() {
            return ManagementFactory.getThreadMXBean().getDaemonThreadCount();
        }

        /**
         * Detects deadlocked threads. Returns the IDs of deadlocked threads,
         * or an empty array if no deadlock is detected.
         */
        public long[] detectDeadlocks() {
            long[] deadlocked = ManagementFactory.getThreadMXBean().findDeadlockedThreads();
            return deadlocked != null ? deadlocked : new long[0];
        }

        /**
         * Returns information about all live threads.
         */
        public List<ThreadSummary> getAllThreadSummaries() {
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            long[] threadIds = threadBean.getAllThreadIds();
            ThreadInfo[] infos = threadBean.getThreadInfo(threadIds);

            List<ThreadSummary> summaries = new ArrayList<>();
            for (ThreadInfo info : infos) {
                if (info != null) {
                    summaries.add(new ThreadSummary(
                            info.getThreadName(),
                            info.getThreadState(),
                            info.isDaemon(),
                            info.getThreadId()
                    ));
                }
            }
            return summaries;
        }

        /**
         * Returns a count of threads grouped by state.
         */
        public Map<Thread.State, Long> getThreadStateDistribution() {
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            long[] threadIds = threadBean.getAllThreadIds();
            ThreadInfo[] infos = threadBean.getThreadInfo(threadIds);

            return Arrays.stream(infos)
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(
                            ThreadInfo::getThreadState,
                            Collectors.counting()
                    ));
        }
    }

    /**
     * Provides JVM runtime information.
     */
    public static class RuntimeInfo {

        /**
         * Returns comprehensive JVM runtime information.
         */
        public record JvmInfo(String vmName, String vmVersion, String vmVendor,
                              String javaVersion, long uptimeMs, int availableProcessors,
                              List<String> inputArguments) {
        }

        /**
         * Gets complete JVM runtime information.
         */
        public JvmInfo getJvmInfo() {
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            return new JvmInfo(
                    runtime.getVmName(),
                    runtime.getVmVersion(),
                    runtime.getVmVendor(),
                    System.getProperty("java.version"),
                    runtime.getUptime(),
                    Runtime.getRuntime().availableProcessors(),
                    runtime.getInputArguments()
            );
        }

        /**
         * Returns system properties related to JVM configuration.
         */
        public Map<String, String> getJvmSystemProperties() {
            Map<String, String> props = new LinkedHashMap<>();
            props.put("java.version", System.getProperty("java.version"));
            props.put("java.vendor", System.getProperty("java.vendor"));
            props.put("java.home", System.getProperty("java.home"));
            props.put("os.name", System.getProperty("os.name"));
            props.put("os.arch", System.getProperty("os.arch"));
            props.put("java.vm.name", System.getProperty("java.vm.name"));
            props.put("java.vm.version", System.getProperty("java.vm.version"));
            return props;
        }

        /**
         * Checks if the JVM is running in a container environment.
         * This is a heuristic based on cgroup detection.
         */
        public boolean isLikelyContainer() {
            // Check for common container indicators
            String cgroupPath = "/proc/1/cgroup";
            try {
                java.nio.file.Path path = java.nio.file.Path.of(cgroupPath);
                if (java.nio.file.Files.exists(path)) {
                    String content = java.nio.file.Files.readString(path);
                    return content.contains("docker") || content.contains("kubepods")
                            || content.contains("containerd");
                }
            } catch (Exception e) {
                // Ignore -- not running on Linux or no access
            }
            return false;
        }
    }

    /**
     * Demonstrates memory allocation patterns and their impact on GC.
     */
    public static class AllocationPatterns {

        /**
         * Creates objects that become garbage quickly (short-lived).
         * These objects are collected in young generation GC.
         */
        public int shortLivedAllocations(int count) {
            int total = 0;
            for (int i = 0; i < count; i++) {
                String s = "item-" + i; // Short-lived, collected in young gen
                total += s.length();
            }
            return total;
        }

        /**
         * Creates objects that survive GC and get promoted to old generation.
         */
        public List<byte[]> longLivedAllocations(int count, int sizeBytes) {
            List<byte[]> list = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                list.add(new byte[sizeBytes]); // Long-lived, promoted to old gen
            }
            return list;
        }

        /**
         * Demonstrates the impact of allocation rate on GC frequency.
         * Higher allocation rates cause more frequent young generation GCs.
         */
        public long measureAllocationImpact(int iterations, int objectSize) {
            long gcCountBefore = getGcCount();

            for (int i = 0; i < iterations; i++) {
                byte[] data = new byte[objectSize]; // Allocate and immediately discard
                if (data.length < 0) break; // Prevent dead code elimination
            }

            long gcCountAfter = getGcCount();
            return gcCountAfter - gcCountBefore;
        }

        private long getGcCount() {
            return ManagementFactory.getGarbageCollectorMXBeans().stream()
                    .mapToLong(GarbageCollectorMXBean::getCollectionCount)
                    .sum();
        }

        /**
         * Demonstrates object pooling as an alternative to frequent allocation.
         */
        public static class SimpleObjectPool<T> {
            private final Deque<T> pool;
            private final java.util.function.Supplier<T> factory;
            private final int maxSize;

            public SimpleObjectPool(java.util.function.Supplier<T> factory, int maxSize) {
                this.pool = new ArrayDeque<>(maxSize);
                this.factory = factory;
                this.maxSize = maxSize;
            }

            public T acquire() {
                T obj = pool.pollFirst();
                return obj != null ? obj : factory.get();
            }

            public void release(T obj) {
                if (pool.size() < maxSize) {
                    pool.offerFirst(obj);
                }
            }

            public int poolSize() {
                return pool.size();
            }
        }
    }

    /**
     * Demonstrates common memory leak patterns for diagnostic practice.
     */
    public static class MemoryLeakPatterns {

        /**
         * Demonstrates an unbounded cache pattern (potential memory leak).
         */
        public static class UnboundedCache {
            private final Map<String, String> cache = new HashMap<>();

            public void put(String key, String value) {
                cache.put(key, value);
            }

            public String get(String key) {
                return cache.get(key);
            }

            public int size() {
                return cache.size();
            }

            public void clear() {
                cache.clear();
            }
        }

        /**
         * Demonstrates a bounded LRU cache (proper pattern).
         */
        public static class BoundedLruCache {
            private final Map<String, String> cache;
            private final int maxSize;

            public BoundedLruCache(int maxSize) {
                this.maxSize = maxSize;
                this.cache = new LinkedHashMap<>(maxSize, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                        return size() > maxSize;
                    }
                };
            }

            public void put(String key, String value) {
                cache.put(key, value);
            }

            public String get(String key) {
                return cache.get(key);
            }

            public int size() {
                return cache.size();
            }

            public int maxSize() {
                return maxSize;
            }
        }

        /**
         * Demonstrates ThreadLocal cleanup (important in thread pool environments).
         */
        public static class SafeThreadLocal {
            private static final ThreadLocal<List<String>> threadData = new ThreadLocal<>();

            public static void set(List<String> data) {
                threadData.set(data);
            }

            public static List<String> get() {
                return threadData.get();
            }

            /**
             * Must be called in a finally block to prevent memory leaks
             * when used with thread pools.
             */
            public static void cleanup() {
                threadData.remove();
            }
        }
    }
}
