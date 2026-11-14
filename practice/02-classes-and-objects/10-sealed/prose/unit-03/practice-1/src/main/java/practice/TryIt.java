package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            Map<String, Refunds.Order> known = Map.of("OR-7001", new Refunds.Order("OR-7001", 12_000, 2_500));
            Refunds.Orders store = id -> known.containsKey(id)
                    ? new Refunds.Ok<>(known.get(id))
                    : new Refunds.Err<>("no order with id " + id);
            List<String> sent = new ArrayList<>();
            Refunds desk = new Refunds(store, (orderId, cents) -> {
                sent.add(orderId + ":" + cents);
                return new Refunds.Ok<>("BANK-818");
            });
            System.out.println("refund 3,000: " + desk.refund("OR-7001", 3_000));
            System.out.println("refund 9,600: " + desk.refund("OR-7001", 9_600));
            System.out.println("refund on a typo: " + desk.refund("OR-7010", 3_000));
            Refunds refusingBank = new Refunds(store, (orderId, cents) -> new Refunds.Err<>("card expired"));
            System.out.println("refund the bank refuses: " + refusingBank.refund("OR-7001", 1_000));
            System.out.println("sent to the gateway: " + sent);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
