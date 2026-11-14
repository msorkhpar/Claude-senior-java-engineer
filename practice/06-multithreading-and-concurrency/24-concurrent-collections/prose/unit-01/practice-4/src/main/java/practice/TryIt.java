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
            FareIndex zurichToLisbon = new FareIndex();
            for (int cents : new int[] {11_000, 3_990, 6_400, 4_000, 15_250, 8_875}) zurichToLisbon.offer(cents);
            var midRange = zurichToLisbon.between(6_400, 11_000);
            var cheap = zurichToLisbon.under(4_000);
            System.out.println("from 64.00 to 110.00: " + midRange);
            System.out.println("under 40.00: " + cheap);
            zurichToLisbon.withdraw(8_875);
            zurichToLisbon.offer(7_200);
            System.out.println("shown range after pricing changed: " + midRange);
            System.out.println("a new search of the same range: " + zurichToLisbon.between(6_400, 11_000));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
