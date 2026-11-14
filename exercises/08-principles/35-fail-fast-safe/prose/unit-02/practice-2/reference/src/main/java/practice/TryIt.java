package practice;

import java.util.ArrayList;
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
            LeaseTable table = new LeaseTable();
            table.acquire("agent-mac-4", "pipeline-882", 3_000);
            table.acquire("gpu-2", "inference-job-5", 8_000);
            table.acquire("scanner", "archive-team", 4_000);
            table.acquire("plotter", "cad-lab", 4_500);
            table.release("scanner", "archive-team");
            table.renew("agent-mac-4", "pipeline-882", 6_000);
            List<String> told = new ArrayList<>();
            System.out.println("sweep at 5000 freed: " + table.sweep(5_000, lease -> told.add(lease.holder())));
            System.out.println("holders told: " + told);
            System.out.println("sweep at 7000 freed: " + table.sweep(7_000, lease -> told.add(lease.holder())));
            System.out.println("gpu-2 held by: " + table.holderOf("gpu-2").holder());
            System.out.println("holders told so far: " + told);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
