package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class NameRegistryTest {

    @Test
    void registersEachNameOnce() {
        NameRegistry registry = new NameRegistry(Collections.synchronizedSet(new HashSet<>()));
        assertThat(registry.register("ada")).isTrue();
        assertThat(registry.register("ada")).isFalse();
        assertThat(registry.isRegistered("ada")).isTrue();
        assertThat(registry.isRegistered("bob")).isFalse();
        assertThat(registry.register("bob")).isTrue();
    }

    @Test
    void equalNamesAreOneName() {
        NameRegistry registry = new NameRegistry(Collections.synchronizedSet(new HashSet<>()));
        assertThat(registry.register(new String("ada"))).isTrue();
        String again = new StringBuilder("ad").append('a').toString();
        assertThat(registry.register(again)).as("registering an equal name again").isFalse();
        assertThat(registry.isRegistered(String.valueOf(new char[] {'a', 'd', 'a'}))).isTrue();
    }

    @Test
    void racingRegistrationsHaveOneWinner() throws InterruptedException {
        GatedSet names = new GatedSet();
        NameRegistry registry = new NameRegistry(names);
        boolean[] won = new boolean[2];
        Thread first = daemon(() -> won[0] = registry.register("eve"));
        assertThat(names.firstAddEntered.await(5, TimeUnit.SECONDS)).as("the first registration reached add").isTrue();
        // The first add is held open before it inserts. The second registration now either waits,
        // or reaches add itself, or finishes; then the first add is let through.
        Thread second = daemon(() -> won[1] = registry.register("eve"));
        waitFor(() -> names.adds.get() >= 2 || blockedBy(second, first) || !second.isAlive(), "the second registration");
        names.release.countDown();
        first.join(5_000);
        second.join(5_000);
        assertThat(first.isAlive() || second.isAlive()).as("both registrations finished").isFalse();
        assertThat(won[0] ^ won[1]).as("exactly one of the two registrations won: " + won[0] + ", " + won[1]).isTrue();
        assertThat(registry.isRegistered("eve")).isTrue();
    }

    /** A set that is thread-safe call by call, whose first add is held open before it inserts. */
    static final class GatedSet extends AbstractSet<String> {
        private final Set<String> inner = new HashSet<>();
        final AtomicInteger adds = new AtomicInteger();
        final CountDownLatch firstAddEntered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);

        @Override
        public boolean add(String value) {
            if (adds.incrementAndGet() == 1) {
                firstAddEntered.countDown();
                try {
                    release.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            synchronized (inner) {
                return inner.add(value);
            }
        }

        @Override
        public boolean contains(Object value) {
            synchronized (inner) {
                return inner.contains(value);
            }
        }

        @Override
        public boolean remove(Object value) {
            synchronized (inner) {
                return inner.remove(value);
            }
        }

        @Override
        public Iterator<String> iterator() {
            synchronized (inner) {
                return new ArrayList<>(inner).iterator();
            }
        }

        @Override
        public int size() {
            synchronized (inner) {
                return inner.size();
            }
        }
    }

    /** True only when the thread waits for a lock that the given holder thread owns. */
    private static boolean blockedBy(Thread waiter, Thread holder) {
        java.lang.management.ThreadInfo info =
                java.lang.management.ManagementFactory.getThreadMXBean().getThreadInfo(waiter.threadId());
        return info != null && info.getLockOwnerId() == holder.threadId();
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
