package practice;

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
            List<Sorting.Parcel> belt = List.of(
                    new Sorting.Parcel("Q-1", new Sorting.Recipient("Ann Example", new Sorting.Address("2 Elm Row", "GL50 1AA"))),
                    new Sorting.Parcel("Q-2", new Sorting.Recipient("Bo Sample", null)),
                    new Sorting.Parcel("Q-3", null),
                    new Sorting.Parcel("Q-4", new Sorting.Recipient("Cy Test", new Sorting.Address("7 Oak Way", null))),
                    new Sorting.Parcel("Q-5", new Sorting.Recipient("Di Demo", new Sorting.Address("1 Pier", "NE1 3PQ"))));
            for (Sorting.Parcel parcel : belt) {
                System.out.println(parcel.id() + " -> " + Sorting.binFor(parcel));
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
