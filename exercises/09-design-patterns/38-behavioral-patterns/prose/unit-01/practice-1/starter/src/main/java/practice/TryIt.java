package practice;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
            Map<String, Long> rates = Map.of("east", 280L, "west", 350L, "island", 600L);
            ShippingQuotes.ByWeight policy = new ShippingQuotes.ByWeight(zone -> {
                if (zone.equals("east")) {
                    try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
                return rates.get(zone);
            });
            ExecutorService pool = Executors.newFixedThreadPool(3);
            try {
                Future<Long> east = pool.submit(() -> policy.quoteCents(new ShippingQuotes.Parcel("q-1", 5_300, "east")));
                Future<Long> island = pool.submit(() -> policy.quoteCents(new ShippingQuotes.Parcel("q-2", 800, "island")));
                Future<Long> west = pool.submit(() -> policy.quoteCents(new ShippingQuotes.Parcel("q-3", 2_000, "west")));
                System.out.println("q-1, 6 kg east: " + result(east));
                System.out.println("q-2, 1 kg island: " + result(island));
                System.out.println("q-3, 2 kg west: " + result(west));
            } finally {
                pool.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** What the quote returned, or the exception it threw on its pool thread. */
    private static long result(Future<Long> quote) throws InterruptedException {
        try {
            return quote.get();
        } catch (java.util.concurrent.ExecutionException e) {
            if (e.getCause() instanceof RuntimeException failure) throw failure;
            throw new IllegalStateException(e.getCause());
        }
    }
}
