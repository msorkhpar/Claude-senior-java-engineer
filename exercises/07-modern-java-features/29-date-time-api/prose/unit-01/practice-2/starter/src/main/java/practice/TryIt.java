package practice;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
            System.out.println("columns 92, 2, 14 read as: " + BirthDates.toLocalDate(92, 2, 14));
            System.out.println("1999-12-05 written as: " + Arrays.toString(BirthDates.toColumns(LocalDate.of(1999, 12, 5))));
            List<BirthDates.Row> rejected = new ArrayList<>();
            Map<String, LocalDate> migrated = BirthDates.migrate(List.of(
                    new BirthDates.Row("K-71", 85, 0, 3),
                    new BirthDates.Row("K-72", 99, 8, 31),
                    new BirthDates.Row("K-73", 106, 11, 24)), rejected);
            System.out.println("migrated: " + migrated);
            System.out.println("set aside: " + rejected.stream().map(BirthDates.Row::customer).toList());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
