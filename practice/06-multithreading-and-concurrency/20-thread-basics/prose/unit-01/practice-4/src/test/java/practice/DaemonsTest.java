package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class DaemonsTest {

    @Test
    void marksAnUnstartedThread() {
        Thread monitor = new Thread(() -> { }, "metrics-monitor");
        assertThat(Daemons.markBackground(monitor)).isTrue();
        assertThat(monitor.isDaemon()).isTrue();
        assertThat(monitor.getState()).isEqualTo(Thread.State.NEW);
    }

    @Test
    void leavesARunningThreadAlone() throws InterruptedException {
        CountDownLatch gate = new CountDownLatch(1);
        Thread worker = new Thread(() -> {
            try {
                gate.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "worker");
        worker.start();
        try {
            boolean answer = Daemons.markBackground(worker);
            assertThat(answer).isFalse();
            assertThat(worker.isDaemon()).isFalse();
        } finally {
            gate.countDown();
            worker.join(2_000);
        }
    }

    @Test
    void leavesAFinishedThreadAlone() throws InterruptedException {
        Thread done = new Thread(() -> { }, "done");
        done.start();
        done.join(2_000);
        assertThat(done.isAlive()).isFalse();
        assertThat(Daemons.markBackground(done)).isFalse();
        assertThat(done.isDaemon()).isFalse();
    }
}
