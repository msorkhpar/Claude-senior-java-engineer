package practice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            List<String> seen = new ArrayList<>();
            Metering.FareListener screen = new Metering.FareListener() {
                @Override public void fareChanged(String route, long cents) { seen.add(route + "=" + cents); }
                @Override public String toString() { return "price alerts for AMS-CPH"; }
            };
            Map<String, Integer> counts = new HashMap<>();
            Metering.FareListener metered = Metering.metered(screen, Metering.FareListener.class, counts);
            Metering.FareBoard board = new Metering.FareBoard();
            board.subscribe(metered);
            System.out.println("log line: subscribed " + metered);
            board.publish("AMS-CPH", 12_900);
            System.out.println("unsubscribed: " + board.unsubscribe(metered));
            board.publish("AMS-CPH", 11_400);
            System.out.println("screen saw: " + seen);
            System.out.println("counts: " + counts);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
