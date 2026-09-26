package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ExclusiveTest {

    @Test
    void runsActionHoldingTheLock() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        AtomicBoolean heldInside = new AtomicBoolean();
        new Exclusive(lock).run(() -> heldInside.set(lock.isHeldByCurrentThread()));
        assertThat(heldInside).isTrue();
        assertThat(lock.isLocked()).isFalse();
    }

    @Test
    void waitingCallerCanBeInterrupted() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        Exclusive exclusive = new Exclusive(lock);
        CountDownLatch held = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread holder = new Thread(() -> {
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
        holder.setDaemon(true);
        holder.start();
        assertThat(held.await(3, TimeUnit.SECONDS)).isTrue();

        AtomicBoolean ran = new AtomicBoolean();
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread caller = new Thread(() -> {
            try {
                exclusive.run(() -> ran.set(true));
            } catch (Throwable t) {
                thrown.set(t);
            }
        });
        caller.setDaemon(true);
        caller.start();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (!lock.hasQueuedThread(caller) && caller.isAlive() && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertThat(lock.hasQueuedThread(caller)).as("the caller waits for the lock").isTrue();
        caller.interrupt();
        caller.join(3_000);
        boolean left = !caller.isAlive();
        release.countDown();
        caller.join(3_000);
        holder.join(3_000);
        assertThat(left).as("the interrupted caller stopped waiting").isTrue();
        assertThat(thrown.get()).isInstanceOf(InterruptedException.class);
        assertThat(ran).as("the action did not run").isFalse();
    }

    @Test
    void releasesWhenActionThrows() {
        ReentrantLock lock = new ReentrantLock();
        IllegalStateException boom = new IllegalStateException("boom");
        assertThatThrownBy(() -> new Exclusive(lock).run(() -> {
            throw boom;
        })).isSameAs(boom);
        assertThat(lock.isLocked()).as("the lock was released").isFalse();
        StackOverflowError deep = new StackOverflowError("deep");
        assertThatThrownBy(() -> new Exclusive(lock).run(() -> {
            throw deep;
        })).isSameAs(deep);
        assertThat(lock.isLocked()).as("an Error releases the lock too").isFalse();
    }
}
