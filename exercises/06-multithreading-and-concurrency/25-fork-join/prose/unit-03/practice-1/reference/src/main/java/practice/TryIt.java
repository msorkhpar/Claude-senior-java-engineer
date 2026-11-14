package practice;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
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
            ForkJoinPool pool = new ForkJoinPool(3);
            try {
                StockValuation valuation = new StockValuation(pool, 2);
                int[] quantities = {10, 0, 3, 7, 0, 2};
                System.out.println("value at (sku + 1) x 10.00: " + valuation.value(quantities, sku -> (sku + 1) * 1_000L));
                System.out.println("asked again: " + valuation.value(quantities, sku -> (sku + 1) * 1_000L));
                System.out.println("empty warehouse: " + valuation.value(new int[0], sku -> 500L));
                int[] bulk = new int[50];
                Arrays.fill(bulk, 3_000_000);
                System.out.println("50 SKUs of 3,000,000 at 999.99, cents: " + valuation.value(bulk, sku -> 99_999L).valueCents());
            } finally {
                pool.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
