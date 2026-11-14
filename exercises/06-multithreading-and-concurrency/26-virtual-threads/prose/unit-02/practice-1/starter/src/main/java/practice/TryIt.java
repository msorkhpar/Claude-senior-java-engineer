package practice;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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
            AtomicInteger inProgress = new AtomicInteger();
            AtomicInteger peak = new AtomicInteger();
            StockClient.Inventory api = sku -> {
                peak.accumulateAndGet(inProgress.incrementAndGet(), Math::max);
                try {
                    Thread.sleep(30);
                    if (sku.startsWith("gone-")) throw new IllegalArgumentException("404 " + sku);
                    return sku.length() * 3;
                } finally {
                    inProgress.decrementAndGet();
                }
            };
            StockClient client = new StockClient(api, 3, Map.of("lamp-01", 75));
            System.out.println("levels: " + client.stockAll(List.of("lamp-01", "desk-200", "chair-7", "shelf-4",
                    "rug-90", "vase-3", "mirror-12", "stool-8")));
            System.out.println("never more than 3 calls at once: " + (peak.get() <= 3));
            for (String sku : List.of("gone-a", "gone-b", "gone-c", "gone-d")) {
                try {
                    client.stock(sku);
                } catch (IllegalArgumentException missing) {
                    System.out.println("lookup failed: " + missing.getMessage());
                }
            }
            System.out.println("a lookup after the failures: " + client.stock("sofa-66"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
