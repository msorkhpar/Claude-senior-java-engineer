package practice;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.Map;
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
            Instant[] now = {Instant.parse("2026-09-02T09:30:00Z")};
            int[] calls = {0};
            ExchangeRates.Treasury treasury = () -> {
                calls[0]++;
                return Map.of("CHF", new BigDecimal(calls[0] == 1 ? "0.9415" : "0.9388"),
                        "SEK", new BigDecimal("11.462"));
            };
            InstantSource clock = () -> now[0];
            ExchangeRates rates = new ExchangeRates(treasury, clock);
            System.out.println("09:30 CHF: " + rates.rate("CHF"));
            now[0] = now[0].plus(Duration.ofMinutes(6));
            System.out.println("09:36 SEK: " + rates.rate("SEK"));
            System.out.println("09:36 CHF: " + rates.rate("CHF"));
            now[0] = now[0].plus(Duration.ofMinutes(5));
            System.out.println("09:41 CHF: " + rates.rate("CHF"));
            System.out.println("treasury calls: " + calls[0]);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
