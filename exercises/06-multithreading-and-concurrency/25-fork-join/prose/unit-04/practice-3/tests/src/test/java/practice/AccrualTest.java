package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccrualTest {

    /** A month's interest on one balance; a negative balance is a bad account. */
    private static final LongUnaryOperator INTEREST = cents -> {
        if (cents < 0) throw new IllegalArgumentException("bad account: " + cents);
        return cents + cents / 400 + 3;
    };

    private ForkJoinPool pool;

    @AfterEach
    void closePool() {
        if (pool != null) pool.shutdownNow();
    }

    /** Runs the call on a daemon thread and waits for it at most 30 seconds. */
    private static void within30s(Runnable call) throws Exception {
        FutureTask<Void> f = new FutureTask<>(call, null);
        Thread t = new Thread(f, "test-caller");
        t.setDaemon(true);
        t.start();
        try {
            f.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Exception x) throw x;
            throw e;
        }
    }

    private static long[] balances(int n) {
        long[] a = new long[n];
        for (int i = 0; i < n; i++) a[i] = 12_345 + (i * 3_571L) % 480_000;
        return a;
    }

    private static long[] expected(long[] before) {
        long[] e = before.clone();
        for (int i = 0; i < e.length; i++) e[i] = INTEREST.applyAsLong(e[i]);
        return e;
    }

    @Test
    void adjustsEveryBalanceOfAGoodBook() throws Exception {
        pool = new ForkJoinPool(3);
        for (int n : new int[] {0, 1, 13, 3_001}) {
            for (int threshold : new int[] {2, 50}) {
                long[] a = balances(n);
                long[] want = expected(a);
                Accrual accrual = new Accrual(pool, threshold, INTEREST);
                within30s(() -> accrual.accrue(a));
                assertThat(a).as("%d balances, threshold %d", n, threshold).containsExactly(want);
            }
        }
    }

    @Test
    void aFailedAccrualLeavesEveryBalanceAsItWas() throws Exception {
        pool = new ForkJoinPool(2);
        long[] a = balances(30);
        a[29] = -250;                                   // the last account is bad
        long[] before = a.clone();
        Accrual accrual = new Accrual(pool, 64, INTEREST);

        assertThatThrownBy(() -> within30s(() -> accrual.accrue(a))).isInstanceOf(IllegalArgumentException.class);
        assertThat(a).as("balances after the failed accrual").containsExactly(before);
    }

    @Test
    void theCallerLearnsThatTheAccrualFailed() throws Exception {
        pool = new ForkJoinPool(2);
        long[] a = balances(500);
        a[123] = -1;
        Accrual accrual = new Accrual(pool, 16, INTEREST);

        assertThatThrownBy(() -> within30s(() -> accrual.accrue(a)))
                .as("accrue() on a book with a bad account")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aPartStillRunningAfterTheFailureCannotChangeTheBook() throws Exception {
        pool = new ForkJoinPool(2);
        long bad = -9, slow = 8_888_888;
        long[] a = {bad, 20_000, 21_000, 22_000, slow, 24_000, 25_000, 26_000};
        long[] before = a.clone();
        CountDownLatch slowStarted = new CountDownLatch(1);
        CountDownLatch callerSawFailure = new CountDownLatch(1);
        LongUnaryOperator interest = cents -> {
            try {
                if (cents == bad) slowStarted.await(20, TimeUnit.SECONDS);     // fail while the other half runs
                if (cents == slow) {
                    slowStarted.countDown();
                    callerSawFailure.await(20, TimeUnit.SECONDS);              // finish after the caller saw the failure
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return INTEREST.applyAsLong(cents);
        };
        Accrual accrual = new Accrual(pool, 4, interest);

        try {
            assertThatThrownBy(() -> within30s(() -> accrual.accrue(a))).isInstanceOf(IllegalArgumentException.class);
        } finally {
            callerSawFailure.countDown();
        }
        pool.shutdown();
        assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).as("the pool's work ended").isTrue();
        assertThat(a).as("balances once every part of the accrual has ended").containsExactly(before);
    }
}
