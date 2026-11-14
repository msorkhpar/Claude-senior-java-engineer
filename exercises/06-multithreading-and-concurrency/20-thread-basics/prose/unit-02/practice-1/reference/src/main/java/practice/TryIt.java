package practice;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
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
            Map<String, Integer> delayMs = Map.of("partsplus", 200, "gearbox", 300, "sprocketry", 400);
            QuoteRound round = new QuoteRound(supplier -> {
                try {
                    Thread.sleep(delayMs.get(supplier));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return 1_000 + supplier.length() * 10;
            });
            Map<String, Integer> prices = new ConcurrentHashMap<>();
            long started = System.nanoTime();
            round.askAll(List.of("partsplus", "gearbox", "sprocketry"), prices::put);
            long tookMs = (System.nanoTime() - started) / 1_000_000;
            System.out.println("prices when askAll returned: " + new TreeMap<>(prices));
            System.out.println("suppliers priced: " + prices.size() + " of 3");
            System.out.println("faster than asking one by one (900 ms): " + (tookMs < 850));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
