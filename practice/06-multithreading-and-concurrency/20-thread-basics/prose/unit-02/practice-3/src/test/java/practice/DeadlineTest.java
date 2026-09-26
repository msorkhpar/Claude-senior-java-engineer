package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class DeadlineTest {

    /** A started daemon worker that waits on {@code never} (at most 10 s) and counts {@code interrupted} down if interrupted. */
    private static Thread stuck(CountDownLatch never, CountDownLatch interrupted) {
        Thread worker = new Thread(() -> {
            try {
                never.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                interrupted.countDown();
            }
        }, "stuck-worker");
        worker.setDaemon(true);
        worker.start();
        return worker;
    }

    @Test
    void tellsFinishedFromStillRunning() throws InterruptedException {
        Thread quick = new Thread(() -> { }, "quick-worker");
        quick.start();
        assertThat(Deadline.await(quick, 5_000)).isEqualTo(Deadline.Outcome.FINISHED);

        CountDownLatch never = new CountDownLatch(1);
        Thread worker = stuck(never, new CountDownLatch(1));
        try {
            assertThat(Deadline.await(worker, 100)).isEqualTo(Deadline.Outcome.CANCELLED);
        } finally {
            never.countDown();
        }
    }

    @Test
    void aCancelledWorkerIsInterrupted() throws InterruptedException {
        CountDownLatch never = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        Thread worker = stuck(never, interrupted);
        try {
            assertThat(Deadline.await(worker, 100)).isEqualTo(Deadline.Outcome.CANCELLED);
            assertThat(interrupted.await(2, TimeUnit.SECONDS)).as("the worker was interrupted").isTrue();
        } finally {
            never.countDown();
        }
    }

    @Test
    void anUnstartedWorkerIsNotWaitedFor() throws InterruptedException {
        Thread idle = new Thread(() -> { }, "idle-worker");
        assertThat(Deadline.await(idle, 100)).isEqualTo(Deadline.Outcome.NOT_STARTED);
    }

    @Test
    void aZeroTimeoutIsRefused() throws InterruptedException {
        CountDownLatch never = new CountDownLatch(1);
        Thread worker = stuck(never, new CountDownLatch(1));
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread caller = new Thread(() -> {
            try {
                Deadline.await(worker, 0);
            } catch (Throwable t) {
                thrown.set(t);
            }
        });
        caller.setDaemon(true);
        try {
            caller.start();
            caller.join(1_000);
            assertThat(thrown.get()).isInstanceOf(IllegalArgumentException.class);
        } finally {
            never.countDown();
        }
    }
}
