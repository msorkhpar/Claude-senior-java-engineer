package practice;

import java.util.concurrent.ForkJoinPool;
import java.util.function.LongUnaryOperator;
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
            LongUnaryOperator points = cents -> cents >= 100_000 ? 25 : cents % 10;
            long[] day = new long[10_000];
            for (int i = 0; i < day.length; i++) day[i] = 500 + (i * 3_331L) % 150_000;
            long expected = 0;
            for (long cents : day) expected += points.applyAsLong(cents);
            ForkJoinPool pool = new ForkJoinPool(3);
            try {
                RiskPoints scorer = new RiskPoints(pool, 500, points);
                System.out.println("total points: " + scorer.total(day));
                System.out.println("one thread counted: " + expected);
                System.out.println("tasks created: " + scorer.tasksCreated());
                System.out.println("an empty day: " + new RiskPoints(pool, 500, points).total(new long[0]));
                RiskPoints exact = new RiskPoints(pool, 500, points);
                exact.total(new long[500]);
                System.out.println("tasks for exactly 500 amounts: " + exact.tasksCreated());
            } finally {
                pool.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
