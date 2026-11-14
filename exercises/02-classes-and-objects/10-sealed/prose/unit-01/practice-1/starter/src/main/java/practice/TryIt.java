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
            System.out.println("order events: " + new java.util.TreeMap<>(PayloadTypes.of(OrderEvent.class)).keySet());
            System.out.println("billing events: " + new java.util.TreeMap<>(PayloadTypes.of(BillingEvent.class)).keySet());
            System.out.println("\"declined\" decodes into: " + PayloadTypes.of(BillingEvent.class).get("declined").getSimpleName());
            try {
                PayloadTypes.of(Reminder.class);
                System.out.println("reminders: mapped");
            } catch (IllegalArgumentException refused) {
                System.out.println("reminders: refused, " + refused.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    sealed interface OrderEvent permits Placed, Shipped {}

    record Placed(String order) implements OrderEvent {}

    record Shipped(String order, String carrier) implements OrderEvent {}

    sealed interface BillingEvent permits Paid, Failure {}

    record Paid(String invoice) implements BillingEvent {}

    sealed interface Failure extends BillingEvent permits Declined, TimedOut {}

    record Declined(String invoice, String code) implements Failure {}

    record TimedOut(String invoice) implements Failure {}

    interface Reminder {}
}
