package practice;

import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class WarehouseTest {

    /** Holds one chosen thread at a chosen call, until the test lets it go on. */
    static final class Hold {
        volatile Thread thread;
        final AtomicInteger countsLeft = new AtomicInteger(Integer.MAX_VALUE);
        final CountDownLatch held = new CountDownLatch(1);
        final CountDownLatch goOn = new CountDownLatch(1);

        void here() {
            if (Thread.currentThread() != thread) return;
            thread = null;
            held.countDown();
            await(goOn);
        }
    }

    /** A thread-safe bin whose add() and nth count() can hold a chosen thread before it takes the bin's lock. */
    static final class HoldingBin implements Warehouse.Bin {
        private int units;
        private final Hold onAdd;
        private final Hold onCount;

        HoldingBin(int units, Hold onAdd, Hold onCount) {
            this.units = units;
            this.onAdd = onAdd;
            this.onCount = onCount;
        }

        @Override
        public int count() {
            if (Thread.currentThread() == onCount.thread && onCount.countsLeft.decrementAndGet() == 0) onCount.here();
            synchronized (this) {
                return units;
            }
        }

        @Override
        public synchronized boolean take(int qty) {
            if (units < qty) return false;
            units -= qty;
            return true;
        }

        @Override
        public void add(int qty) {
            onAdd.here();
            synchronized (this) {
                units += qty;
            }
        }
    }

    interface Call {
        void run() throws Exception;
    }

    static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("latch not opened");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    static Thread thread(String name, Call call, AtomicReference<Throwable> failure) {
        Thread t = new Thread(() -> {
            try {
                call.run();
            } catch (Throwable e) {
                failure.compareAndSet(null, e);
            }
        }, name);
        t.setDaemon(true);
        return t;
    }

    /** Waits until second has finished, or waits for a lock first holds. */
    static void awaitDoneOrWaitingFor(Thread second, Thread first) {
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            if (!second.isAlive()) return;
            ThreadInfo info = mx.getThreadInfo(second.threadId());
            if (info != null && info.getLockOwnerId() == first.threadId()) return;
            LockSupport.parkNanos(200_000);
        }
        fail(second.getName() + " neither finished nor waited for " + first.getName());
    }

    static void interleave(Thread first, Hold hold, Thread second, AtomicReference<Throwable> failure)
            throws InterruptedException {
        hold.thread = first;
        first.start();
        assertThat(hold.held.await(10, TimeUnit.SECONDS)).as("first call reached the held point").isTrue();
        second.start();
        awaitDoneOrWaitingFor(second, first);
        hold.goOn.countDown();
        first.join(10_000);
        second.join(10_000);
        assertThat(first.isAlive()).isFalse();
        assertThat(second.isAlive()).isFalse();
        assertThat(failure.get()).isNull();
    }

    @Test
    void aMoveTakesFromOneBinAndAddsToTheOther() {
        Warehouse w = new Warehouse(Map.of("A-03", new Warehouse.Shelf(17), "C-11", new Warehouse.Shelf(4)));

        assertThat(w.move("A-03", "C-11", 6)).isTrue();

        assertThat(w.count("A-03")).isEqualTo(11);
        assertThat(w.count("C-11")).isEqualTo(10);
    }

    @Test
    void aMoveOfMoreThanTheSourceHoldsMovesNothing() {
        Warehouse w = new Warehouse(Map.of("B-07", new Warehouse.Shelf(3), "D-02", new Warehouse.Shelf(8)));

        assertThat(w.move("B-07", "D-02", 5)).isFalse();

        assertThat(w.count("B-07")).isEqualTo(3);
        assertThat(w.count("D-02")).isEqualTo(8);
    }

    @Test
    void theTotalAddsUpEveryBin() {
        Warehouse w = new Warehouse(Map.of("E-01", new Warehouse.Shelf(9), "E-02", new Warehouse.Shelf(0),
                "F-05", new Warehouse.Shelf(31)));

        assertThat(w.total()).isEqualTo(40);
        w.move("F-05", "E-02", 12);
        assertThat(w.total()).isEqualTo(40);
    }

    @Test
    void aTotalTakenDuringAMoveCountsTheMovedUnitsOnce() throws Exception {
        Hold onAdd = new Hold();
        Hold never = new Hold();
        Warehouse w = new Warehouse(Map.of("G-14", new HoldingBin(25, onAdd, never), "H-09", new HoldingBin(13, onAdd, never)));
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicInteger seen = new AtomicInteger(-1);
        Thread move = thread("move", () -> w.move("G-14", "H-09", 7), failure);
        Thread count = thread("count", () -> seen.set(w.total()), failure);

        interleave(move, onAdd, count, failure);

        assertThat(seen.get()).isEqualTo(38);
    }

    @Test
    void aMoveDuringATotalLeavesTheTotalUnchanged() throws Exception {
        Hold never = new Hold();
        Hold onCount = new Hold();
        onCount.countsLeft.set(2);                       // hold the count after it has read one of the two bins
        Warehouse w = new Warehouse(Map.of("J-21", new HoldingBin(19, never, onCount), "K-30", new HoldingBin(6, never, onCount)));
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicInteger seen = new AtomicInteger(-1);
        Thread count = thread("count", () -> seen.set(w.total()), failure);
        Thread move = thread("move", () -> w.move("J-21", "K-30", 4), failure);

        interleave(count, onCount, move, failure);

        assertThat(seen.get()).isEqualTo(25);
    }
}
