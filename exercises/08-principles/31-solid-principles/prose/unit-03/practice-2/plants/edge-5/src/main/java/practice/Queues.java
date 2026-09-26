package practice;
import java.util.*;
import java.util.concurrent.*;
public final class Queues {
    private Queues() {}
    public interface TaskQueue<T> { boolean offer(T item); T poll(); int size(); boolean isEmpty(); }
    public static final class BoundedTaskQueue<T> implements TaskQueue<T> {
        private final ArrayBlockingQueue<T> q;
        public BoundedTaskQueue(int capacity) { q = new ArrayBlockingQueue<>(capacity); }
        public boolean offer(T item) { try { return q.offer(item, 200, TimeUnit.MILLISECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return false; } }
        public T poll() { return q.poll(); }
        public int size() { return q.size(); }
        public boolean isEmpty() { return q.isEmpty(); }
    }
    public static final class UnboundedTaskQueue<T> implements TaskQueue<T> {
        private final Queue<T> q = new ConcurrentLinkedQueue<>();
        public boolean offer(T item) { return q.offer(item); }
        public T poll() { return q.poll(); }
        public int size() { return q.size(); }
        public boolean isEmpty() { return q.isEmpty(); }
    }
    public static <T> List<T> drainAll(TaskQueue<T> queue) { List<T> out = new ArrayList<>(); T t; while ((t = queue.poll()) != null) out.add(t); return out; }
}
