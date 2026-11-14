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
            ConcurrentHashMap<String, Warehouse.Bins> stock = new ConcurrentHashMap<>();
            stock.put("tape-50m", new Warehouse.Bins(Map.of("A1-01", 10, "A1-02", 2)));
            Warehouse warehouse = new Warehouse(stock);
            System.out.println("move 6 A1-01 -> A1-02: " + warehouse.move("tape-50m", "A1-01", "A1-02", 6));
            System.out.println("move 5 A1-01 -> Z9-99: " + warehouse.move("tape-50m", "A1-01", "Z9-99", 5));
            System.out.println("move 1 of an unknown sku: " + warehouse.move("rope-10m", "A1-01", "A1-02", 1));
            System.out.println("A1-01: " + warehouse.unitsIn("tape-50m", "A1-01") + ", A1-02: "
                    + warehouse.unitsIn("tape-50m", "A1-02") + ", Z9-99: " + warehouse.unitsIn("tape-50m", "Z9-99"));
            System.out.println("tape-50m total: " + warehouse.units("tape-50m"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
