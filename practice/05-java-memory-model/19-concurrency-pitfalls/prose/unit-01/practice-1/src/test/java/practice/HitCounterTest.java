package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class HitCounterTest {

    @Test
    void countsHits() {
        HitCounter counter = new HitCounter();
        assertThat(counter.hits()).isZero();
        counter.hit();
        counter.hit();
        counter.hit();
        assertThat(counter.hits()).isEqualTo(3);
    }

    @Test
    void hitTakesTheCountersLock() throws InterruptedException {
        HitCounter counter = new HitCounter();
        Thread hitter;
        synchronized (counter) {
            hitter = daemon(counter::hit);
            waitFor(() -> blockedOn(hitter, counter) || !hitter.isAlive(), "the hit to wait or finish");
            assertThat(blockedOn(hitter, counter))
                    .as("hit() waits while another thread holds the counter's lock")
                    .isTrue();
        }
        hitter.join(5_000);
        assertThat(counter.hits()).isEqualTo(1);
    }

    @Test
    void readingTakesTheCountersLock() throws InterruptedException {
        HitCounter counter = new HitCounter();
        counter.hit();
        int[] seen = new int[1];
        Thread reader;
        synchronized (counter) {
            reader = daemon(() -> seen[0] = counter.hits());
            waitFor(() -> blockedOn(reader, counter) || !reader.isAlive(), "the read to wait or finish");
            assertThat(blockedOn(reader, counter))
                    .as("hits() waits while another thread holds the counter's lock")
                    .isTrue();
        }
        reader.join(5_000);
        assertThat(seen[0]).isEqualTo(1);
    }

    private static Thread daemon(Runnable body) {
        Thread thread = new Thread(body);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private static boolean blockedOn(Thread thread, Object monitor) {
        ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(thread.threadId());
        return info != null
                && info.getThreadState() == Thread.State.BLOCKED
                && info.getLockInfo() != null
                && info.getLockInfo().getIdentityHashCode() == System.identityHashCode(monitor);
    }

    private static void waitFor(BooleanSupplier condition, String what) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean()) {
            assertThat(System.nanoTime() < deadline).as("timed out waiting for " + what).isTrue();
            Thread.sleep(1);
        }
    }
}
