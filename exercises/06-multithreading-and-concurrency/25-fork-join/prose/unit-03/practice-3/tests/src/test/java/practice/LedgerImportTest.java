package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToLongFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

class LedgerImportTest {

    /** Parses "1234.56" or "-0.07" into cents; anything else is not an amount. */
    private static final ToLongFunction<String> CENTS = row -> {
        if (!row.matches("-?\\d{1,9}\\.\\d\\d")) throw new NumberFormatException("not an amount: " + row);
        return Long.parseLong(row.replace(".", ""));
    };

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

    private static String[] rows(int n) {
        String[] r = new String[n];
        for (int i = 0; i < n; i++) r[i] = String.format("%s%d.%02d", i % 9 == 4 ? "-" : "", (i * 37) % 5_000, i % 100);
        return r;
    }

    private static long expected(String[] rows) {
        long s = 0;
        for (String r : rows) s += CENTS.applyAsLong(r);
        return s;
    }

    @Test
    void totalsAFileAndRefusesABadOne() throws Exception {
        pool = new ForkJoinPool(2);
        for (int n : new int[] {0, 1, 6, 333, 4_096}) {
            for (int threshold : new int[] {1, 8, 500}) {
                String[] file = rows(n);
                LedgerImport ledger = new LedgerImport(pool, threshold, CENTS);
                assertThat(within30s(() -> ledger.total(file))).as("%d rows, threshold %d", n, threshold).isEqualTo(expected(file));
            }
        }
        String[] bad = rows(2_000);
        bad[1_377] = "13,50";
        LedgerImport ledger = new LedgerImport(pool, 16, CENTS);
        assertThatThrownBy(() -> within30s(() -> ledger.total(bad))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theErrorNamesTheBadRowAndWhy() throws Exception {
        pool = new ForkJoinPool(3);
        String[] bad = rows(6_000);
        bad[4_321] = "7,5";
        LedgerImport ledger = new LedgerImport(pool, 25, CENTS);
        Throwable thrown = catchThrowable(() -> within30s(() -> ledger.total(bad)));
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("row 4321: not an amount: 7,5");
    }

    @Test
    void aBadFileStopsBeingParsedSoonAfterItsFirstBadRow() throws Exception {
        pool = new ForkJoinPool(2);
        // Every 20th row is bad, so whatever order the rows are parsed in, a bad one comes early.
        String[] file = rows(8_000);
        for (int i = 7; i < file.length; i += 20) file[i] = "n/a";
        AtomicBoolean badSeen = new AtomicBoolean();
        AtomicInteger parsedAfter = new AtomicInteger();
        ToLongFunction<String> counting = row -> {
            if (badSeen.get()) parsedAfter.incrementAndGet();
            try {
                return CENTS.applyAsLong(row);
            } catch (NumberFormatException e) {
                badSeen.set(true);
                throw e;
            }
        };
        LedgerImport ledger = new LedgerImport(pool, 10, counting);
        assertThatThrownBy(() -> within30s(() -> ledger.total(file))).isInstanceOf(IllegalArgumentException.class);
        // Tasks of the import may still be queued: let the pool run everything it has, then count.
        pool.shutdown();
        assertThat(pool.awaitTermination(20, TimeUnit.SECONDS)).as("the pool finished its tasks").isTrue();
        assertThat(parsedAfter.get()).as("rows parsed after the first bad row, of 8,000").isLessThan(200);
    }

    @Test
    void aFailedImportDoesNotSpoilTheNextOne() throws Exception {
        pool = new ForkJoinPool(2);
        LedgerImport ledger = new LedgerImport(pool, 8, CENTS);
        String[] bad = rows(500);
        bad[260] = "";
        assertThatThrownBy(() -> within30s(() -> ledger.total(bad))).isInstanceOf(IllegalArgumentException.class);
        String[] good = rows(700);
        assertThat(within30s(() -> ledger.total(good))).as("the next file, on the same LedgerImport").isEqualTo(expected(good));
    }
}
