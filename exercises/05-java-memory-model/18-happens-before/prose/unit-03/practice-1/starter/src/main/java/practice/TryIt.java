package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.LongConsumer;
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
            DemoFeed feed = new DemoFeed();
            List<String> alerts = new ArrayList<>();
            feed.trade("ORBX", 512_30);
            PriceWatchers.PriceWatcher ceiling = new PriceWatchers.Ceiling("ORBX", 500_00, alerts::add, feed);
            PriceWatchers.PriceWatcher floor = new Floor("TAVN", 18_00, alerts::add, feed);
            feed.trade("TAVN", 17_40);
            System.out.println("alerts before start: " + alerts.size());
            ceiling.start();
            floor.start();
            feed.trade("ORBX", 498_00);
            feed.trade("TAVN", 16_95);
            System.out.println("alerts after start: " + alerts.size());
            alerts.forEach(a -> System.out.println("alert: " + a));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    static final class DemoFeed implements PriceWatchers.PriceFeed {
        private final Map<String, Long> last = new ConcurrentHashMap<>();
        private final Map<String, List<LongConsumer>> listeners = new ConcurrentHashMap<>();
        @Override public void subscribe(String symbol, LongConsumer listener) {
            listeners.computeIfAbsent(symbol, s -> new CopyOnWriteArrayList<>()).add(listener);
            Long price = last.get(symbol);
            if (price != null) listener.accept(price);
        }
        void trade(String symbol, long cents) {
            last.put(symbol, cents);
            listeners.getOrDefault(symbol, List.of()).forEach(l -> l.accept(cents));
        }
    }

    static final class Floor extends PriceWatchers.PriceWatcher {
        private final long floorCents;
        Floor(String symbol, long floorCents, PriceWatchers.Alerts alerts, PriceWatchers.PriceFeed feed) {
            super(symbol, alerts, feed);
            this.floorCents = floorCents;
        }
        @Override protected void onPrice(long cents) {
            if (cents < floorCents) alert(symbol() + " at " + cents + " is below " + floorCents);
        }
    }
}
