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
            RuleTable table = new RuleTable();
            table.add("black-friday", new Discount(40));
            table.add("vip", new Discount(5));
            table.add("express", new Surcharge(990));
            System.out.println("black-friday on 25000: " + table.apply("black-friday", 25_000));
            System.out.println("vip on 25000: " + table.apply("vip", 25_000));
            System.out.println("express on 25000: " + table.apply("express", 25_000));
            try {
                table.add("broken", new LongRule());
            } catch (IllegalArgumentException refused) {
                System.out.println("refused: " + refused.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    static class Discount {
        private final int percent;
        Discount(int percent) { this.percent = percent; }
        public int apply(int cents) { return cents - cents * percent / 100; }
    }
    static class Surcharge {
        private final int fee;
        Surcharge(int fee) { this.fee = fee; }
        public int apply(int cents) { return cents + fee; }
    }
    static class LongRule {
        public long apply(long cents) { return cents; }
    }
}
