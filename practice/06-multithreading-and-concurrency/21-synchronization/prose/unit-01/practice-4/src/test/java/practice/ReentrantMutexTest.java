package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ReentrantMutexTest {

    private interface Body {
        void run() throws Exception;
    }

    /** Runs the body on a daemon thread and waits for it, at most 5 s; returns what it threw, or null. */
    private static Throwable onAnotherThread(Body body) throws InterruptedException {
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        AtomicBoolean finished = new AtomicBoolean();
        Thread thread = new Thread(() -> {
            try {
                body.run();
            } catch (Throwable t) {
                thrown.set(t);
            }
            finished.set(true);
        });
        thread.setDaemon(true);
        thread.start();
        thread.join(5_000);
        assertThat(finished.get()).as("the other thread finished within 5 s").isTrue();
        return thrown.get();
    }

    /** Whether the thread is inside Object.wait, called from ReentrantMutex.lock. */
    private static boolean waitsInLock(Thread thread) {
        boolean waiting = false;
        boolean inLock = false;
        for (StackTraceElement frame : thread.getStackTrace()) {
            waiting |= frame.getClassName().equals("java.lang.Object") && frame.getMethodName().startsWith("wait");
            inLock |= frame.getClassName().equals(ReentrantMutex.class.getName()) && frame.getMethodName().equals("lock");
        }
        return waiting && inLock;
    }

    @Test
    void locksAndUnlocksOnOneThread() throws InterruptedException {
        ReentrantMutex mutex = new ReentrantMutex();
        assertThat(mutex.holdCount()).isZero();
        mutex.lock();
        assertThat(mutex.holdCount()).isEqualTo(1);
        assertThat(mutex.isHeldByCurrentThread()).isTrue();
        mutex.unlock();
        assertThat(mutex.holdCount()).isZero();
        assertThat(mutex.isHeldByCurrentThread()).isFalse();
        assertThat(mutex.tryLock()).isTrue();
        mutex.unlock();
    }

    @Test
    void theOwnerMayLockAgain() throws InterruptedException {
        ReentrantMutex mutex = new ReentrantMutex();
        AtomicReference<Integer> count = new AtomicReference<>();
        Throwable thrown = onAnotherThread(() -> {
            mutex.lock();
            mutex.lock();
            count.set(mutex.holdCount());
            mutex.unlock();
            mutex.unlock();
        });
        assertThat(thrown).isNull();
        assertThat(count.get()).isEqualTo(2);
    }

    @Test
    void releasedOnlyWhenTheCountReachesZero() throws InterruptedException {
        ReentrantMutex mutex = new ReentrantMutex();
        mutex.lock();
        mutex.lock();
        mutex.unlock();
        AtomicBoolean took = new AtomicBoolean(true);
        assertThat(onAnotherThread(() -> took.set(mutex.tryLock()))).isNull();
        assertThat(took.get()).as("another thread took a mutex still held once").isFalse();
        mutex.unlock();
        assertThat(onAnotherThread(() -> {
            took.set(mutex.tryLock());
            if (took.get()) {
                mutex.unlock();
            }
        })).isNull();
        assertThat(took.get()).as("the mutex is free after the second unlock").isTrue();
    }

    @Test
    void onlyTheOwnerMayUnlock() throws InterruptedException {
        ReentrantMutex mutex = new ReentrantMutex();
        mutex.lock();
        Throwable thrown = onAnotherThread(mutex::unlock);
        assertThat(thrown).isInstanceOf(IllegalMonitorStateException.class);
        assertThat(mutex.holdCount()).isEqualTo(1);
        mutex.unlock();
        Throwable onFree = null;
        try {
            mutex.unlock();
        } catch (Throwable t) {
            onFree = t;
        }
        assertThat(onFree).isInstanceOf(IllegalMonitorStateException.class);
    }

    @Test
    void aWaiterWakesOnRelease() throws InterruptedException {
        ReentrantMutex mutex = new ReentrantMutex();
        mutex.lock();
        AtomicBoolean got = new AtomicBoolean();
        Thread waiter = new Thread(() -> {
            try {
                mutex.lock();
                got.set(true);
                mutex.unlock();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        waiter.setDaemon(true);
        waiter.start();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!waitsInLock(waiter) && System.nanoTime() < deadline) {
            Thread.sleep(1);
        }
        assertThat(waitsInLock(waiter)).as("the waiter waits in lock() while the mutex is held").isTrue();
        assertThat(got.get()).isFalse();
        mutex.unlock();
        waiter.join(5_000);
        assertThat(got.get()).as("the waiter got the mutex after it was released").isTrue();
    }
}
