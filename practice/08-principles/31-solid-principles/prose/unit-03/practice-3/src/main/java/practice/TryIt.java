package practice;

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
            Quotas.DailyQuota quota = new Quotas.DailyQuota(10);
            System.out.println("asked 4, granted: " + quota.grant(4));
            System.out.println("asked 0, granted: " + quota.grant(0));
            List<String> batch = List.of("sms-a", "sms-b", "sms-c", "sms-d", "sms-e", "sms-f", "sms-g", "sms-h");
            System.out.println("sendable from a batch of 8: " + Quotas.sendable(batch, quota));
            System.out.println("remaining: " + quota.remaining());
            System.out.println("asked 3 after that, granted: " + quota.grant(3));
            System.out.println("unlimited, batch of 8: " + Quotas.sendable(batch, new Quotas.Unlimited()).size());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
