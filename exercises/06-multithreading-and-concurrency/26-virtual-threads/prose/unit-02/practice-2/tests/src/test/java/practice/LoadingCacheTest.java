package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class LoadingCacheTest {

    private static final Set<Thread.State> WAITING_SOMEHOW =
            EnumSet.of(Thread.State.WAITING, Thread.State.TIMED_WAITING, Thread.State.BLOCKED);

    /** Polls until the condition holds or 5 s pass; says whether it held. */
    private static boolean eventually(BooleanSupplier condition) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) {
                return false;
            }
            LockSupport.parkNanos(1_000_000);
        }
        return true;
    }

    @Test
    void loadsOnceAndThenAnswersFromTheCache() {
        AtomicInteger loads = new AtomicInteger();
        LoadingCache cache = new LoadingCache(key -> {
            loads.incrementAndGet();
            return key + "-row";
        });
        assertThat(cache.get("a")).isEqualTo("a-row");
        assertThat(cache.get("a")).isEqualTo("a-row");
        assertThat(loads.get()).isEqualTo(1);
    }

    @Test
    void differentKeysLoadSeparately() {
        LoadingCache cache = new LoadingCache(key -> key.toUpperCase() + key.length());
        assertThat(cache.get("ab")).isEqualTo("AB2");
        assertThat(cache.get("xyz")).isEqualTo("XYZ3");
    }

    /**
     * A first caller enters the loader and blocks there until released. A second caller then asks for
     * the same key; the test waits until it is waiting somehow, records how, and releases the loader.
     */
    private static final class TwoCallers {
        final AtomicInteger loads = new AtomicInteger();
        final CountDownLatch loading = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final AtomicReference<String> first = new AtomicReference<>();
        final AtomicReference<String> second = new AtomicReference<>();
        Thread.State secondWaitedAs;

        void run() throws InterruptedException {
            LoadingCache cache = new LoadingCache(key -> {
                loads.incrementAndGet();
                loading.countDown();
                try {
                    release.await(8, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return new String("value of " + key);
            });
            Thread a = Thread.ofVirtual().start(() -> first.set(cache.get("slow")));
            Thread b = null;
            try {
                assertThat(loading.await(5, TimeUnit.SECONDS)).as("the first caller is loading").isTrue();
                Thread started = Thread.ofVirtual().start(() -> second.set(cache.get("slow")));
                b = started;
                assertThat(eventually(() -> WAITING_SOMEHOW.contains(started.getState())))
                        .as("the second caller waits (state %s)", started.getState()).isTrue();
                secondWaitedAs = started.getState();
            } finally {
                release.countDown();
            }
            a.join(5_000);
            b.join(5_000);
        }
    }

    @Test
    void aWaitingCallerParksOnTheLock() throws InterruptedException {
        TwoCallers run = new TwoCallers();
        run.run();
        assertThat(run.secondWaitedAs).as("how the second caller waited").isEqualTo(Thread.State.WAITING);
        assertThat(run.second.get()).isEqualTo("value of slow");
    }

    @Test
    void theLoaderRunsOnceForTwoCallers() throws InterruptedException {
        TwoCallers run = new TwoCallers();
        run.run();
        assertThat(run.first.get()).isEqualTo("value of slow");
        assertThat(run.second.get()).isEqualTo("value of slow");
        assertThat(run.loads.get()).as("loader calls").isEqualTo(1);
    }

    @Test
    void aFailedLoadLeavesTheCacheUsable() throws InterruptedException {
        AtomicInteger loads = new AtomicInteger();
        LoadingCache cache = new LoadingCache(key -> {
            if (loads.incrementAndGet() == 1) {
                throw new IllegalStateException("database down");
            }
            return key + "-row";
        });
        assertThatThrownBy(() -> cache.get("x")).isInstanceOf(IllegalStateException.class);
        AtomicReference<String> later = new AtomicReference<>();
        Thread other = Thread.ofVirtual().start(() -> later.set(cache.get("x")));
        other.join(3_000);
        assertThat(other.isAlive()).as("the next caller is still waiting for the lock").isFalse();
        assertThat(later.get()).isEqualTo("x-row");
        assertThat(loads.get()).isEqualTo(2);
    }
}
