package practice;

import java.util.ArrayList;
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
            List<Object> fromYaml = new ArrayList<>(List.of("https://store.example.org", "https://ops.example.org"));
            String[] fromArgs = {"https://edge.example.org"};
            Object[] fromPlugin = {"https://legacy.example.org", "https://media.example.org"};
            System.out.println("yaml list: " + new Settings(Map.of("allowed-origins", fromYaml)).strings("allowed-origins"));
            System.out.println("command line: " + new Settings(Map.of("allowed-origins", fromArgs)).strings("allowed-origins"));
            System.out.println("old plugin: " + new Settings(Map.of("allowed-origins", fromPlugin)).strings("allowed-origins"));
            List<Object> withPort = new ArrayList<>(List.of("https://store.example.org", 9090));
            try {
                new Settings(Map.of("allowed-origins", withPort)).strings("allowed-origins");
                System.out.println("list with a port: accepted");
            } catch (IllegalArgumentException refused) {
                System.out.println("list with a port: refused, " + refused.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
