package practice;

import java.time.Duration;
import java.time.LocalTime;
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
            System.out.println("shift 22:30 to 06:45 lasts: " + Timesheet.length(LocalTime.of(22, 30), LocalTime.of(6, 45)));
            System.out.println("label of 31h 07m: " + Timesheet.label(Duration.ofHours(31).plusMinutes(7)));
            System.out.println("label of -1h 25m: " + Timesheet.label(Duration.ofMinutes(-85)));
            List<Timesheet.Shift> nights = List.of(
                    new Timesheet.Shift(LocalTime.of(22, 30), LocalTime.of(6, 45)),
                    new Timesheet.Shift(LocalTime.of(22, 30), LocalTime.of(6, 45)),
                    new Timesheet.Shift(LocalTime.of(22, 0), LocalTime.of(7, 0)));
            System.out.println("three nights against 24h: " + Timesheet.balance(nights, Duration.ofHours(24)));
            System.out.println("three nights against 27h: " + Timesheet.balance(nights, Duration.ofHours(27)));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
