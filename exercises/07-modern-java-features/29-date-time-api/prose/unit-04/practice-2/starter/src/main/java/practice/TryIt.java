package practice;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) {
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
            StepClock clock = new StepClock("2027-04-02T09:30:00.555666777Z");
            PageStore store = new PageStore(clock);
            PageStore.Version created = store.create("faq", "first draft");
            System.out.println("stamp handed out: " + created.updatedAt());
            clock.advance(Duration.ofMillis(400));
            PageStore.Version saved = store.save("faq", "second draft", created.updatedAt());
            System.out.println("edit from the created stamp saved, new stamp: " + saved.updatedAt());
            clock.advance(Duration.ofMillis(200));
            try {
                store.save("faq", "stale edit", created.updatedAt());
                System.out.println("edit from the replaced version: saved");
            } catch (PageStore.EditConflict conflict) {
                System.out.println("edit from the replaced version: " + conflict.getMessage());
            }
            System.out.println("page text now: " + store.read("faq").text());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** A clock moved by hand; its readings carry nanoseconds. */
    static final class StepClock extends Clock {
        private Instant now;

        StepClock(String start) { now = Instant.parse(start); }

        void advance(Duration d) { now = now.plus(d); }

        @Override public Instant instant() { return now; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { throw new UnsupportedOperationException(); }
    }
}
