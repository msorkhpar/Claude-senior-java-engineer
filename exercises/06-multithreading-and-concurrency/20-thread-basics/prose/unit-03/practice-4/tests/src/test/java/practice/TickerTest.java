package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class TickerTest {

    private static Thread daemon(Runnable body) {
        Thread thread = new Thread(body);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    /** Waits, at most 5 s, until the thread sleeps (or has ended). */
    private static void awaitSleeping(Thread thread) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (thread.getState() != Thread.State.TIMED_WAITING && thread.isAlive() && System.nanoTime() < deadline) {
            Thread.sleep(1);
        }
    }

    @Test
    void cancelStopsIt() throws InterruptedException {
        CountDownLatch threeTicks = new CountDownLatch(3);
        Ticker ticker = new Ticker(threeTicks::countDown, 1, () -> { });
        Thread thread = daemon(ticker);
        assertThat(threeTicks.await(5, TimeUnit.SECONDS)).as("the ticker ticked three times").isTrue();
        ticker.cancel();
        thread.join(5_000);
        assertThat(thread.isAlive()).as("the ticker's thread ended after cancel()").isFalse();
    }

    @Test
    void cancelWakesASleepingTicker() throws InterruptedException {
        CountDownLatch ticked = new CountDownLatch(1);
        Ticker ticker = new Ticker(ticked::countDown, 60_000, () -> { });
        Thread thread = daemon(ticker);
        assertThat(ticked.await(5, TimeUnit.SECONDS)).isTrue();
        awaitSleeping(thread);
        ticker.cancel();
        thread.join(5_000);
        assertThat(thread.isAlive()).as("cancel() woke the sleeping ticker").isFalse();
    }

    @Test
    void anInterruptAloneStopsIt() throws InterruptedException {
        CountDownLatch ticked = new CountDownLatch(1);
        Thread thread = daemon(new Ticker(ticked::countDown, 60_000, () -> { }));
        assertThat(ticked.await(5, TimeUnit.SECONDS)).isTrue();
        awaitSleeping(thread);
        thread.interrupt();
        thread.join(5_000);
        assertThat(thread.isAlive()).as("the interrupt stopped the ticker").isFalse();
    }

    @Test
    void cleanupRunsWhenInterrupted() throws InterruptedException {
        CountDownLatch ticked = new CountDownLatch(1);
        AtomicInteger cleanups = new AtomicInteger();
        Thread thread = daemon(new Ticker(ticked::countDown, 60_000, cleanups::incrementAndGet));
        assertThat(ticked.await(5, TimeUnit.SECONDS)).isTrue();
        awaitSleeping(thread);
        thread.interrupt();
        thread.join(5_000);
        assertThat(thread.isAlive()).isFalse();
        assertThat(cleanups.get()).as("cleanup runs once").isEqualTo(1);
    }

    @Test
    void anInterruptLeavesTheFlagSet() throws InterruptedException {
        CountDownLatch ticked = new CountDownLatch(1);
        Ticker ticker = new Ticker(ticked::countDown, 60_000, () -> { });
        AtomicBoolean flagAfterRun = new AtomicBoolean();
        Thread thread = daemon(() -> {
            ticker.run();
            flagAfterRun.set(Thread.currentThread().isInterrupted());
        });
        assertThat(ticked.await(5, TimeUnit.SECONDS)).isTrue();
        thread.interrupt();
        thread.join(5_000);
        assertThat(thread.isAlive()).as("the interrupt stopped the ticker").isFalse();
        assertThat(flagAfterRun.get()).as("run() returned with the interrupt flag set").isTrue();
    }

    @Test
    void theCancelledFlagIsVolatile() throws Exception {
        Ticker ticker = new Ticker(() -> { }, 1, () -> { });
        ticker.cancel();
        Field field = Ticker.class.getDeclaredField("cancelled");
        assertThat(field.getType()).isEqualTo(boolean.class);
        assertThat(Modifier.isVolatile(field.getModifiers())).as("cancelled is volatile").isTrue();
    }
}
