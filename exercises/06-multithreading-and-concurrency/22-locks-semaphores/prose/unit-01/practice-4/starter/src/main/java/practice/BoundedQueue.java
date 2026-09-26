package practice;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/** A bounded first-in, first-out queue on one lock with two conditions. */
public final class BoundedQueue<T> {

    private final int capacity;
    private final Deque<T> items = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();

    public BoundedQueue(int capacity) {
        this.capacity = capacity;
    }

    /** Adds an item, waiting while the queue is full. */
    public void put(T item) throws InterruptedException {
        throw new UnsupportedOperationException("write put");
    }

    /** Removes the oldest item, waiting while the queue is empty. */
    public T take() throws InterruptedException {
        throw new UnsupportedOperationException("write take");
    }

    /** Removes the oldest item, waiting at most {@code timeout}; null if none came. */
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("write poll");
    }

    /** The number of items held. */
    public int size() {
        throw new UnsupportedOperationException("write size");
    }
}
