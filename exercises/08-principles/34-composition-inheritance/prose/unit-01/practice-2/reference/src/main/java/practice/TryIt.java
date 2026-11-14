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
            RecentLookups<String, String> geo = new RecentLookups<>(3);
            System.out.println("next to drop when empty: " + geo.nextToDrop());
            geo.put("10.0.4.1", "PT");
            geo.put("10.0.4.2", "AR");
            geo.put("10.0.4.3", "NZ");
            geo.get("10.0.4.1");
            System.out.println("next to drop after reading 10.0.4.1: " + geo.nextToDrop());
            geo.put("10.0.4.4", "IN");
            System.out.println("cached: " + geo.keySet());
            geo.putAll(java.util.Map.of("10.0.4.5", "EG", "10.0.4.6", "MX"));
            System.out.println("after warm-up of 2: " + geo.size() + " answers, next to drop " + geo.nextToDrop());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
