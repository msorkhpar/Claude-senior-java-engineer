package com.github.msorkhpar.claudejavatutor.concurrencypitfalls;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Demonstrates best practices for safe concurrent programming.
 * <p>
 * This class consolidates recommended patterns:
 * <ul>
 *   <li>Prefer immutability: use records and final fields</li>
 *   <li>Confine state to a single thread (ThreadLocal, stack confinement)</li>
 *   <li>Use higher-level concurrent abstractions (ConcurrentHashMap, BlockingQueue)</li>
 *   <li>Minimize the scope of shared mutable state and lock scope</li>
 *   <li>Use ReadWriteLock for read-heavy workloads</li>
 *   <li>Prefer ReentrantLock over synchronized for virtual threads</li>
 *   <li>Producer-consumer pattern with BlockingQueue</li>
 * </ul>
 *
 * @see README_5.3.4.md
 */
public class ConcurrencyBestPractices {

    // ── Best Practice 1: Prefer Immutable Value Objects ──────────────────────

    /**
     * An immutable configuration using a record.
     * Records generate final fields for all components, making them naturally thread-safe.
     * Share freely across threads without synchronization.
     */
    public record AppConfig(String host, int port, boolean sslEnabled, int maxRetries) {

        public AppConfig {
            Objects.requireNonNull(host, "host must not be null");
            if (port < 0 || port > 65535) {
                throw new IllegalArgumentException("Invalid port: " + port);
            }
            if (maxRetries < 0) {
                throw new IllegalArgumentException("maxRetries must be non-negative");
            }
        }

        public AppConfig withHost(String newHost) {
            return new AppConfig(newHost, port, sslEnabled, maxRetries);
        }

        public AppConfig withPort(int newPort) {
            return new AppConfig(host, newPort, sslEnabled, maxRetries);
        }
    }

    /**
     * Thread-safe config holder: replaces the config atomically via volatile reference.
     * Readers always see a fully constructed, consistent AppConfig.
     */
    public static class ConfigManager {
        private volatile AppConfig config;

        public ConfigManager(AppConfig initial) {
            this.config = Objects.requireNonNull(initial);
        }

        public void updateConfig(AppConfig newConfig) {
            this.config = Objects.requireNonNull(newConfig);
        }

        public AppConfig getConfig() {
            return config;
        }
    }

    // ── Best Practice 2: Thread Confinement via ThreadLocal ──────────────────

    /**
     * Per-thread request context using ThreadLocal.
     * No shared state between threads = no synchronization needed.
     * Critical: always call clear() when done to prevent memory leaks in thread pools.
     */
    public static class RequestContext {
        private static final ThreadLocal<String> requestId = ThreadLocal.withInitial(() -> "");
        private static final ThreadLocal<Long> startTime = ThreadLocal.withInitial(System::nanoTime);

        public static void begin(String id) {
            requestId.set(id);
            startTime.set(System.nanoTime());
        }

        public static String getRequestId() {
            return requestId.get();
        }

        public static long getElapsedNanos() {
            return System.nanoTime() - startTime.get();
        }

        public static void clear() {
            requestId.remove();
            startTime.remove();
        }
    }

    // ── Best Practice 3: Use ConcurrentHashMap for Shared Maps ───────────────

    /**
     * A thread-safe service registry using ConcurrentHashMap.
     * Demonstrates computeIfAbsent, putIfAbsent, and atomic operations on the map.
     */
    public static class ServiceRegistry {
        private final ConcurrentHashMap<String, String> services = new ConcurrentHashMap<>();

        /**
         * Registers a service. Returns false if already registered.
         */
        public boolean register(String name, String endpoint) {
            return services.putIfAbsent(name, endpoint) == null;
        }

        /**
         * Looks up a service endpoint by name.
         */
        public Optional<String> lookup(String name) {
            return Optional.ofNullable(services.get(name));
        }

        /**
         * Unregisters a service. Returns true if it was registered.
         */
        public boolean unregister(String name) {
            return services.remove(name) != null;
        }

        /**
         * Returns all registered service names as an unmodifiable set.
         */
        public Set<String> getServiceNames() {
            return Collections.unmodifiableSet(services.keySet());
        }

        public int size() {
            return services.size();
        }
    }

    // ── Best Practice 4: Producer-Consumer with BlockingQueue ────────────────

    /**
     * Demonstrates the producer-consumer pattern using a bounded BlockingQueue.
     * Producers add items; consumers take items. The queue handles all synchronization.
     */
    public static class ProducerConsumer<T> {
        private final BlockingQueue<T> queue;
        private final AtomicInteger producedCount = new AtomicInteger(0);
        private final AtomicInteger consumedCount = new AtomicInteger(0);

        public ProducerConsumer(int capacity) {
            this.queue = new ArrayBlockingQueue<>(capacity);
        }

