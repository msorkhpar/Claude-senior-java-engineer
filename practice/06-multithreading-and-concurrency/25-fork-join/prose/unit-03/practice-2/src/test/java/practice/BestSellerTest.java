package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class BestSellerTest {

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

    private static BestSeller.Top expected(long[] ids, int[] units) {
        Map<Long, Long> sold = new HashMap<>();
        for (int i = 0; i < ids.length; i++) sold.merge(ids[i], (long) units[i], Long::sum);
        BestSeller.Top best = null;
        for (var e : sold.entrySet()) {
            if (best == null || e.getValue() > best.units()
                    || (e.getValue() == best.units() && e.getKey() < best.productId())) {
                best = new BestSeller.Top(e.getKey(), e.getValue());
            }
        }
        return best;
    }

    private BestSeller.Top top(int threshold, long[] ids, int[] units) throws Exception {
        BestSeller seller = new BestSeller(pool, threshold);
        return within30s(() -> seller.top(ids, units));
    }

    @Test
    void findsTheBestSellerOfADay() throws Exception {
        pool = new ForkJoinPool(3);

        // one order line
        assertThat(top(4, new long[] {880_017}, new int[] {3})).isEqualTo(new BestSeller.Top(880_017, 3));

        // a short day, counted by one task; 610_002 and 610_009 tie at 9 units, and the smaller id wins
        long[] shortIds = {610_009, 610_002, 610_005, 610_009, 610_002, 610_005, 610_009};
        int[] shortUnits = {4, 2, 3, 1, 7, 5, 4};
        assertThat(top(50, shortIds, shortUnits)).isEqualTo(new BestSeller.Top(610_002, 9));

        // a long day on which product 770_007 outsells every other product on every stretch of lines
        int n = 3_001;
        long[] ids = new long[n];
        int[] units = new int[n];
        for (int i = 0; i < n; i++) {
            ids[i] = i % 2 == 0 ? 770_007 : 770_100 + (i / 2) % 97;
            units[i] = i % 2 == 0 ? 6 : 1 + i % 3;
        }
        for (int threshold : new int[] {1, 16, 5_000}) {
            assertThat(top(threshold, ids, units)).as("threshold %d", threshold).isEqualTo(expected(ids, units));
        }
    }

    @Test
    void aProductThatComesSecondEverywhereCanWinTheDay() throws Exception {
        pool = new ForkJoinPool(3);
        // Every stretch of three lines has its own leader with 7 units; 450_000 sells 5 units in
        // every stretch, and 40 units over the day.
        int n = 24;
        long[] ids = new long[n];
        int[] units = new int[n];
        for (int s = 0; s < n / 3; s++) {
            ids[3 * s] = 450_101 + s;
            units[3 * s] = 7;
            ids[3 * s + 1] = 450_000;
            units[3 * s + 1] = 5;
            ids[3 * s + 2] = 450_901 + s;
            units[3 * s + 2] = 1;
        }
        assertThat(top(3, ids, units)).as("threshold 3").isEqualTo(new BestSeller.Top(450_000, 40));
        assertThat(top(12, ids, units)).as("threshold 12").isEqualTo(new BestSeller.Top(450_000, 40));
    }
}
