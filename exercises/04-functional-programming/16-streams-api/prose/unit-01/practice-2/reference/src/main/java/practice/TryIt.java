package practice;

import java.util.ArrayList;
import java.util.List;
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
            List<UnpaidInvoices.Invoice> ledger = new ArrayList<>(List.of(
                    new UnpaidInvoices.Invoice("B-1", 2_000, false),
                    new UnpaidInvoices.Invoice("B-2", 9_900, true),
                    new UnpaidInvoices.Invoice("B-3", 350, false)));
            UnpaidInvoices panel = new UnpaidInvoices(ledger);
            System.out.println("refresh 1: " + panel.count() + " unpaid, " + panel.totalCents() + " cents, " + panel.ids());
            ledger.add(new UnpaidInvoices.Invoice("B-4", 1_250, false));
            ledger.add(new UnpaidInvoices.Invoice("B-5", 400, true));
            System.out.println("refresh 2: " + panel.count() + " unpaid, " + panel.totalCents() + " cents, " + panel.ids());
            System.out.println("refresh 3, count again: " + panel.count());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
