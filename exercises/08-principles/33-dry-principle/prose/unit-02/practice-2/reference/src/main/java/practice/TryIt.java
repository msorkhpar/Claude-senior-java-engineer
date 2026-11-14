package practice;

import java.util.Map;
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
            Warehouse w = new Warehouse(Map.of("P-02", new Warehouse.Shelf(40), "Q-08", new Warehouse.Shelf(5),
                    "R-19", new Warehouse.Shelf(12)));
            System.out.println("total before: " + w.total());
            System.out.println("move 15 P-02 -> Q-08: " + w.move("P-02", "Q-08", 15));
            System.out.println("move 30 R-19 -> P-02: " + w.move("R-19", "P-02", 30));
            System.out.println("P-02: " + w.count("P-02") + ", Q-08: " + w.count("Q-08") + ", R-19: " + w.count("R-19"));
            RuntimeException[] failed = {null};
            Thread mover = new Thread(() -> {
                try {
                    for (int i = 0; i < 2_000; i++) { w.move("Q-08", "R-19", 1); w.move("R-19", "Q-08", 1); }
                } catch (RuntimeException e) {
                    failed[0] = e;
                }
            });
            mover.start();
            int lowest = Integer.MAX_VALUE, highest = 0;
            do { int t = w.total(); lowest = Math.min(lowest, t); highest = Math.max(highest, t); } while (mover.isAlive());
            mover.join();
            if (failed[0] != null) throw failed[0];
            System.out.println("totals seen while moving: " + lowest + " to " + highest);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
