package practice;

import java.math.BigDecimal;
import java.util.function.Supplier;
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
            int[] apiCalls = {0};
            RateService.RateApi api = pair -> {
                apiCalls[0]++;
                return pair.equals("EUR/USD") ? new BigDecimal("1.0842") : new BigDecimal("0.8571");
            };
            RateService quiet = new RateService(api, new PrintedLog(false));
            for (int i = 0; i < 3; i++) {
                quiet.rate("EUR/USD");
            }
            System.out.println("debug off, API calls for 3 lookups: " + apiCalls[0]);
            System.out.println("debug off, snapshots taken: " + quiet.snapshotsTaken);
            RateService loud = new RateService(api, new PrintedLog(true));
            System.out.println("EUR/GBP: " + loud.rate("EUR/GBP"));
            System.out.println("EUR/USD: " + loud.rate("EUR/USD"));
            System.out.println("API calls in all: " + apiCalls[0]);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** Prints debug lines when on, and ignores them when off. */
    record PrintedLog(boolean on) implements RateService.DebugLog {
        @Override
        public void debug(String message) {
            if (on) System.out.println("debug: " + message);
        }

        @Override
        public void debug(Supplier<String> message) {
            if (on) System.out.println("debug: " + message.get());
        }
    }
}
