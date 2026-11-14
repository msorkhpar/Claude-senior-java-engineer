package practice;

import java.util.HashMap;
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
            int boxes = 7;
            char size = 'M';
            Map<String, Object> row = new HashMap<>();
            row.put("boxes", 7);
            row.put("size", 'M');
            row.put("express", false);
            row.put("comment", null);
            System.out.println("typed int: " + JsonValue.of(boxes) + ", row int: " + JsonValue.of(row.get("boxes")));
            System.out.println("typed char: " + JsonValue.of(size) + ", row char: " + JsonValue.of(row.get("size")));
            System.out.println("row boolean: " + JsonValue.of(row.get("express")));
            System.out.println("row missing: " + JsonValue.of(row.get("comment")));
            System.out.println("text: " + JsonValue.of("12\" ruler"));
            System.out.println("decimal: " + JsonValue.of(1.75));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
