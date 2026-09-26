package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class BoundedQueueTest {

    interface Call {
        Object call() throws Exception;
    }

    private static Thread start(AtomicReference<Object> result, Call body) {
        Thread t = new Thread(() -> {
            try {
                result.set(body.call());
            } catch (Exception e) {
                result.set(e);
            }
        });
        t.setDaemon(true);
        t.start();
        return t;
    }

    /** Waits (at most 3 s) until {@code t} is parked or finished; returns whether it is parked. */
    private static boolean parked(Thread t) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (System.nanoTime() < deadline) {
            Thread.State s = t.getState();
            if (s == Thread.State.WAITING || s == Thread.State.TIMED_WAITING) {
                return true;
            }
            if (s == Thread.State.TERMINATED) {
                return false;
            }
            Thread.onSpinWait();
        }
        return false;
    }

    @Test
    void putThenTakeInOrder() throws InterruptedException {
        BoundedQueue<String> queue = new BoundedQueue<>(2);
        queue.put("a");
        queue.put("b");
        assertThat(queue.size()).isEqualTo(2);
        assertThat(queue.take()).isEqualTo("a");
        assertThat(queue.take()).isEqualTo("b");
        assertThat(queue.size()).isZero();
    }

    @Test
    void putWakesAWaitingTaker() throws InterruptedException {
        BoundedQueue<String> queue = new BoundedQueue<>(2);
        AtomicReference<Object> got = new AtomicReference<>();
        Thread taker = start(got, queue::take);
        assertThat(parked(taker)).as("take waits on an empty queue").isTrue();
        queue.put("x");
        taker.join(3_000);
        assertThat(taker.isAlive()).as("the put woke the taker").isFalse();
        assertThat(got.get()).isEqualTo("x");
    }

    @Test
    void takeWakesAWaitingPutter() throws InterruptedException {
        BoundedQueue<String> queue = new BoundedQueue<>(1);
        queue.put("a");
        AtomicReference<Object> done = new AtomicReference<>();
        Thread putter = start(done, () -> {
            queue.put("b");
            return "put";
        });
        assertThat(parked(putter)).as("put waits on a full queue").isTrue();
        assertThat(queue.take()).isEqualTo("a");
        putter.join(3_000);
        assertThat(putter.isAlive()).as("the take woke the putter").isFalse();
        assertThat(done.get()).isEqualTo("put");
        assertThat(queue.take()).isEqualTo("b");
    }

    @Test
    void pollWakesAWaitingPutter() throws InterruptedException {
        BoundedQueue<String> queue = new BoundedQueue<>(1);
        queue.put("a");
        AtomicReference<Object> done = new AtomicReference<>();
        Thread putter = start(done, () -> {
            queue.put("b");
            return "put";
        });
        assertThat(parked(putter)).as("put waits on a full queue").isTrue();
        assertThat(queue.poll(1, TimeUnit.SECONDS)).isEqualTo("a");
        putter.join(3_000);
        assertThat(putter.isAlive()).as("the poll woke the putter").isFalse();
        assertThat(done.get()).isEqualTo("put");
        assertThat(queue.take()).isEqualTo("b");
    }

    @Test
    void pollGivesUpWhenEmpty() throws InterruptedException {
        BoundedQueue<String> queue = new BoundedQueue<>(2);
        AtomicReference<Object> got = new AtomicReference<>("unset");
        Thread poller = start(got, () -> queue.poll(100_000, TimeUnit.MICROSECONDS));
        poller.join(4_000);
        boolean gaveUp = !poller.isAlive();
        queue.put("late");
        poller.join(3_000);
        assertThat(gaveUp).as("poll gave up at its timeout").isTrue();
        assertThat(got.get()).isNull();
    }

    @Test
    void pollWaitsForALatePut() throws InterruptedException {
        BoundedQueue<String> queue = new BoundedQueue<>(2);
        AtomicReference<Object> got = new AtomicReference<>("unset");
        Thread poller = start(got, () -> queue.poll(8, TimeUnit.SECONDS));
        boolean waiting = parked(poller);
        queue.put("z");
        poller.join(3_000);
        assertThat(waiting).as("poll waits on an empty queue").isTrue();
        assertThat(got.get()).isEqualTo("z");
    }

    @Test
    void twoSeparateConditions() throws Exception {
        BoundedQueue<String> queue = new BoundedQueue<>(2);
        Set<Object> conditions = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Field field : BoundedQueue.class.getDeclaredFields()) {
            if (Condition.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                conditions.add(field.get(queue));
            }
        }
        assertThat(conditions).as("two separate Condition objects, one for each side").hasSize(2);
    }

    @Test
    void thePollTimeoutIsKeptExactly() throws Exception {
        BoundedQueue<String> queue = new BoundedQueue<>(2);
        long start = System.nanoTime();
        assertThat(queue.poll(250_000, TimeUnit.MICROSECONDS)).isNull();
        long waited = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertThat(waited).as("a timeout of 250,000 microseconds waits the full 250 ms").isGreaterThanOrEqualTo(245);
        start = System.nanoTime();
        assertThat(queue.poll(1_600, TimeUnit.MILLISECONDS)).isNull();
        waited = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertThat(waited).as("a timeout of 1,600 ms waits the full 1.6 s, not a rounded second").isGreaterThanOrEqualTo(1_595);
    }
}
