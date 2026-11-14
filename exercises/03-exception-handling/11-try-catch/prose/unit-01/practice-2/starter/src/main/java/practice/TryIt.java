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
            RateClient.Source mirror = pair -> new BigDecimal("10.87");
            RateClient healthy = new RateClient(pair -> new BigDecimal("10.86"), mirror);
            System.out.println("EUR-SEK from the primary: " + healthy.rate("EUR-SEK"));
            RateClient degraded = new RateClient(pair -> { throw new IOException("502 Bad Gateway"); }, mirror);
            System.out.println("EUR-SEK from the mirror: " + degraded.rate("EUR-SEK"));
            RateClient down = new RateClient(pair -> { throw new IOException("502 Bad Gateway"); },
                    pair -> { throw new IOException("TLS handshake failed"); });
            try {
                down.rate("USD-PLN");
            } catch (RateClient.RatesUnavailableException unavailable) {
                System.out.println("checkout gets: " + unavailable.getMessage());
                System.out.println("cause: " + unavailable.getCause());
                for (Throwable suppressed : unavailable.getSuppressed()) {
                    System.out.println("suppressed: " + suppressed);
                }
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
