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
            List<String> fromFeed = new ArrayList<>(List.of("bag-1", "bag-2"));
            Shipment s = new Shipment("ES-41092-L", "Valencia", 2, fromFeed);
            fromFeed.set(0, "bag-9");
            System.out.println("routing code: " + s.routingCode());
            System.out.println("labels after the feed changed its list: " + s.labels());
            String[][] bad = {{"ES-41093-L", null, "2"}, {"  ", "Bilbao", "1"}, {"ES-41094-L", "Bilbao", "0"}};
            for (String[] f : bad) {
                try {
                    new Shipment(f[0], f[1], Integer.parseInt(f[2]), List.of("bag-1"));
                    System.out.println("accepted: " + String.join(", ", java.util.Arrays.asList(f)));
                } catch (NullPointerException | IllegalArgumentException refused) {
                    System.out.println("refused " + refused.getClass().getSimpleName() + ": " + refused.getMessage());
                }
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
