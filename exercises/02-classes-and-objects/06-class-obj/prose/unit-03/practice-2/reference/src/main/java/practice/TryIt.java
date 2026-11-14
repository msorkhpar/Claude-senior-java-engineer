package practice;

import java.io.IOException;
import java.io.UncheckedIOException;
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
            AtomicInteger loads = new AtomicInteger();
            RateCard.Source.rates = () -> {
                if (loads.incrementAndGet() == 1) {
                    throw new UncheckedIOException(new IOException("rate service busy"));
                }
                return Map.of("USD", 108L, "JPY", 16_250L);
            };
            System.out.println("health check: " + RateCard.version());
            try {
                System.out.println("USD: " + RateCard.centsPerEuro("USD"));
            } catch (UncheckedIOException failed) {
                System.out.println("first request: failed, " + failed.getCause().getMessage());
            }
            System.out.println("USD: " + RateCard.centsPerEuro("USD"));
            System.out.println("JPY: " + RateCard.centsPerEuro("JPY"));
            System.out.println("loads asked of the rate service: " + loads.get());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
