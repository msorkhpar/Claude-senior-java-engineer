package practice;

import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class WalletTest {

    private static final ThreadMXBean MX = ManagementFactory.getThreadMXBean();
    private static final Runnable NOTHING = () -> {};

    @Test
    void transfersMoveMoneyFromCheckingToSavings() throws Exception {
        Wallet wallet = new Wallet(12_345, 600, NOTHING);
        Future<?> done = onDaemon(() -> { wallet.transfer(2_000); wallet.transfer(345); });
        done.get(5, TimeUnit.SECONDS);

        assertThat(call(wallet::statement)).isEqualTo(new Wallet.Statement(10_000, 2_945));
        assertThat(call(wallet::total)).isEqualTo(12_945L);
    }

    @Test
    void transfersFromManyThreadsLoseNothing() throws Exception {
        Wallet wallet = new Wallet(90_000, 7, NOTHING);
        ExecutorService pool = Executors.newFixedThreadPool(4, WalletTest::daemon);
        try {
            List<Future<?>> all = new ArrayList<>();
            for (int t = 0; t < 4; t++) {
                all.add(pool.submit(() -> { for (int i = 0; i < 2_500; i++) wallet.transfer(3); }));
            }
            for (Future<?> f : all) f.get(10, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }

        assertThat(call(wallet::statement)).isEqualTo(new Wallet.Statement(60_000, 30_007));
        assertThat(call(wallet::total)).isEqualTo(90_007L);
    }

    @Test
    void totalDuringATransferCountsTheMoneyOnce() throws Exception {
        long total = readWhileATransferIsPaused(new Wallet.Statement(5_000, 250), 1_200, Wallet::total);

        assertThat(total).as("total() read while a transfer of 1,200 was between debit and credit")
                .isEqualTo(5_250L);
    }

    @Test
    void statementDuringATransferAddsUp() throws Exception {
        Wallet.Statement seen = readWhileATransferIsPaused(new Wallet.Statement(8_000, 41), 700, Wallet::statement);

        assertThat(seen).as("statement() read while a transfer of 700 was between debit and credit")
                .isIn(new Wallet.Statement(8_000, 41), new Wallet.Statement(7_300, 741));
    }

    /**
     * Starts a transfer that pauses in midTransfer, then reads the wallet on another thread. Waits until
     * the read has returned, or is waiting for a lock the paused transfer holds, then lets the transfer
     * finish and returns what the read saw.
     */
    private <T> T readWhileATransferIsPaused(Wallet.Statement start, long amount, Function<Wallet, T> read)
            throws Exception {
        CountDownLatch paused = new CountDownLatch(1), resume = new CountDownLatch(1);
        Wallet wallet = new Wallet(start.checking(), start.savings(), () -> {
            paused.countDown();
            try {
                resume.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        FutureTask<Void> transfer = new FutureTask<>(() -> wallet.transfer(amount), null);
        Thread writer = daemon(transfer);
        writer.start();
        try {
            assertThat(paused.await(5, TimeUnit.SECONDS)).as("the transfer reached midTransfer").isTrue();
            FutureTask<T> reading = new FutureTask<>(() -> read.apply(wallet));
            Thread reader = daemon(reading);
            reader.start();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (!reading.isDone() && !waitsFor(reader, writer) && System.nanoTime() < deadline) {
                Thread.onSpinWait();
            }
            resume.countDown();
            T seen = reading.get(5, TimeUnit.SECONDS);
            transfer.get(5, TimeUnit.SECONDS);
            return seen;
        } finally {
            resume.countDown();
        }
    }

    private static boolean waitsFor(Thread waiter, Thread holder) {
        ThreadInfo info = MX.getThreadInfo(waiter.threadId());
        return info != null && info.getLockOwnerId() == holder.threadId();
    }

    private static <T> T call(java.util.concurrent.Callable<T> c) throws Exception {
        FutureTask<T> task = new FutureTask<>(c);
        daemon(task).start();
        return task.get(5, TimeUnit.SECONDS);
    }

    private static Future<?> onDaemon(Runnable r) {
        FutureTask<Void> task = new FutureTask<>(r, null);
        daemon(task).start();
        return task;
    }

    private static Thread daemon(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    }
}
