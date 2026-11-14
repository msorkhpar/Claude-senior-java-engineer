package practice;

import java.io.IOException;
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
            CountDownLatch vendorAnswers = new CountDownLatch(1);
            ScoringClient client = new ScoringClient(payment -> {
                if (payment.startsWith("ERR")) throw new IOException("vendor returned 502");
                if (payment.startsWith("SLOW")) vendorAnswers.await(5, TimeUnit.SECONDS);
                return payment.length() * 3;
            }, 2, 200);
            ExecutorService checkouts = Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            });
            try {
                System.out.println("score PAY-77: " + client.score("PAY-77"));
                try {
                    client.score("ERR-12");
                } catch (IOException failed) {
                    System.out.println("ERR-12 failed at the vendor: " + failed.getMessage());
                }
                System.out.println("free sessions after the failure: " + client.freeSessions() + " of 2");
                checkouts.submit(() -> client.score("SLOW-1"));
                checkouts.submit(() -> client.score("SLOW-2"));
                for (int i = 0; i < 100 && client.freeSessions() > 0; i++) Thread.sleep(10);
                try {
                    client.score("PAY-78");
                } catch (ScoringClient.Busy busy) {
                    System.out.println("while both sessions are busy: " + busy.getMessage());
                }
                vendorAnswers.countDown();
                for (int i = 0; i < 100 && client.freeSessions() < 2; i++) Thread.sleep(10);
                System.out.println("free sessions at the end: " + client.freeSessions() + " of 2");
            } finally {
                vendorAnswers.countDown();
                checkouts.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
