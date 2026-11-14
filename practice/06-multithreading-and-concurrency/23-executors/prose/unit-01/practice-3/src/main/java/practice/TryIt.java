package practice;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
            CountDownLatch never = new CountDownLatch(1);
            ExecutorService pool = Executors.newSingleThreadExecutor();
            try {
                pool.execute(new InvoiceRun("inv-run-A", 50, never));
                pool.execute(new InvoiceRun("inv-run-B", -1, never));
                pool.execute(new InvoiceRun("inv-run-C", 50, never));
                List<Runnable> handBack = Drain.stop(pool, 300, TimeUnit.MILLISECONDS);
                System.out.println("handed back to the broker: " + handBack);
                System.out.println("pool terminated: " + pool.isTerminated());
                System.out.println("hook thread interrupted: " + Thread.currentThread().isInterrupted());
            } finally {
                never.countDown();
                pool.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** An invoice run that takes millis to finish, or runs until it is interrupted when millis is -1. */
    record InvoiceRun(String name, long millis, CountDownLatch never) implements Runnable {
        @Override public void run() {
            try {
                if (millis < 0) never.await(); else Thread.sleep(millis);
                System.out.println(name + " finished");
            } catch (InterruptedException e) {
                System.out.println(name + " was asked to stop");
            }
        }
        @Override public String toString() { return name; }
    }
}
