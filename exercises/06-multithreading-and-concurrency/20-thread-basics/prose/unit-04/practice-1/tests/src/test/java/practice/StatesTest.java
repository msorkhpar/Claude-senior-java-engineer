package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class StatesTest {

    private final Object monitor = new Object();
    private final ReentrantLock lock = new ReentrantLock();
    private final CountDownLatch done = new CountDownLatch(1);

    /** The state the thread reaches within 5 s, if it reaches {@code wanted}; otherwise the state it is in then. */
    private static Thread.State awaitState(Thread thread, Thread.State wanted) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (thread.getState() != wanted && System.nanoTime() < deadline) {
            Thread.sleep(1);
        }
        return thread.getState();
    }

    private static final ThreadMXBean MX = ManagementFactory.getThreadMXBean();

    /**
     * Whether, within 5 s, the JVM reports the thread in {@code state} while parked on a lock whose
     * class starts with {@code lockClass} and, when {@code owner} is not null, is held by {@code owner}.
     * A brief block on some JVM-internal lock never matches, so a thread parked elsewhere cannot pass.
     */
    private static boolean awaitParkedOn(Thread thread, Thread.State state, String lockClass, Thread owner)
            throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            ThreadInfo info = MX.getThreadInfo(thread.threadId());
            if (info != null && info.getThreadState() == state && info.getLockInfo() != null
                    && info.getLockInfo().getClassName().startsWith(lockClass)
                    && (owner == null || info.getLockOwnerId() == owner.threadId())) {
                return true;
            }
            Thread.sleep(1);
        }
        return false;
    }

    private void letGoAndJoin(Thread thread) throws InterruptedException {
        done.countDown();
        thread.join(5_000);
        assertThat(thread.isAlive()).as("the thread finished once let go").isFalse();
    }

    @Test
    void newRunnableAndTerminated() throws InterruptedException {
        Thread fresh = States.enter(Thread.State.NEW, monitor, lock, done);
        assertThat(fresh.getState()).isEqualTo(Thread.State.NEW);
        assertThat(fresh.isDaemon()).isTrue();

        Thread working = States.enter(Thread.State.RUNNABLE, monitor, lock, done);
        assertThat(awaitState(working, Thread.State.RUNNABLE)).isEqualTo(Thread.State.RUNNABLE);
        letGoAndJoin(working);

        Thread finished = States.enter(Thread.State.TERMINATED, monitor, lock, new CountDownLatch(1));
        assertThat(finished.getState()).isEqualTo(Thread.State.TERMINATED);
    }

    @Test
    void blockedMeansWaitingForAMonitor() throws InterruptedException {
        lock.lock();
        Thread thread;
        boolean seen;
        synchronized (monitor) {
            thread = States.enter(Thread.State.BLOCKED, monitor, lock, done);
            seen = awaitParkedOn(thread, Thread.State.BLOCKED, "java.lang.Object", Thread.currentThread());
        }
        lock.unlock();
        letGoAndJoin(thread);
        assertThat(seen).as("BLOCKED on the monitor the test holds").isTrue();
    }

    @Test
    void waitingForAReentrantLockIsWaiting() throws InterruptedException {
        Thread thread;
        boolean seen;
        synchronized (monitor) {
            lock.lock();
            try {
                thread = States.enter(Thread.State.WAITING, monitor, lock, done);
                seen = awaitParkedOn(thread, Thread.State.WAITING, "java.util.concurrent.locks.ReentrantLock",
                        Thread.currentThread());
            } finally {
                lock.unlock();
            }
        }
        letGoAndJoin(thread);
        assertThat(seen).as("WAITING on the ReentrantLock the test holds").isTrue();
    }

    @Test
    void aWaitWithATimeoutIsTimedWaiting() throws InterruptedException {
        Thread thread = States.enter(Thread.State.TIMED_WAITING, monitor, lock, done);
        boolean seen = awaitParkedOn(thread, Thread.State.TIMED_WAITING, "java.util.concurrent.CountDownLatch", null);
        letGoAndJoin(thread);
        assertThat(seen).as("TIMED_WAITING on the latch").isTrue();
    }
}
