package practice;

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
            SeatMap flight = new SeatMap("1A", "1B", "12C", "12D", "30E");
            flight.assign("Jane Doe", "1A");
            flight.assign("John Roe", "12C");
            flight.rebook("Jane Doe", "1A", "30E");
            System.out.println("after the move: " + flight.snapshot());
            try {
                flight.rebook("Jane Doe", "30E", "12C");
            } catch (IllegalArgumentException refused) {
                System.out.println("taken seat refused: " + refused.getMessage());
            }
            try {
                flight.rebook("Jane Doe", "30E", "44Z");
            } catch (IllegalArgumentException refused) {
                System.out.println("unknown seat refused: " + refused.getMessage());
            }
            try {
                flight.rebook("Jane Doe", "12C", "1B");
            } catch (IllegalArgumentException refused) {
                System.out.println("someone else's seat refused: " + refused.getMessage());
            }
            System.out.println("seats after the refusals: " + flight.snapshot());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
