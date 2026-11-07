package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDispatchTest {

    /** An archive that fails the first {@code failuresPerId} stores of each id, then stores it. */
    static final class FlakyArchive implements OrderDispatch.Archive {
        private final int failuresPerId;
        private final Map<String, Integer> attempts = new HashMap<>();
        final List<String> stored = Collections.synchronizedList(new ArrayList<>());
        final List<String> storingThreads = Collections.synchronizedList(new ArrayList<>());

        FlakyArchive(int failuresPerId) {
            this.failuresPerId = failuresPerId;
        }

        @Override
        public synchronized void store(String orderId) throws IOException {
            storingThreads.add(Thread.currentThread().getName());
            int attempt = attempts.merge(orderId, 1, Integer::sum);
            if (attempt <= failuresPerId) throw new IOException("archive volume busy, attempt " + attempt);
            stored.add(orderId);
        }
    }

    private ExecutorService workers;
    private ExecutorService caller;

    @BeforeEach
    void startPools() {
        AtomicInteger n = new AtomicInteger();
        workers = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "archive-worker-" + n.incrementAndGet());
            t.setDaemon(true);
            return t;
        });
        caller = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "test-caller");
            t.setDaemon(true);
            return t;
        });
    }

    @AfterEach
    void stopPools() {
        workers.shutdownNow();
        caller.shutdownNow();
    }

    /** Runs dispatch on a thread of its own, so a dispatcher that never returns fails the test instead of hanging it. */
    private OrderDispatch.Report dispatch(OrderDispatch.Archive archive, String... ids) throws Exception {
        OrderDispatch dispatcher = new OrderDispatch(workers, archive);
        Future<OrderDispatch.Report> report = caller.submit(() -> dispatcher.dispatch(List.of(ids)));
        return report.get(20, TimeUnit.SECONDS);
    }

    @Test
    void archivesEveryNewOrderOnThePool() throws Exception {
        FlakyArchive archive = new FlakyArchive(0);

        OrderDispatch.Report report = dispatch(archive, "INV-4471", "INV-4472", "RMA-0090");

        assertThat(report.archived()).containsExactly("INV-4471", "INV-4472", "RMA-0090");
        assertThat(report.failed()).isEmpty();
        assertThat(archive.stored).containsExactly("INV-4471", "INV-4472", "RMA-0090");
        assertThat(archive.storingThreads).allMatch(name -> name.startsWith("archive-worker-"));
    }

    @Test
    void aRepeatedOrderIsArchivedOnce() throws Exception {
        FlakyArchive archive = new FlakyArchive(0);

        OrderDispatch.Report report = dispatch(archive, "INV-5120", "RMA-0007", "INV-5120", "INV-5120");

        assertThat(archive.stored).containsExactly("INV-5120", "RMA-0007");
        assertThat(report.archived()).containsExactly("INV-5120", "RMA-0007");
    }

    @Test
    void anOrderTheArchiveRefusesOnceIsArchivedOnRetry() throws Exception {
        FlakyArchive archive = new FlakyArchive(1);

        OrderDispatch.Report report = dispatch(archive, "INV-6301", "INV-6302");

        assertThat(report.archived()).containsExactly("INV-6301", "INV-6302");
        assertThat(report.failed()).isEmpty();
        assertThat(archive.stored).containsExactly("INV-6301", "INV-6302");
    }

    @Test
    void anOrderTheArchiveKeepsRefusingIsReportedAsFailed() throws Exception {
        FlakyArchive archive = new FlakyArchive(OrderDispatch.ATTEMPTS);

        OrderDispatch.Report report = dispatch(archive, "RMA-0314");

        assertThat(report.failed()).containsExactly("RMA-0314");
        assertThat(report.archived()).isEmpty();
        assertThat(archive.stored).isEmpty();
    }
}
