package practice;

import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AuditTrailsTest {

    /** A trail another team wrote against version 1: it knows nothing about batches. */
    static final class TeamTrail implements AuditTrails.AuditTrail {
        private final List<String> lines = new ArrayList<>();

        @Override
        public void append(String entry) {
            lines.add("team: " + entry);
        }

        @Override
        public List<String> entries() {
            return List.copyOf(lines);
        }
    }

    @Test
    void aBatchIsRecordedInOrderAfterEarlierEntries() {
        AuditTrails.LockedTrail trail = new AuditTrails.LockedTrail();
        trail.append("login u-305");

        trail.appendAll(List.of("debit acct-88 4200", "credit acct-12 4200"));

        assertThat(trail.entries())
                .containsExactly("login u-305", "debit acct-88 4200", "credit acct-12 4200");
    }

    @Test
    void aTrailWrittenBeforeBatchesExistedRecordsThemThroughItsOwnAppend() {
        TeamTrail trail = new TeamTrail();

        trail.appendAll(List.of("refund r-19", "refund r-20"));

        assertThat(trail.entries()).containsExactly("team: refund r-19", "team: refund r-20");
    }

    @Test
    void theReportThreadNeverSeesHalfABatch() throws Exception {
        AuditTrails.LockedTrail trail = new AuditTrails.LockedTrail();
        trail.entries();                                   // warm up before the threads start
        AtomicReference<List<String>> seenByReport = new AtomicReference<>();
        AtomicReference<String> problem = new AtomicReference<>();

        // A batch that, when its second entry is read, runs the report on another thread and
        // waits until that thread has either read the trail or is blocked on the trail's lock.
        List<String> batch = new AbstractList<>() {
            private final String[] items = {"debit acct-31 990", "credit acct-77 990"};
            private boolean reportStarted;

            @Override
            public String get(int index) {
                if (index == 1 && !reportStarted) {
                    reportStarted = true;
                    Thread report = new Thread(() -> seenByReport.set(trail.entries()));
                    report.setDaemon(true);
                    report.start();
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
                    while (report.isAlive() && !blockedOn(report, trail)) {
                        if (System.nanoTime() > deadline) {
                            problem.set("the report thread neither finished nor waited for the trail");
                            break;
                        }
                        Thread.onSpinWait();
                    }
                }
                return items[index];
            }

            @Override
            public int size() {
                return items.length;
            }
        };

        Thread writer = new Thread(() -> trail.appendAll(batch));
        writer.setDaemon(true);
        writer.start();
        writer.join(TimeUnit.SECONDS.toMillis(20));
        assertThat(writer.isAlive()).as("appendAll did not return").isFalse();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (seenByReport.get() == null && System.nanoTime() < deadline) Thread.onSpinWait();

        assertThat(problem.get()).isNull();
        assertThat(seenByReport.get())
                .as("what the report read while the batch was being recorded")
                .containsExactly("debit acct-31 990", "credit acct-77 990");
    }

    private static boolean blockedOn(Thread thread, Object lock) {
        ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(thread.threadId());
        return info != null
                && info.getThreadState() == Thread.State.BLOCKED
                && info.getLockInfo() != null
                && info.getLockInfo().getIdentityHashCode() == System.identityHashCode(lock);
    }
}
