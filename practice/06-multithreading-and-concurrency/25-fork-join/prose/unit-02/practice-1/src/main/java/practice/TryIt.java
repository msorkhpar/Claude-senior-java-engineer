package practice;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
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
            Set<ForkJoinPool> pools = ConcurrentHashMap.newKeySet();
            InvoiceExport export = new InvoiceExport(invoice -> {
                ForkJoinPool pool = ForkJoinTask.getPool();
                if (pool != null) pools.add(pool);
                if (invoice.endsWith("X")) throw new IllegalArgumentException("unknown tax region on " + invoice);
                return 19L * Integer.parseInt(invoice.substring(3));
            }, 3);
            System.out.println("total tax: " + export.totalTax(List.of("FY-100", "FY-250", "FY-075", "FY-420")));
            try {
                export.totalTax(List.of("FY-100", "FY-555X", "FY-250"));
            } catch (IllegalArgumentException failed) {
                System.out.println("failed export: " + failed.getMessage());
            }
            System.out.println("pools used: " + pools.size());
            System.out.println("all of them shut down: " + pools.stream().allMatch(ForkJoinPool::isShutdown));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
