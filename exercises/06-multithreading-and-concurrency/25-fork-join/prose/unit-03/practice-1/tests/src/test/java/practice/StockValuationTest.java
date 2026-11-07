package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntToLongFunction;

import static org.assertj.core.api.Assertions.assertThat;

class StockValuationTest {

    /** A made-up price list: 2.49 to 1,234.56, by SKU index. */
    private static final IntToLongFunction PRICES = sku -> 249 + (sku * 4_177L) % 123_207;

    private ForkJoinPool pool;

    @AfterEach
    void closePool() {
        if (pool != null) pool.shutdownNow();
    }

    /** Starts the call on a daemon thread. */
    private static <T> FutureTask<T> started(Callable<T> call, String name) {
        FutureTask<T> f = new FutureTask<>(call);
        Thread t = new Thread(f, name);
        t.setDaemon(true);
        t.start();
        return f;
    }

    /** Runs the call on a daemon thread and waits for it at most 30 seconds. */
    private static <T> T within30s(Callable<T> call) throws Exception {
        return result(started(call, "test-caller"));
    }

    private static <T> T result(FutureTask<T> f) throws Exception {
        try {
            return f.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Exception x) throw x;
            throw e;
        }
    }

    private static int[] quantities(int n, int seed) {
        int[] q = new int[n];
        for (int i = 0; i < n; i++) q[i] = ((i + seed) * 31) % 17 == 0 ? 0 : 1 + ((i * 7 + seed) % 400);
        return q;
    }

    private static StockValuation.Totals expected(int[] q, IntToLongFunction prices) {
        long v = 0;
        int out = 0;
        for (int i = 0; i < q.length; i++) {
            v += q[i] * prices.applyAsLong(i);
            if (q[i] == 0) out++;
        }
        return new StockValuation.Totals(v, out);
    }

    @Test
    void valuesEveryWarehouseOnAFreshValuation() throws Exception {
        pool = new ForkJoinPool(3);
        for (int n : new int[] {0, 1, 7, 64, 5_003}) {
            for (int threshold : new int[] {1, 4, 250}) {
                int[] q = quantities(n, n);
                StockValuation valuation = new StockValuation(pool, threshold);
                assertThat(within30s(() -> valuation.value(q, PRICES)))
                        .as("%d SKUs, threshold %d", n, threshold).isEqualTo(expected(q, PRICES));
            }
        }
        int[] huge = new int[64];
        java.util.Arrays.fill(huge, 2_000_000);
        StockValuation valuation = new StockValuation(pool, 8);
        assertThat(within30s(() -> valuation.value(huge, sku -> 99_999)).valueCents())
                .as("64 SKUs of 2,000,000 units at 999.99").isEqualTo(64L * 2_000_000 * 99_999);
    }

    @Test
    void theSameValuationGivesTheSameAnswerTwice() throws Exception {
        pool = new ForkJoinPool(3);
        StockValuation valuation = new StockValuation(pool, 16);
        int[] q = quantities(900, 3);
        StockValuation.Totals first = within30s(() -> valuation.value(q, PRICES));
        StockValuation.Totals second = within30s(() -> valuation.value(q, PRICES));
        assertThat(first).isEqualTo(expected(q, PRICES));
        assertThat(second).as("the second valuation on the same StockValuation").isEqualTo(expected(q, PRICES));
    }

    @Test
    void twoValuationsAtOnceEachGetTheirOwnTotals() throws Exception {
        pool = new ForkJoinPool(2);
        StockValuation valuation = new StockValuation(pool, 50);

        // The screen's valuation (5 SKUs, one task) is held at its first price lookup
        // until the nightly valuation has finished.
        int[] screen = {3, 0, 12, 5, 8};
        CountDownLatch screenWaiting = new CountDownLatch(1);
        CountDownLatch nightlyDone = new CountDownLatch(1);
        AtomicBoolean first = new AtomicBoolean(true);
        IntToLongFunction heldPrices = sku -> {
            if (first.getAndSet(false)) {
                screenWaiting.countDown();
                try {
                    if (!nightlyDone.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("nightly valuation never finished");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            }
            return PRICES.applyAsLong(sku);
        };
        FutureTask<StockValuation.Totals> screenRun = started(() -> valuation.value(screen, heldPrices), "screen");
        assertThat(screenWaiting.await(10, TimeUnit.SECONDS)).as("the screen's valuation started").isTrue();

        int[] nightly = quantities(400, 11);
        StockValuation.Totals nightlyTotals;
        try {
            nightlyTotals = within30s(() -> valuation.value(nightly, PRICES));
        } finally {
            nightlyDone.countDown();
        }
        StockValuation.Totals screenTotals = result(screenRun);

        assertThat(nightlyTotals).as("the nightly valuation").isEqualTo(expected(nightly, PRICES));
        assertThat(screenTotals).as("the screen's valuation, which ran during the nightly one").isEqualTo(expected(screen, PRICES));
    }
}
