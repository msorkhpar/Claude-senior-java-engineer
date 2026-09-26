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
class CatalogTest {

    private static final Runnable NOTHING = () -> { };

    private static Thread start(Runnable body) {
        Thread t = new Thread(body);
        t.setDaemon(true);
        t.start();
        return t;
    }

    /** Waits until {@code t} is parked or finished, at most 3 s, and returns that state. */
    private static Thread.State settle(Thread t) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (System.nanoTime() < deadline) {
            Thread.State s = t.getState();
            if (s == Thread.State.WAITING || s == Thread.State.TIMED_WAITING || s == Thread.State.TERMINATED) {
                return s;
            }
            Thread.onSpinWait();
        }
        return t.getState();
    }

    @Test
    void publishThenLookup() {
        Catalog catalog = new Catalog();
        catalog.publish("k", "v", NOTHING);
        assertThat(catalog.lookup("k", NOTHING)).isEqualTo("v");
        assertThat(catalog.lookup("gone", NOTHING)).isNull();
    }

    /** The first reader, while it holds the read lock, waits (at most 3 s) for a second reader to get inside. */
    @Test
    void readersShareTheLock() throws InterruptedException {
        Catalog catalog = new Catalog();
        catalog.publish("k", "v", NOTHING);
        CountDownLatch firstInside = new CountDownLatch(1);
        CountDownLatch secondInside = new CountDownLatch(1);
        AtomicBoolean together = new AtomicBoolean();
        Thread first = start(() -> catalog.lookup("k", () -> {
            firstInside.countDown();
            try {
                together.set(secondInside.await(3, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
        assertThat(firstInside.await(3, TimeUnit.SECONDS)).isTrue();
        Thread second = start(() -> catalog.lookup("k", secondInside::countDown));
        first.join(5_000);
        second.join(5_000);
        assertThat(together).as("both readers held the read lock at once").isTrue();
    }

    /** Inside the publish hook, a reader starts; the hook waits (at most 3 s) until it is parked or finished. */
    @Test
    void writerExcludesReaders() throws InterruptedException {
        Catalog catalog = new Catalog();
        catalog.publish("k", "old", NOTHING);
        AtomicReference<String> read = new AtomicReference<>();
        AtomicReference<Thread> reader = new AtomicReference<>();
        AtomicReference<Thread.State> seen = new AtomicReference<>();
        catalog.publish("k", "new", () -> {
            Thread t = start(() -> read.set(catalog.lookup("k", NOTHING)));
            reader.set(t);
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
            while (System.nanoTime() < deadline) {
                Thread.State s = t.getState();
                if (s == Thread.State.WAITING || s == Thread.State.TERMINATED) {
                    break;
                }
                Thread.onSpinWait();
            }
            seen.set(t.getState());
        });
        reader.get().join(3_000);
        assertThat(seen.get()).as("the reader waited for the writer").isEqualTo(Thread.State.WAITING);
        assertThat(read.get()).isEqualTo("new");
    }

    @Test
    void missLoadsWithoutUpgrading() throws InterruptedException {
        Catalog catalog = new Catalog();
        AtomicInteger loads = new AtomicInteger();
        AtomicReference<String> first = new AtomicReference<>();
        AtomicReference<String> second = new AtomicReference<>();
        Thread caller = start(() -> {
            first.set(catalog.lookupOrLoad("k", key -> {
                loads.incrementAndGet();
                return key.toUpperCase();
            }));
            second.set(catalog.lookupOrLoad("k", key -> {
                loads.incrementAndGet();
                return "again";
            }));
        });
        caller.join(3_000);
        assertThat(caller.isAlive()).as("lookupOrLoad deadlocked on its own read lock").isFalse();
        assertThat(first.get()).isEqualTo("K");
        assertThat(second.get()).isEqualTo("K");
        assertThat(loads).hasValue(1);
    }

    @Test
    void theWriteHookRunsAfterStoring() throws Exception {
        Catalog catalog = new Catalog();
        AtomicReference<String> seen = new AtomicReference<>("unset");
        catalog.publish("k", "v", () -> seen.set(catalog.lookup("k", NOTHING)));
        assertThat(seen.get()).as("the hook runs after the value is stored").isEqualTo("v");
    }

    @Test
    void readersKeepWritersOut() throws Exception {
        Catalog catalog = new Catalog();
        catalog.publish("k", "old", NOTHING);
        AtomicReference<Thread.State> writer = new AtomicReference<>();
        AtomicReference<Thread> publisher = new AtomicReference<>();
        String read = catalog.lookup("k", () -> {
            Thread t = start(() -> catalog.publish("k", "new", NOTHING));
            publisher.set(t);
            writer.set(settle(t));
        });
        publisher.get().join(3_000);
        assertThat(writer.get()).as("a writer waits while whileReading runs under the read lock").isEqualTo(Thread.State.WAITING);
        assertThat(read).isEqualTo("old");
        assertThat(catalog.lookup("k", NOTHING)).isEqualTo("new");
    }

    @Test
    void theLoaderRunsUnderTheWriteLock() throws Exception {
        Catalog catalog = new Catalog();
        AtomicReference<Thread.State> reader = new AtomicReference<>();
        AtomicReference<Thread> other = new AtomicReference<>();
        String loaded = catalog.lookupOrLoad("k", key -> {
            Thread t = start(() -> catalog.lookup("k", NOTHING));
            other.set(t);
            reader.set(settle(t));
            return key.toUpperCase();
        });
        other.get().join(3_000);
        assertThat(loaded).isEqualTo("K");
        assertThat(reader.get()).as("the loader runs while lookupOrLoad holds the write lock, so a reader waits")
                .isEqualTo(Thread.State.WAITING);
    }
}
