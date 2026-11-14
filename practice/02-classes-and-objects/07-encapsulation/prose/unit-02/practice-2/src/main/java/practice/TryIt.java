package practice;

import java.time.Instant;
import java.util.Date;
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
            Inspection inspection = new Inspection("yard-3", Date.from(Instant.parse("2028-06-01T07:45:00Z")),
                    "frayed cable", "missing guard", "wet floor");
            System.out.println("site: " + inspection.getSite());
            System.out.println("inspected at: " + inspection.getInspectedAt().toInstant());
            System.out.println("defects: " + inspection.getDefects());
            inspection.getInspectedAt().setTime(0L);
            List<String> exported = inspection.getDefects();
            try {
                exported.set(0, "fixed");
            } catch (UnsupportedOperationException refused) {
                System.out.println("export could not edit the list");
            }
            System.out.println("inspected at, after a report changed its copy: " + inspection.getInspectedAt().toInstant());
            System.out.println("defects, after the export: " + inspection.getDefects());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
