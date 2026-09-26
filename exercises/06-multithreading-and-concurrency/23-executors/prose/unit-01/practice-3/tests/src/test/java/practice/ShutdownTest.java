package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ShutdownTest {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<String> ran = new CopyOnWriteArrayList<>();
    private final CountDownLatch started = new CountDownLatch(1);
    private final CountDownLatch interrupted = new CountDownLatch(1);

    @AfterEach
    void stop() throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    /** A task that waits on the gate (at most 5 s), noting whether it was interrupted instead. */
    private Runnable waitingOn(CountDownLatch gate, String name) {
        return () -> {
            started.countDown();
            try {
                gate.await(5, TimeUnit.SECONDS);
                ran.add(name);
            } catch (InterruptedException e) {
                interrupted.countDown();
            }
        };
    }

    private Runnable recording(String name) {
        return () -> ran.add(name);
    }

    /** Waits, at most 5 s, until the condition holds. */
    private static void until(java.util.function.BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Thread.sleep(1);
        }
    }

    @Test
    void anIdlePoolClosesWithNothingLeft() throws Exception {
        executor.submit(recording("a")).get(5, TimeUnit.SECONDS);
        assertThat(Shutdown.close(executor, 5, TimeUnit.SECONDS)).isEmpty();
        assertThat(executor.isTerminated()).isTrue();
        assertThat(ran).containsExactly("a");
    }

    @Test
    void queuedTasksStillRun() throws InterruptedException {
        CountDownLatch gate = new CountDownLatch(1);
        executor.execute(waitingOn(gate, "a"));
        executor.execute(recording("b"));
        executor.execute(recording("c"));
        assertThat(started.await(5, TimeUnit.SECONDS)).as("the first task started").isTrue();
        AtomicReference<List<Runnable>> left = new AtomicReference<>();
        Thread closer = new Thread(() -> left.set(Shutdown.close(executor, 5, TimeUnit.SECONDS)));
        closer.setDaemon(true);
        closer.start();
        until(() -> executor.isShutdown() || !closer.isAlive());
        gate.countDown();
        closer.join(5_000);
        assertThat(closer.isAlive()).as("close returned").isFalse();
        assertThat(left.get()).isEmpty();
        assertThat(ran).containsExactly("a", "b", "c");
    }

    @Test
    void aStuckTaskIsForced() throws InterruptedException {
        CountDownLatch never = new CountDownLatch(1);
        CountDownLatch mayExit = new CountDownLatch(1);
        executor.execute(() -> {
            started.countDown();
            try {
                never.await(8, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                interrupted.countDown();
                try {
                    mayExit.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException again) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        Runnable queued = recording("b");
        executor.execute(queued);
        assertThat(started.await(5, TimeUnit.SECONDS)).as("the first task started").isTrue();
        Thread caller = Thread.currentThread();
        Thread releaser = new Thread(() -> {
            try {
                if (interrupted.await(8, TimeUnit.SECONDS)) {
                    until(() -> caller.getState() == Thread.State.TIMED_WAITING);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                mayExit.countDown();
            }
        });
        releaser.setDaemon(true);
        releaser.start();
        List<Runnable> left = Shutdown.close(executor, 3, TimeUnit.SECONDS);
        assertThat(executor.isTerminated()).as("close waited for the forced shutdown to finish").isTrue();
        assertThat(left).containsExactly(queued);
        assertThat(interrupted.await(5, TimeUnit.SECONDS)).as("the running task was interrupted").isTrue();
        assertThat(ran).isEmpty();
    }

    @Test
    void anInterruptedCallerForcesAndKeepsTheFlag() throws InterruptedException {
        CountDownLatch never = new CountDownLatch(1);
        executor.execute(waitingOn(never, "a"));
        assertThat(started.await(5, TimeUnit.SECONDS)).as("the task started").isTrue();
        AtomicBoolean flagKept = new AtomicBoolean();
        Thread closer = new Thread(() -> {
            Shutdown.close(executor, 30, TimeUnit.SECONDS);
            flagKept.set(Thread.currentThread().isInterrupted());
        });
        closer.setDaemon(true);
        closer.start();
        until(() -> (executor.isShutdown() && closer.getState() == Thread.State.TIMED_WAITING) || !closer.isAlive());
        closer.interrupt();
        closer.join(5_000);
        assertThat(closer.isAlive()).as("close returned after the interrupt").isFalse();
        assertThat(flagKept.get()).as("the caller's interrupt flag is still set").isTrue();
        assertThat(interrupted.await(5, TimeUnit.SECONDS)).as("the running task was interrupted").isTrue();
        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).as("the executor terminated").isTrue();
    }
}
