package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointsLedgerTest {

    private static final List<PointsLedger.Entry> CAFE_FILE = List.of(
            new PointsLedger.Entry("m-1042", 120), new PointsLedger.Entry("m-2207", 35));
    private static final List<PointsLedger.Entry> AIRLINE_FILE = List.of(
            new PointsLedger.Entry("m-1042", 800), new PointsLedger.Entry("m-3315", 60));
    private static final List<PointsLedger.Entry> OVERDRAWN_FILE = List.of(
            new PointsLedger.Entry("m-2207", -500));

    /** A daemon thread that runs the task, records what it throws, and counts down when it ends. */
    private static Thread daemon(String name, ThrowingTask task, AtomicReference<Throwable> thrown, CountDownLatch done) {
        Thread t = new Thread(() -> {
            try {
                task.run();
            } catch (Throwable e) {
                thrown.set(e);
            } finally {
                done.countDown();
            }
        }, name);
        t.setDaemon(true);
        return t;
    }

    interface ThrowingTask {
        void run() throws Exception;
    }

    private static void awaitQueued(PointsLedger ledger, Thread t) {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!ledger.lock.hasQueuedThread(t) && System.nanoTime() < end) Thread.onSpinWait();
        assertThat(ledger.lock.hasQueuedThread(t)).as("the importer waits for the ledger").isTrue();
    }

    @Test
    void filesImportedFromTwoWorkersBothLand() throws InterruptedException {
        PointsLedger ledger = new PointsLedger();
        CountDownLatch done = new CountDownLatch(2);
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        daemon("worker-1", () -> ledger.importFile(CAFE_FILE), thrown, done).start();
        daemon("worker-2", () -> ledger.importFile(AIRLINE_FILE), thrown, done).start();

        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(thrown.get()).isNull();
        assertThat(ledger.balance("m-1042")).isEqualTo(920);
        assertThat(ledger.balance("m-2207")).isEqualTo(35);
        assertThat(ledger.balance("m-3315")).isEqualTo(60);
    }

    @Test
    void aRejectedFileDoesNotHoldUpTheImportsAfterIt() throws InterruptedException {
        PointsLedger ledger = new PointsLedger();
        assertThatThrownBy(() -> ledger.importFile(OVERDRAWN_FILE)).isInstanceOf(IllegalArgumentException.class);

        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        daemon("worker-2", () -> ledger.importFile(CAFE_FILE), thrown, done).start();

        assertThat(done.await(5, TimeUnit.SECONDS)).as("the next file is imported").isTrue();
        assertThat(thrown.get()).isNull();
        assertThat(ledger.balance("m-1042")).isEqualTo(120);
    }

    @Test
    void aWorkerWaitingForTheLedgerStopsWhenTheDeployInterruptsIt() throws InterruptedException {
        PointsLedger ledger = new PointsLedger();
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread worker = daemon("worker-3", () -> ledger.importFile(AIRLINE_FILE), thrown, done);
        ledger.lock.lock();                       // a long import holds the ledger
        try {
            worker.start();
            awaitQueued(ledger, worker);
            worker.interrupt();
            assertThat(done.await(5, TimeUnit.SECONDS)).as("the worker stops waiting").isTrue();
        } finally {
            ledger.lock.unlock();
        }
        assertThat(ledger.balance("m-1042")).isZero();
    }

    @Test
    void theInterruptedWorkerReportsThatItWasInterrupted() throws InterruptedException {
        PointsLedger ledger = new PointsLedger();
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread worker = daemon("worker-4", () -> ledger.importFile(CAFE_FILE), thrown, done);
        ledger.lock.lock();
        try {
            worker.start();
            awaitQueued(ledger, worker);
            worker.interrupt();
            done.await(5, TimeUnit.SECONDS);
        } finally {
            ledger.lock.unlock();
        }
        assertThat(thrown.get()).isInstanceOf(InterruptedException.class);
    }
}
