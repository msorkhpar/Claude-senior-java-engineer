package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
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
            TagTally.Counts shop = TagTally.tally(List.of("retro", "gift", "retro"));
            System.out.println("small shop: " + new TreeMap<>(shop.asMap()));
            shop.addAll(TagTally.tally(List.of("gift", "eco")));
            System.out.println("after adding another shop: " + new TreeMap<>(shop.asMap()));
            List<String> catalogue = new ArrayList<>();
            for (int i = 0; i < 9_000; i++) catalogue.add(i % 3 == 0 ? "eco" : "retro");
            TagTally.Counts large = TagTally.tally(catalogue);
            System.out.println("large catalogue, eco: " + large.of("eco") + " (expected 3000)");
            System.out.println("large catalogue, retro: " + large.of("retro") + " (expected 6000)");
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
