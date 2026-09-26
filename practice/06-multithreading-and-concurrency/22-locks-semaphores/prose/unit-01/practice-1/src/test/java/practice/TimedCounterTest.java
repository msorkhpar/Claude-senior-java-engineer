package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class TimedCounterTest {

    /** A thread that holds {@code lock} until {@link #release()} is called. */
    private static final class Holder {
        private final CountDownLatch held = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);
        private final Thread thread;

        Holder(ReentrantLock lock) throws InterruptedException {
            thread = new Thread(() -> {
                lock.lock();
                try {
                    held.countDown();
                    release.await(8, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            });
            thread.setDaemon(true);
            thread.start();
            assertThat(held.await(3, TimeUnit.SECONDS)).as("the holder took the lock").isTrue();
        }

        void release() throws InterruptedException {
            release.countDown();
            thread.join(3_000);
        }
    }

    private static Thread start(Runnable body) {
        Thread t = new Thread(body);
        t.setDaemon(true);
        t.start();
        return t;
    }

    private static boolean waitFor(BooleanSupplier condition) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            Thread.onSpinWait();
        }
        return condition.getAsBoolean();
    }

    @Test
    void incrementsWhenFree() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        TimedCounter counter = new TimedCounter(lock);
        assertThat(counter.tryIncrement()).isTrue();
        assertThat(counter.increment(1, TimeUnit.SECONDS)).isTrue();
        assertThat(counter.count()).isEqualTo(2);
        assertThat(lock.isLocked()).as("every call released the lock").isFalse();
    }

    @Test
    void tryIncrementRefusesWhileHeld() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        TimedCounter counter = new TimedCounter(lock);
        Holder holder = new Holder(lock);
        AtomicReference<Object> result = new AtomicReference<>();
        Thread caller = start(() -> {
            try {
                result.set(counter.tryIncrement());
            } catch (RuntimeException e) {
                result.set(e);
            }
        });
        caller.join(3_000);
        assertThat(caller.isAlive()).as("tryIncrement does not wait").isFalse();
        holder.release();
        assertThat(result.get()).isEqualTo(false);
        assertThat(counter.count()).isZero();
    }

    @Test
    void timedIncrementGivesUp() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        TimedCounter counter = new TimedCounter(lock);
        Holder holder = new Holder(lock);
        AtomicReference<Object> result = new AtomicReference<>();
        Thread caller = start(() -> {
            try {
                result.set(counter.increment(100_000, TimeUnit.MICROSECONDS));
            } catch (Exception e) {
                result.set(e);
            }
        });
        caller.join(4_000);
        boolean gaveUp = !caller.isAlive();
        Object answer = result.get();
        holder.release();
        caller.join(3_000);
        assertThat(gaveUp).as("the timed increment gave up while the lock was still held").isTrue();
        assertThat(answer).isEqualTo(false);
        assertThat(counter.count()).isZero();
    }

    @Test
    void timedIncrementWaitsForRelease() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        TimedCounter counter = new TimedCounter(lock);
        Holder holder = new Holder(lock);
        AtomicReference<Object> result = new AtomicReference<>();
        Thread caller = start(() -> {
            try {
                result.set(counter.increment(8, TimeUnit.SECONDS));
            } catch (Exception e) {
                result.set(e);
            }
        });
        boolean queued = waitFor(() -> lock.hasQueuedThread(caller) || !caller.isAlive());
        holder.release();
        caller.join(3_000);
        assertThat(queued).isTrue();
        assertThat(result.get()).as("the increment waited for the release").isEqualTo(true);
        assertThat(counter.count()).isEqualTo(1);
    }

    @Test
    void contendedMeansSomeoneWaits() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        TimedCounter counter = new TimedCounter(lock);
        Holder holder = new Holder(lock);
        boolean heldAlone = counter.isContended();
        Thread waiter = start(() -> {
            lock.lock();
            lock.unlock();
        });
        boolean queued = waitFor(() -> lock.hasQueuedThread(waiter));
        boolean withWaiter = counter.isContended();
        holder.release();
        waiter.join(3_000);
        assertThat(heldAlone).as("held, but nobody waits").isFalse();
        assertThat(queued).isTrue();
        assertThat(withWaiter).as("a thread waits for the lock").isTrue();
    }
}
