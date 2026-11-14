package practice;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            ExecutorService pool = Executors.newFixedThreadPool(2);
            try {
                OrderDispatch dispatcher = new OrderDispatch(pool, new BusyVolume());
                OrderDispatch.Report report = dispatcher.dispatch(List.of("RET-10", "RET-11", "RET-10", "RET-BAD"));
                System.out.println("archived: " + report.archived());
                System.out.println("failed: " + report.failed());
            } finally {
                pool.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** Busy on the first try of every order, and never accepts RET-BAD. */
    static class BusyVolume implements OrderDispatch.Archive {
        private final Map<String, Integer> tries = new HashMap<>();

        @Override
        public synchronized void store(String orderId) throws IOException {
            int attempt = tries.merge(orderId, 1, Integer::sum);
            if (attempt == 1 || orderId.equals("RET-BAD")) {
                throw new IOException("volume busy for " + orderId + ", attempt " + attempt);
            }
            System.out.println("stored " + orderId + " on attempt " + attempt);
        }
    }
}
