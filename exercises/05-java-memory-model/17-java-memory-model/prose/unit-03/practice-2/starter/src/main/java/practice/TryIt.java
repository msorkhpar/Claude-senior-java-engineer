package practice;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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
            AtomicInteger serviceCalls = new AtomicInteger();
            TaxTables tables = new TaxTables(() -> new TaxTables.Rates("2026-Q" + serviceCalls.incrementAndGet(),
                    Map.of("AT", 2_000, "IT", 2_200, "NL", 2_100)));
            System.out.println("tax service calls before checkout: " + serviceCalls.get());
            TaxTables.Rates first = tables.rates();
            TaxTables.Rates second = tables.rates();
            System.out.println("version: " + first.version());
            System.out.println("IT basis points: " + first.basisPoints("IT"));
            System.out.println("same tables both times: " + (first == second));
            System.out.println("tax service calls after two checkouts: " + serviceCalls.get());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
