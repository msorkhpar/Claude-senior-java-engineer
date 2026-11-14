package practice;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
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
            AuditReconciler reconciler = new AuditReconciler();
            Instant precise = Instant.parse("2026-02-11T10:20:30.123456789Z");
            System.out.println("recorded: " + precise);
            System.out.println("as the library stores it: " + reconciler.asStored(precise));
            System.out.println("untouched copy matches: " + reconciler.matches(precise, Date.from(precise)));
            Map<String, Instant> recorded = new LinkedHashMap<>();
            recorded.put("txn-81", precise);
            recorded.put("txn-82", Instant.parse("2026-02-11T10:21:00.400Z"));
            recorded.put("txn-83", Instant.parse("2026-02-11T10:22:15.000777Z"));
            Map<String, Date> audited = new LinkedHashMap<>();
            audited.put("txn-81", Date.from(precise));
            audited.put("txn-82", Date.from(Instant.parse("2026-02-11T10:21:00.401Z")));
            System.out.println("changed or missing: " + reconciler.changed(recorded, audited));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
