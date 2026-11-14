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
            LongUnaryOperator net = cents -> cents - cents / 100;
            long[] day = new long[64_000];
            long expected = 0;
            for (int i = 0; i < day.length; i++) {
                day[i] = 5_000 + (i * 4_001L) % 700_000;
                expected += net.applyAsLong(day[i]);
            }
            ForkJoinPool staging = new ForkJoinPool(2);
            ForkJoinPool production = new ForkJoinPool(6);
            try {
                SettlementJob onStaging = new SettlementJob(staging, 100, net);
                System.out.println("staging total right: " + (onStaging.total(day) == expected));
                System.out.println("staging pieces (64 to 128 wanted): " + onStaging.pieces());
                SettlementJob onProduction = new SettlementJob(production, 100, net);
                System.out.println("production total right: " + (onProduction.total(day) == expected));
                System.out.println("production pieces (192 to 384 wanted): " + onProduction.pieces());
                onProduction.total(new long[80]);
                System.out.println("pieces for a batch of 80 trades: " + onProduction.pieces());
            } finally {
                staging.shutdownNow();
                production.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
