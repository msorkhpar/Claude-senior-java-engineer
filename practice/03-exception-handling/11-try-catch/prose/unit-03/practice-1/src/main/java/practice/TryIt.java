package practice;

import java.io.IOException;
import java.math.BigDecimal;
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
            int[] fetches = {0};
            QuoteService service = new QuoteService(sku -> {
                fetches[0]++;
                if (sku.startsWith("DOWN")) {
                    throw new IOException("pricing backend: 503");
                }
                return new BigDecimal("18.75");
            }, 2);
            System.out.println("quote LMP-12: " + service.quote("LMP-12"));
            System.out.println("quote LMP-12 again: " + service.quote("LMP-12") + ", backend fetches: " + fetches[0]);
            for (int i = 1; i <= 3; i++) {
                try {
                    service.quote("DOWN-" + i);
                } catch (IOException failed) {
                    System.out.println("quote DOWN-" + i + ": " + failed.getMessage() + ", in flight after: " + service.inFlight());
                }
            }
            System.out.println("quote CUP-3 after the outage: " + service.quote("CUP-3"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
