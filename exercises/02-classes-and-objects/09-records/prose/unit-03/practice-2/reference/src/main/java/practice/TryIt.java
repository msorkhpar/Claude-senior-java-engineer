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
            RetryPolicy policy = new RetryPolicy(2);
            System.out.println("first failure, deliver again: " + policy.failed(delivery("rpt-55")));
            System.out.println("second failure, deliver again: " + policy.failed(delivery("rpt-55")));
            System.out.println("failures of rpt-55: " + policy.failures(delivery("rpt-55")));
            System.out.println("failures of mail-12: " + policy.failures(delivery("mail-12")));
            System.out.println("mail-12 fails once, deliver again: " + policy.failed(delivery("mail-12")));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** Each delivery arrives in a new Job object, read from its message. */
    private static RetryPolicy.Job delivery(String id) {
        return new RetryPolicy.Job(new String(id), "build-report", "{\"job\":\"" + id + "\"}");
    }
}
