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
            ShippingFees fees = new ShippingFees()
                    .tier(2_000, 990)
                    .tier(250, 410)
                    .tier(10_000, 2_200)
                    .tier(750, 560);
            System.out.println("fee for 300 g: " + fees.feeFor(300));
            System.out.println("fee for 2000 g: " + fees.feeFor(2_000));
            System.out.println("fee for 1999 g: " + fees.feeFor(1_999));
            System.out.println("fee for 40000 g: " + fees.feeFor(40_000));
            System.out.println("tiers 250..2000: " + fees.tierStartsBetween(250, 2_000));
            try {
                fees.feeFor(120);
            } catch (IllegalArgumentException refused) {
                System.out.println("120 g refused: " + refused.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
