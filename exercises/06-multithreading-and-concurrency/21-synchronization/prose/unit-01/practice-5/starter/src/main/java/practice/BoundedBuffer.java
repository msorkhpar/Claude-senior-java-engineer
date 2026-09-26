package practice;

public final class BoundedBuffer<T> {

    public BoundedBuffer(int capacity) {
    }

    /** Adds an item, waiting while the buffer is full. */
    public void put(T item) throws InterruptedException {
        throw new UnsupportedOperationException("write put");
    }

    /** Removes and returns the oldest item, waiting while the buffer is empty. */
    public T take() throws InterruptedException {
        throw new UnsupportedOperationException("write take");
    }

    public int size() {
        throw new UnsupportedOperationException("write size");
    }
}
