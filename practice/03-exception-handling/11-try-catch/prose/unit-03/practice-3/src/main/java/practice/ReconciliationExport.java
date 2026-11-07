package practice;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Writes the nightly reconciliation file. Only one export may run at a time, so each run holds
 * the export lock while it queries and writes.
 */
public class ReconciliationExport {

    /** Loads the rows to export; it fails with an unchecked exception, a timeout for example. */
    public interface Query {
        List<String> rows();
    }

    /** Writes the file; close() flushes the buffered rows and can fail. */
    public interface ReportWriter {
        void write(String row) throws IOException;

        void close() throws IOException;
    }

    private final ReentrantLock exportLock;

    public ReconciliationExport(ReentrantLock exportLock) {
        this.exportLock = exportLock;
    }

    /** Runs one export: queries the rows and writes them, then closes the writer and the lock. */
    public void run(Query query, ReportWriter out) throws IOException {
        exportLock.lock();
        Throwable failure = null;
        try {
            for (String row : query.rows()) out.write(row);
        } catch (Throwable t) {
            failure = t;
            throw t;
        } finally {
            release(out, failure);
        }
    }

    /**
     * Closes the writer and releases the export lock once the export has ended. {@code failure}
     * is what ended the export, or null when it finished its rows.
     */
    void release(ReportWriter out, Throwable failure) throws IOException {
        // TODO: Close the writer and release the export lock. Closing can fail too. Whoever is
        //       paged reads the exception run() throws, and the next export must be able to
        //       start whatever happened to this one.
        throw new IllegalStateException("TODO: release");
    }
}
