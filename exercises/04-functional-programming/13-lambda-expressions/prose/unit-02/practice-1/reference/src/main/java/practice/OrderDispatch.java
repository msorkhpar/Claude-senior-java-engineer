package practice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Archives incoming orders on a worker pool: each order id once, with a retry for I/O failures. */
public class OrderDispatch {

    /** Stores one order in the archive. Throws IOException when the archive can't take it right now. */
    public interface Archive {
        void store(String orderId) throws IOException;
    }

    /** What a dispatch did: the ids it archived, in order, and the ids it gave up on. */
    public record Report(List<String> archived, List<String> failed) {}

    static final int ATTEMPTS = 3;

    private final ExecutorService pool;
    private final Archive archive;
    private final Set<String> seen = ConcurrentHashMap.newKeySet();

    public OrderDispatch(ExecutorService pool, Archive archive) {
        this.pool = pool;
        this.archive = archive;
    }

    /** Archives every id the dispatcher hasn't seen before, retrying I/O failures. */
    public Report dispatch(List<String> orderIds) throws InterruptedException {
        List<String> archived = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        for (String id : orderIds) {
            if (Boolean.FALSE.equals(await(remember(id)))) continue;   // seen before: already archived
            if (archiveWithRetry(id)) archived.add(id);
            else failed.add(id);
        }
        return new Report(archived, failed);
    }

    private boolean archiveWithRetry(String id) throws InterruptedException {
        for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
            try {
                store(id).get(5, TimeUnit.SECONDS);
                return true;
            } catch (ExecutionException e) {
                if (!(e.getCause() instanceof IOException)) return false;   // only I/O failures are retried
            } catch (TimeoutException e) {
                return false;
            }
        }
        return false;
    }

    private static Object await(Future<?> f) throws InterruptedException {
        try {
            return f.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("remembering an id failed", e);
        }
    }

    /**
     * Records the id on the pool. dispatch() reads the Future's value: false means the id was
     * already recorded, so the order is not archived again.
     */
    Future<?> remember(String orderId) {
        return pool.submit(() -> seen.add(orderId));
    }

    /**
     * Stores the order in the archive on the pool. dispatch() reads how the Future ends: normally
     * when the order is stored; with the archive's IOException as the cause when it isn't, which
     * dispatch() retries.
     */
    Future<?> store(String orderId) {
        return pool.submit(() -> {
            archive.store(orderId);
            return null;
        });
    }
}
