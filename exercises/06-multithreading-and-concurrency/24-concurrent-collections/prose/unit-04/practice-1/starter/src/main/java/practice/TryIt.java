package practice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
            Map<String, Long> prices = CatalogMaps.newPriceIndex(300);
            ConcurrentHashMap<String, Integer> stock = CatalogMaps.newStockCache(300);
            for (int i = 0; i < 300; i++) {
                prices.put("ART-" + (500 + i), 250L + i);
                stock.put("ART-" + (500 + i), i % 7);
            }
            System.out.println("price index rows: " + prices.size() + ", ART-799 costs " + prices.get("ART-799"));
            System.out.println("stock cache rows: " + stock.size() + ", ART-510 in stock " + stock.get("ART-510"));
            CatalogMaps.RecentLookups recent = new CatalogMaps.RecentLookups(3);
            recent.put("ART-501", 251);
            recent.put("ART-502", 252);
            recent.put("ART-503", 253);
            recent.get("ART-501");
            recent.put("ART-504", 254);
            System.out.println("recent size: " + recent.size());
            System.out.println("ART-501 (read again) kept: " + recent.contains("ART-501"));
            System.out.println("ART-502 (least recently used) kept: " + recent.contains("ART-502"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
