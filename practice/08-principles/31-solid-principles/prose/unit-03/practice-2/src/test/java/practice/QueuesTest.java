package practice;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class QueuesTest {

    static List<Queues.TaskQueue<Integer>> both() {
        return List.of(new Queues.BoundedTaskQueue<>(10), new Queues.UnboundedTaskQueue<>());
    }

    @Test
    void bothQueuesHandOutItemsInOrder() {
        for (Queues.TaskQueue<Integer> queue : both()) {
            assertThat(queue.offer(1000)).isTrue();
            assertThat(queue.offer(2000)).isTrue();
            assertThat(queue.offer(3000)).isTrue();
            assertThat(queue.size()).isEqualTo(3);
            assertThat(Queues.drainAll(queue)).containsExactly(1000, 2000, 3000);
            assertThat(queue.isEmpty()).isTrue();
        }
    }

    @Test
    void aFullBoundedQueueSaysFalse() {
        Queues.TaskQueue<String> queue = new Queues.BoundedTaskQueue<>(2);
        assertThat(queue.offer("a")).isTrue();
        assertThat(queue.offer("b")).isTrue();
        assertThat(queue.offer("c")).isFalse();
        assertThat(queue.size()).isEqualTo(2);
        assertThat(Queues.drainAll(queue)).containsExactly("a", "b");
    }

    @Test
    void anEmptyQueuePollsNull() {
        for (Queues.TaskQueue<Integer> queue : both()) {
            assertThat(queue.poll()).isNull();
            queue.offer(4000);
            assertThat(queue.poll()).isEqualTo(4000);
            assertThat(queue.poll()).isNull();
        }
    }

    @Test
    void nullIsRejectedTheSameWayByBoth() {
        for (Queues.TaskQueue<Integer> queue : both()) {
            assertThatNullPointerException().isThrownBy(() -> queue.offer(null));
            assertThat(queue.size()).isZero();
        }
    }

    @Test
    void anUnboundedQueueAlwaysAccepts() {
        Queues.TaskQueue<Integer> queue = new Queues.UnboundedTaskQueue<>();
        for (int i = 0; i < 100_000; i++) {
            assertThat(queue.offer(i)).isTrue();
        }
        assertThat(queue.size()).isEqualTo(100_000);
    }

    @Test
    void aFullBoundedQueueAnswersAtOnce() throws Exception {
        Queues.BoundedTaskQueue<String> full = new Queues.BoundedTaskQueue<>(1);
        full.offer("a");
        java.util.concurrent.atomic.AtomicReference<Boolean> answer = new java.util.concurrent.atomic.AtomicReference<>();
        Thread caller = new Thread(() -> answer.set(full.offer("b")));
        caller.setDaemon(true);
        caller.start();
        boolean waited = false;
        long deadline = System.nanoTime() + 3_000_000_000L;
        while (caller.isAlive() && System.nanoTime() < deadline) {
            Thread.State s = caller.getState();
            if (s == Thread.State.WAITING || s == Thread.State.TIMED_WAITING) {
                waited = true;
                break;
            }
            Thread.onSpinWait();
        }
        caller.join(3_000);
        assertThat(waited).as("offer never waited for room").isFalse();
        assertThat(answer.get()).isFalse();
    }
}
