package practice;

import java.util.stream.Stream;
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
            int[] read = {0};
            Stream<PaymentWatch.Payment> live = Stream.generate(() -> {
                read[0]++;
                if (read[0] > 10_000) throw new IllegalStateException("the feed was read past 10000 payments");
                long cents = read[0] % 3 == 0 ? 750_000 + read[0] * 1_000L : 9_900;
                return new PaymentWatch.Payment("F-" + read[0], "acct-" + read[0], cents);
            });
            int[] assessed = {0};
            PaymentWatch watch = new PaymentWatch(payment -> {
                assessed[0]++;
                return new PaymentWatch.Assessment(payment, (int) (payment.amountCents() / 1_000 % 97));
            }, 500_000);
            System.out.println("review sheet:");
            System.out.println(watch.reviewSheet(live, 3));
            System.out.println("payments read from the feed: " + read[0]);
            System.out.println("sent to the risk service: " + assessed[0]);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
