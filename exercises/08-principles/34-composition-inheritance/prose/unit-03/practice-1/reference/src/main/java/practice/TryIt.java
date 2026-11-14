package practice;

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
            for (SeatHolds store : new SeatHolds[] {SeatHolds.plain(), SeatHolds.sorted(), SeatHolds.shared()}) {
                store.hold("C4-11", "Alex Example");
                store.hold("C4-12", "Sam Sample");
                store.hold("C4-11", "Lee Placeholder");
                String nullGuest;
                try {
                    store.hold("C4-13", null);
                    nullGuest = "accepted";
                } catch (NullPointerException refused) {
                    nullGuest = "refused";
                }
                Map<String, String> listed = store.holds();
                for (String seat : listed.keySet()) store.release(seat);
                System.out.println("C4-11 " + listed.get("C4-11") + ", null guest " + nullGuest
                        + ", listed " + listed.size() + ", left after releasing them " + store.holds().size());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
