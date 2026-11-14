package practice;

import java.time.Instant;
import java.util.Date;
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
            Date scanned = Date.from(Instant.parse("2026-03-10T09:15:00Z"));
            Shipment first = new Shipment("PKG-1001", scanned);
            scanned.setTime(Instant.parse("2026-03-10T11:40:00Z").toEpochMilli());
            Shipment second = new Shipment("PKG-1002", scanned);
            System.out.println(first.trackingId() + " dispatched: " + first.dispatchedAt().toInstant());
            System.out.println(second.trackingId() + " dispatched: " + second.dispatchedAt().toInstant());
            Date shown = first.dispatchedAt();
            shown.setTime(shown.getTime() + 3 * 60 * 60 * 1000);
            System.out.println(first.trackingId() + " after a report shifted its copy: " + first.dispatchedAt().toInstant());
            System.out.println(first.trackingId() + " before 10:00Z: " + first.dispatchedBefore(Date.from(Instant.parse("2026-03-10T10:00:00Z"))));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
