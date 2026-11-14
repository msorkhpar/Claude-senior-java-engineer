package practice;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
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
            SeatMap quick = new SeatMap(Duration.ofMillis(100));
            System.out.println("G-3 for cust-71: " + quick.hold("G-3", "cust-71"));
            System.out.println("G-3 for cust-88: " + quick.hold("G-3", "cust-88"));
            quick.lock.lock();
            try {
                System.out.println("G-4 during a long re-sync (100 ms budget): " + holdOnOwnThread(quick, "G-4", "cust-88").get(5, TimeUnit.SECONDS));
            } finally {
                quick.lock.unlock();
            }
            System.out.println("G-4 holder after BUSY: " + quick.holder("G-4"));
            SeatMap patient = new SeatMap(Duration.ofSeconds(2));
            CompletableFuture<SeatMap.Answer> answer;
            patient.lock.lock();
            try {
                answer = holdOnOwnThread(patient, "H-9", "cust-90");
                Thread.sleep(200);
            } finally {
                patient.lock.unlock();
            }
            System.out.println("H-9 during a 200 ms re-sync (2 s budget): " + answer.get(5, TimeUnit.SECONDS));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    private static CompletableFuture<SeatMap.Answer> holdOnOwnThread(SeatMap map, String seat, String customer) {
        CompletableFuture<SeatMap.Answer> answer = new CompletableFuture<>();
        Thread t = new Thread(() -> {
            try {
                answer.complete(map.hold(seat, customer));
            } catch (Throwable e) {
                answer.completeExceptionally(e);
            }
        }, "request-" + customer);
        t.setDaemon(true);
        t.start();
        return answer;
    }
}
