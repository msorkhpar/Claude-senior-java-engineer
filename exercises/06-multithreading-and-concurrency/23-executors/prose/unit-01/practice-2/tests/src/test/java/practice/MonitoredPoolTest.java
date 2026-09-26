package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class MonitoredPoolTest {

    private MonitoredPool pool;

    @AfterEach
    void stop() throws InterruptedException {
        if (pool != null) {
            pool.shutdownNow();
            pool.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private void drain() throws InterruptedException {
        pool.shutdown();
        assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).as("the pool terminated").isTrue();
    }

    @Test
    void reportsAFailureFromExecute() throws InterruptedException {
        pool = new MonitoredPool(1);
        pool.execute(() -> { });
        pool.execute(() -> {
            throw new IllegalStateException("boom");
        });
        drain();
        assertThat(pool.failures()).containsExactly("boom");
        assertThat(pool.successes()).isEqualTo(1);
    }

    @Test
    void reportsAFailureFromSubmit() throws InterruptedException {
        pool = new MonitoredPool(1);
        pool.submit(() -> {
            throw new IllegalStateException("bad");
        });
        pool.submit(() -> { });
        drain();
        assertThat(pool.failures()).containsExactly("bad");
        assertThat(pool.successes()).isEqualTo(1);
    }

    @Test
    void reportsTheCauseNotTheWrapper() throws InterruptedException {
        pool = new MonitoredPool(1);
        Callable<String> failing = () -> {
            throw new IOException("disk");
        };
        pool.submit(failing);
        drain();
        assertThat(pool.failures()).containsExactly("disk");
        assertThat(pool.successes()).isZero();
    }

    @Test
    void finishedOnlyAfterTermination() throws InterruptedException {
        pool = new MonitoredPool(1);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        pool.execute(() -> {
            started.countDown();
            try {
                release.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertThat(started.await(5, TimeUnit.SECONDS)).as("the task started").isTrue();
        pool.shutdown();
        assertThat(pool.finished()).as("finished while a task still runs").isFalse();
        release.countDown();
        assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).as("the pool terminated").isTrue();
        assertThat(pool.finished()).as("finished after termination").isTrue();
        assertThat(pool.successes()).isEqualTo(1);
    }
}
