package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
            long[] bytes = {120, 30, 40, 900, 20, 25, 35, 60};
            List<String> batches = Collections.synchronizedList(new ArrayList<>());
            List<String> cuts = Collections.synchronizedList(new ArrayList<>());
            ArchiveJob.Probe probe = new ArchiveJob.Probe() {
                @Override public void batch(int from, int to) { batches.add("[" + from + ", " + to + ")"); }
                @Override public void cut(int from, int mid, int to) { cuts.add("[" + from + ", " + to + ") at " + mid); }
            };
            ForkJoinPool pool = new ForkJoinPool(3);
            try {
                long compressed = new ArchiveJob(pool, bytes, 200, i -> bytes[i] / 4, probe).run();
                System.out.println("compressed size, a quarter of each file: " + compressed);
                Collections.sort(batches);
                Collections.sort(cuts);
                System.out.println("batches: " + batches);
                System.out.println("cuts: " + cuts);
            } finally {
                pool.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
