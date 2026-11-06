package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 30, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class WarehouseTest {

    static boolean blockedBy(Thread waiter, Thread holder) {
        ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(waiter.threadId());
        return info != null && info.getLockOwnerId() == holder.threadId();
    }

    /**
     * Lines two operations up. Each one, once it holds its first bin, waits (at most 5 s) until the
     * other one also holds a first bin, is waiting for the bin this one holds, or has finished.
     * Two operations that take their bins in one order can't both hold a first bin, so the second
     * waits for the first; two that take them in different orders both hold one and go on.
     */
    static final class LineUp implements Warehouse.Probe {
        final AtomicReferenceArray<Thread> threads = new AtomicReferenceArray<>(2);
        final Map<Thread, Boolean> holding = new ConcurrentHashMap<>();

        @Override
        public void holdingFirst(Warehouse.Bin first) {
            Thread me = Thread.currentThread();
            holding.put(me, true);
            Thread other = threads.get(0) == me ? threads.get(1) : threads.get(0);
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (System.nanoTime() < deadline) {
                if (holding.containsKey(other) || other.getState() == Thread.State.TERMINATED || blockedBy(other, me)) {
                    return;
                }
                Thread.onSpinWait();
            }
        }

        /** Runs both at once and reports whether both finished within 10 s. */
        boolean bothFinish(Runnable first, Runnable second) throws InterruptedException {
            Thread a = new Thread(first);
            Thread b = new Thread(second);
            a.setDaemon(true);                       // a deadlocked pair must not keep the JVM alive
            b.setDaemon(true);
            threads.set(0, a);
            threads.set(1, b);
            a.start();
            b.start();
            a.join(10_000);
            b.join(10_000);
            return !a.isAlive() && !b.isAlive();
        }
    }

    @Test
    void movesStockAndRefusesAShortBin() {
        Warehouse warehouse = new Warehouse();
        Warehouse.Bin shelf = new Warehouse.Bin("C-02-1", 40);
        Warehouse.Bin pallet = new Warehouse.Bin("B-11-4", 5);

        assertThat(warehouse.move(shelf, pallet, 15)).isTrue();
        assertThat(warehouse.move(pallet, shelf, 21)).isFalse();

        assertThat(shelf.quantity()).isEqualTo(25);
        assertThat(pallet.quantity()).isEqualTo(20);
    }

    @Test
    void countsTwoBinsTogether() {
        Warehouse warehouse = new Warehouse();
        Warehouse.Bin left = new Warehouse.Bin("D-07-3", 12);
        Warehouse.Bin right = new Warehouse.Bin("A-01-1", 30);

        assertThat(warehouse.count(left, right)).isEqualTo(42);
        assertThat(warehouse.count(right, left)).isEqualTo(42);
    }

    @Test
    void movesBothWaysBetweenTwoBinsFinish() throws InterruptedException {
        LineUp lineUp = new LineUp();
        Warehouse warehouse = new Warehouse(lineUp);
        Warehouse.Bin east = new Warehouse.Bin("E-03-2", 70);
        Warehouse.Bin west = new Warehouse.Bin("W-09-1", 50);
        AtomicInteger moved = new AtomicInteger();

        boolean finished = lineUp.bothFinish(
                () -> { if (warehouse.move(east, west, 8)) moved.incrementAndGet(); },
                () -> { if (warehouse.move(west, east, 13)) moved.incrementAndGet(); });

        assertThat(finished).as("the two moves are deadlocked").isTrue();
        assertThat(moved.get()).isEqualTo(2);
        assertThat(east.quantity()).isEqualTo(75);
        assertThat(west.quantity()).isEqualTo(45);
    }

    @Test
    void aCountDuringAMoveBetweenTheSameBinsFinishes() throws InterruptedException {
        LineUp lineUp = new LineUp();
        Warehouse warehouse = new Warehouse(lineUp);
        Warehouse.Bin low = new Warehouse.Bin("G-01-1", 9);
        Warehouse.Bin high = new Warehouse.Bin("H-05-2", 16);
        AtomicInteger total = new AtomicInteger(-1);

        boolean finished = lineUp.bothFinish(
                () -> warehouse.move(low, high, 4),
                () -> total.set(warehouse.count(high, low)));

        assertThat(finished).as("the move and the count are deadlocked").isTrue();
        assertThat(total.get()).isEqualTo(25);
        assertThat(high.quantity()).isEqualTo(20);
    }
}
