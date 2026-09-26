package practice;

public final class WorkBuffer<T> {

    public WorkBuffer(int capacity) {
    }

    /** Adds the item, waiting while the buffer is full. */
    public void put(T item) throws InterruptedException {
        throw new UnsupportedOperationException("write put");
    }

    /** Removes the oldest item, waiting while the buffer is empty. */
    public T take() throws InterruptedException {
        throw new UnsupportedOperationException("write take");
    }

    /** Adds the item if room appears within the timeout; returns whether it did. */
    public boolean offer(T item, long timeoutMs) throws InterruptedException {
        throw new UnsupportedOperationException("write offer");
    }

    /** The oldest item, or null if none appears within the timeout. */
    public T poll(long timeoutMs) throws InterruptedException {
        throw new UnsupportedOperationException("write poll");
    }

    public int produced() {
        throw new UnsupportedOperationException("write produced");
    }

    public int consumed() {
        throw new UnsupportedOperationException("write consumed");
    }

    public int size() {
        throw new UnsupportedOperationException("write size");
    }
}
