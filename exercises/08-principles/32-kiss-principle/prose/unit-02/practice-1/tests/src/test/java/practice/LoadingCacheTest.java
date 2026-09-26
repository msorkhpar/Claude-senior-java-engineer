package practice;

import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

class LoadingCacheTest {

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            if (!latch.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("the test never released the loader");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @Test
    void loadsAKeyOnceAndReusesIt() throws Exception {
        LoadingCache<String, Integer> cache = new LoadingCache<>();
        AtomicInteger calls = new AtomicInteger();
        Function<String, Integer> loader = key -> {
            calls.incrementAndGet();
            return key.length() * 1000;
        };
        assertThat(cache.get("answer", loader)).isEqualTo(6000);
        assertThat(cache.get("answer", loader)).isEqualTo(6000);
        assertThat(cache.get("hi", loader)).isEqualTo(2000);
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void twoCallersRacingForOneKeyLoadItOnce() throws Exception {
        LoadingCache<String, String> cache = new LoadingCache<>();
        AtomicInteger calls = new AtomicInteger();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Function<String, String> loader = key -> {
            calls.incrementAndGet();
            entered.countDown();
            awaitQuietly(release);
            return key.toUpperCase();
        };
        AtomicReference<String> first = new AtomicReference<>();
        AtomicReference<String> second = new AtomicReference<>();
        Thread a = new Thread(() -> first.set(cache.get("key", loader)));
        a.start();
        awaitQuietly(entered);
        Thread b = new Thread(() -> second.set(cache.get("key", loader)));
        b.start();
        // The second caller arrives while the first load is still running: wait until it either
        // blocks on the map or starts a second load of its own.
        while (calls.get() < 2 && b.getState() != Thread.State.BLOCKED && b.getState() != Thread.State.WAITING
                && b.getState() != Thread.State.TERMINATED) {
            Thread.onSpinWait();
        }
        release.countDown();
        a.join();
        b.join();
        assertThat(calls.get()).isEqualTo(1);
        assertThat(first.get()).isEqualTo("KEY");
        assertThat(second.get()).isEqualTo("KEY");
    }

    @Test
    void invalidateMakesTheNextGetLoadAgain() throws Exception {
        LoadingCache<String, Integer> cache = new LoadingCache<>();
        AtomicInteger calls = new AtomicInteger();
        Function<String, Integer> loader = key -> 1000 + calls.incrementAndGet();
        assertThat(cache.get("k", loader)).isEqualTo(1001);
        assertThat(cache.get("j", loader)).isEqualTo(1002);
        cache.invalidate("k");
        assertThat(cache.get("k", loader)).isEqualTo(1003);
        // only k was forgotten: j is still cached
        assertThat(cache.get("j", loader)).isEqualTo(1002);
        assertThat(calls.get()).isEqualTo(3);
    }

    @Test
    void loadsOfDifferentKeysDoNotWaitForEachOther() throws Exception {
        LoadingCache<String, String> cache = new LoadingCache<>();
        CountDownLatch aEntered = new CountDownLatch(1);
        CountDownLatch bLoaded = new CountDownLatch(1);
        AtomicReference<String> first = new AtomicReference<>();
        Thread a = new Thread(() -> first.set(cache.get("a", key -> {
            aEntered.countDown();
            try {
                // the load of "a" can only finish once the load of "b" has run alongside it
                return bLoaded.await(10, TimeUnit.SECONDS) ? "A" : "b was never loaded alongside a";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "interrupted";
            }
        })));
        a.start();
        awaitQuietly(aEntered);
        String second = cache.get("b", key -> {
            bLoaded.countDown();
            return "B";
        });
        a.join();
        assertThat(second).isEqualTo("B");
        assertThat(first.get()).isEqualTo("A");
    }
}
