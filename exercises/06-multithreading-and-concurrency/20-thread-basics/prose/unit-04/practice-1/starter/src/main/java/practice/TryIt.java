package practice;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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
            Object inventory = new Object();
            CountDownLatch release = new CountDownLatch(1);
            CountDownLatch holding = new CountDownLatch(1);
            Thread holder = daemon("stock-sync", () -> {
                synchronized (inventory) {
                    holding.countDown();
                    while (release.getCount() > 0) Thread.onSpinWait();
                }
            });
            holding.await();
            Thread order1 = daemon("order-1", () -> { synchronized (inventory) { } });
            Thread order2 = daemon("order-2", () -> { synchronized (inventory) { } });
            Thread mailer = daemon("mailer", () -> {
                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            try {
                List<Thread> threads = List.of(holder, order1, order2, mailer);
                waitFor(order1, Thread.State.BLOCKED);
                waitFor(order2, Thread.State.BLOCKED);
                waitFor(mailer, Thread.State.WAITING);
                ThreadInfo[] snapshot = ManagementFactory.getThreadMXBean()
                        .getThreadInfo(threads.stream().mapToLong(Thread::threadId).toArray());
                for (ThreadInfo info : snapshot) System.out.println(info.getThreadName() + ": " + ThreadTriage.classify(info));
                System.out.println("summary: " + ThreadTriage.summary(snapshot));
                System.out.println("stuck behind: " + ThreadTriage.stuckBehind(snapshot));
            } finally {
                release.countDown();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    private static Thread daemon(String name, Runnable body) {
        Thread t = new Thread(body, name);
        t.setDaemon(true);
        t.start();
        return t;
    }

    private static void waitFor(Thread t, Thread.State state) {
        long deadline = System.nanoTime() + 5_000_000_000L;
        while (t.getState() != state && System.nanoTime() < deadline) Thread.yield();
    }
}
