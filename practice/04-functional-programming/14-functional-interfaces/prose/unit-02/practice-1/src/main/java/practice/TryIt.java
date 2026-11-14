package practice;

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
            int[] loads = {0};
            Once<String> card = new Once<>(() -> {
                loads[0]++;
                if (loads[0] == 1) throw new IllegalStateException("upstream down, load " + loads[0]);
                return "rate card v" + loads[0];
            });
            System.out.println("loads after creating it: " + loads[0]);
            try {
                card.get();
            } catch (IllegalStateException outage) {
                System.out.println("first request failed: " + outage.getMessage());
            }
            System.out.println("second request: " + card.get());
            System.out.println("third request: " + card.get());
            System.out.println("loads in all: " + loads[0]);
            Once<String> noCard = new Once<>(() -> {
                loads[0]++;
                return null;
            });
            noCard.get();
            System.out.println("desk without a card: " + noCard.get() + ", loads in all: " + loads[0]);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
