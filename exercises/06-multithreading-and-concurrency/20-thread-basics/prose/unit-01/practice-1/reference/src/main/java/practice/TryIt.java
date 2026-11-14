package practice;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
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
            ConcurrentLinkedQueue<String> inbox = new ConcurrentLinkedQueue<>(List.of("case-310: refund", "case-311: login"));
            List<String> log = new CopyOnWriteArrayList<>();
            CountDownLatch handled = new CountDownLatch(2);
            AtomicReference<Throwable> failure = new AtomicReference<>();
            ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "helpdesk-worker-1");
                t.setDaemon(true);
                t.setUncaughtExceptionHandler((thread, e) -> {
                    failure.set(e);
                    handled.countDown();
                    handled.countDown();
                });
                return t;
            });
            try {
                pool.execute(new InboxPoller(inbox::poll, line -> {
                    log.add(line);
                    if (line.contains(" handled ")) handled.countDown();
                }));
                handled.await(5, TimeUnit.SECONDS);
            } finally {
                pool.shutdownNow();
            }
            System.out.println("pool stopped: " + pool.awaitTermination(5, TimeUnit.SECONDS));
            if (failure.get() instanceof RuntimeException e) throw e;
            log.forEach(line -> System.out.println("log: " + line));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
