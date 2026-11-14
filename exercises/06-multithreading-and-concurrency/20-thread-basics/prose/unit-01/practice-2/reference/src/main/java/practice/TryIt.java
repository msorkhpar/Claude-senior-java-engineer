package practice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
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
            List<String> askedOn = new CopyOnWriteArrayList<>();
            Map<String, Callable<QuoteBoard.Quote>> suppliers = new LinkedHashMap<>();
            suppliers.put("fastenco", () -> {
                askedOn.add(Thread.currentThread().getName());
                return new QuoteBoard.Quote("fastenco", 2_180);
            });
            suppliers.put("boltworks", () -> {
                askedOn.add(Thread.currentThread().getName());
                return new QuoteBoard.Quote("boltworks", 1_945);
            });
            suppliers.put("rivet-hub", () -> null);
            System.out.println("cheapest: " + QuoteBoard.cheapest(suppliers));
            System.out.println("asked on threads: " + askedOn.stream().sorted().toList());
            suppliers.put("rivet-hub", () -> {
                throw new IllegalStateException("HTTP 500 from api.example.com");
            });
            try {
                System.out.println("cheapest with a failing supplier: " + QuoteBoard.cheapest(suppliers));
            } catch (QuoteBoard.QuoteFailed failed) {
                System.out.println("check failed: " + failed.getMessage());
                System.out.println("cause: " + failed.getCause());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
