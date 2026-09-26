package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class FanOutTest {

    /** Waits, at most 5 s, until the thread recorded in the reference has ended. */
    private static void awaitEnded(AtomicReference<Thread> thread) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while ((thread.get() == null || thread.get().isAlive()) && System.nanoTime() < deadline) {
            Thread.sleep(1);
        }
    }

    @Test
    void returnsEveryResult() throws Exception {
        List<Callable<String>> tasks = List.of(() -> "a", () -> "b", () -> "c");
        assertThat(FanOut.all(tasks)).containsExactlyInAnyOrder("a", "b", "c");
    }

    @Test
    void keepsTaskOrderWhenALaterTaskFinishesFirst() throws Exception {
        AtomicReference<Thread> second = new AtomicReference<>();
        List<Callable<String>> tasks = List.of(
                () -> {
                    awaitEnded(second);
                    return "first";
                },
                () -> {
                    second.set(Thread.currentThread());
                    return "second";
                });
        assertThat(FanOut.all(tasks)).containsExactly("first", "second");
    }

    @Test
    void runsEachTaskOnAVirtualThread() throws Exception {
        Callable<Boolean> virtual = () -> Thread.currentThread().isVirtual();
        assertThat(FanOut.all(List.of(virtual, virtual, virtual))).containsExactly(true, true, true);
    }

    @Test
    void keepsEveryTaskInFlightAtOnce() throws Exception {
        int n = 50;
        CountDownLatch allStarted = new CountDownLatch(n);
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            tasks.add(() -> {
                allStarted.countDown();
                return allStarted.await(3, TimeUnit.SECONDS);
            });
        }
        assertThat(FanOut.all(tasks)).as("each task saw all 50 in flight").containsOnly(true).hasSize(n);
    }

    @Test
    void rethrowsAFailedTasksOwnException() {
        AtomicReference<Thread> later = new AtomicReference<>();
        List<Callable<String>> tasks = List.of(() -> "a", () -> {
            awaitEnded(later);
            throw new IOException("down");
        }, () -> {
            later.set(Thread.currentThread());
            throw new IOException("later");
        });
        assertThatThrownBy(() -> FanOut.all(tasks)).isInstanceOf(IOException.class).hasMessage("down");
    }

    @Test
    void aFailureStillWaitsForEveryTask() {
        AtomicBoolean slowFinished = new AtomicBoolean();
        List<Callable<String>> tasks = List.of(() -> {
            throw new IOException("fast failure");
        }, () -> {
            Thread.sleep(500);
            slowFinished.set(true);
            return "slow";
        });
        assertThatThrownBy(() -> FanOut.all(tasks)).isInstanceOf(IOException.class).hasMessage("fast failure");
        assertThat(slowFinished).as("all waited for every task, the slow one too, before it threw").isTrue();
    }
}
