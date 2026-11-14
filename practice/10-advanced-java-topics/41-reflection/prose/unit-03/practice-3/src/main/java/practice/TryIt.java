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
            PriceRules.Rule summer = PriceRules.bind(Summer.class, "summerSale");
            PriceRules.Rule student = PriceRules.bind(Student.class, "discount");
            System.out.println("summer sale on 25000: " + summer.apply(25_000));
            System.out.println("summer sale on 7000000000: " + summer.apply(7_000_000_000L));
            System.out.println("student discount on 1200: " + student.apply(1_200));
            for (String method : new String[] {"autumnSale", "roundToTens"}) {
                try {
                    PriceRules.bind(Summer.class, method);
                } catch (IllegalArgumentException refused) {
                    System.out.println("refused: " + refused.getMessage());
                }
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    public static class Summer {
        public long summerSale(long cents) { return cents * 70 / 100; }
        public int roundToTens(long cents) { return (int) (cents / 1000 * 1000); }
    }
    public static class Student {
        private final long off = 500;
        public long discount(long cents) { return Math.max(0, cents - off); }
    }
}
