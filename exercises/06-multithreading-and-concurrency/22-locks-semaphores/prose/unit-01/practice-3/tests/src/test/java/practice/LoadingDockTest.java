package practice;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

class LoadingDockTest {

    interface DockCall {
        void run() throws InterruptedException;
    }

    /** Runs the call on a daemon thread; the latch counts down when it returns. */
    private static CountDownLatch onItsOwnThread(String name, DockCall call, AtomicReference<Throwable> thrown) {
        CountDownLatch returned = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                call.run();
                returned.countDown();
            } catch (Throwable e) {
                thrown.set(e);
            }
        }, name);
        t.setDaemon(true);
        t.start();
        return returned;
    }

    private static boolean waiting(LoadingDock dock, Condition condition) {
        dock.lock.lock();
        try {
            return dock.lock.hasWaiters(condition);
        } finally {
            dock.lock.unlock();
        }
    }

    /** Spins until the condition holds, for at most five seconds; answers whether it held. */
    private static boolean within5s(BooleanSupplier condition) {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > end) return false;
            Thread.onSpinWait();
        }
        return true;
    }

    @Test
    void trucksTakeFreeBaysAndTheEmptyDockClosesAtOnce() throws InterruptedException {
        LoadingDock dock = new LoadingDock(3);
        dock.arrive();
        dock.arrive();
        assertThat(dock.occupied()).isEqualTo(2);
        dock.leave();
        dock.leave();
        assertThat(dock.occupied()).isZero();
        dock.awaitEmpty();
    }

    @Test
    void aTruckThatLosesTheBayToANewcomerWaitsForTheNextOne() throws InterruptedException {
        LoadingDock dock = new LoadingDock(1);
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        dock.arrive();                                            // truck NL-41 is in the only bay
        CountDownLatch inBay = onItsOwnThread("truck-BE-07", dock::arrive, thrown);
        assertThat(within5s(() -> waiting(dock, dock.bayFree))).as("BE-07 waits for a bay").isTrue();

        dock.lock.lock();
        try {
            dock.leave();                                         // NL-41 leaves, BE-07 is woken...
            dock.arrive();                                        // ...but DE-93 drives in first
        } finally {
            dock.lock.unlock();
        }

        assertThat(within5s(() -> inBay.getCount() == 0 || waiting(dock, dock.bayFree))).isTrue();
        assertThat(inBay.getCount()).as("BE-07 is still waiting").isEqualTo(1);
        assertThat(dock.occupied()).isEqualTo(1);
        assertThat(thrown.get()).isNull();
    }

    @Test
    void aTruckWaitingForABayGetsInWhenATruckLeaves() throws InterruptedException {
        LoadingDock dock = new LoadingDock(2);
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        dock.arrive();
        dock.arrive();
        CountDownLatch inBay = onItsOwnThread("truck-FR-12", dock::arrive, thrown);
        assertThat(within5s(() -> waiting(dock, dock.bayFree))).as("FR-12 waits for a bay").isTrue();

        dock.leave();

        assertThat(inBay.await(5, TimeUnit.SECONDS)).as("FR-12 takes the free bay").isTrue();
        assertThat(dock.occupied()).isEqualTo(2);
    }

    @Test
    void theNightCloseGoesAheadWhenTheLastTruckLeaves() throws InterruptedException {
        LoadingDock dock = new LoadingDock(2);
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        dock.arrive();
        dock.arrive();
        CountDownLatch closed = onItsOwnThread("night-close", dock::awaitEmpty, thrown);
        assertThat(within5s(() -> waiting(dock, dock.dockEmpty))).as("the night close waits").isTrue();

        dock.leave();
        dock.leave();

        assertThat(closed.await(5, TimeUnit.SECONDS)).as("the night close goes ahead").isTrue();
    }

    @Test
    void theNightCloseWokenWhileATruckIsStillInKeepsWaiting() throws InterruptedException {
        LoadingDock dock = new LoadingDock(2);
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        dock.arrive();
        dock.arrive();
        CountDownLatch closed = onItsOwnThread("night-close", dock::awaitEmpty, thrown);
        assertThat(within5s(() -> waiting(dock, dock.dockEmpty))).as("the night close waits").isTrue();

        dock.leave();
        dock.lock.lock();
        try {
            dock.dockEmpty.signalAll();                           // a wake-up with one truck still in
        } finally {
            dock.lock.unlock();
        }

        assertThat(within5s(() -> closed.getCount() == 0 || waiting(dock, dock.dockEmpty))).isTrue();
        assertThat(closed.getCount()).as("the gate stays open while a truck is in").isEqualTo(1);
        assertThat(thrown.get()).isNull();
    }
}
