package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class ShardReportTest {

    /** Holds the shards that "overrun" until the test is over. */
    private final CountDownLatch hold = new CountDownLatch(1);

    private final ExecutorService caller = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "report-caller");
        t.setDaemon(true);
        return t;
    });

    @AfterEach
    void releaseEverything() {
        hold.countDown();
        caller.shutdownNow();
    }

    private static ShardReport.Shard matching(long... amounts) {
        return add -> {
            for (long a : amounts) add.accept(a);
        };
    }

    /** Matches some amounts, then waits for data that doesn't arrive before the test ends. */
    private ShardReport.Shard overrunning(long... amounts) {
        return add -> {
            for (long a : amounts) add.accept(a);
            hold.await();
            add.accept(1_000_000);
        };
    }

    private ShardReport.Report report(List<ShardReport.Shard> shards, Duration budget) throws Exception {
        Future<ShardReport.Report> f = caller.submit(() -> ShardReport.run(shards, budget));
        try {
            return f.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            fail("the report was not ready 10 seconds after it was asked for, with a budget of " + budget);
            return null;
        }
    }

    @Test
    void totalsEveryShardWhenAllFinishInTime() throws Exception {
        ShardReport.Report r = report(List.of(
                matching(310, 4_200),
                matching(7_015),
                matching(),
                matching(99, 1, 20_000)), Duration.ofSeconds(8));

        assertThat(r.totalCents()).isEqualTo(31_625);
        assertThat(r.unfinished()).isEmpty();
    }

    @Test
    void aShardStillRunningAtTheDeadlineIsListedAndNotCounted() throws Exception {
        ShardReport.Report r = report(List.of(
                matching(2_500),
                overrunning(1_250, 4_000),
                matching(600, 70)), Duration.ofSeconds(2));

        assertThat(r.unfinished()).containsExactly(1);
        assertThat(r.totalCents()).isEqualTo(3_170);
    }

    @Test
    void aBudgetAlreadySpentStillProducesAReport() throws Exception {
        ShardReport.Report r = report(List.of(
                overrunning(80),
                overrunning(),
                overrunning(5, 5)), Duration.ZERO);

        assertThat(r.unfinished()).containsExactly(0, 1, 2);
        assertThat(r.totalCents()).isZero();
    }

    @Test
    void aShardThatNeverFinishesDoesNotHoldTheReportPastItsBudget() throws Exception {
        ShardReport.Report r = report(List.of(
                overrunning(9_000),
                matching(45)), Duration.ofSeconds(2));

        assertThat(r.unfinished()).containsExactly(0);
        assertThat(r.totalCents()).isEqualTo(45);
    }
}
