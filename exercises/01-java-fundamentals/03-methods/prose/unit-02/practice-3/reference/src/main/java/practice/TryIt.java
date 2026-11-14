package practice;

import java.math.BigDecimal;
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
            List<String> billed = new ArrayList<>();
            PriceBook book = new PriceBook(sku -> {
                billed.add(sku);
                return new BigDecimal("12.30");
            });
            book.list("CAM-77", new BigDecimal("199.00"));
            book.list("TRI-05", null);
            System.out.println("CAM-77 on file: " + book.price("CAM-77"));
            System.out.println("TRI-05 on file: " + book.price("TRI-05"));
            System.out.println("CAM-77 price or quote: " + book.priceOrQuote("CAM-77"));
            System.out.println("TRI-05 price or quote: " + book.priceOrQuote("TRI-05"));
            System.out.println("BAG-90 price or quote: " + book.priceOrQuote("BAG-90"));
            System.out.println("quote requests billed: " + billed);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
