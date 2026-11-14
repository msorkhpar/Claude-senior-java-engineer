package practice;

import java.time.LocalDateTime;
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
            LocalDateTime tue1000 = LocalDateTime.of(2027, 6, 15, 10, 0);
            LocalDateTime tue1130 = LocalDateTime.of(2027, 6, 15, 11, 30);
            Booking b = Booking.builder().end(tue1130).room("Harbour").start(tue1000).organiser("Alex Example").build();
            System.out.println("booked: " + b.room() + " for " + b.organiser() + ", " + b.start() + " to " + b.end());
            Booking.Builder[] forms = {
                    Booking.builder().room("Harbour").organiser("Alex Example").start(tue1130).end(tue1000),
                    Booking.builder().room("Harbour").start(tue1000).end(tue1130),
                    Booking.builder().room("Loft").organiser("Sam Sample").start(tue1000).end(tue1000)};
            for (Booking.Builder form : forms) {
                try {
                    Booking made = form.build();
                    System.out.println("created: " + made.room() + " " + made.start() + " to " + made.end());
                } catch (Booking.InvalidBooking refused) {
                    System.out.println("refused: " + refused.getMessage());
                }
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
