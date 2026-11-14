package practice;

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
            ServiceThreads invoices = new ServiceThreads("invoices", false);
            ServiceThreads metrics = new ServiceThreads("metrics", true);
            Thread i1 = invoices.newThread(() -> { });
            Thread i2 = invoices.newThread(() -> { });
            Thread m1 = metrics.newThread(() -> { });
            System.out.println("threads: " + i1.getName() + ", " + i2.getName() + ", " + m1.getName());
            System.out.println("invoices daemon: " + i1.isDaemon() + ", metrics daemon: " + m1.isDaemon());
            AtomicReference<Thread> fromDaemon = new AtomicReference<>();
            Thread housekeeping = new Thread(() -> fromDaemon.set(new ServiceThreads("payouts", false).newThread(() -> { })), "housekeeping");
            housekeeping.setDaemon(true);
            housekeeping.start();
            housekeeping.join(5_000);
            System.out.println("payouts thread made on a daemon thread: " + fromDaemon.get().getName()
                    + ", daemon " + fromDaemon.get().isDaemon());
            ExecutorService pool = Executors.newFixedThreadPool(1, new ServiceThreads("refunds", false));
            try {
                System.out.println("refunds pool runs on: " + pool.submit(() -> Thread.currentThread().getName()).get(5, TimeUnit.SECONDS));
            } finally {
                pool.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
