package practice;

import java.util.concurrent.ForkJoinPool;
import java.util.function.ToLongFunction;
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
            ToLongFunction<String> cents = row -> {
                if (!row.matches("-?\\d{1,9}\\.\\d\\d")) throw new NumberFormatException("not an amount: " + row);
                return Long.parseLong(row.replace(".", ""));
            };
            ForkJoinPool pool = new ForkJoinPool(3);
            try {
                LedgerImport ledger = new LedgerImport(pool, 2, cents);
                String[] good = {"120.00", "-15.50", "3.25", "980.10", "0.15"};
                System.out.println("good file, cents: " + ledger.total(good));
                String[] bad = {"40.00", "12.00", "7.70", "2.000,00", "1.00", "6.30"};
                try {
                    ledger.total(bad);
                } catch (IllegalArgumentException rejected) {
                    System.out.println("rejected: " + rejected.getMessage());
                }
                System.out.println("good file again, cents: " + ledger.total(good));
            } finally {
                pool.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
