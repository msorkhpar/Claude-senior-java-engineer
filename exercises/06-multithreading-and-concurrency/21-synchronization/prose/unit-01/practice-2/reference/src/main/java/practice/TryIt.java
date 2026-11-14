package practice;

import java.util.ArrayList;
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
            List<String> charges = new ArrayList<>();
            SeatMap.Gateway gateway = (card, cents) -> {
                charges.add(card + " " + cents);
                if (card.equals("card-time-out")) throw new IllegalStateException("gateway timed out");
                return !card.equals("card-declined");
            };
            SeatMap seats = new SeatMap(List.of("B5", "B6", "B7"), 7_250, gateway);
            System.out.println("B5 to card-1234: " + seats.book("B5", "card-1234"));
            System.out.println("B5 again to card-5678: " + seats.book("B5", "card-5678"));
            System.out.println("B6 to card-declined: " + seats.book("B6", "card-declined") + ", B6 now " + seats.state("B6"));
            try {
                seats.book("B7", "card-time-out");
            } catch (IllegalStateException failed) {
                System.out.println("B7 charge failed: " + failed.getMessage() + ", B7 now " + seats.state("B7"));
            }
            System.out.println("B7 to card-8888: " + seats.book("B7", "card-8888"));
            System.out.println("states: B5 " + seats.state("B5") + ", B6 " + seats.state("B6") + ", B7 " + seats.state("B7"));
            System.out.println("charges: " + charges);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
