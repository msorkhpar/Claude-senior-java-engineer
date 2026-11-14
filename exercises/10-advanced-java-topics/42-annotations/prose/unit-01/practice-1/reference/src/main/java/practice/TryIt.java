package practice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
            List<Object> checkout = new ArrayList<>(List.of(30, 200, 1_500));
            Map<String, Object> raw = new LinkedHashMap<>();
            raw.put("/checkout", checkout);
            raw.put("/status", new ArrayList<>(List.of(600)));
            RateLimits limits = RateLimits.load(raw);
            System.out.println("/checkout tier 1: " + limits.perMinute("/checkout", 1));
            System.out.println("/checkout capacity: " + limits.capacity("/checkout"));
            checkout.set(0, "lots");
            System.out.println("/checkout tier 0 after the tree changed: " + limits.perMinute("/checkout", 0));
            Map<String, Object> quoted = new LinkedHashMap<>();
            quoted.put("/refunds", new ArrayList<>(List.of(10, "40")));
            try {
                RateLimits.load(quoted);
            } catch (IllegalArgumentException refused) {
                System.out.println("refused: " + refused.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
