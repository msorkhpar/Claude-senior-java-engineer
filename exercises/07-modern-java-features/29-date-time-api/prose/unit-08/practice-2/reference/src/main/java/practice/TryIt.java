package practice;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
            Instant scanned = Instant.parse("2026-05-06T22:40:00Z");
            ScanTimeColumn tokyo = new ScanTimeColumn(ZoneId.of("Asia/Tokyo"));
            ScanTimeColumn denver = new ScanTimeColumn(ZoneId.of("America/Denver"));
            System.out.println("Tokyo column shows: " + tokyo.toColumn(scanned).toLocalDateTime());
            System.out.println("Denver column shows: " + denver.toColumn(scanned).toLocalDateTime());
            Timestamp row = Timestamp.valueOf(LocalDateTime.parse("2026-05-07T08:15:00"));
            System.out.println("Tokyo row 2026-05-07 08:15 was scanned at: " + tokyo.fromColumn(row));
            System.out.println("Denver row 2026-05-07 08:15 was scanned at: " + denver.fromColumn(row));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
