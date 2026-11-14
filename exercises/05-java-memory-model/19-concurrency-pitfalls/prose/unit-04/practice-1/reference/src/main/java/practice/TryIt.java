package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
            ExecutorService desk = Executors.newSingleThreadExecutor(r -> new Thread(r, "desk-loop"));
            try {
                ThreadNamingBook book = new ThreadNamingBook();
                FxDesk fx = new FxDesk(desk, book);
                fx.onQuote("EUR/USD", CompletableFuture.completedFuture(1_084_500L));
                CompletableFuture<Long> later = new CompletableFuture<>();
                fx.onQuote("USD/SEK", later);
                Thread adapter = new Thread(() -> later.complete(10_512_300L), "adapter-3");
                adapter.start();
                adapter.join(5_000);
                System.out.println("EUR/USD: " + fx.rate("EUR/USD").get(5, TimeUnit.SECONDS));
                System.out.println("USD/SEK: " + fx.rate("USD/SEK").get(5, TimeUnit.SECONDS));
                System.out.println("GBP/PLN: " + fx.rate("GBP/PLN").get(5, TimeUnit.SECONDS));
                book.calls.forEach(c -> System.out.println("book call: " + c));
            } finally {
                desk.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    static final class ThreadNamingBook implements FxDesk.Book {
        final Map<String, Long> rates = new HashMap<>();
        final List<String> calls = Collections.synchronizedList(new ArrayList<>());
        @Override public void apply(String pair, long micros) {
            calls.add("apply " + pair + " on " + Thread.currentThread().getName());
            rates.put(pair, micros);
        }
        @Override public Long rate(String pair) {
            calls.add("rate " + pair + " on " + Thread.currentThread().getName());
            return rates.get(pair);
        }
    }
}
