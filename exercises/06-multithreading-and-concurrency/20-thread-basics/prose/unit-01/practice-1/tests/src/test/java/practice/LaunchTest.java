package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class LaunchTest {

    @Test
    void runsTheTaskOnANamedThread() throws InterruptedException {
        AtomicInteger runs = new AtomicInteger();
        Thread thread = Launch.launch("payment-processor-1", runs::incrementAndGet);
        thread.join(2_000);
        assertThat(thread.isAlive()).isFalse();
        assertThat(runs.get()).isEqualTo(1);
        assertThat(thread.getName()).isEqualTo("payment-processor-1");
    }

    @Test
    void runsOnTheNewThreadNotTheCaller() throws InterruptedException {
        AtomicReference<Thread> seen = new AtomicReference<>();
        Thread thread = Launch.launch("worker-7", () -> seen.set(Thread.currentThread()));
        thread.join(2_000);
        assertThat(seen.get()).isNotSameAs(Thread.currentThread());
        assertThat(seen.get()).isSameAs(thread);
    }

    @Test
    void returnsWithoutWaitingForTheTask() throws InterruptedException {
        CountDownLatch gate = new CountDownLatch(1);
        AtomicReference<Thread> launched = new AtomicReference<>();
        AtomicBoolean returned = new AtomicBoolean();
        Thread caller = new Thread(() -> {
            launched.set(Launch.launch("slow", () -> {
                try {
                    gate.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
            returned.set(true);
        });
        caller.setDaemon(true);
        try {
            caller.start();
            caller.join(2_000);
            assertThat(returned.get()).as("launch returned while the task still waited").isTrue();
            assertThat(launched.get().isAlive()).isTrue();
        } finally {
            gate.countDown();
        }
        launched.get().join(2_000);
    }
}
