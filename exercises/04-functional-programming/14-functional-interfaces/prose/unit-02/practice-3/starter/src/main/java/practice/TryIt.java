package practice;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) throws Exception {
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
            RegionReport report = new RegionReport();
            Map<String, SortedSet<String>> byRegion = new TreeMap<>(report.skusByRegion(List.of(
                    new RegionReport.Sale("north", "stove"), new RegionReport.Sale("south", "hammock"),
                    new RegionReport.Sale("north", "boots"), new RegionReport.Sale("north", "stove"),
                    new RegionReport.Sale("south", "flask"))));
            System.out.print(report.render(byRegion));
            StringBuilder mine = report.scratch.get();
            StringBuilder[] other = new StringBuilder[1];
            Thread worker = new Thread(() -> other[0] = report.scratch.get());
            worker.start();
            worker.join();
            System.out.println("same buffer on the next report: " + (report.scratch.get() == mine));
            System.out.println("another thread shares this buffer: " + (other[0] == mine));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
