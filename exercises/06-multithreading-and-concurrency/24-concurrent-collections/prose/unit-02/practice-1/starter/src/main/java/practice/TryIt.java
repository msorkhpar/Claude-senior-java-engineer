package practice;

import java.util.ArrayList;
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
            Quotas quotas = new Quotas();
            ExecutorService threads = Executors.newFixedThreadPool(6);
            try {
                List<Future<?>> topUps = new ArrayList<>();
                for (int t = 0; t < 3; t++) {
                    topUps.add(threads.submit(() -> { for (int i = 0; i < 500; i++) quotas.grant("client-42", 2); }));
                }
                for (Future<?> f : topUps) finish(f);
                System.out.println("after 1500 top-ups of 2 calls: " + quotas.remaining("client-42"));
                List<Future<Integer>> requests = new ArrayList<>();
                for (int t = 0; t < 6; t++) {
                    requests.add(threads.submit(() -> {
                        int served = 0;
                        for (int i = 0; i < 700; i++) if (quotas.tryUse("client-42")) served++;
                        return served;
                    }));
                }
                int served = 0;
                for (Future<Integer> f : requests) served += finish(f);
                System.out.println("calls served out of 4200 requests: " + served);
                System.out.println("left after the requests: " + quotas.remaining("client-42"));
                System.out.println("unknown client served: " + quotas.tryUse("client-99"));
            } finally {
                threads.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** Waits for a thread's work and passes on what it threw, so Run shows it. */
    static <T> T finish(Future<T> work) throws Exception {
        try {
            return work.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException stop) throw stop;
            throw e;
        }
    }
}
