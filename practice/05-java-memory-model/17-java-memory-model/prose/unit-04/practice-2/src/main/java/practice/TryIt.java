package practice;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
            List<List<LitmusModel.Op>> flags = List.of(
                    List.of(LitmusModel.write("east", 1), LitmusModel.read("west", "e")),
                    List.of(LitmusModel.write("west", 1), LitmusModel.read("east", "w")));
            System.out.println("flags, no fence: " + sorted(LitmusModel.outcomesWithStoreBuffers(flags)));
            List<List<LitmusModel.Op>> fenced = List.of(
                    List.of(LitmusModel.write("east", 1), LitmusModel.fence(), LitmusModel.read("west", "e")),
                    List.of(LitmusModel.write("west", 1), LitmusModel.fence(), LitmusModel.read("east", "w")));
            System.out.println("flags, fenced: " + sorted(LitmusModel.outcomesWithStoreBuffers(fenced)));
            List<List<LitmusModel.Op>> handoff = List.of(
                    List.of(LitmusModel.write("data", 42), LitmusModel.write("ready", 1)),
                    List.of(LitmusModel.read("ready", "r"), LitmusModel.read("data", "d")));
            System.out.println("ready then data: " + sorted(LitmusModel.outcomesWithStoreBuffers(handoff)));
            List<List<LitmusModel.Op>> own = List.of(
                    List.of(LitmusModel.write("count", 5), LitmusModel.read("count", "mine")));
            System.out.println("own write read back: " + sorted(LitmusModel.outcomesWithStoreBuffers(own)));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    private static List<String> sorted(Set<Map<String, Integer>> outcomes) {
        return outcomes.stream().map(o -> new TreeMap<>(o).toString()).sorted().toList();
    }
}
