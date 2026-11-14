package practice;

import java.util.List;
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
            PointsLedger ledger = new PointsLedger();
            ledger.importFile(List.of(new PointsLedger.Entry("m-501", 300), new PointsLedger.Entry("m-502", 40)));
            System.out.println("m-501 after the bakery file: " + ledger.balance("m-501"));
            try {
                ledger.importFile(List.of(new PointsLedger.Entry("m-502", -90)));
            } catch (IllegalArgumentException rejected) {
                System.out.println("rejected: " + rejected.getMessage());
            }
            ledger.importFile(List.of(new PointsLedger.Entry("m-501", 25)));
            System.out.println("m-501 after the next file: " + ledger.balance("m-501"));
            AtomicReference<Object> outcome = new AtomicReference<>("still waiting");
            Thread worker = new Thread(() -> {
                try {
                    ledger.importFile(List.of(new PointsLedger.Entry("m-501", 1_000)));
                    outcome.set("imported");
                } catch (InterruptedException | RuntimeException e) {
                    outcome.set(e);
                }
            }, "worker");
            worker.setDaemon(true);
            ledger.lock.lock();
            try {
                worker.start();
                Thread.sleep(200);
                worker.interrupt();
                worker.join(1_000);
            } finally {
                ledger.lock.unlock();
            }
            worker.join(1_000);
            System.out.println("worker interrupted while waiting ended with: " + outcome.get());
            System.out.println("m-501 at the end: " + ledger.balance("m-501"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
