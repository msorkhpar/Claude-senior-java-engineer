package practice;

import java.math.BigDecimal;
import java.time.LocalDate;
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
            Locale.setDefault(Locale.GERMANY);
            String plain = ReminderLetter.render(new ReminderLetter.Invoice("Sample Bakery GmbH", "INV-2027-0093",
                    new BigDecimal("2475.5"), LocalDate.of(2027, 2, 28), null));
            plain.lines().forEach(line -> System.out.println("letter 1: " + line));
            String noted = ReminderLetter.render(new ReminderLetter.Invoice("Demo Florists Ltd", "INV-2027-0094",
                    new BigDecimal("89.99"), LocalDate.of(2027, 3, 5), "Pay 50% now, 50% in April as agreed."));
            noted.lines().forEach(line -> System.out.println("letter 2: " + line));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
