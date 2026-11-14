package practice;

import java.time.LocalDate;
import java.util.Set;
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
            LocalDate holiday = LocalDate.of(2027, 5, 3);
            Deadlines deadlines = new Deadlines(Set.of(holiday));
            System.out.println("order Wed 2027-04-28 ships by: " + deadlines.shipBy(LocalDate.of(2027, 4, 28)));
            System.out.println("order Fri 2027-04-30 ships by: " + deadlines.shipBy(LocalDate.of(2027, 4, 30)));
            System.out.println("ticket Wed 2027-04-28 due: " + deadlines.replyBy(LocalDate.of(2027, 4, 28)));
            System.out.println("ticket Sat 2027-05-01 due: " + deadlines.replyBy(LocalDate.of(2027, 5, 1)));
            System.out.println("ticket Fri 2027-04-30 due: " + deadlines.replyBy(LocalDate.of(2027, 4, 30)));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
