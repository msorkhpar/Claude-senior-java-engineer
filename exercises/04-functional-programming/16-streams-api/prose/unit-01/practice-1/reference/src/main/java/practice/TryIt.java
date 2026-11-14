package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
            List<Dispatcher.Courier> nearestFirst = List.of(new Dispatcher.Courier("Ada", "east"),
                    new Dispatcher.Courier("Bea", "east"), new Dispatcher.Courier("Cal", "east"),
                    new Dispatcher.Courier("Dee", "east"), new Dispatcher.Courier("Eli", "east"));
            Set<String> free = Set.of("Cal", "Eli");
            List<String> asked = new ArrayList<>();
            Dispatcher dispatcher = new Dispatcher(courier -> {
                asked.add(courier.name());
                return free.contains(courier.name());
            });
            System.out.println("job goes to: " + dispatcher.assign(nearestFirst));
            System.out.println("service asked about: " + asked);
            asked.clear();
            System.out.println("promise fast delivery: " + dispatcher.canPromiseFastDelivery(nearestFirst));
            System.out.println("service asked about: " + asked);
            System.out.println("nobody in the zone: " + dispatcher.assign(List.of()));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
