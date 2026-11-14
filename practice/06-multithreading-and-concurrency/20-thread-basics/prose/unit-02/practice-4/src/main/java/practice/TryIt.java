package practice;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
            AtomicInteger runs = new AtomicInteger();
            CountDownLatch handlerSet = new CountDownLatch(1);
            CountDownLatch shutdown = new CountDownLatch(1);
            Supervisor supervisor = new Supervisor("shipments-consumer", () -> {
                if (runs.incrementAndGet() == 1) {
                    try {
                        handlerSet.await(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    throw new IllegalArgumentException("shipment 52: unknown carrier");
                }
                try {
                    shutdown.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            try {
                supervisor.check();
                Thread first = supervisor.worker();
                first.setUncaughtExceptionHandler((t, e) -> System.out.println(t.getName() + " died: " + e.getMessage()));
                handlerSet.countDown();
                first.join(5_000);
                System.out.println("first consumer alive: " + first.isAlive());
                supervisor.check();
                supervisor.check();
                supervisor.check();
                System.out.println("consumer after three more checks: " + supervisor.worker().getName()
                        + ", alive " + supervisor.worker().isAlive());
                System.out.println("times the consumer was started: " + runs.get());
            } finally {
                shutdown.countDown();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
