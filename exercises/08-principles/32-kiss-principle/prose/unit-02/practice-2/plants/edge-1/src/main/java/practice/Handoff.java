package practice;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Handoff<T> {

    private final BlockingQueue<T> queue;

    /** Creates a hand-off holding at most capacity items. */
    public Handoff(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    /** Adds an item, waiting while the hand-off is full. */
    public void produce(T item) throws InterruptedException {
        queue.put(item);
    }

    /** Removes the oldest item, waiting while the hand-off is empty. */
    public T consume() throws InterruptedException {
        return queue.poll();
    }

    /** Like consume, but an interrupt becomes an IllegalStateException and the interrupt flag is restored. */
    public T next() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while waiting", e);
        }
    }

    /** Returns how many items wait in the hand-off. */
    public int size() {
        return queue.size();
    }
}
