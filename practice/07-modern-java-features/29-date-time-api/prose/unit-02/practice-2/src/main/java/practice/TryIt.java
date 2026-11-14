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
            NightClock clock = new NightClock("2042-01-17T23:50:00Z");
            CarPark park = new CarPark(clock);
            CarPark.Ticket ticket = park.enter("NW-302-J");
            System.out.println("entered: " + ticket.entered());
            System.out.println("paid until: " + ticket.paidUntil());
            clock.advance(Duration.ofMinutes(20));
            System.out.println("overstayed after 20 min: " + park.overstayed(ticket));
            CarPark.Ticket paid = park.pay(ticket, Duration.ofMinutes(30));
            System.out.println("paid until after paying 30 min: " + paid.paidUntil());
            System.out.println("overstayed after paying: " + park.overstayed(paid));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** A clock moved by hand, so the night shift can be tried at any time of day. */
    static final class NightClock extends Clock {
        private Instant now;

        NightClock(String start) { this.now = Instant.parse(start); }

        void advance(Duration by) { now = now.plus(by); }

        @Override public Instant instant() { return now; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { throw new UnsupportedOperationException("instants only"); }
    }
}
