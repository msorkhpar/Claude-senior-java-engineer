package practice;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class LazyTest {

    @Test
    void createsTheValueOnceAndKeepsIt() {
        AtomicInteger calls = new AtomicInteger();
        Lazy<Object> lazy = new Lazy<>(() -> {
            calls.incrementAndGet();
            return new Object();
        });
        Object first = lazy.get();
        Object second = lazy.get();
        assertThat(first).isNotNull();
        assertThat(second).isSameAs(first);
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void nothingIsCreatedBeforeTheFirstGet() {
        AtomicInteger calls = new AtomicInteger();
        Lazy<String> lazy = new Lazy<>(() -> {
            calls.incrementAndGet();
            return "settings";
        });
        assertThat(calls.get()).isZero();
        assertThat(lazy.get()).isEqualTo("settings");
        assertThat(calls.get()).isEqualTo(1);
    }

    /**
     * A forced interleaving, not a race: thread A is held inside the supplier until
     * thread B has either entered the supplier too (a second creation) or is parked
     * waiting for A (the value is guarded). Only then is A let go.
     */
    @Test
    void concurrentCallersShareOneValue() {
        assertTimeoutPreemptively(Duration.ofSeconds(20), () -> {
            AtomicInteger calls = new AtomicInteger();
            CountDownLatch firstInside = new CountDownLatch(1);
            CountDownLatch release = new CountDownLatch(1);
            Lazy<Object> lazy = new Lazy<>(() -> {
                if (calls.incrementAndGet() == 1) {
                    firstInside.countDown();
                    try {
                        release.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                return new Object();
            });
            AtomicReference<Object> seenByA = new AtomicReference<>();
            AtomicReference<Object> seenByB = new AtomicReference<>();
            Thread a = new Thread(() -> seenByA.set(lazy.get()), "caller-a");
            Thread b = new Thread(() -> seenByB.set(lazy.get()), "caller-b");
            a.start();
            assertThat(firstInside.await(10, TimeUnit.SECONDS)).as("the first caller reached the supplier").isTrue();
            b.start();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            int parkedReadings = 0;
            while (System.nanoTime() < deadline && calls.get() < 2 && b.isAlive() && parkedReadings < 5) {
                Thread.State state = b.getState();
                boolean parked = state == Thread.State.BLOCKED
                        || state == Thread.State.WAITING
                        || state == Thread.State.TIMED_WAITING;
                parkedReadings = parked ? parkedReadings + 1 : 0;
                Thread.sleep(20);
            }
            release.countDown();
            a.join(10_000);
            b.join(10_000);
            assertThat(calls.get()).as("times the supplier ran").isEqualTo(1);
            assertThat(seenByA.get()).isNotNull();
            assertThat(seenByB.get()).isSameAs(seenByA.get());
        });
    }
}
