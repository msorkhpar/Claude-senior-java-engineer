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
            DeliveryRules.Store store = new DeliveryRules.Store();
            store.block("spam.example.net");
            store.setMaxSizeKb("partner.example.org", 1_024);
            DeliveryRules.Rules inFlight = store.current();
            System.out.println("spam.example.net blocked: " + inFlight.isBlocked("spam.example.net"));
            System.out.println("partner.example.org max kb: " + inFlight.maxSizeKb("partner.example.org"));
            store.unblock("spam.example.net");
            store.setMaxSizeKb("partner.example.org", 256);
            System.out.println("after changes, rules read earlier: blocked " + inFlight.isBlocked("spam.example.net")
                    + ", max kb " + inFlight.maxSizeKb("partner.example.org"));
            DeliveryRules.Rules fresh = store.current();
            System.out.println("after changes, rules read now: blocked " + fresh.isBlocked("spam.example.net")
                    + ", max kb " + fresh.maxSizeKb("partner.example.org"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
