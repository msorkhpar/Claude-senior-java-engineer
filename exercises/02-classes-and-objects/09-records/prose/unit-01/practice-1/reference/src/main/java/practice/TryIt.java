package practice;

import java.util.HashSet;
import java.util.Set;
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
            int[] driverBuffer = {1840, 1852, 1849};
            Batch stored = new Batch("barn-4", driverBuffer);
            driverBuffer[0] = 9999;
            System.out.println("stored after the driver reused its buffer: " + stored);
            Batch resent = new Batch("barn-4", new int[] {1840, 1852, 1849});
            System.out.println("resent equals stored: " + stored.equals(resent));
            System.out.println("same hash code: " + (stored.hashCode() == resent.hashCode()));
            Set<Batch> gateway = new HashSet<>();
            gateway.add(stored);
            System.out.println("gateway stores the resent batch: " + gateway.add(resent));
            int[] read = stored.samples();
            read[1] = 0;
            System.out.println("stored after a reader changed its copy: " + stored);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
