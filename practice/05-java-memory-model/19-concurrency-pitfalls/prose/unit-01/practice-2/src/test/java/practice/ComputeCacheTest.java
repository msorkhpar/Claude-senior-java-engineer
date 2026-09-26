package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ComputeCacheTest {

    @Test
    void computesAndCaches() {
        AtomicInteger runs = new AtomicInteger();
        ComputeCache cache = new ComputeCache(key -> {
            runs.incrementAndGet();
            return key.toUpperCase();
        });
        assertThat(cache.get("a")).isEqualTo("A");
        assertThat(cache.get("a")).isEqualTo("A");
        assertThat(runs.get()).isEqualTo(1);
        assertThat(cache.get("b")).isEqualTo("B");
        assertThat(cache.size()).isEqualTo(2);
        assertThat(runs.get()).isEqualTo(2);
    }

    @Test
    void racingCallersShareOneComputation() throws InterruptedException {
        AtomicInteger runs = new AtomicInteger();
        CountDownLatch firstInside = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ComputeCache cache = new ComputeCache(key -> {
            if (runs.incrementAndGet() == 1) {
                firstInside.countDown();
                try {
                    release.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return key.toUpperCase();
        });
        String[] got = new String[2];
        Thread first = daemon(() -> got[0] = cache.get("x"));
        assertThat(firstInside.await(5, TimeUnit.SECONDS)).as("the first caller started computing").isTrue();
        // The first computation is now in progress and held open. A second caller for the same
        // key must wait for it; if it computes the key itself, the function runs twice.
        Thread second = daemon(() -> got[1] = cache.get("x"));
        waitFor(() -> runs.get() >= 2 || blockedBy(second, first) || !second.isAlive(), "the second caller to wait or compute");
        release.countDown();
        first.join(5_000);
        second.join(5_000);
        assertThat(first.isAlive() || second.isAlive()).as("both callers finished").isFalse();
        assertThat(got).containsExactly("X", "X");
        assertThat(runs.get()).as("times the function ran for one key").isEqualTo(1);
    }

    /** True only when the thread waits for a lock that the given holder thread owns. */
    private static boolean blockedBy(Thread waiter, Thread holder) {
        java.lang.management.ThreadInfo info =
                java.lang.management.ManagementFactory.getThreadMXBean().getThreadInfo(waiter.threadId());
        return info != null && info.getLockOwnerId() == holder.threadId();
    }

    @Test
    void equalKeysShareOneEntry() {
        AtomicInteger runs = new AtomicInteger();
        ComputeCache cache = new ComputeCache(key -> {
            runs.incrementAndGet();
            return key.toUpperCase();
        });
        String built = new StringBuilder("a").append('b').toString();
        assertThat(cache.get(new String("ab"))).isEqualTo("AB");
        assertThat(cache.get(built)).isEqualTo("AB");
        assertThat(runs.get()).as("times the function ran for one key text").isEqualTo(1);
        assertThat(cache.size()).isEqualTo(1);
    }

    private static Thread daemon(Runnable body) {
        Thread thread = new Thread(body);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private static void waitFor(BooleanSupplier condition, String what) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean()) {
            assertThat(System.nanoTime() < deadline).as("timed out waiting for " + what).isTrue();
            Thread.sleep(1);
        }
    }
}
