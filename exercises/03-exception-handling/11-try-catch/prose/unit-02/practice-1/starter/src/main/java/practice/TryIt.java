package practice;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
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
            ScriptedCarrier carrier = new ScriptedCarrier(
                    "TRK-1001",
                    new LabelBooking.AddressRejected("house number missing"), "TRK-1002",
                    new LabelBooking.AddressRejected("house number missing"), new LabelBooking.AddressRejected("no such town"),
                    new LabelBooking.AddressRejected("house number missing"), new LabelBooking.CarrierException("504 Gateway Timeout"));
            LabelBooking booking = new LabelBooking(carrier);
            System.out.println("parcel A: " + booking.ship("PKG-A", "5 Elm Road, Leeds"));
            System.out.println("parcel B: " + booking.ship("PKG-B", "  9  oak   lane, bath "));
            System.out.println("parcel C: " + booking.ship("PKG-C", "1 Nowhere St, Atlantis"));
            System.out.println("parcel D: " + booking.ship("PKG-D", "2 Pier  Way, Hull"));
            System.out.println("addresses sent: " + carrier.addresses);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** A carrier that answers from a script: a tracking number, or an exception to throw. */
    static final class ScriptedCarrier implements LabelBooking.Carrier {
        private final Deque<Object> answers = new ArrayDeque<>();
        final List<String> addresses = new ArrayList<>();

        ScriptedCarrier(Object... answers) {
            this.answers.addAll(List.of(answers));
        }

        @Override public String book(String parcel, String address) {
            addresses.add(address);
            Object next = answers.poll();
            if (next instanceof RuntimeException e) {
                throw e;
            }
            return (String) next;
        }
    }
}
