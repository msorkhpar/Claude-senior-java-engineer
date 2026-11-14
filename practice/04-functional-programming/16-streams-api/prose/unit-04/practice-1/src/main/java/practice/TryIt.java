package practice;

import java.util.ArrayList;
import java.util.Collections;
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
            List<LoyaltyPoints.Purchase> few = List.of(new LoyaltyPoints.Purchase("rc-1", 40),
                    new LoyaltyPoints.Purchase("rc-2", 15));
            System.out.println("earned, 2 purchases, bonus 100: " + LoyaltyPoints.earned(few, 100));
            List<LoyaltyPoints.Purchase> many = new ArrayList<>();
            for (int i = 0; i < 2_000; i++) many.add(new LoyaltyPoints.Purchase("rc-" + i, 2));
            System.out.println("earned, 2000 purchases of 2, bonus 100: " + LoyaltyPoints.earned(many, 100));
            System.out.println("remaining, 900 less 50 and 25: " + LoyaltyPoints.remaining(900, List.of(50, 25)));
            List<Integer> redemptions = Collections.nCopies(1_500, 3);
            System.out.println("remaining, 10000 less 1500 redemptions of 3: " + LoyaltyPoints.remaining(10_000, redemptions));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
