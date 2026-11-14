package practice;

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
            Warehouse warehouse = new Warehouse();
            Warehouse.Bin rack = new Warehouse.Bin("R-04-2", 60);
            Warehouse.Bin packing = new Warehouse.Bin("P-01-1", 10);
            System.out.println("move 25 rack -> packing: " + warehouse.move(rack, packing, 25));
            System.out.println("move 50 packing -> rack: " + warehouse.move(packing, rack, 50));
            System.out.println("rack " + rack.quantity() + ", packing " + packing.quantity()
                    + ", count both: " + warehouse.count(packing, rack));
            Thread pickers = new Thread(() -> { for (int i = 0; i < 2_000; i++) warehouse.move(rack, packing, 1); }, "pickers");
            Thread rebalancer = new Thread(() -> { for (int i = 0; i < 2_000; i++) warehouse.move(packing, rack, 1); }, "rebalancer");
            pickers.setDaemon(true);
            rebalancer.setDaemon(true);
            pickers.start();
            rebalancer.start();
            pickers.join(5_000);
            rebalancer.join(5_000);
            System.out.println("opposite moves both finished: " + !(pickers.isAlive() || rebalancer.isAlive()));
            System.out.println("units in both bins: " + warehouse.count(rack, packing));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
