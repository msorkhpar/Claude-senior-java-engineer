package practice;

import java.util.concurrent.CountDownLatch;
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
            ShipmentJournal journal = new ShipmentJournal();
            SlowParcel slow = new SlowParcel();
            AtomicReference<RuntimeException> failed = new AtomicReference<>();
            Thread slowScanner = scanner(() -> journal.record(slow), failed);
            slow.inLookup.await(2, TimeUnit.SECONDS);
            Thread fastScanner = scanner(() -> journal.record(new Parcel("PL-0450", "WAW-1", 380)), failed);
            fastScanner.join(1_000);
            boolean fastDone = !fastScanner.isAlive();
            slow.answer.countDown();
            slowScanner.join();
            fastScanner.join();
            if (failed.get() != null) throw failed.get();
            System.out.println("quick event recorded during the slow lookup: " + fastDone);
            System.out.print("journal:\n" + journal.contents());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    record Parcel(String parcel, String depot, int grams) implements ShipmentJournal.Event { }

    /** A parcel whose depot lookup waits until the answer is released. */
    static class SlowParcel implements ShipmentJournal.Event {
        final CountDownLatch inLookup = new CountDownLatch(1);
        final CountDownLatch answer = new CountDownLatch(1);
        @Override public String parcel() { return "SE-3001"; }
        @Override public int grams() { return 1200; }
        @Override public String depot() {
            inLookup.countDown();
            try {
                answer.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "GOT-7";
        }
    }

    /** Starts a scanner thread; what it throws is kept for main to report. */
    private static Thread scanner(Runnable work, AtomicReference<RuntimeException> failed) {
        Thread t = new Thread(() -> {
            try {
                work.run();
            } catch (RuntimeException e) {
                failed.compareAndSet(null, e);
            }
        });
        t.start();
        return t;
    }
}
