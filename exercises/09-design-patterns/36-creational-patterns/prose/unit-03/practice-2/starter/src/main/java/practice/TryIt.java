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
            Invoice.Builder business = Invoice.builder().line("Business plan base fee: 499.00");
            Invoice first = business.copy().customer("Example Corp").line("Overage 3,000 calls: 15.00").build();
            Invoice second = business.copy().customer("Sample GmbH").build();
            business.line("Business plan, price change: 20.00");
            System.out.println(first.customer() + ": " + first.lines());
            System.out.println(second.customer() + ": " + second.lines());
            try {
                first.lines().add("Credit: -499.00");
                System.out.println("lines() accepted an extra line");
            } catch (UnsupportedOperationException refused) {
                System.out.println("lines() refused an extra line");
            }
            System.out.println(first.customer() + " after all that: " + first.lines());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
