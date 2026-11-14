package practice;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
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
            Set<Thread> lookupThreads = ConcurrentHashMap.newKeySet();
            PriceRefresh refresh = new PriceRefresh(sku -> {
                lookupThreads.add(Thread.currentThread());
                return 450L + sku.length() * 100L;
            }, 4);
            var prices = refresh.refresh(List.of("BOWL-12", "SPOON-7", "FORK-5", "PLATE-330", "KNIFE-9"));
            System.out.println("prices: " + new TreeMap<>(prices));
            System.out.println("at most 4 lookup threads: " + (lookupThreads.size() <= 4));
            System.out.println("all on the refresh's own pool: " + lookupThreads.stream().allMatch(t ->
                    t instanceof ForkJoinWorkerThread w && w.getPool() != ForkJoinPool.commonPool()));
            System.out.println("empty list: " + refresh.refresh(List.of()));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
