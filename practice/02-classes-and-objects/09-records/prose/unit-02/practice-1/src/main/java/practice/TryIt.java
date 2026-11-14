package practice;

import java.util.HashMap;
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
            RateCache cache = new RateCache();
            Map<String, Integer> buffer = new HashMap<>();
            buffer.put("box-1", 3_100);
            buffer.put("box-2", 420);
            RateCache.Shipment shipment = new RateCache.Shipment("NPX", buffer);
            cache.put(shipment, 2_675L);
            buffer.put("box-3", 900);
            System.out.println("parcels after the client reused its map: " + shipment.parcels());
            System.out.println("rate found again: " + cache.rateFor(shipment));
            RateCache.Shipment bigger = shipment.withParcel("box-3", 900);
            System.out.println("with one more parcel: " + bigger.parcels().size() + " parcels, rate " + cache.rateFor(bigger));
            System.out.println("original still has: " + shipment.parcels().size() + " parcels");
            try {
                shipment.parcels().clear();
            } catch (UnsupportedOperationException refused) {
                System.out.println("clearing the returned map: refused");
            }
            System.out.println("cache size: " + cache.size());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
