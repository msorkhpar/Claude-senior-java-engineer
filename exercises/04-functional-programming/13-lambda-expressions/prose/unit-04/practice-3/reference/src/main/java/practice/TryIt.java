package practice;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
            ImportAudit audit = new ImportAudit(row -> row.split(",").length == 3);
            ExecutorService pool = Executors.newFixedThreadPool(2);
            try {
                Future<Integer> north = pool.submit(() -> audit.rejected(rows("P1,pen,3", "P2,ink", "P3,pad,1", "P4")));
                Future<Integer> south = pool.submit(() -> audit.rejected(rows("Q1,cup,2", "Q2,mug,9", "Q3")));
                System.out.println("north file rejected rows: " + resultOf(north));
                System.out.println("south file rejected rows: " + resultOf(south));
                System.out.println("an empty file rejected rows: " + audit.rejected(rows()));
            } finally {
                pool.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    private static ImportAudit.RowSource rows(String... rows) {
        return action -> List.of(rows).forEach(action);
    }

    private static int resultOf(Future<Integer> audit) throws Exception {
        try {
            return audit.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException failed) {
            if (failed.getCause() instanceof RuntimeException stopped) throw stopped;
            throw failed;
        }
    }
}
