package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

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
        private final ArrayBlockingQueue<T> queue;

        public BoundedTaskQueue(int capacity) {
            this.queue = new ArrayBlockingQueue<>(capacity);
        }

        public boolean offer(T item) {
            return queue.offer(item);
        }

        public T poll() {
            return queue.poll();
        }

        public int size() {
            return queue.size();
        }

        public boolean isEmpty() {
            return queue.isEmpty();
        }
    }

    public static final class UnboundedTaskQueue<T> implements TaskQueue<T> {
        private final ArrayBlockingQueue<T> queue = new ArrayBlockingQueue<>(1024);

        public boolean offer(T item) {
            return queue.offer(item);
        }

        public T poll() {
            return queue.poll();
        }

        public int size() {
            return queue.size();
        }

        public boolean isEmpty() {
            return queue.isEmpty();
        }
    }

    /** A client written against TaskQueue: takes every item, head first. */
    public static <T> List<T> drainAll(TaskQueue<T> queue) {
        List<T> taken = new ArrayList<>();
        while (!queue.isEmpty()) {
            taken.add(queue.poll());
        }
        return taken;
    }
}
