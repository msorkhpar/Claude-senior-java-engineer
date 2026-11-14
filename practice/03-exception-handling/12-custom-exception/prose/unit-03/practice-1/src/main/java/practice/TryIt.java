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
            Checkout booked = new Checkout((order, postcode) -> "PKG-" + order);
            System.out.println("booked: " + booked.placeOrder("A-501", "LS1 4AP"));
            Checkout noPostcode = new Checkout((order, postcode) -> {
                throw new Checkout.AddressRejectedException(postcode);
            });
            System.out.println("bad postcode: " + noPostcode.placeOrder("A-502", "XX9 9XX"));
            Checkout full = new Checkout((order, postcode) -> {
                throw new Checkout.NoCarrierException("no van free on Saturday");
            });
            System.out.println("no carrier: " + full.placeOrder("A-503", "LS1 4AP"));
            Checkout fragile = new Checkout((order, postcode) -> {
                throw new Checkout.ShippingException("glass needs special packing");
            });
            System.out.println("other refusal: " + fragile.placeOrder("A-504", "LS1 4AP"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
