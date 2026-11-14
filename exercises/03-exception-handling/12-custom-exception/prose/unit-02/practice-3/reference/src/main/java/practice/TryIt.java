package practice;

import java.sql.SQLException;
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
            Map<String, CustomerService.Customer> rows = Map.of(
                    "C-1201", new CustomerService.Customer("C-1201", "Example Bakery Co"));
            CustomerService service = new CustomerService(rows::get);
            System.out.println("found: " + service.find("C-1201"));
            try {
                service.find("C-7777");
            } catch (CustomerService.UnknownCustomerException unknown) {
                System.out.println("unknown: " + unknown.getMessage());
            }
            CustomerService down = new CustomerService(id -> { throw new SQLException("connection pool exhausted", "08001"); });
            try {
                down.find("C-1201");
            } catch (CustomerService.CustomerStoreException failed) {
                System.out.println("store failure: " + failed.getMessage());
                System.out.println("cause for the logs: " + failed.getCause());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
