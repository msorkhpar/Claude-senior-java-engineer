package practice;

import java.time.Instant;
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
            ArchiveStamps stamps = new ArchiveStamps();
            System.out.println("contract 2031: " + stamps.header("contract-88", Instant.parse("2031-06-01T09:30:00Z")));
            System.out.println("letter 1955: " + stamps.header("letter-12", Instant.parse("1955-11-05T06:00:00Z")));
            try {
                System.out.println("lease 2045: " + stamps.header("lease-45", Instant.parse("2045-02-01T00:00:00Z")));
            } catch (ArithmeticException refused) {
                System.out.println("lease 2045 refused: " + refused.getMessage());
            }
            try {
                System.out.println("map 1890: " + stamps.header("map-1890", Instant.parse("1890-05-01T00:00:00Z")));
            } catch (ArithmeticException refused) {
                System.out.println("map 1890 refused: " + refused.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
