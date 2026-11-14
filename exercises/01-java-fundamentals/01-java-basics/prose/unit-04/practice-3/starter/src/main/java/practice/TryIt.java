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
            long tenant = FeatureMask.of(2, 45);
            System.out.println("bit 45: " + FeatureMask.bit(45));
            System.out.println("has feature 13: " + FeatureMask.hasAll(tenant, FeatureMask.of(13)));
            System.out.println("has feature 45: " + FeatureMask.hasAll(tenant, FeatureMask.of(45)));
            long trialsOff = FeatureMask.disable(tenant, FeatureMask.of(2, 8));
            System.out.println("after trials off, has 2: " + FeatureMask.hasAll(trialsOff, FeatureMask.of(2))
                    + ", has 8: " + FeatureMask.hasAll(trialsOff, FeatureMask.of(8))
                    + ", has 45: " + FeatureMask.hasAll(trialsOff, FeatureMask.of(45)));
            System.out.println("has bundle {2, 45, 46}: " + FeatureMask.hasAll(tenant, FeatureMask.of(2, 45, 46)));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
