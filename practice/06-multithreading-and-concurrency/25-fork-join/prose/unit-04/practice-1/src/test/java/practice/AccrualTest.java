package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccrualTest {

    /** A month's interest on one balance: a made-up rule, cheap and deterministic. */
    private static final LongUnaryOperator INTEREST = cents -> cents + cents / 250 + 11;

    private ForkJoinPool pool;

    @AfterEach
    void closePool() {
        if (pool != null) pool.shutdownNow();
    }

    /** Starts the call on a daemon thread; the caller decides how long to wait for it. */
    private static FutureTask<Void> start(Runnable call) {
        FutureTask<Void> f = new FutureTask<>(call, null);
        Thread t = new Thread(f, "test-caller");
        t.setDaemon(true);
        t.start();
        return f;
    }

    private static void within30s(Runnable call) throws Exception {
        try {
            start(call).get(30, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Exception x) throw x;
            throw e;
        }
    }

    private static long[] balances(int n) {
        long[] a = new long[n];
        for (int i = 0; i < n; i++) a[i] = 4_321 + (i * 6_007L) % 250_000;
        return a;
    }

    private static long[] expected(long[] before, LongUnaryOperator adjust) {
        long[] e = before.clone();
        for (int i = 0; i < e.length; i++) e[i] = adjust.applyAsLong(e[i]);
        return e;
    }

    @Test
    void adjustsABookThatOneTaskCanHandle() throws Exception {
        pool = new ForkJoinPool(3);
        for (int n : new int[] {0, 1, 6, 40}) {
            long[] a = balances(n);
            long[] want = expected(a, INTEREST);
            Accrual accrual = new Accrual(pool, 40, INTEREST);
            within30s(() -> accrual.accrue(a));
            assertThat(a).as("%d balances, threshold 40", n).containsExactly(want);
        }
    }

    @Test
    void everyBalanceOfALargeBookIsAdjustedExactlyOnce() throws Exception {
        pool = new ForkJoinPool(3);
        for (int n : new int[] {3, 11, 1_000, 4_097}) {
            for (int threshold : new int[] {2, 5, 64}) {
                long[] a = balances(n);
                long[] want = expected(a, INTEREST);
                Accrual accrual = new Accrual(pool, threshold, INTEREST);
                within30s(() -> accrual.accrue(a));
                assertThat(a).as("%d balances, threshold %d", n, threshold).containsExactly(want);
            }
        }
    }

    @Test
    void accrueReturnsOnlyOnceEveryBalanceIsAdjusted() throws Exception {
        pool = new ForkJoinPool(2);
        long[] a = balances(96);
        long held = 7_777_777;                     // one account whose adjustment the test holds
        a[71] = held;
        long[] want = expected(a, INTEREST);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        LongUnaryOperator holdOne = cents -> {
            if (cents == held) {
                entered.countDown();
                try {
                    release.await(20, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return INTEREST.applyAsLong(cents);
        };
        Accrual accrual = new Accrual(pool, 4, holdOne);

        FutureTask<Void> run = start(() -> accrual.accrue(a));
        try {
            assertThat(entered.await(20, TimeUnit.SECONDS)).as("the held account was reached").isTrue();
            assertThatThrownBy(() -> run.get(1, TimeUnit.SECONDS))
                    .as("accrue() returned while one account was still being adjusted")
                    .isInstanceOf(TimeoutException.class);
        } finally {
            release.countDown();
        }
        run.get(30, TimeUnit.SECONDS);
        assertThat(a).containsExactly(want);
    }

    @Test
    void theTwoHalvesOfALargeBookAreAdjustedAtTheSameTime() throws Exception {
        pool = new ForkJoinPool(2);
        // the first and the last account meet only if both halves of the book are adjusted at once
        long first = 5_100_001, last = 5_200_002;
        long[] a = {first, 30_000, 31_000, 32_000, 33_000, 34_000, 35_000, last};
        long[] want = expected(a, INTEREST);
        CyclicBarrier meet = new CyclicBarrier(2);
        LongUnaryOperator waitsForTheOtherEnd = cents -> {
            if (cents == first || cents == last) {
                try {
                    meet.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("interrupted while waiting for the other end", e);
                } catch (BrokenBarrierException | TimeoutException e) {
                    throw new IllegalStateException("the other end of the book was not being adjusted", e);
                }
            }
            return INTEREST.applyAsLong(cents);
        };
        Accrual accrual = new Accrual(pool, 4, waitsForTheOtherEnd);
        within30s(() -> accrual.accrue(a));
        assertThat(a).containsExactly(want);
    }
}
