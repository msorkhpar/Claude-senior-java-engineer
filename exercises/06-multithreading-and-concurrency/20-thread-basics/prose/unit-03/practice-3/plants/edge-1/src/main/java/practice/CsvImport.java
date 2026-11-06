package practice;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Imports a customer's CSV file in the background: each chunk of the file is parsed and staged,
 * and the staged rows become visible only when the import is committed. The user can cancel an
 * import from the UI while it runs.
 */
public class CsvImport {

    /** Parses one chunk of the file into rows. Slow: it waits on the file storage. */
    public interface Parser {
        List<String> parse(String chunk) throws InterruptedException;
    }

    /** Where rows wait until the import is committed. */
    public interface Store {
        void stage(String importId, List<String> rows);

        void commit(String importId);
    }

    private final ExecutorService pool;
    private final Parser parser;
    private final Store store;

    public CsvImport(ExecutorService pool, Parser parser, Store store) {
        this.pool = pool;
        this.parser = parser;
        this.store = store;
    }

    /** Starts an import in the background. */
    public Future<?> start(String importId, List<String> chunks) {
        return pool.submit(() -> run(importId, chunks));
    }

    /** What the UI's Cancel button calls. True when the import was cancelled. */
    public boolean cancel(Future<?> running) {
        return running.cancel(false);
    }

    void run(String importId, List<String> chunks) {
        for (String chunk : chunks) {
            List<String> rows = parse(chunk);
            if (rows == null) return;
            store.stage(importId, rows);
        }
        if (mayCommit()) store.commit(importId);
    }

    /** The rows of one chunk, or null when the import must end here. */
    List<String> parse(String chunk) {
        try {
            return parser.parse(chunk);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /** Whether the staged rows may become visible now. */
    boolean mayCommit() {
        return !Thread.currentThread().isInterrupted();
    }
}
