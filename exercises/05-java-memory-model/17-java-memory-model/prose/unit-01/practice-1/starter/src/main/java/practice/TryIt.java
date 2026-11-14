package practice;

import java.util.LinkedHashMap;
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
            Map<String, Long> store = new LinkedHashMap<>(Map.of("GBP", 1_164_200L, "JPY", 6_120L, "NOK", 85_400L));
            RateTable table = new RateTable(sink -> store.forEach(sink));
            System.out.println("GBP rate: " + table.rate("GBP"));
            System.out.println("NOK rate: " + table.rate("NOK"));
            Map<String, Long> forTheReport = table.rates();
            try {
                forTheReport.put("GBP", 1L);
            } catch (UnsupportedOperationException refused) {
                System.out.println("report map refuses changes");
            }
            System.out.println("GBP after the report: " + table.rate("GBP"));
            store.put("GBP", 1_170_900L);
            table.refresh();
            System.out.println("GBP after refresh: " + table.rate("GBP"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
