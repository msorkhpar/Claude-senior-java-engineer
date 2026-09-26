package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class FanoutTest {

    /** Reads the list a few times over at most two seconds, until it holds no gap. */
    private static List<Integer> settled(List<Integer> list) throws InterruptedException {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        List<Integer> copy = new ArrayList<>(list);
        while (copy.contains(null) && System.nanoTime() < end) {
            Thread.sleep(10);
            copy = new ArrayList<>(list);
        }
        return copy;
    }

    /** Whether the thread is parked inside Thread.join, read from its stack rather than its state. */
    private static boolean inJoin(Thread thread) {
        Thread.State state = thread.getState();
        if (state != Thread.State.WAITING && state != Thread.State.TIMED_WAITING) {
            return false;
        }
        for (StackTraceElement frame : thread.getStackTrace()) {
            if (frame.getClassName().equals("java.lang.Thread") && frame.getMethodName().equals("join")) {
                return true;
            }
        }
        return false;
    }

    @Test
    void returnsEveryResultInInputOrder() throws InterruptedException {
        assertThat(settled(Fanout.mapAll(List.of(3, 1, 4, 2), x -> x * x))).containsExactly(9, 1, 16, 4);
        assertThat(settled(Fanout.mapAll(List.of(5), x -> x + 1))).containsExactly(6);
        assertThat(Fanout.mapAll(List.of(), x -> x)).isEmpty();
    }

    @Test
    void runsEveryInputAtTheSameTime() throws InterruptedException {
        CountDownLatch arrived = new CountDownLatch(3);
        Function<Integer, Integer> meetTheOthers = x -> {
            arrived.countDown();
            try {
                if (!arrived.await(2, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("input " + x + " ran alone");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
            return x * 10;
        };
        assertThat(settled(Fanout.mapAll(List.of(1, 2, 3), meetTheOthers))).containsExactly(10, 20, 30);
    }

    @Test
    void returnsOnlyWhenEveryWorkerIsDone() throws InterruptedException {
        CountDownLatch gate = new CountDownLatch(1);
        Function<Integer, Integer> held = x -> {
            try {
                gate.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return x * x;
        };
        AtomicReference<List<Integer>> atReturn = new AtomicReference<>();
        Thread caller = new Thread(() -> {
            try {
                atReturn.set(new ArrayList<>(Fanout.mapAll(List.of(2, 3, 4), held)));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        caller.setDaemon(true);
        caller.start();
        // Open the gate only once the caller is inside Thread.join or has already returned.
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (System.nanoTime() < end && caller.getState() != Thread.State.TERMINATED && !inJoin(caller)) {
            Thread.sleep(5);
        }
        gate.countDown();
        caller.join(3_000);
        assertThat(atReturn.get()).containsExactly(4, 9, 16);
    }
}
