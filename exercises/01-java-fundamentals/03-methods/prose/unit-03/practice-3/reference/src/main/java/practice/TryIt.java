package practice;

import java.util.Arrays;
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
            Checkout.Basket till = new Checkout.Basket();
            till.add(new Checkout.Item("TEA-50", 2));
            till.add(new Checkout.Item("CHOC-10", 4));
            Checkout.PlacedOrder first = till.place("T9-0101");
            till.add(new Checkout.Item("SALT-1", 1));
            Checkout.PlacedOrder second = till.place("T9-0102");
            System.out.println("first order: " + first.items() + ", units " + first.units());
            System.out.println("second order: " + second.items());
            try {
                first.items().add(new Checkout.Item("EXTRA-1", 1));
                System.out.println("a reader added an item: " + first.items());
            } catch (UnsupportedOperationException refused) {
                System.out.println("a reader could not add an item");
            }
            try {
                new Checkout.PlacedOrder("T9-0103", List.of());
            } catch (IllegalArgumentException refused) {
                System.out.println("empty order refused: " + refused.getMessage());
            }
            try {
                new Checkout.PlacedOrder("T9-0104", Arrays.asList(new Checkout.Item("OIL-2", 1), null));
                System.out.println("order with a missing item placed");
            } catch (NullPointerException refused) {
                System.out.println("order with a missing item refused");
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
