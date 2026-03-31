package com.github.msorkhpar.claudejavatutor.happensbefore;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Demonstrates correct patterns for ensuring memory visibility across threads.
 * <p>
 * Patterns covered:
 * <ul>
 *   <li>Volatile flag for cooperative stop (single writer, multiple readers)</li>
 *   <li>Synchronized counter (compound read-modify-write operations)</li>
 *   <li>AtomicInteger counter (lock-free alternative to synchronized)</li>
 *   <li>Safe publication via final fields (immutable objects)</li>
 *   <li>Thread confinement patterns (stack confinement, ThreadLocal)</li>
 *   <li>CountDownLatch for staged initialization visibility</li>
 *   <li>ReadWriteLock for read-heavy workloads</li>
 *   <li>ReentrantLock for virtual-thread-friendly locking</li>
 *   <li>ConcurrentHashMap for safe shared map access</li>
 * </ul>
 *
 * @see README_5.2.3.md
 */
public class MemoryVisibility {

    // ── Pattern 1: Volatile Flag (Single-writer, Multi-reader) ───────────────

    /**
     * A cooperative cancellation service that uses a volatile boolean flag.
     * Pattern: single writer sets flag; multiple readers poll flag.
     * volatile is exactly right — no compound operation needed.
     */
    public static class CooperativeCancellation {
        private volatile boolean cancelled = false;
        private final AtomicInteger workCompleted = new AtomicInteger(0);

        /**
         * Signal all workers to cancel. Volatile write — immediately visible to all readers.
         */
        public void cancel() {
            cancelled = true;
        }

        /**
         * Check whether cancellation has been requested. Volatile read — always fresh.
         */
        public boolean isCancelled() {
            return cancelled;
        }

        /**
         * Worker method: processes items until cancelled or no more items.
         * Demonstrates proper cooperative cancellation check.
         */
        public int processUntilCancelled(List<Integer> items) {
            int processed = 0;
            for (Integer item : items) {
                if (isCancelled()) break; // volatile read — JIT cannot hoist this out
                workCompleted.incrementAndGet();
                processed++;
            }
            return processed;
        }

        public int getWorkCompleted() {
            return workCompleted.get();
        }
    }

    // ── Pattern 2: Synchronized Counter (Compound Operations) ────────────────

    /**
     * A thread-safe counter using synchronized methods.
     * Demonstrates the monitor lock rule for compound operations.
     * All accesses go through the same monitor ('this'), ensuring mutual exclusion
     * and visibility for every read and write.
     */
    public static class SynchronizedSharedCounter {
        private long value = 0L;
        private final List<Long> history = new ArrayList<>();

        /**
         * Atomically increment and record the new value.
         * Both increment and history.add are under the same lock — compound atomicity.
         */
        public synchronized void increment() {
            value++;
            history.add(value);
        }

        /**
         * Atomically add delta to the counter.
         */
        public synchronized void add(long delta) {
            value += delta;
        }

        /**
         * Conditional increment: only increment if value < max.
         * Classic check-then-act pattern — MUST be synchronized.
         */
        public synchronized boolean incrementIfBelow(long max) {
            if (value < max) {
                value++;
                return true;
            }
            return false;
        }

        public synchronized long getValue() {
            return value;
        }

        public synchronized List<Long> getHistory() {
            return new ArrayList<>(history);
        }

        public synchronized void reset() {
            value = 0;
            history.clear();
        }
    }

    // ── Pattern 3: AtomicInteger Counter (Lock-free) ──────────────────────────

    /**
     * A thread-safe counter using AtomicInteger — lock-free alternative to synchronized.
     * Suitable when the operation is a single atomic read-modify-write on one variable.
     */
    public static class AtomicSharedCounter {
        private final AtomicLong value = new AtomicLong(0);
        private final AtomicLong maxSeen = new AtomicLong(Long.MIN_VALUE);

        /**
         * Atomically increment and return the new value.
         */
        public long increment() {
            return value.incrementAndGet();
        }

        /**
         * Atomically add delta and return the new value.
         */
        public long addAndGet(long delta) {
            return value.addAndGet(delta);
        }

        /**
         * Atomically set to max(current, candidate).
         * Demonstrates a CAS loop for a non-trivial atomic update.
         */
        public void updateMax(long candidate) {
            long current;
            do {
                current = maxSeen.get();
                if (candidate <= current) return; // already has a larger value
            } while (!maxSeen.compareAndSet(current, candidate));
        }

        public long getValue() {
            return value.get();
        }

        public long getMaxSeen() {
            return maxSeen.get();
        }
    }

    // ── Pattern 4: Immutable Object with Final Fields ─────────────────────────

