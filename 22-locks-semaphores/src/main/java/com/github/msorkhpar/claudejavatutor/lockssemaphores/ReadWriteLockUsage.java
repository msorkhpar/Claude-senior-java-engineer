package com.github.msorkhpar.claudejavatutor.lockssemaphores;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Demonstrates ReentrantReadWriteLock usage for read-heavy scenarios.
 * <p>
 * A read-write lock allows multiple concurrent readers but exclusive writer access.
 * This is optimal when reads vastly outnumber writes, as readers do not block each other.
 * <p>
 * Topics covered:
 * - ReadWriteLock interface
 * - ReentrantReadWriteLock (fair and non-fair)
 * - Read lock (shared) vs write lock (exclusive)
 * - Lock downgrading (write -> read)
 * - Diagnostic methods
 */
public class ReadWriteLockUsage<K, V> {

    private final Map<K, V> cache = new HashMap<>();
    private final ReentrantReadWriteLock rwLock;
    private final ReentrantReadWriteLock.ReadLock readLock;
    private final ReentrantReadWriteLock.WriteLock writeLock;

    public ReadWriteLockUsage() {
        this(false);
    }

    public ReadWriteLockUsage(boolean fair) {
        this.rwLock = new ReentrantReadWriteLock(fair);
        this.readLock = rwLock.readLock();
        this.writeLock = rwLock.writeLock();
    }

    /**
     * Reads a value from the cache. Multiple readers can execute concurrently.
     *
     * @param key the key to look up
     * @return the value, or null if not present
     */
    public V get(K key) {
        readLock.lock();
        try {
            return cache.get(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Writes a value into the cache. Only one writer at a time; blocks all readers.
     *
     * @param key   the key
     * @param value the value
     * @return the previous value, or null if none
     */
    public V put(K key, V value) {
        writeLock.lock();
        try {
            return cache.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes a key from the cache.
     *
     * @param key the key to remove
     * @return the removed value, or null if not present
     */
    public V remove(K key) {
        writeLock.lock();
        try {
            return cache.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Returns true if the cache contains the key (read operation).
     */
    public boolean containsKey(K key) {
        readLock.lock();
        try {
            return cache.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns the size of the cache (read operation).
     */
    public int size() {
        readLock.lock();
        try {
            return cache.size();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Demonstrates lock downgrading: acquire write lock, perform update,
     * downgrade to read lock (to continue reading without allowing other writers),
     * then release both.
     * <p>
     * Lock downgrading (write -> read) is supported.
     * Lock upgrading (read -> write) is NOT supported and will deadlock.
     *
     * @param key   the key to update
     * @param value the new value
     * @return the updated value (read under read lock after downgrade)
     */
    public V putAndReadWithDowngrade(K key, V value) {
        writeLock.lock();
        try {
            cache.put(key, value);
            // Downgrade: acquire read lock while still holding write lock
            readLock.lock();
        } finally {
            // Release write lock; read lock is still held
            writeLock.unlock();
        }
        try {
            // Now holding only the read lock - other readers can proceed,
            // but no writer can intervene
            return cache.get(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Puts a value only if the key is not already present (compute-if-absent pattern).
     * Demonstrates write lock for conditional update.
     *
     * @param key   the key
     * @param value the value to put if absent
     * @return the existing value if present, or the new value if inserted
     */
    public V putIfAbsent(K key, V value) {
        writeLock.lock();
        try {
            V existing = cache.get(key);
            if (existing != null) {
                return existing;
            }
            cache.put(key, value);
            return value;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Clears the entire cache.
     */
    public void clear() {
        writeLock.lock();
        try {
            cache.clear();
        } finally {
            writeLock.unlock();
        }
    }

    // -----------------------------------------------------------------------
    // Diagnostic methods
    // -----------------------------------------------------------------------

    /**
     * Returns the number of threads holding the read lock.
     */
    public int getReadLockCount() {
        return rwLock.getReadLockCount();
    }

    /**
     * Returns true if the write lock is currently held.
     */
    public boolean isWriteLocked() {
        return rwLock.isWriteLocked();
    }

    /**
     * Returns true if this lock uses fair ordering.
     */
    public boolean isFair() {
        return rwLock.isFair();
    }
}
