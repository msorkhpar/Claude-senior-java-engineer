package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class KeeperTest {

    @Test
    void startsAThreadThatWasNeverStarted() throws InterruptedException {
        AtomicInteger runs = new AtomicInteger();
        Runnable job = runs::incrementAndGet;
        Thread thread = new Thread(job, "report-1");
        Thread result = Keeper.ensureStarted(thread, job);
        assertThat(result).isSameAs(thread);
        result.join(2_000);
        assertThat(result.getState()).isEqualTo(Thread.State.TERMINATED);
        assertThat(runs.get()).isEqualTo(1);
    }

    @Test
    void leavesARunningThreadRunning() throws InterruptedException {
        CountDownLatch gate = new CountDownLatch(1);
        AtomicInteger runs = new AtomicInteger();
        Runnable job = () -> {
            runs.incrementAndGet();
            try {
                gate.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
        Thread thread = new Thread(job, "report-2");
        thread.start();
        try {
            Thread result = Keeper.ensureStarted(thread, job);
            assertThat(result).isSameAs(thread);
        } finally {
            gate.countDown();
            thread.join(2_000);
        }
        assertThat(runs.get()).isEqualTo(1);
    }

    @Test
    void replacesAFinishedThread() throws InterruptedException {
        AtomicInteger runs = new AtomicInteger();
        Runnable job = runs::incrementAndGet;
        Thread thread = new Thread(job, "report-3");
        thread.start();
        thread.join(2_000);
        Thread result = Keeper.ensureStarted(thread, job);
        assertThat(result).isNotSameAs(thread);
        result.join(2_000);
        assertThat(runs.get()).isEqualTo(2);
    }

    @Test
    void theReplacementKeepsTheName() throws InterruptedException {
        Runnable job = () -> { };
        Thread thread = new Thread(job, "report-4");
        thread.start();
        thread.join(2_000);
        Thread result = Keeper.ensureStarted(thread, job);
        result.join(2_000);
        assertThat(result.getName()).isEqualTo("report-4");
    }
}
