package practice;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
            AtomicInteger calls = new AtomicInteger();
            QuoteFeed flaky = new QuoteFeed(attempt -> calls.incrementAndGet() == 4, 5);
            System.out.println("reconnect with 5 ms back-offs: " + within(flaky::reconnect, 3));
            System.out.println("status: " + flaky.status());
            QuoteFeed down = new QuoteFeed(attempt -> false, 60_000);
            Thread feedThread = new Thread(() -> {
                try {
                    down.reconnect();
                } catch (InterruptedException | RuntimeException e) {
                    System.out.println("reconnect ended with: " + e);
                }
            }, "feed");
            feedThread.setDaemon(true);
            feedThread.start();
            for (int i = 0; i < 100 && feedThread.getState() != Thread.State.TIMED_WAITING; i++) Thread.sleep(10);
            System.out.println("status during a 60 s back-off: " + within(down::status, 2));
            within(() -> { down.close(); return null; }, 2);
            feedThread.join(2_000);
            System.out.println("feed thread still backing off after close: " + feedThread.isAlive());
            System.out.println("status after close: " + within(down::status, 2));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    private static <T> Object within(Callable<T> call, int seconds) throws InterruptedException {
        FutureTask<T> task = new FutureTask<>(call);
        Thread t = new Thread(task, "monitoring");
        t.setDaemon(true);
        t.start();
        try {
            return task.get(seconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return "no answer within " + seconds + " s";
        } catch (ExecutionException e) {
            throw e.getCause() instanceof RuntimeException r ? r : new IllegalStateException(e.getCause());
        }
    }
}
