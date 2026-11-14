package practice;

import java.sql.SQLTimeoutException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
            ExecutorService pool = Executors.newFixedThreadPool(3);
            try {
                Map<String, Callable<String>> queries = new LinkedHashMap<>();
                queries.put("weekly-revenue", () -> { Thread.sleep(400); return "2.7M"; });
                queries.put("cache-hit-rate", () -> { Thread.sleep(150); throw new SQLTimeoutException("metrics replica busy"); });
                queries.put("error-rate", () -> "1.1%");
                long start = System.nanoTime();
                Dashboard.Screen screen = (panel, content) -> System.out.println(
                        "shown after " + ((System.nanoTime() - start) / 100_000_000) * 100 + "+ ms: " + panel + " = " + content);
                new Dashboard(pool, screen).load(queries);
                System.out.println("load returned");
            } finally {
                pool.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
