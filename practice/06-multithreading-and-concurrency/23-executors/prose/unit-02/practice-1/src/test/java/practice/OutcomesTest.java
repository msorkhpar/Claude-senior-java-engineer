package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class OutcomesTest {

    @Test
    void describesAValue() throws InterruptedException {
        FutureTask<Integer> task = new FutureTask<>(() -> 42);
        task.run();
        assertThat(Outcomes.describe(task, 1, TimeUnit.SECONDS)).isEqualTo("value: 42");
    }

    @Test
    void describesTheCauseOfAFailure() throws InterruptedException {
        FutureTask<String> task = new FutureTask<>(() -> {
            throw new IOException("File not found");
        });
        task.run();
        assertThat(Outcomes.describe(task, 1, TimeUnit.SECONDS)).isEqualTo("failed: IOException: File not found");
    }

    @Test
    void describesACancelledTask() throws InterruptedException {
        FutureTask<String> task = new FutureTask<>(() -> "never");
        task.cancel(false);
        task.run();
        assertThat(Outcomes.describe(task, 1, TimeUnit.SECONDS)).isEqualTo("cancelled");
    }

    @Test
    void givesUpAfterTheTimeout() throws InterruptedException {
        FutureTask<String> neverRun = new FutureTask<>(() -> "late");
        assertThat(Outcomes.describe(neverRun, 50, TimeUnit.MILLISECONDS)).isEqualTo("timed out");
    }

    @Test
    void waitsForALateResult() throws InterruptedException {
        FutureTask<String> task = new FutureTask<>(() -> "late");
        AtomicReference<String> described = new AtomicReference<>();
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread waiter = new Thread(() -> {
            try {
                described.set(Outcomes.describe(task, 5, TimeUnit.SECONDS));
            } catch (Throwable t) {
                thrown.set(t);
            }
        });
        waiter.setDaemon(true);
        waiter.start();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (waiter.isAlive() && waiter.getState() != Thread.State.TIMED_WAITING && System.nanoTime() < deadline) {
            Thread.sleep(1);
        }
        task.run();
        waiter.join(5_000);
        assertThat(thrown.get()).isNull();
        assertThat(described.get()).as("describe waited for the result that came in time").isEqualTo("value: late");
    }

    @Test
    void anInterruptLeavesAsAnException() {
        FutureTask<String> neverRun = new FutureTask<>(() -> "late");
        Thread.currentThread().interrupt();
        try {
            assertThatThrownBy(() -> Outcomes.describe(neverRun, 1, TimeUnit.SECONDS))
                    .isInstanceOf(InterruptedException.class);
        } finally {
            Thread.interrupted();
        }
    }
}
