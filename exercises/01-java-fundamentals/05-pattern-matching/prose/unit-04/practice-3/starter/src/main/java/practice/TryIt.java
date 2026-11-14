package practice;

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
            Map<String, Object> parsed = new LinkedHashMap<>();
            parsed.put("zone", "ap-south");
            parsed.put("workers", 12);
            parsed.put("gzip", true);
            parsed.put("timeout", null);
            parsed.put("hosts", List.of("x.example.net", "y.example.net", 8080));
            Settings settings = new Settings();
            settings.load(parsed);
            System.out.println("values: " + settings.values());
            System.out.println("unset: " + settings.unset());
            Map<String, Object> wrong = new LinkedHashMap<>();
            wrong.put("zone", "ap-south");
            wrong.put("factor", 1.5);
            try {
                new Settings().load(wrong);
                System.out.println("factor 1.5: loaded");
            } catch (IllegalArgumentException refused) {
                System.out.println("factor 1.5: refused, " + refused.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
