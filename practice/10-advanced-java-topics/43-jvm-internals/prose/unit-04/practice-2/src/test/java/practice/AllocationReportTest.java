package practice;

import jdk.jfr.Recording;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Records a real JFR recording once: an export worker that builds CSV files and SVG badges, and a
 * worker that only waits. Each test reads that recording through AllocationReport.
 */
class AllocationReportTest {

    private static final String CSV = "practice.AllocationReportTest.exportCsv";
    private static final String BADGE = "practice.AllocationReportTest.renderBadge";

    @TempDir
    static Path dir;
    static Path recording;
    static final AtomicLong allocatedByWorker = new AtomicLong();
    static long sink;

    static String exportCsv(int rows) {
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            csv.append("order-").append(i).append(',').append(i * 31 % 977).append(",EUR\n");
        }
        return csv.toString();
    }

    static String renderBadge(int build) {
        return ("<svg><text>build " + build + "</text></svg>").repeat(40);
    }

    /** Looked up here, on the test's thread, so that the worker allocates only in the two methods. */
    private static final com.sun.management.ThreadMXBean THREADS =
            (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();

    private static void work() {
        long before = THREADS.getCurrentThreadAllocatedBytes();
        long chars = 0;
        for (int round = 0; round < 15_000; round++) {    // the two kinds of work take turns, about 6 GB
            chars += exportCsv(4_000).length();
            for (int build = 0; build < 40; build++) chars += renderBadge(round * 40 + build).length();
        }
        allocatedByWorker.set(THREADS.getCurrentThreadAllocatedBytes() - before);
        sink = chars;
    }

    @BeforeAll
    static void record() throws Exception {
        recording = dir.resolve("export.jfr");
        CountDownLatch idleStarted = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try (Recording r = new Recording()) {
            r.enable("jdk.ObjectAllocationSample").with("throttle", "150/s").withStackTrace();
            r.start();
            Thread idle = new Thread(() -> {
                idleStarted.countDown();
                try {
                    release.await(60, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "idle-worker");
            idle.setDaemon(true);
            idle.start();
            idleStarted.await(10, TimeUnit.SECONDS);
            Thread worker = new Thread(AllocationReportTest::work, "export-worker");
            worker.setDaemon(true);
            worker.start();
            worker.join(TimeUnit.SECONDS.toMillis(60));
            release.countDown();
            r.stop();
            r.dump(recording);
        }
    }

    @Test
    void reportsTheWorkerThatAllocatedAndNotTheOneThatWaited() throws Exception {
        assertThat(AllocationReport.bytesByMethod(recording, "export-worker", "practice")).isNotEmpty();
        assertThat(AllocationReport.bytesByMethod(recording, "idle-worker", "practice")).isEmpty();
    }

    @Test
    void theTotalComesCloseToWhatTheWorkerAllocated() throws Exception {
        Map<String, Long> report = AllocationReport.bytesByMethod(recording, "export-worker", "practice");
        long total = report.values().stream().mapToLong(Long::longValue).sum();

        assertThat(allocatedByWorker.get()).isGreaterThan(1L << 30);
        assertThat((double) total).isBetween(0.5 * allocatedByWorker.get(), 1.5 * allocatedByWorker.get());
    }

    @Test
    void theReportNamesOnlyMethodsOfTheApplication() throws Exception {
        Map<String, Long> report = AllocationReport.bytesByMethod(recording, "export-worker", "practice");

        assertThat(report).isNotEmpty();
        assertThat(report.keySet()).allMatch(method -> method.startsWith("practice."));
    }

    @Test
    void eachSampleIsChargedToTheMethodThatAllocated() throws Exception {
        Map<String, Long> report = AllocationReport.bytesByMethod(recording, "export-worker", "practice");

        assertThat(report).containsKey(CSV);
        assertThat(report.keySet()).isSubsetOf(CSV, BADGE);
    }
}
