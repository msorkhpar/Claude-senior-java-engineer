package practice;

import java.time.LocalDate;
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
            Map<String, String> query = Map.of("page", "3", "limit", "40", "since", "2026-02-01", "customer", "NORTHWIND-9", "express", "false");
            Integer page = QueryParam.PAGE.get(query, Integer.class);
            System.out.println("page: " + page);
            LocalDate since = QueryParam.SINCE.get(query, LocalDate.class);
            System.out.println("since: " + since);
            int limit = QueryParam.LIMIT.get(query, int.class);
            System.out.println("limit as int: " + limit);
            Number asNumber = QueryParam.LIMIT.get(query, Number.class);
            System.out.println("limit as Number: " + asNumber);
            System.out.println("customer: " + QueryParam.CUSTOMER.get(query, String.class));
            try {
                Object wrong = QueryParam.LIMIT.get(query, (Class<?>) String.class);
                System.out.println("limit as String: accepted " + wrong);
            } catch (ClassCastException refused) {
                System.out.println("limit as String: refused in get");
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
