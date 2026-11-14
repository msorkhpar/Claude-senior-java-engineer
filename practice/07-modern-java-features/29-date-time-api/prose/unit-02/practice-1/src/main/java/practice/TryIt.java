package practice;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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
            Billing billing = new Billing(Clock.fixed(Instant.parse("2030-09-10T22:45:00Z"), ZoneId.of("Europe/Berlin")));
            ZoneId losAngeles = ZoneId.of("America/Los_Angeles");
            ZoneId tokyo = ZoneId.of("Asia/Tokyo");
            System.out.println("invoice date, paid 2030-09-10T03:30Z in Los Angeles: " + billing.invoiceDate(Instant.parse("2030-09-10T03:30:00Z"), losAngeles));
            System.out.println("Tokyo's 2030-09-10 starts at: " + billing.startOf(LocalDate.of(2030, 9, 10), tokyo));
            System.out.println("today in Los Angeles: " + billing.today(losAngeles));
            System.out.println("today in Tokyo: " + billing.today(tokyo));
            System.out.println("Tokyo statement for 2030-09-10: " + billing.statement(LocalDate.of(2030, 9, 10), tokyo, List.of(
                    Instant.parse("2030-09-09T14:00:00Z"), Instant.parse("2030-09-09T16:30:00Z"),
                    Instant.parse("2030-09-10T14:59:00Z"), Instant.parse("2030-09-10T15:00:00Z"))));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
