package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class BalanceTest {

    private static Thread daemon(Runnable body) {
        Thread thread = new Thread(body);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    /** Whether {@code waiter} is blocked on a lock that {@code holder} holds. */
    private static boolean blockedBy(Thread waiter, Thread holder) {
        ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(waiter.threadId());
        return info != null && info.getLockOwnerId() == holder.threadId();
    }

    /** Waits, at most 5 s, until {@code waiter} is blocked on a lock {@code holder} holds, or {@code inside} is true. */
    private static void awaitBlockedByOr(Thread waiter, Thread holder, AtomicBoolean inside) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!inside.get() && !blockedBy(waiter, holder) && System.nanoTime() < deadline) {
            Thread.sleep(1);
        }
    }

    @Test
    void appliesChangesInOrder() {
        Balance balance = new Balance(10);
        assertThat(balance.update(x -> x + 5)).isEqualTo(15);
        assertThat(balance.update(x -> x * 2)).isEqualTo(30);
        assertThat(balance.get()).isEqualTo(30);
    }

    @Test
    void oneUpdateAtATime() throws InterruptedException {
        Balance balance = new Balance(0);
        CountDownLatch firstInside = new CountDownLatch(1);
        CountDownLatch letFirstFinish = new CountDownLatch(1);
        AtomicBoolean secondInside = new AtomicBoolean();
        Thread first = daemon(() -> balance.update(x -> {
            firstInside.countDown();
            try {
                letFirstFinish.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return x + 1;
        }));
        assertThat(firstInside.await(5, TimeUnit.SECONDS)).as("the first update started").isTrue();
        Thread second = daemon(() -> balance.update(x -> {
            secondInside.set(true);
            return x + 1;
        }));
        awaitBlockedByOr(second, first, secondInside);
        boolean ranBeside = secondInside.get();
        letFirstFinish.countDown();
        first.join(5_000);
        second.join(5_000);
        assertThat(ranBeside).as("the second update ran while the first was still running").isFalse();
        assertThat(balance.get()).isEqualTo(2);
    }

    @Test
    void outsidersCannotStallIt() throws InterruptedException {
        Balance balance = new Balance(30);
        AtomicInteger seen = new AtomicInteger(-1);
        Thread updater;
        synchronized (balance) {
            updater = daemon(() -> seen.set(balance.update(x -> x + 1)));
            updater.join(5_000);
            assertThat(updater.isAlive()).as("update waited for an outsider's lock on the Balance").isFalse();
        }
        updater.join(5_000);
        assertThat(seen.get()).isEqualTo(31);
        assertThat(balance.get()).isEqualTo(31);
    }

    @Test
    void aFailedChangeReleasesTheLock() throws InterruptedException {
        Balance balance = new Balance(30);
        assertThatThrownBy(() -> balance.update(x -> {
            throw new IllegalStateException("no");
        })).isInstanceOf(IllegalStateException.class);
        AtomicInteger seen = new AtomicInteger(-1);
        Thread other = daemon(() -> seen.set(balance.update(x -> x + 1)));
        other.join(5_000);
        assertThat(other.isAlive()).as("the next update is stuck behind the failed one").isFalse();
        assertThat(seen.get()).isEqualTo(31);
    }
}
