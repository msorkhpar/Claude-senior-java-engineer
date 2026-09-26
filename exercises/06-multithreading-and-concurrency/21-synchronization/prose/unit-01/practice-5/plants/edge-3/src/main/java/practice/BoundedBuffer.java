package practice;

import java.util.ArrayDeque;

public final class BoundedBuffer<T> {

    private final Object monitor = new Object();
    private final ArrayDeque<T> items = new ArrayDeque<>();
    private final int capacity;

    public BoundedBuffer(int capacity) {
        this.capacity = capacity;
    }

    /** Adds an item, waiting while the buffer is full. */
    public void put(T item) throws InterruptedException {
        synchronized (monitor) {
            while (items.size() == capacity) {
                monitor.wait();
            }
            items.addLast(item);
            monitor.notifyAll();
        }
    }

    /** Removes and returns the oldest item, waiting while the buffer is empty. */
    public T take() throws InterruptedException {
        synchronized (monitor) {

            T item = items.pollFirst();
            monitor.notifyAll();
            return item;
        }
    }

    public int size() {
        synchronized (monitor) {
            return items.size();
        }
    }
}
