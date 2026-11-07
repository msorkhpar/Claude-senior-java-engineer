package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import practice.AuditLog.Event;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class AuditLogTest {

    private final ExecutorService exporter = daemonPool("export");
    private final ExecutorService requests = daemonPool("request");
    private final CountDownLatch release = new CountDownLatch(1);

    private static ExecutorService daemonPool(String name) {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, name);
            t.setDaemon(true);
            return t;
        });
    }

    @AfterEach
    void cleanUp() {
        release.countDown();
        exporter.shutdownNow();
        requests.shutdownNow();
    }

    /** An archive that signals at its first event and holds the export there until released. */
    private Consumer<Event> slowArchive(CountDownLatch reached, List<Event> written) {
        return e -> {
            written.add(e);
            reached.countDown();
            try {
                if (!release.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("never released");
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        };
    }

    @Test
    void theExportHandsOverEveryEventInOrder() throws Exception {
        AuditLog log = new AuditLog();
        log.append("ops-7", "open ticket");
        log.append("ops-2", "change limit");
        log.append("ops-7", "close ticket");
        List<Event> written = new ArrayList<>();

        int handed = exporter.submit(() -> log.export(written::add)).get(5, TimeUnit.SECONDS);

        assertThat(handed).isEqualTo(3);
        assertThat(written).extracting(Event::seq).containsExactly(1L, 2L, 3L);
        assertThat(written).extracting(Event::action).containsExactly("open ticket", "change limit", "close ticket");
    }

    @Test
    void anAppendCostsTheSameWhateverTheLogHolds() throws Exception {
        AuditLog log = new AuditLog();
        Future<Long> measured = requests.submit(() -> {
            for (int i = 0; i < 20_000; i++) log.append("batch", "import row " + i);
            var mx = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
            long id = Thread.currentThread().threadId();
            long before = mx.getThreadAllocatedBytes(id);
            for (int i = 0; i < 1_000; i++) log.append("ops-4", "view");
            return mx.getThreadAllocatedBytes(id) - before;
        });
        long bytes = measured.get(60, TimeUnit.SECONDS);

        // 1,000 appends at 20,000 events; a few dozen bytes per event is what a stored event costs
        assertThat(bytes / 1_000).as("bytes allocated per append to a log of 20,000 events")
                .isLessThan(8_000);
    }

    @Test
    void anAppendDoesNotWaitForAnExport() throws Exception {
        AuditLog log = new AuditLog();
        log.append("ops-1", "login");
        log.append("ops-1", "export");
        CountDownLatch reached = new CountDownLatch(1);
        exporter.submit(() -> log.export(slowArchive(reached, new CopyOnWriteArrayList<>())));
        assertThat(reached.await(5, TimeUnit.SECONDS)).as("the export reached the archive").isTrue();

        Future<Event> append = requests.submit(() -> log.append("ops-9", "logout"));
        try {
            append.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            fail("append() was still waiting while the export wrote to the archive");
        }
    }

    @Test
    void anExportSurvivesAppendsMadeWhileItRuns() throws Exception {
        AuditLog log = new AuditLog();
        for (int i = 0; i < 4; i++) log.append("ops-3", "step " + i);
        CountDownLatch reached = new CountDownLatch(1);
        List<Event> written = new CopyOnWriteArrayList<>();
        Future<Integer> export = exporter.submit(() -> log.export(slowArchive(reached, written)));
        assertThat(reached.await(5, TimeUnit.SECONDS)).as("the export reached the archive").isTrue();

        Future<?> more = requests.submit(() -> {
            log.append("ops-5", "refund");
            log.append("ops-5", "refund");
        });
        try {
            more.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // another case checks that appends don't wait; here only the export matters
        }
        release.countDown();

        try {
            export.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            fail("the export failed because events were appended while it ran: " + e.getCause());
        }
        List<Long> seqs = written.stream().map(Event::seq).toList();
        assertThat(seqs).as("every event that was there when the export began, in order")
                .startsWith(1L, 2L, 3L, 4L);
        assertThat(seqs).as("each event once").doesNotHaveDuplicates();
    }
}