    /**
     * An immutable configuration object. All fields are final, no mutation after construction.
     * Properly constructed — 'this' does not escape during construction.
     * Once safely published (via volatile reference), all fields are visible without locks.
     */
    public static final class ImmutableConfig {
        private final String host;
        private final int port;
        private final int timeoutMs;
        private final boolean sslEnabled;

        public ImmutableConfig(String host, int port, int timeoutMs, boolean sslEnabled) {
            // All final field writes happen before the constructor exits.
            // No 'this' escape — final field guarantee is valid.
            this.host = Objects.requireNonNull(host, "host must not be null");
            this.port = port;
            this.timeoutMs = timeoutMs;
            this.sslEnabled = sslEnabled;
        }

        public String getHost() { return host; }
        public int getPort() { return port; }
        public int getTimeoutMs() { return timeoutMs; }
        public boolean isSslEnabled() { return sslEnabled; }

        /**
         * Return a new ImmutableConfig with host changed. Demonstrates immutable update.
         */
        public ImmutableConfig withHost(String newHost) {
            return new ImmutableConfig(newHost, port, timeoutMs, sslEnabled);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ImmutableConfig other)) return false;
            return port == other.port && timeoutMs == other.timeoutMs
                    && sslEnabled == other.sslEnabled && host.equals(other.host);
        }

        @Override
        public int hashCode() {
            return Objects.hash(host, port, timeoutMs, sslEnabled);
        }
    }

    /**
     * A configuration holder that safely publishes ImmutableConfig via volatile reference.
     * Readers see the fully initialized config without any synchronization.
     */
    public static class ConfigHolder {
        private volatile ImmutableConfig config; // volatile for safe publication

        public ConfigHolder(ImmutableConfig initial) {
            this.config = Objects.requireNonNull(initial);
        }

        /**
         * Atomically replace the configuration. Volatile write — immediately visible.
         */
        public void updateConfig(ImmutableConfig newConfig) {
            config = Objects.requireNonNull(newConfig); // volatile write
        }

        /**
         * Read the current configuration. Volatile read — always fresh.
         */
        public ImmutableConfig getConfig() {
            return config; // volatile read — sees fully initialized object
        }
    }

    // ── Pattern 5: Thread Confinement ─────────────────────────────────────────

    /**
     * Demonstrates stack confinement: local variables are on the thread's stack
     * and cannot be accessed by other threads (assuming references don't escape).
     * No synchronization needed.
     */
    public static class StackConfinementDemo {

        /**
         * All intermediate computation uses local variables — stack-confined.
         * Result is returned by value; no shared state.
         */
        public int sumSquaresOfOdds(List<Integer> numbers) {
            List<Integer> odds = new ArrayList<>(); // stack-confined (local variable)
            for (Integer n : numbers) {
                if (n % 2 != 0) {
                    odds.add(n); // adding to local list — no sharing
                }
            }
            int sum = 0; // stack-confined
            for (Integer odd : odds) {
                sum += odd * odd;
            }
            return sum; // returned by value — safe
        }
    }

    /**
     * Demonstrates ThreadLocal for per-thread state that must not be shared.
     * Each thread gets its own independent RequestContext — no synchronization needed.
     */
    public static class RequestContextHolder {
        private static final ThreadLocal<String> userId =
                ThreadLocal.withInitial(() -> "anonymous");

        private static final ThreadLocal<List<String>> auditLog =
                ThreadLocal.withInitial(ArrayList::new);

        public static void setUserId(String id) {
            userId.set(id);
        }

        public static String getUserId() {
            return userId.get();
        }

        public static void addAuditEntry(String entry) {
            auditLog.get().add(entry);
        }

        public static List<String> getAuditLog() {
            return new ArrayList<>(auditLog.get());
        }

        /**
         * IMPORTANT: clear ThreadLocal when done to prevent memory leaks in thread pools.
         */
        public static void clear() {
            userId.remove();
            auditLog.remove();
        }
    }

    // ── Pattern 6: CountDownLatch for Staged Initialization ──────────────────

    /**
     * Demonstrates using CountDownLatch to ensure initialization visibility.
     * Workers signal when done; consumers wait before reading results.
     * The CountDownLatch's happens-before guarantees all writes before countDown()
     * are visible after await() returns.
     */
    public static class StagedInitializer {
        private final int[] results;
        private final CountDownLatch initialized;

        public StagedInitializer(int size) {
            this.results = new int[size];
            this.initialized = new CountDownLatch(size);
        }

        /**
         * Worker: computes result for its partition and signals completion.
         * The write to results[index] happens-before countDown(), which happens-before
         * any await() returning — transitivity guarantees result is visible.
         */
        public Runnable createWorker(int index, int value) {
            return () -> {
                results[index] = value; // write before countDown
                initialized.countDown(); // countDown hb await
            };
        }

        /**
         * Wait for all workers to complete, then return results.
         * All workers' writes are visible after await() returns.
         */
        public int[] awaitAndGetResults() throws InterruptedException {
            initialized.await(); // hb all countDown() calls
            return results.clone();
        }

        public boolean awaitWithTimeout(long timeout, TimeUnit unit) throws InterruptedException {
            return initialized.await(timeout, unit);
        }
    }

    // ── Pattern 7: ReadWriteLock for Read-heavy Workloads ─────────────────────

    /**
     * A thread-safe in-memory registry using ReentrantReadWriteLock.
     * Multiple threads can read concurrently; writes are exclusive.
     * Suitable for read-heavy workloads where writes are infrequent.
     */
    public static class ReadHeavyRegistry {
        private final Map<String, String> data = new HashMap<>();
        private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
        private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

        /**
         * Register a key-value pair. Exclusive write lock.
         */
        public void register(String key, String value) {
            writeLock.lock();
            try {
                data.put(key, value);
            } finally {
                writeLock.unlock();
            }
        }

        /**
         * Look up a key. Shared read lock — concurrent readers allowed.
         */
        public String lookup(String key) {
            readLock.lock();
            try {
                return data.get(key);
            } finally {
                readLock.unlock();
            }
        }

        /**
         * Check if a key is registered. Shared read lock.
         */
        public boolean contains(String key) {
            readLock.lock();
            try {
                return data.containsKey(key);
            } finally {
                readLock.unlock();
            }
        }

        /**
         * Remove a key. Exclusive write lock.
         */
        public boolean remove(String key) {
            writeLock.lock();
            try {
                return data.remove(key) != null;
            } finally {
                writeLock.unlock();
            }
        }

        public int size() {
            readLock.lock();
            try {
                return data.size();
            } finally {
                readLock.unlock();
            }
        }
    }

    // ── Pattern 8: ReentrantLock (Virtual-Thread-Friendly) ────────────────────

    /**
     * A virtual-thread-friendly counter using ReentrantLock instead of synchronized.
     * synchronized blocks in virtual threads can pin the carrier thread;
     * ReentrantLock avoids this by using a non-pinning park mechanism.
     */
    public static class VirtualThreadFriendlyCounter {
        private final ReentrantLock lock = new ReentrantLock();
        private long count = 0;

        public void increment() {
            lock.lock();
            try {
                count++;
            } finally {
                lock.unlock();
            }
        }

        public long get() {
            lock.lock();
            try {
                return count;
            } finally {
                lock.unlock();
            }
        }

        /**
         * Try increment with a timeout — avoids blocking indefinitely.
         * Returns true if increment was successful, false on timeout.
         */
        public boolean tryIncrement(long timeoutMs) throws InterruptedException {
            if (lock.tryLock(timeoutMs, TimeUnit.MILLISECONDS)) {
                try {
                    count++;
                    return true;
                } finally {
                    lock.unlock();
                }
            }
            return false;
        }
    }

    // ── Pattern 9: ConcurrentHashMap for Safe Shared Map ─────────────────────

    /**
     * Demonstrates using ConcurrentHashMap for thread-safe shared map access.
     * ConcurrentHashMap provides atomic compound operations (computeIfAbsent, merge, etc.)
     * and high-concurrency reads with per-bucket write locking.
     */
    public static class SharedWordCounter {
        private final ConcurrentHashMap<String, AtomicInteger> counts = new ConcurrentHashMap<>();

        /**
         * Count a word. Uses computeIfAbsent (atomic) + AtomicInteger.incrementAndGet.
         * Thread-safe without any external synchronization.
         */
        public void countWord(String word) {
            counts.computeIfAbsent(word, k -> new AtomicInteger(0)).incrementAndGet();
        }

        /**
         * Get the count for a word. Returns 0 if not seen.
         */
        public int getCount(String word) {
            AtomicInteger counter = counts.get(word);
            return counter == null ? 0 : counter.get();
        }

        /**
         * Get the total number of words counted (sum of all counts).
         */
        public long getTotalCount() {
            return counts.values().stream().mapToLong(AtomicInteger::get).sum();
        }

        /**
         * Returns the most frequent word, or empty if no words counted.
         */
        public Optional<String> getMostFrequent() {
            return counts.entrySet().stream()
                    .max(Map.Entry.comparingByValue(
                            Comparator.comparingInt(AtomicInteger::get)))
                    .map(Map.Entry::getKey);
        }

        public int distinctWordCount() {
            return counts.size();
        }
    }
}
