package practice;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
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
            Map<String, Rates.RateListener> waiting = new ConcurrentHashMap<>();
            Rates rates = new Rates((pair, listener) -> {
                if (!pair.matches("[A-Z]{3}/[A-Z]{3}")) throw new IllegalArgumentException("not a currency pair: " + pair);
                waiting.put(pair, listener);
            });
            CompletableFuture<BigDecimal> eurUsd = rates.rate("EUR/USD");
            CompletableFuture<BigDecimal> nokDkk = rates.rate("NOK/DKK");
            CompletableFuture<BigDecimal> bad = rates.rate("eur-usd");
            System.out.println("EUR/USD done before the client answers: " + eurUsd.isDone());
            Thread client = new Thread(() -> {
                waiting.get("EUR/USD").onRate("EUR/USD", new BigDecimal("1.0763"));
                waiting.get("NOK/DKK").onFailure("NOK/DKK", "feed stale");
            }, "fx-io");
            client.start();
            client.join();
            System.out.println("EUR/USD: " + eurUsd.orTimeout(2, TimeUnit.SECONDS).join());
            System.out.println("NOK/DKK: " + nokDkk.orTimeout(2, TimeUnit.SECONDS).handle((r, e) -> r != null ? r.toString() : "failed: " + e.getMessage()).join());
            System.out.println("eur-usd: " + bad.handle((r, e) -> r != null ? r.toString() : "failed: " + e.getMessage()).join());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