        /**
         * Produces an item. Blocks if the queue is full.
         */
        public void produce(T item) throws InterruptedException {
            queue.put(item);
            producedCount.incrementAndGet();
        }

        /**
         * Produces an item with a timeout. Returns false if the queue was full.
         */
        public boolean tryProduce(T item, long timeoutMs) throws InterruptedException {
            boolean offered = queue.offer(item, timeoutMs, TimeUnit.MILLISECONDS);
            if (offered) producedCount.incrementAndGet();
            return offered;
        }

        /**
         * Consumes an item. Blocks if the queue is empty.
         */
        public T consume() throws InterruptedException {
            T item = queue.take();
            consumedCount.incrementAndGet();
            return item;
        }

        /**
         * Consumes an item with a timeout. Returns null if the queue was empty.
         */
        public T tryConsume(long timeoutMs) throws InterruptedException {
            T item = queue.poll(timeoutMs, TimeUnit.MILLISECONDS);
            if (item != null) consumedCount.incrementAndGet();
            return item;
        }

        public int getProducedCount() {
            return producedCount.get();
        }

        public int getConsumedCount() {
            return consumedCount.get();
        }

        public int queueSize() {
            return queue.size();
        }
    }

    // ── Best Practice 5: ReadWriteLock for Read-Heavy Data ───────────────────

    /**
     * A thread-safe cache using ReadWriteLock.
     * Multiple readers can read concurrently; writes are exclusive.
     */
    public static class ReadWriteCache<K, V> {
        private final Map<K, V> data = new HashMap<>();
        private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

        public void put(K key, V value) {
            rwLock.writeLock().lock();
            try {
                data.put(key, value);
            } finally {
                rwLock.writeLock().unlock();
            }
        }

        public V get(K key) {
            rwLock.readLock().lock();
            try {
                return data.get(key);
            } finally {
                rwLock.readLock().unlock();
            }
        }

        public boolean containsKey(K key) {
            rwLock.readLock().lock();
            try {
                return data.containsKey(key);
            } finally {
                rwLock.readLock().unlock();
            }
        }

        public V remove(K key) {
            rwLock.writeLock().lock();
            try {
                return data.remove(key);
            } finally {
                rwLock.writeLock().unlock();
            }
        }

        public int size() {
            rwLock.readLock().lock();
            try {
                return data.size();
            } finally {
                rwLock.readLock().unlock();
            }
        }

        /**
         * Returns a snapshot of all entries (defensive copy under read lock).
         */
        public Map<K, V> snapshot() {
            rwLock.readLock().lock();
            try {
                return new HashMap<>(data);
            } finally {
                rwLock.readLock().unlock();
            }
        }
    }

    // ── Best Practice 6: Minimize Lock Scope ─────────────────────────────────

    /**
     * Demonstrates minimizing lock scope by computing expensive values outside the lock.
     * Only the actual state update is performed under the lock.
     */
    public static class MinimalLockScope {
        private final ReentrantLock lock = new ReentrantLock();
        private final List<String> results = new ArrayList<>();

        /**
         * Processes an item: expensive computation is done OUTSIDE the lock,
         * only the result insertion is under the lock.
         */
        public String processAndStore(String input) {
            // Expensive computation outside the lock -- no contention
            String computed = expensiveTransform(input);

            lock.lock();
            try {
                results.add(computed);
            } finally {
                lock.unlock();
            }
            return computed;
        }

        /**
         * Returns a snapshot of all results.
         */
        public List<String> getResults() {
            lock.lock();
            try {
                return new ArrayList<>(results);
            } finally {
                lock.unlock();
            }
        }

        /**
         * Simulates an expensive transformation.
         */
        private String expensiveTransform(String input) {
            return input.toUpperCase() + "-PROCESSED";
        }
    }

    // ── Best Practice 7: Use CopyOnWriteArrayList for Read-Heavy Lists ──────

    /**
     * Demonstrates CopyOnWriteArrayList for scenarios with many readers and few writers.
     * Iterators are always safe (they work on a snapshot) -- no ConcurrentModificationException.
     */
    public static class EventListenerRegistry {
        private final List<String> listeners = new CopyOnWriteArrayList<>();

        public void addListener(String listener) {
            listeners.add(listener);
        }

        public boolean removeListener(String listener) {
            return listeners.remove(listener);
        }

        /**
         * Notifies all listeners. Safe to iterate even while other threads add/remove listeners.
         */
        public List<String> getListeners() {
            return new ArrayList<>(listeners);
        }

        public int listenerCount() {
            return listeners.size();
        }

        /**
         * Checks if a listener is registered.
         */
        public boolean hasListener(String listener) {
            return listeners.contains(listener);
        }
    }
}
