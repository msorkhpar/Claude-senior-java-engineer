package com.github.msorkhpar.claudejavatutor.lockssemaphores;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstrates Condition objects for waiting and signaling (6.3.1.3).
 * <p>
 * A bounded blocking queue that uses two Condition objects (notFull, notEmpty)
 * on a single ReentrantLock. This is the classic producer-consumer pattern
 * that shows why multiple Conditions per lock are useful.
 * <p>
 * Topics covered:
 * - Creating Conditions from Lock.newCondition()
 * - await() / signal() / signalAll()
 * - Spurious wakeup handling with while-loop guards
 * - Timed await with timeout
 */
public class ConditionUsage<T> {

    private final Queue<T> queue;
    private final int capacity;
    private final ReentrantLock lock;
    private final Condition notFull;
    private final Condition notEmpty;

    public ConditionUsage(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive, got: " + capacity);
        }
        this.capacity = capacity;
        this.queue = new LinkedList<>();
        this.lock = new ReentrantLock();
        this.notFull = lock.newCondition();
        this.notEmpty = lock.newCondition();
    }

    /**
     * Inserts an item, blocking if the queue is full until space is available.
     *
     * @param item the item to insert (must not be null)
     * @throws InterruptedException if interrupted while waiting
     * @throws NullPointerException if item is null
     */
    public void put(T item) throws InterruptedException {
        if (item == null) {
            throw new NullPointerException("Null items are not allowed");
        }
        lock.lock();
        try {
            while (queue.size() == capacity) {
                notFull.await(); // wait until space available; loop handles spurious wakeups
            }
            queue.offer(item);
            notEmpty.signal(); // wake one consumer
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes and returns an item, blocking if the queue is empty until one is available.
     *
     * @return the removed item
     * @throws InterruptedException if interrupted while waiting
     */
    public T take() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                notEmpty.await(); // wait until item available; loop handles spurious wakeups
            }
            T item = queue.poll();
            notFull.signal(); // wake one producer
            return item;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Attempts to insert an item within the specified timeout.
     *
     * @param item    the item to insert
     * @param timeout the maximum time to wait
     * @param unit    the time unit
     * @return true if the item was inserted, false if timeout expired
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean offer(T item, long timeout, TimeUnit unit) throws InterruptedException {
        if (item == null) {
            throw new NullPointerException("Null items are not allowed");
        }
        long nanos = unit.toNanos(timeout);
        lock.lock();
        try {
            while (queue.size() == capacity) {
                if (nanos <= 0) {
                    return false;
                }
                nanos = notFull.awaitNanos(nanos);
            }
            queue.offer(item);
            notEmpty.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Attempts to remove and return an item within the specified timeout.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit
     * @return the item, or null if timeout expired
     * @throws InterruptedException if interrupted while waiting
     */
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        lock.lock();
        try {
            while (queue.isEmpty()) {
                if (nanos <= 0) {
                    return null;
                }
                nanos = notEmpty.awaitNanos(nanos);
            }
            T item = queue.poll();
            notFull.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the current size of the queue.
     */
    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns true if the queue is empty.
     */
    public boolean isEmpty() {
        lock.lock();
        try {
            return queue.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns true if the queue is full.
     */
    public boolean isFull() {
        lock.lock();
        try {
            return queue.size() == capacity;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the capacity of this bounded queue.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Signals all waiting producers. Useful for shutdown scenarios.
     */
    public void signalAllProducers() {
        lock.lock();
        try {
            notFull.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Signals all waiting consumers. Useful for shutdown scenarios.
     */
    public void signalAllConsumers() {
        lock.lock();
        try {
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
