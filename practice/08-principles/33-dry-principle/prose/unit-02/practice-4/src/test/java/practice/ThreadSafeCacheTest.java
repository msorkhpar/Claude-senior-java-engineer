package practice;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

class ThreadSafeCacheTest {

    @Test
    void computesOnFirstAccessAndReusesTheValue() throws Exception {
        ThreadSafeCache<String, String> cache = new ThreadSafeCache<>();
        AtomicInteger calls = new AtomicInteger();
        Function<String, String> load = key -> {
            calls.incrementAndGet();
            return new String("value of " + key);
        };
        String first = cache.getOrCompute("user-1001", load);
        String again = cache.getOrCompute("user-1001", load);
        assertThat(first).isEqualTo("value of user-1001");
        assertThat(again).isSameAs(first);
        assertThat(calls.get()).isEqualTo(1);
        assertThat(cache.get("user-1001")).containsSame(first);
        assertThat(cache.get("user-2002")).isEmpty();
        assertThat(cache.size()).isEqualTo(1);
        cache.invalidate("user-1001");
        assertThat(cache.get("user-1001")).isEmpty();
        assertThat(cache.getOrCompute("user-1001", load)).isEqualTo("value of user-1001");
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void twoThreadsRacingForOneKeyComputeItOnce() throws Exception {
        ThreadSafeCache<String, String> cache = new ThreadSafeCache<>();
        cache.getOrCompute("warm-up", key -> "ready");
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<String> seenBySecond = new AtomicReference<>();
        Thread[] second = new Thread[1];
        Function<String, String> load = key -> {
            calls.incrementAndGet();
            if (Thread.currentThread() != second[0]) {
                // the first caller lets the second one in while it is still computing, and waits
                // until the second caller either computes too or is held back by the cache
                second[0].start();
                while (calls.get() < 2
                        && second[0].getState() != Thread.State.BLOCKED
                        && second[0].getState() != Thread.State.WAITING) {
                    Thread.onSpinWait();
                }
            }
            return new String("value of " + key);
        };
        second[0] = new Thread(() -> seenBySecond.set(cache.getOrCompute("order-5000", load)));
        String seenByFirst = cache.getOrCompute("order-5000", load);
        second[0].join();
        assertThat(calls.get()).as("times the value was computed").isEqualTo(1);
        assertThat(seenBySecond.get()).isSameAs(seenByFirst);
    }

    @Test
    void invalidateForgetsOnlyThatKey() throws Exception {
        ThreadSafeCache<String, String> cache = new ThreadSafeCache<>();
        AtomicInteger calls = new AtomicInteger();
        Function<String, String> load = key -> {
            calls.incrementAndGet();
            return new String("value of " + key);
        };
        String kept = cache.getOrCompute("a", load);
        cache.getOrCompute("b", load);
        cache.invalidate("b");
        assertThat(cache.size()).isEqualTo(1);
        assertThat(cache.get("a")).containsSame(kept);
        assertThat(cache.getOrCompute("a", load)).isSameAs(kept);
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void twoKeysAreComputedAtOnce() throws Exception {
        ThreadSafeCache<String, String> cache = new ThreadSafeCache<>();
        java.util.concurrent.CountDownLatch aStarted = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch bDone = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicBoolean overlapped = new java.util.concurrent.atomic.AtomicBoolean();
        Thread slow = new Thread(() -> cache.getOrCompute("a", k -> {
            aStarted.countDown();
            try {
                overlapped.set(bDone.await(5, java.util.concurrent.TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "A";
        }));
        slow.setDaemon(true);
        slow.start();
        assertThat(aStarted.await(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
        Thread quick = new Thread(() -> {
            cache.getOrCompute("b", k -> "B");
            bDone.countDown();
        });
        quick.setDaemon(true);
        quick.start();
        slow.join(10_000);
        quick.join(10_000);
        assertThat(overlapped.get()).as("\"b\" was computed while \"a\" was still computing").isTrue();
        assertThat(cache.get("b")).contains("B");
    }
}
