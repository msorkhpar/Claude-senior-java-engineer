package practice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
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
            CountingStore store = new CountingStore();
            store.put("SKU-3100", 2_450L);
            Long previous = store.put("SKU-3100", 2_300L);
            System.out.println("overwritten price: " + previous);
            Map<String, Long> batch = new LinkedHashMap<>();
            batch.put("SKU-8800", 640L);
            batch.put("SKU-1200", 15_000L);
            batch.put("SKU-5500", 99L);
            store.putAll(batch);
            System.out.println("after an unsorted batch, writes: " + store.writes());
            CountingStore nightly = new CountingStore();
            nightly.putAll(new TreeMap<>(store));
            System.out.println("nightly reload, writes: " + nightly.writes());
            System.out.println("prices: " + nightly);
            System.out.println("first sku: " + nightly.firstKey());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
