package practice;

import java.util.Arrays;
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
            LongUnaryOperator interest = cents -> {
                if (cents < 0) throw new IllegalArgumentException("corrupt balance " + cents);
                return cents + cents / 200;
            };
            ForkJoinPool pool = new ForkJoinPool(3);
            try {
                Accrual accrual = new Accrual(pool, 2, interest);
                long[] good = {20_000, 400_000, 0, 7_500, 1_200_000};
                accrual.accrue(good);
                System.out.println("good book: " + Arrays.toString(good));
                long[] bad = {20_000, 400_000, -300, 7_500, 1_200_000};
                try {
                    accrual.accrue(bad);
                } catch (IllegalArgumentException failed) {
                    System.out.println("accrual failed: " + failed.getMessage());
                }
                System.out.println("bad book afterwards: " + Arrays.toString(bad));
            } finally {
                pool.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
