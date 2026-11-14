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
            VisitReport report = new VisitReport();
            List<VisitReport.Visit> morning = List.of(
                    new VisitReport.Visit("ann", "/home", 1), new VisitReport.Visit("bob", "/shop", 4),
                    new VisitReport.Visit("ann", "/cart", 6), new VisitReport.Visit("cat", "/blog", 9),
                    new VisitReport.Visit("bob", "/home", 12));
            for (VisitReport.Visit landing : report.landingVisits(morning)) {
                System.out.println("morning landing: " + landing.user() + " on " + landing.page());
            }
            List<VisitReport.Visit> evening = List.of(
                    new VisitReport.Visit("bob", "/sale", 2), new VisitReport.Visit("ann", "/faq", 3));
            System.out.println("evening landings: " + report.landingVisits(evening));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
