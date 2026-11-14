package practice;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) {
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
            Migrations.SchemaHistory history = new Migrations.SchemaHistory();
            new Migrations.AddEmailColumn().apply(history);
            new CreateInvoiceTable().apply(history);
            new Migrations.AddEmailColumn().apply(history);
            System.out.println("statements run: " + history.statements());
            System.out.println("applied: " + history.appliedVersions());
            try {
                new DropLegacyColumn().apply(history);
            } catch (IllegalArgumentException failed) {
                System.out.println("V22 failed: " + failed.getMessage());
            }
            System.out.println("applied after the failure: " + history.appliedVersions());
            System.out.println("history locked: " + history.isLocked());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    static class CreateInvoiceTable extends Migrations.Migration {
        CreateInvoiceTable() { super("V21"); }

        @Override public void migrate(Migrations.SchemaHistory history) {
            history.execute("CREATE TABLE invoice (id BIGINT PRIMARY KEY)");
        }
    }

    static class DropLegacyColumn extends Migrations.Migration {
        DropLegacyColumn() { super("V22"); }

        @Override public void migrate(Migrations.SchemaHistory history) {
            throw new IllegalArgumentException("column fax does not exist");
        }
    }
}
