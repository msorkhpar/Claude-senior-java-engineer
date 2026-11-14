package practice;

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
            List<CityReport.Sale> sales = List.of(
                    new CityReport.Sale("oslo", "c-1", 1_800), new CityReport.Sale("bonn", "c-2", 300),
                    new CityReport.Sale("oslo", "c-1", 450), new CityReport.Sale("aarhus", "c-3", 2_200),
                    new CityReport.Sale("bonn", "c-4", 90));
            System.out.print(CityReport.render(sales, 1_000));
            System.out.println("customers by city: " + new java.util.TreeMap<>(CityReport.customersByCity(sales)));
            System.out.print("quiet day: " + CityReport.render(List.of(new CityReport.Sale("bonn", "c-9", 120)), 1_000));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
