package practice;

import java.util.concurrent.ForkJoinPool;
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
            ForkJoinPool pool = new ForkJoinPool(3);
            try {
                BestSeller seller = new BestSeller(pool, 2);
                long[] ids = {9_001, 9_002, 9_003, 9_002, 9_001, 9_003, 9_004};
                int[] units = {5, 4, 2, 3, 2, 5, 6};
                System.out.println("tie at 7 units: " + seller.top(ids, units));
                long[] spread = {7_310, 7_200, 7_420, 7_200, 7_530, 7_200, 7_640, 7_200};
                int[] spreadUnits = {9, 4, 8, 4, 7, 4, 6, 4};
                System.out.println("second on every line, first on the day: " + seller.top(spread, spreadUnits));
                System.out.println("one line: " + seller.top(new long[] {4_444}, new int[] {12}));
            } finally {
                pool.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
