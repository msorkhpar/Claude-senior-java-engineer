package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class PriceCacheTest {

    private static final ThreadMXBean THREADS = ManagementFactory.getThreadMXBean();

    /**
     * Waits, at most 20 s, until t has finished or is parked inside PriceCache.put on a
     * java.util.concurrent lock or condition, or in Object.wait on a monitor; true if parked there.
     */
    private static boolean parked(Thread t) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while (System.nanoTime() < deadline) {
            Thread.State state = t.getState();
            if (state == Thread.State.TERMINATED) {
                return false;
            }
            ThreadInfo info = THREADS.getThreadInfo(t.threadId(), Integer.MAX_VALUE);
            if ((state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING)
                    && info != null && info.getLockInfo() != null && insidePut(info)
                    && (info.getLockInfo().getClassName().startsWith("java.util.concurrent.")
                        || waitsOnMonitor(info))) {
                return true;
            }
            LockSupport.parkNanos(1_000_000);
        }
        return false;
    }

    private static boolean insidePut(ThreadInfo info) {
        for (StackTraceElement frame : info.getStackTrace()) {
            if (frame.getClassName().equals("practice.PriceCache") && frame.getMethodName().equals("put")) {
                return true;
            }
        }
        return false;
    }

    private static boolean waitsOnMonitor(ThreadInfo info) {
        for (StackTraceElement frame : info.getStackTrace()) {
            if (frame.getClassName().equals("java.lang.Object") && frame.getMethodName().startsWith("wait")) {
                return true;
            }
        }
        return false;
    }

    private static Thread daemon(Runnable body) {
        Thread t = new Thread(body);
        t.setDaemon(true);
        t.start();
        return t;
    }

    /** Starts a reader that stays inside view() until release opens; returns once it is inside. */
    private static Thread readerInside(PriceCache cache, CountDownLatch release) throws InterruptedException {
        CountDownLatch inside = new CountDownLatch(1);
        Thread reader = daemon(() -> cache.view("tea", p -> {
            inside.countDown();
            try {
                release.await(50, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return p;
        }));
        assertThat(inside.await(20, TimeUnit.SECONDS)).as("the reader is inside view()").isTrue();
        return reader;
    }

    @Test
    void storesAndReadsPrices() {
        PriceCache cache = new PriceCache();
        assertThat(cache.get("coffee")).isNull();
        cache.put("tea", 3);
        cache.put("cake", 5);
        assertThat(cache.get("tea")).isEqualTo(3);
        Integer doubled = cache.view("tea", p -> p * 2);
        String missing = cache.view("coffee", p -> p == null ? "none" : "some");
        assertThat(doubled).isEqualTo(6);
        assertThat(missing).isEqualTo("none");
        Map<String, Integer> snap = cache.snapshot();
        cache.put("tea", 4);
        assertThat(snap).containsExactlyInAnyOrderEntriesOf(Map.of("tea", 3, "cake", 5));
        assertThat(cache.get("tea")).isEqualTo(4);
    }

    @Test
    void readersDoNotWaitForEachOther() throws InterruptedException {
        PriceCache cache = new PriceCache();
        cache.put("tea", 3);
        CountDownLatch release = new CountDownLatch(1);
        Thread first = readerInside(cache, release);
        AtomicReference<Integer> seen = new AtomicReference<>();
        Thread second = daemon(() -> seen.set(cache.get("tea")));
        second.join(10_000);
        boolean finishedWhileTheFirstRead = !second.isAlive();
        release.countDown();
        first.join(20_000);
        second.join(20_000);
        assertThat(finishedWhileTheFirstRead).as("a second reader shares the read lock").isTrue();
        assertThat(seen.get()).isEqualTo(3);
    }

    @Test
    void aWriterWaitsForReaders() throws InterruptedException {
        PriceCache cache = new PriceCache();
        cache.put("tea", 3);
        CountDownLatch release = new CountDownLatch(1);
        Thread reader = readerInside(cache, release);
        Thread writer = daemon(() -> cache.put("tea", 4));
        boolean waitedWhileReading = parked(writer);
        release.countDown();
        reader.join(20_000);
        writer.join(20_000);
        assertThat(waitedWhileReading).as("put waits while a reader holds the read lock").isTrue();
        assertThat(writer.isAlive()).isFalse();
        assertThat(cache.get("tea")).isEqualTo(4);
    }
}
