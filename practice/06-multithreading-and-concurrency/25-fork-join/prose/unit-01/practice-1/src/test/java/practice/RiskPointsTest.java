package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

class RiskPointsTest {

    /** Points for one amount: a made-up formula, cheap and deterministic. */
    private static final LongUnaryOperator POINTS = cents -> (cents % 97) + (cents > 50_000 ? 40 : 3);

    private ForkJoinPool pool;

    @AfterEach
    void closePool() {
        if (pool != null) pool.shutdownNow();
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

    private static long[] amounts(int n) {
        long[] a = new long[n];
        for (int i = 0; i < n; i++) a[i] = 1_999 + (i * 7_919L) % 120_000;
        return a;
    }

    private static long expected(long[] a, LongUnaryOperator points) {
        long s = 0;
        for (long x : a) s += points.applyAsLong(x);
        return s;
    }

    @Test
    void totalsThePointsOfEveryAmount() throws Exception {
        pool = new ForkJoinPool(3);
        for (int n : new int[] {0, 1, 2, 9, 1_000, 12_347}) {
            for (int threshold : new int[] {1, 5, 300}) {
                long[] a = amounts(n);
                RiskPoints scorer = new RiskPoints(pool, threshold, POINTS);
                long total = within30s(() -> scorer.total(a));
                assertThat(total).as("%d amounts, threshold %d", n, threshold).isEqualTo(expected(a, POINTS));
            }
        }
    }

    @Test
    void aRangeThatFitsTheThresholdIsScoredWithoutMoreTasks() throws Exception {
        pool = new ForkJoinPool(3);

        RiskPoints exact = new RiskPoints(pool, 640, POINTS);
        long[] a = amounts(640);
        assertThat(within30s(() -> exact.total(a))).isEqualTo(expected(a, POINTS));
        assertThat(exact.tasksCreated()).as("tasks for 640 amounts with a threshold of 640").isEqualTo(1);

        RiskPoints day = new RiskPoints(pool, 250, POINTS);
        long[] b = amounts(6_000);
        assertThat(within30s(() -> day.total(b))).isEqualTo(expected(b, POINTS));
        assertThat(day.tasksCreated()).as("tasks for 6,000 amounts with a threshold of 250")
                .isLessThan(4 * 6_000 / 250);
    }

    @Test
    void bothHalvesOfALongRangeAreScoredAtTheSameTime() throws Exception {
        pool = new ForkJoinPool(2);
        // 31_001 is the first amount and 42_002 the last: they meet only if both halves run at once.
        long[] a = {31_001, 120, 130, 140, 150, 160, 170, 42_002};
        CyclicBarrier meet = new CyclicBarrier(2);
        LongUnaryOperator waitsForTheOtherEnd = cents -> {
            if (cents == 31_001 || cents == 42_002) {
                try {
                    meet.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("interrupted while waiting for the other end", e);
                } catch (BrokenBarrierException | TimeoutException e) {
                    throw new IllegalStateException("the other end of the day was not being scored", e);
                }
            }
            return POINTS.applyAsLong(cents);
        };
        RiskPoints scorer = new RiskPoints(pool, 4, waitsForTheOtherEnd);

        assertThat(within30s(() -> scorer.total(a))).isEqualTo(expected(a, POINTS));
    }
}
