package practice;

import java.time.Instant;
import java.time.ZoneId;
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
            Renewals.Subscription lima = new Renewals.Subscription("cust-4410", ZoneId.of("America/Lima"), Instant.parse("2026-01-31T02:00:00Z"));
            Renewals.Subscription tokyo = new Renewals.Subscription("cust-5521", ZoneId.of("Asia/Tokyo"), Instant.parse("2026-03-14T12:00:00Z"));
            System.out.println("cust-4410 renews at: " + Renewals.nextRenewal(lima));
            System.out.println(Renewals.receiptLine(lima));
            System.out.println("cust-5521 renews at: " + Renewals.nextRenewal(tokyo));
            System.out.println(Renewals.receiptLine(tokyo));
            System.out.println("due by 2026-04-14T00:00Z: " + Renewals.dueBy(List.of(lima, tokyo), Instant.parse("2026-04-14T00:00:00Z")));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
