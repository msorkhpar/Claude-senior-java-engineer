package com.github.msorkhpar.claudejavatutor.dryprinciple;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * Demonstrates the benefits of DRY for concurrency:
 * - Improved code maintainability
 * - Consistency in concurrent behavior
 */
public class DryBenefits {

    // ========== 8.3.3.1: Improved Code Maintainability ==========

    /**
     * VIOLATION: Each repository duplicates read-write locking logic.
     * If the locking strategy changes, all repositories must be updated.
     */
    public static class ViolationUserRepository {
        private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        private final Map<String, String> users = new HashMap<>();

        public void addUser(String id, String name) {
            rwLock.writeLock().lock();
            try {
                users.put(id, name);
            } finally {
                rwLock.writeLock().unlock();
            }
        }

        public String getUser(String id) {
            rwLock.readLock().lock();
            try {
                return users.get(id);
            } finally {
                rwLock.readLock().unlock();
            }
        }

        public List<String> getAllUsers() {
            rwLock.readLock().lock();
            try {
                return new ArrayList<>(users.values());
            } finally {
                rwLock.readLock().unlock();
            }
        }
    }

    /**
     * DRY: A reusable read-write locked wrapper for any data source.
     * If the locking strategy changes, only this class needs updating.
     */
    public static class ReadWriteLockedResource<T> {
        private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        private final T resource;

        public ReadWriteLockedResource(T resource) {
            this.resource = Objects.requireNonNull(resource, "resource must not be null");
        }

        public <R> R read(Function<T, R> readAction) {
            Objects.requireNonNull(readAction, "readAction must not be null");
            rwLock.readLock().lock();
            try {
                return readAction.apply(resource);
            } finally {
                rwLock.readLock().unlock();
            }
        }

        public <R> R write(Function<T, R> writeAction) {
            Objects.requireNonNull(writeAction, "writeAction must not be null");
            rwLock.writeLock().lock();
            try {
                return writeAction.apply(resource);
            } finally {
                rwLock.writeLock().unlock();
            }
        }

        public void writeVoid(java.util.function.Consumer<T> writeAction) {
            Objects.requireNonNull(writeAction, "writeAction must not be null");
            rwLock.writeLock().lock();
            try {
                writeAction.accept(resource);
            } finally {
                rwLock.writeLock().unlock();
            }
        }
    }

    /**
     * DRY: UserRepository using the reusable ReadWriteLockedResource.
     */
    public static class DryUserRepository {
        private final ReadWriteLockedResource<Map<String, String>> lockedUsers =
                new ReadWriteLockedResource<>(new HashMap<>());

        public void addUser(String id, String name) {
            lockedUsers.writeVoid(users -> users.put(id, name));
        }

        public String getUser(String id) {
            return lockedUsers.read(users -> users.get(id));
        }

        public List<String> getAllUsers() {
            return lockedUsers.read(users -> new ArrayList<>(users.values()));
        }

        public int size() {
            return lockedUsers.read(Map::size);
        }
    }

    /**
     * DRY: ProductRepository reusing the same ReadWriteLockedResource.
     * Any lock strategy changes automatically apply here too.
     */
    public static class DryProductRepository {
        private final ReadWriteLockedResource<Map<String, Double>> lockedProducts =
                new ReadWriteLockedResource<>(new HashMap<>());

        public void addProduct(String name, double price) {
            lockedProducts.writeVoid(products -> products.put(name, price));
        }

        public Double getPrice(String name) {
            return lockedProducts.read(products -> products.get(name));
        }

        public List<String> getAllProductNames() {
            return lockedProducts.read(products -> new ArrayList<>(products.keySet()));
        }
    }

    // ========== 8.3.3.2: Consistency in Concurrent Behavior ==========

    /**
     * VIOLATION: Inconsistent thread-safe counter implementations.
     * One uses synchronized, the other uses AtomicInteger -- mixed approaches lead to bugs.
     */
    public static class InconsistentCounters {
        private int syncCounter = 0;
        private final AtomicInteger atomicCounter = new AtomicInteger(0);

        public synchronized void incrementSync() {
            syncCounter++;
        }

        public synchronized int getSyncCount() {
            return syncCounter;
        }

        public void incrementAtomic() {
            atomicCounter.incrementAndGet();
        }

        public int getAtomicCount() {
            return atomicCounter.get();
        }
    }

    /**
     * DRY: A single consistent thread-safe counter abstraction.
     * All code uses the same mechanism, ensuring consistent behavior.
     */
    public static class ConsistentCounter {
        private final AtomicInteger count = new AtomicInteger(0);

        public int increment() {
            return count.incrementAndGet();
        }

        public int decrement() {
            return count.decrementAndGet();
        }

        public int get() {
            return count.get();
        }

        public void reset() {
            count.set(0);
        }
    }

    /**
     * DRY: Reusable event bus that ensures consistent publish-subscribe behavior.
     * All event types use the same thread-safe mechanism.
     */
    public static class SimpleEventBus {
        private final Map<String, List<java.util.function.Consumer<Object>>> listeners =
                new ConcurrentHashMap<>();

        public void subscribe(String eventType, java.util.function.Consumer<Object> listener) {
            Objects.requireNonNull(eventType, "eventType must not be null");
            Objects.requireNonNull(listener, "listener must not be null");
            listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
        }

        public void publish(String eventType, Object event) {
            Objects.requireNonNull(eventType, "eventType must not be null");
            List<java.util.function.Consumer<Object>> eventListeners = listeners.get(eventType);
            if (eventListeners != null) {
                for (var listener : eventListeners) {
                    listener.accept(event);
                }
            }
        }

        public int listenerCount(String eventType) {
            List<java.util.function.Consumer<Object>> eventListeners = listeners.get(eventType);
            return eventListeners == null ? 0 : eventListeners.size();
        }
    }
}
