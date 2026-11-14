package practice;

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
            TenantStorage storage = new TenantStorage(5);
            TenantStorage.Placement small = storage.place("carts/eu-west/77", 4, 250);
            System.out.println("small tenant shard: " + small.shard());
            System.out.println("small tenant quota bytes: " + small.quotaBytes());
            TenantStorage.Placement large = storage.place("ledger/q3", 4_000, 1_024);
            System.out.println("4000-user tenant quota bytes: " + large.quotaBytes());
            System.out.println("same key, same shard: " + (storage.place("ledger/q3", 4_000, 1_024).shard() == large.shard()));
            System.out.println("shard of \"GydZG_\": " + storage.shardFor("GydZG_"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
