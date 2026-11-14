package practice;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
            Books ok = new Books();
            ok.posting().post("INV-5001", 64_000);
            System.out.println("posted: " + ok.calls);
            Books full = new Books();
            full.archiveFails = new IOException("archive disk full");
            try {
                full.posting().post("INV-5002", 9_900);
            } catch (IOException failed) {
                System.out.println("archive failed: " + failed.getMessage() + ", calls " + full.calls);
            }
            Books conflict = new Books();
            conflict.ledgerFails = new SQLException("could not serialize access", "40001");
            try {
                conflict.posting().post("INV-5003", 1_200);
            } catch (SQLException failed) {
                System.out.println("ledger conflict, SQLState " + failed.getSQLState() + ", calls " + conflict.calls);
            }
            Books bug = new Books();
            bug.ledgerFails = new IllegalArgumentException("currency XYZ is not enabled");
            try {
                bug.posting().post("INV-5004", 500);
            } catch (IllegalArgumentException failed) {
                System.out.println("ledger bug: " + failed.getMessage() + ", calls " + bug.calls);
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** A ledger, an archive and a transaction that record every call. */
    static final class Books implements InvoicePosting.Ledger, InvoicePosting.Archive, InvoicePosting.Transaction {
        final List<String> calls = new ArrayList<>();
        Exception ledgerFails;
        IOException archiveFails;

        @Override public void insert(String invoiceId, long cents) throws SQLException {
            calls.add("insert");
            if (ledgerFails instanceof SQLException e) throw e;
            if (ledgerFails instanceof RuntimeException e) throw e;
        }

        @Override public void store(String invoiceId) throws IOException {
            calls.add("store");
            if (archiveFails != null) throw archiveFails;
        }

        @Override public void begin() { calls.add("begin"); }

        @Override public void commit() { calls.add("commit"); }

        @Override public void rollback() { calls.add("rollback"); }

        InvoicePosting posting() { return new InvoicePosting(this, this, this); }
    }
}
