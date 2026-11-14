package practice;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;
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
            Instant early = Instant.parse("2026-08-21T06:05:30.250Z");
            Instant late = Instant.parse("2026-08-21T22:10:01.999Z");
            List<Date> gatewayFirst = List.of(new Date(late.toEpochMilli()), new Date(early.toEpochMilli()),
                    new Timestamp(late.toEpochMilli()), new Timestamp(early.toEpochMilli()));
            System.out.println("gateway first: " + LoginAudit.distinctMoments(gatewayFirst));
            List<Date> storeFirst = List.of(new Timestamp(early.toEpochMilli()), new Date(early.toEpochMilli()),
                    new Timestamp(late.toEpochMilli()), new Date(late.toEpochMilli()));
            System.out.println("session store first: " + LoginAudit.distinctMoments(storeFirst));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
