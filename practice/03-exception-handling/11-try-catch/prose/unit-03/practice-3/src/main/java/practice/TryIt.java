package practice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) throws Exception {
        // Turn the logger up, so the log lines of your code show under the printed lines.
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s %5$s%n");
        // Only this handler prints, and it leaves out the JDK's own detail.
        Logger root = Logger.getLogger("");
        for (java.util.logging.Handler before : root.getHandlers()) {
            root.removeHandler(before);
        }
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFilter(entry -> !String.valueOf(entry.getLoggerName()).matches("(java|javax|jdk|sun|com\\.sun)\\..*"));
        root.setLevel(Level.ALL);
        root.addHandler(handler);
        JDK_EXIT.setLevel(Level.OFF);

        // A stand-in for the collaborator, like the one the tests use for the first main case.
        try {
            ReentrantLock lock = new ReentrantLock();
            ReconciliationExport export = new ReconciliationExport(lock);
            Writer good = new Writer(null);
            export.run(() -> List.of("ACC-2001;CHF;310.00", "ACC-2002;CHF;-12.50"), good);
            System.out.println("rows written: " + good.rows + ", closed: " + good.closed + ", locked: " + lock.isLocked());
            try {
                export.run(() -> List.of("ACC-2003;CHF;8.00"), new Writer(new IOException("disk quota exceeded")));
            } catch (IOException closeFailed) {
                System.out.println("close alone failed: " + closeFailed.getMessage() + ", locked: " + lock.isLocked());
            }
            try {
                export.run(() -> { throw new LedgerTimeout("ledger query timed out"); },
                        new Writer(new IOException("disk quota exceeded")));
            } catch (LedgerTimeout ended) {
                System.out.println("export ended by: " + ended.getMessage());
                for (Throwable kept : ended.getSuppressed()) {
                    System.out.println("kept on it: " + kept.getMessage());
                }
                System.out.println("locked: " + lock.isLocked());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    static final class LedgerTimeout extends RuntimeException {
        LedgerTimeout(String message) { super(message); }
    }

    /** A report writer that keeps its rows, and fails on close when given an exception. */
    static final class Writer implements ReconciliationExport.ReportWriter {
        final List<String> rows = new ArrayList<>();
        final IOException onClose;
        boolean closed;

        Writer(IOException onClose) { this.onClose = onClose; }

        @Override public void write(String row) { rows.add(row); }

        @Override public void close() throws IOException {
            closed = true;
            if (onClose != null) throw onClose;
        }
    }
}
