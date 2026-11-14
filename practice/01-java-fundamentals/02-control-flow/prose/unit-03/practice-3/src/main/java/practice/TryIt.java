package practice;

import java.util.ArrayList;
import java.util.List;
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
            System.out.println("shard 300..304: " + ids(300, 304));
            System.out.println("empty shard 80..79: " + ids(80, 79));
            int top = Integer.MAX_VALUE;
            System.out.println("last shard: " + ids(top - 2, top));
            System.out.println("checked in 1000..1009: " + InvoiceSweep.sweep(new InvoiceSweep.Shard(1_000, 1_009), id -> { }));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** The ids forEachId hands over, stopping the run if it hands over more than 50. */
    private static List<Integer> ids(int first, int last) {
        List<Integer> seen = new ArrayList<>();
        InvoiceSweep.forEachId(first, last, id -> {
            if (seen.size() == 50) throw new IllegalStateException("more than 50 ids, last " + id);
            seen.add(id);
        });
        return seen;
    }
}
