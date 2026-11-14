package practice;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.Locale;
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
            DispatchAdapter adapter = new DispatchAdapter((requested, depot) -> {
                GregorianCalendar confirmed = new GregorianCalendar(depot, Locale.ROOT);
                confirmed.setTimeInMillis(requested.getTimeInMillis() + 20 * 60 * 1000);
                return confirmed;
            });
            ZonedDateTime pickup = ZonedDateTime.of(2026, 4, 14, 16, 0, 0, 0, ZoneId.of("Europe/Rome"));
            GregorianCalendar handed = adapter.toLibrary(pickup);
            System.out.println("library gets zone: " + handed.getTimeZone().getID() + ", hour " + handed.get(GregorianCalendar.HOUR_OF_DAY));
            System.out.println("depot zone for Australia/Perth: " + adapter.depotZone("Australia/Perth").getID());
            System.out.println("confirmed at a Perth depot: " + adapter.book("Australia/Perth", pickup));
            try {
                System.out.println("depot zone for Europe/Rom: " + adapter.depotZone("Europe/Rom").getID());
            } catch (DateTimeException refused) {
                System.out.println("depot zone for Europe/Rom refused: " + refused.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
