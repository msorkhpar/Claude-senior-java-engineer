package practice;

import java.time.LocalDate;
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
            LocalDate adult = LocalDate.of(1990, 2, 20);
            LocalDate teen = LocalDate.of(2009, 7, 3);
            System.out.println("days overdue, due 2026-06-20, back 2026-08-30: " + Fines.daysOverdue(LocalDate.of(2026, 6, 20), LocalDate.of(2026, 8, 30)));
            System.out.println("adult fine for that: " + Fines.fineCents(adult, LocalDate.of(2026, 6, 20), LocalDate.of(2026, 8, 30)) + " cents");
            System.out.println("teen's age on 2027-07-01: " + Fines.ageOn(teen, LocalDate.of(2027, 7, 1)));
            System.out.println("teen's fine, due 2027-06-26, back 2027-07-01: " + Fines.fineCents(teen, LocalDate.of(2027, 6, 26), LocalDate.of(2027, 7, 1)) + " cents");
            System.out.println("returned early: " + Fines.fineCents(adult, LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 2)) + " cents");
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
