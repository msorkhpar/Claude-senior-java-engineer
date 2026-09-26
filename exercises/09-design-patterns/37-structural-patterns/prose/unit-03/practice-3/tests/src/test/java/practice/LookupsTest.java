package practice;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class LookupsTest {

    /** The real service: counts how often it is asked, and builds each answer fresh. */
    static final class CountingService implements Lookups.DataLookupService {
        final AtomicInteger asked = new AtomicInteger();

        @Override
        public String lookup(String key) {
            asked.incrementAndGet();
            return new StringBuilder("Result for: ").append(key).toString();
        }
    }

    @Test
    void repeatsComeFromTheCache() {
        CountingService real = new CountingService();
        Lookups.CachingProxy proxy = new Lookups.CachingProxy(real);

        String first = proxy.lookup("k1");
        String second = proxy.lookup("k1");

        assertThat(first).isEqualTo("Result for: k1");
        assertThat(second).isSameAs(first);
        assertThat(real.asked).hasValue(1);
        assertThat(proxy.isCached("k1")).isTrue();
        assertThat(proxy.cacheSize()).isEqualTo(1);

        proxy.clearCache();
        assertThat(proxy.isCached("k1")).isFalse();
        assertThat(proxy.lookup("k1")).isEqualTo("Result for: k1");
        assertThat(real.asked).hasValue(2);
    }

    @Test
    void equalKeysShareOneEntry() {
        CountingService real = new CountingService();
        Lookups.CachingProxy proxy = new Lookups.CachingProxy(real);
        String key = new StringBuilder("order-").append(1234).toString();
        String sameText = new StringBuilder("order-").append(1234).toString();

        proxy.lookup(key);
        proxy.lookup(sameText);

        assertThat(real.asked).hasValue(1);
        assertThat(proxy.isCached(new String(key))).isTrue();
        assertThat(proxy.cacheSize()).isEqualTo(1);
    }

    @Test
    void everyKeyKeepsItsOwnEntry() {
        CountingService real = new CountingService();
        Lookups.CachingProxy proxy = new Lookups.CachingProxy(real);

        assertThat(proxy.lookup("a")).isEqualTo("Result for: a");
        assertThat(proxy.lookup("b")).isEqualTo("Result for: b");
        assertThat(proxy.lookup("a")).isEqualTo("Result for: a");

        assertThat(real.asked).hasValue(2);
        assertThat(proxy.cacheSize()).isEqualTo(2);
    }

    /**
     * Thread A is held inside the real service for "k". Thread B then looks up "k": a correct proxy makes
     * B wait for A's result; a broken one lets B ask the real service itself. Only then is A released.
     */
    @Test
    void racingLookupsOfOneKeyAskOnce() throws Exception {
        CountDownLatch firstInside = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger asked = new AtomicInteger();
        Lookups.CachingProxy proxy = new Lookups.CachingProxy(key -> {
            if (asked.incrementAndGet() == 1) {
                firstInside.countDown();
                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return "Result for: " + key;
        });
        AtomicReference<String> seenByA = new AtomicReference<>();
        AtomicReference<String> seenByB = new AtomicReference<>();

        Thread a = new Thread(() -> seenByA.set(proxy.lookup("k")));
        a.start();
        firstInside.await();
        Thread b = new Thread(() -> seenByB.set(proxy.lookup("k")));
        b.start();
        while (asked.get() < 2 && !waitsForALock(b) && b.isAlive()) {
            Thread.onSpinWait();
        }
        release.countDown();
        a.join();
        b.join();

        assertThat(asked).hasValue(1);
        assertThat(seenByA.get()).isEqualTo("Result for: k");
        assertThat(seenByB.get()).isEqualTo("Result for: k");
    }

    private static boolean waitsForALock(Thread t) {
        Thread.State s = t.getState();
        return s == Thread.State.BLOCKED || s == Thread.State.WAITING || s == Thread.State.TIMED_WAITING;
    }
}
