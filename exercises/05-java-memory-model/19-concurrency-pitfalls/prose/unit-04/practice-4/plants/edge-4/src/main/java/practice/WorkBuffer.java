package practice;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class WorkBuffer<T> {

    private final BlockingQueue<T> queue;
    private final AtomicInteger produced = new AtomicInteger();
    private final AtomicInteger consumed = new AtomicInteger();

    public WorkBuffer(int capacity) {
        queue = new ArrayBlockingQueue<>(capacity);
    }

    /** Adds the item, waiting while the buffer is full. */
    public void put(T item) throws InterruptedException {
        queue.put(item);
        produced.incrementAndGet();
    }

    /** Removes the oldest item, waiting while the buffer is empty. */
    public T take() throws InterruptedException {
        T item = queue.take();
        consumed.incrementAndGet();
        return item;
    }

    /** Adds the item if room appears within the timeout; returns whether it did. */
    public boolean offer(T item, long timeoutMs) throws InterruptedException {
        boolean added = queue.offer(item, timeoutMs, TimeUnit.MILLISECONDS);
        if (added) {
            produced.incrementAndGet();
        }
        return added;
    }

    /** The oldest item, or null if none appears within the timeout. */
    public T poll(long timeoutMs) throws InterruptedException {
        T item = queue.poll();
        if (item != null) {
            consumed.incrementAndGet();
        }
        return item;
    }

    public int produced() {
        return produced.get();
    }

    public int consumed() {
        return consumed.get();
    }

    public int size() {
        return queue.size();
    }
}
