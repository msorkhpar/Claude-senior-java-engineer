package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

class SettlementJobTest {

    /** Net amount of one trade: a made-up fee rule, cheap and deterministic. */
    private static final LongUnaryOperator NET = cents -> cents - cents / 250 - 3;

    private final List<ForkJoinPool> pools = new ArrayList<>();

    private ForkJoinPool pool(int parallelism) {
        ForkJoinPool p = new ForkJoinPool(parallelism);
        pools.add(p);
        return p;
    }

    @AfterEach
    void closePools() {
        pools.forEach(ForkJoinPool::shutdownNow);
    }

    /** Runs the call on a daemon thread and waits for it at most 30 seconds. */
    private static <T> T within30s(Callable<T> call) throws Exception {
        FutureTask<T> f = new FutureTask<>(call);
        Thread t = new Thread(f, "test-caller");
        t.setDaemon(true);
        t.start();
        try {
            return f.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Exception x) throw x;
            throw e;
        }
    }

    private static long[] trades(int n) {
        long[] a = new long[n];
        for (int i = 0; i < n; i++) a[i] = 10_000 + (i * 6_007L) % 900_000;
        return a;
    }

    private static long expected(long[] a) {
        long s = 0;
        for (long x : a) s += NET.applyAsLong(x);
        return s;
    }

    private static int piecesFor(ForkJoinPool pool, int minRows, int rows) throws Exception {
        SettlementJob job = new SettlementJob(pool, minRows, NET);
        long[] a = trades(rows);
        assertThat(within30s(() -> job.total(a))).as("total of %d trades", rows).isEqualTo(expected(a));
        return job.pieces();
    }

    @Test
    void netsEveryTradeOfTheDay() throws Exception {
        ForkJoinPool two = pool(2);
        ForkJoinPool five = pool(5);
        for (ForkJoinPool p : List.of(two, five)) {
            for (int n : new int[] {0, 1, 7, 333, 10_001, 77_777}) {
                for (int minRows : new int[] {1, 40}) {
                    long[] a = trades(n);
                    SettlementJob job = new SettlementJob(p, minRows, NET);
                    assertThat(within30s(() -> job.total(a)))
                            .as("%d trades, minRows %d, pool of %d", n, minRows, p.getParallelism())
                            .isEqualTo(expected(a));
                }
            }
        }
    }

    @Test
    void everyWorkerGetsThirtyTwoToSixtyFourPieces() throws Exception {
        int pieces = piecesFor(pool(3), 100, 48_000);
        assertThat(pieces).as("pieces for 48,000 trades on a pool of 3").isBetween(96, 192);
    }

    @Test
    void thePiecesFollowThePoolTheJobRunsIn() throws Exception {
        assertThat(piecesFor(pool(1), 100, 400_000)).as("pieces on a pool of 1").isBetween(32, 64);
        assertThat(piecesFor(pool(40), 100, 400_000)).as("pieces on a pool of 40").isBetween(1_280, 2_560);
    }

    @Test
    void aSmallBatchIsNotCutUp() throws Exception {
        ForkJoinPool four = pool(4);
        assertThat(piecesFor(four, 250, 250)).as("pieces for 250 trades, minRows 250").isEqualTo(1);
        assertThat(piecesFor(four, 250, 600)).as("pieces for 600 trades, minRows 250").isLessThanOrEqualTo(4);
    }
}
