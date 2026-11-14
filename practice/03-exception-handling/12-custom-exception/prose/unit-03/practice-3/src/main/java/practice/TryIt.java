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
            TransferLimits limits = new TransferLimits(500, 1_000_000);
            TransferLimits.TransferRequest small = new TransferLimits.TransferRequest("pay-301",
                    "NL91ABNA0417164300", "FR1420041010050500013M02606", "key_example_123", 250);
            TransferLimits.TransferRequest large = new TransferLimits.TransferRequest("pay-302",
                    "NL91ABNA0417164300", "FR1420041010050500013M02606", "key_example_456", 1_200_000);
            TransferLimits.TransferRequest fine = new TransferLimits.TransferRequest("pay-303",
                    "NL91ABNA0417164300", "FR1420041010050500013M02606", "key_example_789", 40_000);
            limits.check(fine);
            System.out.println("pay-303 of 40000 cents: accepted");
            for (TransferLimits.TransferRequest request : new TransferLimits.TransferRequest[] {small, large}) {
                try {
                    limits.check(request);
                    System.out.println(request.id() + ": accepted");
                } catch (IllegalArgumentException rejected) {
                    System.out.println("rejected: " + rejected.getMessage());
                }
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
