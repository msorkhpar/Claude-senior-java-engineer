package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class SlowRequestsTest {

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

    private static int[] latencies(int n) {
        int[] ms = new int[n];
        for (int i = 0; i < n; i++) ms[i] = 3 + (int) ((i * 40_503L) % 1_400);
        return ms;
    }

    @Test
    void reportsEverySlowRequestAndItsTime() throws Exception {
        pool = new ForkJoinPool(3);
        for (int n : new int[] {0, 1, 7, 2_000, 9_999}) {
            for (int threshold : new int[] {1, 16, 700}) {
                int[] ms = latencies(n);
                long slow = 0, millis = 0;
                for (int x : ms) if (x > 600) { slow++; millis += x; }

                SlowRequests.Tally tally = new SlowRequests.Tally();
                tally.report(5, 4_000);                       // the morning's scan already reported
                SlowRequests scanner = new SlowRequests(pool, threshold, 600);
                within30s(() -> scanner.scan(ms, tally));

                assertThat(tally.requests()).as("slow requests of %d, threshold %d", n, threshold).isEqualTo(5 + slow);
                assertThat(tally.millis()).as("their milliseconds, %d, threshold %d", n, threshold).isEqualTo(4_000 + millis);
            }
        }
    }

    @Test
    void eachPartOfTheScanTakesTheTallysLockAtMostOnce() throws Exception {
        pool = new ForkJoinPool(3);
        int[] ms = latencies(4_096);                          // 4,096 / 512: eight parts of the scan
        SlowRequests.Tally tally = new SlowRequests.Tally();
        SlowRequests scanner = new SlowRequests(pool, 512, 600);
        within30s(() -> scanner.scan(ms, tally));

        assertThat(tally.requests()).as("slow requests found").isGreaterThan(1_000);
        assertThat(tally.reports()).as("reports to the tally for a scan in eight parts").isLessThanOrEqualTo(8);
    }
}
