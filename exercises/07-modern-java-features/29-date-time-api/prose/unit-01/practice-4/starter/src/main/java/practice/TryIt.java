package practice;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
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
            TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
            Date evening = Date.from(Instant.parse("2026-01-15T22:10:00Z"));
            System.out.println("22:10 UTC on Jan 15 counts on: " + SettlementReport.businessDay(evening));
            System.out.println("settlement day read with getDate: " + SettlementReport.businessDay(java.sql.Date.valueOf("2026-01-14")));
            System.out.println("totals: " + SettlementReport.totals(List.of(
                    new SettlementReport.Payment(Date.from(Instant.parse("2026-01-15T02:00:00Z")), 3_400),
                    new SettlementReport.Payment(evening, 700),
                    new SettlementReport.Payment(Date.from(Instant.parse("2026-01-16T05:00:00Z")), 1_100))));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
