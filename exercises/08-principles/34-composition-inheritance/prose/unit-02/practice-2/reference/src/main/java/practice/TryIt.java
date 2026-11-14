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
            int[] opened = {0};
            TenantPools pools = new TenantPools(tenant -> {
                opened[0]++;
                return new TenantPools.Pool(tenant);
            });
            TenantPools.Pool alpine = pools.poolFor("tenant-alpine");
            System.out.println("same pool again: " + (pools.poolFor("tenant-alpine") == alpine));
            System.out.println("coastal pool tenant: " + pools.poolFor("tenant-coastal").tenant());
            System.out.println("discard alpine: " + pools.discard("tenant-alpine", alpine));
            TenantPools.Pool fresh = pools.poolFor("tenant-alpine");
            System.out.println("discard old alpine again: " + pools.discard("tenant-alpine", alpine));
            System.out.println("fresh pool kept: " + (pools.poolFor("tenant-alpine") == fresh));
            System.out.println("pools opened: " + opened[0] + ", held: " + pools.size());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
