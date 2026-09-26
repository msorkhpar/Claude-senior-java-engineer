package practice;

import java.util.List;

public final class Queues {

    private Queues() {
    }

    /**
     * offer(item) adds the item and returns true, or returns false when it cannot accept it;
     * offer(null) throws NullPointerException; poll() returns and removes the head, or null
     * when empty; size() is the current count.
     */
    public interface TaskQueue<T> {
        boolean offer(T item);

        T poll();

        int size();

        boolean isEmpty();
    }

    public static final class BoundedTaskQueue<T> implements TaskQueue<T> {

        public BoundedTaskQueue(int capacity) {
        }

        public boolean offer(T item) {
            throw new UnsupportedOperationException("write offer");
        }

        public T poll() {
            throw new UnsupportedOperationException("write poll");
        }

        public int size() {
            throw new UnsupportedOperationException("write size");
        }

        public boolean isEmpty() {
            throw new UnsupportedOperationException("write isEmpty");
        }
    }

    public static final class UnboundedTaskQueue<T> implements TaskQueue<T> {

        public boolean offer(T item) {
            throw new UnsupportedOperationException("write offer");
        }

        public T poll() {
            throw new UnsupportedOperationException("write poll");
        }

        public int size() {
            throw new UnsupportedOperationException("write size");
        }

        public boolean isEmpty() {
            throw new UnsupportedOperationException("write isEmpty");
        }
    }

    /** A client written against TaskQueue: takes every item, head first. */
    public static <T> List<T> drainAll(TaskQueue<T> queue) {
        throw new UnsupportedOperationException("write drainAll");
    }
}
