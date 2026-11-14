package practice;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
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
            AtomicInteger chargeTries = new AtomicInteger();
            boolean charged = Retries.send(new Retries.Charge(45_00), failingTimes(2, chargeTries));
            System.out.println("charge accepted: " + charged + ", tries: " + chargeTries.get());
            AtomicInteger refundTries = new AtomicInteger();
            boolean refunded = Retries.send(new Retries.Refund(45_00, "ch_900"), failingTimes(2, refundTries));
            System.out.println("refund accepted: " + refunded + ", tries: " + refundTries.get());
            Retries.idempotentAttempts = 2;
            AtomicInteger downTries = new AtomicInteger();
            boolean refundedWhileDown = Retries.send(new Retries.Refund(9_90, "ch_901"), failingTimes(50, downTries));
            System.out.println("policy now 2, refund accepted: " + refundedWhileDown + ", tries: " + downTries.get());
            System.out.println("refund maxAttempts: " + new Retries.Refund(1_00, "ch_902").maxAttempts());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** A bank that refuses the first {@code failures} attempts, counting every attempt. */
    private static BooleanSupplier failingTimes(int failures, AtomicInteger attempts) {
        return () -> attempts.incrementAndGet() > failures;
    }
}
