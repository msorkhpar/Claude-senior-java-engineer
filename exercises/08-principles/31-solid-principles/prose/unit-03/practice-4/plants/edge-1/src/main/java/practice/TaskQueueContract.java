package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The contract check every TaskQueue implementation runs in CI. Teams plug in their own queues
 * (bounded, unbounded, persistent) and the dispatcher, written against TaskQueue, must work with
 * each of them.
 */
public final class TaskQueueContract {

    /** The most items a check offers to one queue; every bounded queue in use holds fewer. */
    static final int FILL = 1_000;

    private TaskQueueContract() {
    }

    /** A queue of tasks shared by producers and the dispatcher. */
    public interface TaskQueue<T> {
        /**
         * Adds an item if the queue can take it now.
         *
         * @return true if added, false if the queue can't take it now (for example, it is full)
         * @throws NullPointerException if item is null; no other exception, ever
         */
        boolean offer(T item);

        /** @return the oldest item, removing it, or null if the queue is empty */
        T poll();
    }

    /** The clauses of the contract that {@code make}'s queues break, by name; empty if none. */
    public static List<String> breaches(Supplier<TaskQueue<String>> make) {
        List<String> broken = new ArrayList<>();
        if (!rejectsNull(make.get())) broken.add("rejects null");
        if (!offerNeverThrows(make.get())) broken.add("offer never throws");
        if (!pollOnEmptyReturnsNull(make.get())) broken.add("poll on empty returns null");
        if (!firstInFirstOut(make.get())) broken.add("first in, first out");
        return broken;
    }

    /** Whether offer(null) is refused the way the contract says. */
    static boolean rejectsNull(TaskQueue<String> queue) {
        try {
            queue.offer(null);
            return false;
        } catch (NullPointerException e) {
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Whether offering items, up to FILL of them, ever throws. */
    static boolean offerNeverThrows(TaskQueue<String> queue) {
        try {
            for (int i = 0; i < FILL; i++) {
                if (!queue.offer("task-" + i)) return false;
            }
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    static boolean pollOnEmptyReturnsNull(TaskQueue<String> queue) {
        try {
            return queue.poll() == null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    static boolean firstInFirstOut(TaskQueue<String> queue) {
        List<String> accepted = new ArrayList<>();
        for (String item : List.of("alpha", "bravo", "charlie")) {
            if (queue.offer(item)) accepted.add(item);
        }
        List<String> polled = new ArrayList<>();
        for (String item; (item = queue.poll()) != null; ) polled.add(item);
        return polled.equals(accepted);
    }
}
