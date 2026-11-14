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
            PriceBooks.LivePriceBook live = new PriceBooks.LivePriceBook();
            live.setPrice("desk-d3", 21_000);
            live.setPrice("chair-c7", 8_500);
            PriceBooks.FrozenPriceBook april = PriceBooks.FrozenPriceBook.of(live);
            new PriceBooks.Repricer(live).discount(10);
            System.out.println("live desk-d3: " + live.priceOf("desk-d3"));
            System.out.println("frozen desk-d3: " + april.priceOf("desk-d3"));
            System.out.println("frozen skus: " + new java.util.TreeSet<>(april.skus()));
            System.out.println("frozen chair-c7: " + april.priceOf("chair-c7"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
