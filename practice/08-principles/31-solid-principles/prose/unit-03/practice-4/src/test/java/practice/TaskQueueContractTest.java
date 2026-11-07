package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.assertj.core.api.Assertions.assertThat;

class TaskQueueContractTest {

    /** Unbounded, keeps the contract. */
    static final class Unbounded implements TaskQueueContract.TaskQueue<String> {
        private final ConcurrentLinkedQueue<String> items = new ConcurrentLinkedQueue<>();
        public boolean offer(String item) { return items.offer(Objects.requireNonNull(item)); }
        public String poll() { return items.poll(); }
    }

    /** Holds at most 12 items and says false after that: keeps the contract. */
    static final class Bounded implements TaskQueueContract.TaskQueue<String> {
        private final ArrayBlockingQueue<String> items = new ArrayBlockingQueue<>(12);
        public boolean offer(String item) { return items.offer(item); }
        public String poll() { return items.poll(); }
    }

    /** Holds at most 40 items and throws IllegalStateException("Queue full") after that. */
    static final class ThrowsWhenFull implements TaskQueueContract.TaskQueue<String> {
        private final ArrayBlockingQueue<String> items = new ArrayBlockingQueue<>(40);
        public boolean offer(String item) { return items.add(item); }
        public String poll() { return items.poll(); }
    }

    /** Takes null like any other item. */
    static final class AcceptsNull implements TaskQueueContract.TaskQueue<String> {
        private final LinkedList<String> items = new LinkedList<>();
        public boolean offer(String item) { return items.offer(item); }
        public String poll() { return items.poll(); }
    }

    /** Refuses null with IllegalArgumentException instead of NullPointerException. */
    static final class NullIsAnIllegalArgument implements TaskQueueContract.TaskQueue<String> {
        private final ConcurrentLinkedQueue<String> items = new ConcurrentLinkedQueue<>();
        public boolean offer(String item) {
            if (item == null) throw new IllegalArgumentException("item must not be null");
            return items.offer(item);
        }
        public String poll() { return items.poll(); }
    }

    /** Serves the newest item first. */
    static final class NewestFirst implements TaskQueueContract.TaskQueue<String> {
        private final Deque<String> items = new ArrayDeque<>();
        public boolean offer(String item) { items.push(Objects.requireNonNull(item)); return true; }
        public String poll() { return items.poll(); }
    }

    @Test
    void anUnboundedQueueThatKeepsTheContractHasNoBreaches() {
        assertThat(TaskQueueContract.breaches(Unbounded::new)).isEmpty();
    }

    @Test
    void aQueueThatTakesNullIsReported() {
        assertThat(TaskQueueContract.breaches(AcceptsNull::new)).containsExactly("rejects null");
    }

    @Test
    void aQueueThatServesNewestFirstIsReported() {
        assertThat(TaskQueueContract.breaches(NewestFirst::new)).containsExactly("first in, first out");
    }

    @Test
    void aBoundedQueueThatSaysFalseWhenFullHasNoBreaches() {
        assertThat(TaskQueueContract.breaches(Bounded::new)).isEmpty();
    }

    @Test
    void aQueueThatThrowsWhenFullIsReported() {
        assertThat(TaskQueueContract.breaches(ThrowsWhenFull::new)).containsExactly("offer never throws");
    }

    @Test
    void aQueueThatRefusesNullWithAnotherExceptionIsReported() {
        assertThat(TaskQueueContract.breaches(NullIsAnIllegalArgument::new)).containsExactly("rejects null");
    }
}
