package practice;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) throws Exception {
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
            PointsAccount account = new PointsAccount(600);
            System.out.println("purchase +90: " + account.apply(p -> p + 90));
            System.out.println("double weekend: " + account.apply(p -> p * 2));
            System.out.println("half expires: " + account.apply(p -> p / 2));
            Thread purchases = new Thread(() -> { for (int i = 0; i < 10_000; i++) account.apply(p -> p + 1); });
            Thread refunds = new Thread(() -> { for (int i = 0; i < 10_000; i++) account.apply(p -> p - 1); });
            purchases.start();
            refunds.start();
            purchases.join();
            refunds.join();
            System.out.println("after 10,000 +1 and 10,000 -1 at once: " + account.balance());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
