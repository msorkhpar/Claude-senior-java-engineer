package practice;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusReportTest {

    /** Every call into the report goes through this thread, with a timeout, so a hang fails one case. */
    private static final ExecutorService CALLER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "reconciliation");
        t.setDaemon(true);
        return t;
    });

    @AfterAll
    static void stopTheCaller() {
        CALLER.shutdownNow();
    }

    private static <T> T call(Callable<T> work) throws Throwable {
        try {
            return CALLER.submit(work).get(20, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    private static final List<String> IDS = IntStream.range(0, 36).mapToObj(i -> "pay-" + (5_000 + 11 * i)).toList();

    private static String settled(String id) {
        return id.endsWith("3") ? "RETURNED " + id : "SETTLED " + id;
    }

    /** A bank that answers only once {@code n} lookups are waiting for it together, or after five seconds. */
    private static StatusReport.BankClient answeringWhenBusy(int n, AtomicBoolean answeredAlone, Set<Thread> threads) {
        CountDownLatch together = new CountDownLatch(n);
        return id -> {
            threads.add(Thread.currentThread());
            together.countDown();
            try {
                if (!together.await(5, TimeUnit.SECONDS)) answeredAlone.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return settled(id);
        };
    }

    @Test
    void returnsEveryStatusInTheOrderOfTheIds() throws Throwable {
        List<String> statuses = call(() -> new StatusReport(StatusReportTest::settled, 3).statuses(IDS));

        assertThat(statuses).containsExactlyElementsOf(IDS.stream().map(StatusReportTest::settled).toList());
        assertThat(call(() -> new StatusReport(StatusReportTest::settled, 2).statuses(List.of()))).isEmpty();
    }

    @Test
    void keepsTheLookupsOffTheCommonPool() throws Throwable {
        Set<Thread> threads = ConcurrentHashMap.newKeySet();
        StatusReport report = new StatusReport(answeringWhenBusy(2, new AtomicBoolean(), threads), 2);

        call(() -> report.statuses(IDS));

        assertThat(threads).isNotEmpty().allSatisfy(t -> assertThat(
                t instanceof ForkJoinWorkerThread worker && worker.getPool() == ForkJoinPool.commonPool())
                .as("%s belongs to the common pool", t.getName()).isFalse());
    }

    @Test
    void runsAsManyLookupsAtOnceAsItIsAllowed() throws Throwable {
        AtomicBoolean answeredAlone = new AtomicBoolean();
        StatusReport report = new StatusReport(answeringWhenBusy(3, answeredAlone, ConcurrentHashMap.newKeySet()), 3);

        List<String> statuses = call(() -> report.statuses(IDS));

        assertThat(answeredAlone).as("fewer than three lookups were ever waiting on the bank together").isFalse();
        assertThat(statuses).containsExactlyElementsOf(IDS.stream().map(StatusReportTest::settled).toList());
    }

    @Test
    void aFailedLookupReachesTheCallerAsItself() {
        StatusReport report = new StatusReport(id -> {
            if (id.equals("pay-5132")) throw new IllegalArgumentException("unknown payment " + id);
            return settled(id);
        }, 3);

        assertThatThrownBy(() -> call(() -> report.statuses(IDS)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasStackTraceContaining("unknown payment pay-5132");
    }

    @Test
    void leavesNoLookupThreadBehindWhenALookupFails() throws Throwable {
        Set<Thread> threads = ConcurrentHashMap.newKeySet();
        StatusReport report = new StatusReport(id -> {
            threads.add(Thread.currentThread());
            if (id.equals("pay-5275")) throw new IllegalStateException("bank timed out on " + id);
            return settled(id);
        }, 3);

        assertThatThrownBy(() -> call(() -> report.statuses(IDS))).isInstanceOf(IllegalStateException.class);

        for (Thread t : threads) t.join(5_000);
        assertThat(threads).isNotEmpty().allSatisfy(t ->
                assertThat(t.isAlive()).as("%s is still alive 5 s after the report failed", t.getName()).isFalse());
    }
}
