package practice;

import java.time.Duration;
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
            AlertRule.RuleBook book = new AlertRule.RuleBook();
            AlertRule memory = new AlertRule(book, "memory.used.percent", 88.0, Duration.ofMinutes(5));
            AlertRule errors = new AlertRule(book, "api.errors.count", 40, Duration.ofSeconds(150));
            System.out.println("rule: " + memory.metric() + " over " + memory.threshold() + " for " + memory.window());
            System.out.println("rule: " + errors.metric() + " over " + errors.threshold() + " for " + errors.window());
            try {
                new AlertRule(book, "api.latency.ms", 400, Duration.ofSeconds(20));
                System.out.println("20 s window: accepted");
            } catch (IllegalArgumentException refused) {
                System.out.println("20 s window: refused, " + refused.getMessage());
            }
            try {
                new AlertRule(book, "api.latency.ms", Double.NaN, Duration.ofMinutes(2));
                System.out.println("NaN threshold: accepted");
            } catch (IllegalArgumentException refused) {
                System.out.println("NaN threshold: refused, " + refused.getMessage());
            }
            System.out.println("rules the evaluator walks: " + book.rules().size());
            System.out.println("minutes of samples needed: " + book.samplesNeeded());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
