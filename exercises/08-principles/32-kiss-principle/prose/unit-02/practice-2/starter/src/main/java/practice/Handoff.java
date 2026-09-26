package practice;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Handoff<T> {

    private final BlockingQueue<T> queue;

    /** Creates a hand-off holding at most capacity items. */
    public Handoff(int capacity) {
        throw new UnsupportedOperationException("write Handoff");
    }

    /** Adds an item, waiting while the hand-off is full. */
    public void produce(T item) throws InterruptedException {
        throw new UnsupportedOperationException("write produce");
    }

    /** Removes the oldest item, waiting while the hand-off is empty. */
    public T consume() throws InterruptedException {
        throw new UnsupportedOperationException("write consume");
    }

    /** Like consume, but an interrupt becomes an IllegalStateException and the interrupt flag is restored. */
    public T next() {
        throw new UnsupportedOperationException("write next");
    }

    /** Returns how many items wait in the hand-off. */
    public int size() {
        throw new UnsupportedOperationException("write size");
    }
}
