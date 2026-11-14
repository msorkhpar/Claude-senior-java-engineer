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
            Dispatcher console = new Dispatcher(new Lockers());
            System.out.println("assign: " + console.call("assign", "LK-40", 3));
            System.out.println("assign zero: " + console.call("assign", "LK-40", 0));
            System.out.println("assign three as a word: " + console.call("assign", "LK-40", "three"));
            System.out.println("open: " + console.call("open", "LK-40"));
            System.out.println("free: " + console.call("free", "B"));
            System.out.println("unknown: " + console.call("explode"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    public static class Lockers {
        public String assign(String locker, int parcels) {
            if (parcels <= 0) throw new IllegalArgumentException("parcels must be positive, was " + parcels);
            return parcels + " parcels in " + locker;
        }
        public int free(String bank) { return bank.charAt(0) - 'A' + 4; }
        public void open(String locker) throws java.io.IOException {
            throw new java.io.IOException("door controller of " + locker + " offline");
        }
    }
}
