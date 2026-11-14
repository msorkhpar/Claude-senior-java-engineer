package practice;

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
            Checkout checkout = new Checkout(Map.of("SKU-BOOK-11", 2_450L, "SKU-LAMP-3", 3_999L));
            String first = checkout.customerId("sam@example.org");
            String again = checkout.customerId("sam@example.org");
            System.out.println("first visit id: " + first + ", second visit id: " + again);
            System.out.println("customer rows inserted: " + checkout.inserts);
            System.out.println("lamp price: " + checkout.price("SKU-LAMP-3") + ", again: " + checkout.price("SKU-LAMP-3"));
            System.out.println("book price: " + checkout.price("SKU-BOOK-11"));
            System.out.println("catalogue calls: " + checkout.catalogueCalls);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
