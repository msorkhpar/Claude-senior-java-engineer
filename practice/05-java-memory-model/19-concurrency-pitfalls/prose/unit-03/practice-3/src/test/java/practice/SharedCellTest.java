package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class SharedCellTest {

    private static final ThreadMXBean THREADS = ManagementFactory.getThreadMXBean();

    /** Waits, at most 20 s, until t has finished or waits for a lock that owner holds; true if it waits on owner. */
    private static boolean waitsOn(Thread t, Thread owner) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while (System.nanoTime() < deadline) {
            if (t.getState() == Thread.State.TERMINATED) {
                return false;
            }
            ThreadInfo info = THREADS.getThreadInfo(t.threadId());
            if (info != null && info.getLockOwnerId() == owner.threadId()) {
                return true;
            }
            LockSupport.parkNanos(1_000_000);
        }
        return false;
    }

    private static Thread daemon(Runnable body) {
        Thread t = new Thread(body);
        t.setDaemon(true);
        t.start();
        return t;
    }

    /** Runs the call in another thread while this thread holds the cell's monitor. */
    private static void waitsForTheCell(SharedCell cell, Runnable call) throws InterruptedException {
        Thread other;
        synchronized (cell) {
            other = daemon(call);
            assertThat(waitsOn(other, Thread.currentThread())).as("the call waits for the cell's monitor").isTrue();
        }
        other.join(20_000);
        assertThat(other.isAlive()).isFalse();
    }

    @Test
    void keepsTheValue() throws InterruptedException {
        SharedCell cell = new SharedCell();
        assertThat(cell.get()).isZero();
        cell.set(5);
        assertThat(cell.get()).isEqualTo(5);
        assertThat(cell.addAndGet(3)).isEqualTo(8);
        assertThat(cell.addAndGet(-10)).isEqualTo(-2);
        synchronized (cell) {
            if (cell.get() == -2) {
                cell.set(1);
            }
        }
        assertThat(cell.get()).isEqualTo(1);

        Thread writer = daemon(() -> cell.set(42));
        writer.join(20_000);
        AtomicInteger seen = new AtomicInteger();
        Thread reader = daemon(() -> seen.set(cell.get()));
        reader.join(20_000);
        assertThat(seen.get()).isEqualTo(42);
    }

    @Test
    void getLocksTheCell() throws InterruptedException {
        SharedCell cell = new SharedCell();
        cell.set(7);
        AtomicInteger seen = new AtomicInteger();
        waitsForTheCell(cell, () -> seen.set(cell.get()));
        assertThat(seen.get()).isEqualTo(7);
    }

    @Test
    void setLocksTheCell() throws InterruptedException {
        SharedCell cell = new SharedCell();
        cell.set(1);
        waitsForTheCell(cell, () -> cell.set(9));
        assertThat(cell.get()).isEqualTo(9);
    }

    @Test
    void addAndGetLocksTheCell() throws InterruptedException {
        SharedCell cell = new SharedCell();
        cell.set(4);
        AtomicInteger seen = new AtomicInteger();
        waitsForTheCell(cell, () -> seen.set(cell.addAndGet(6)));
        assertThat(seen.get()).isEqualTo(10);
        assertThat(cell.get()).isEqualTo(10);
    }
}
