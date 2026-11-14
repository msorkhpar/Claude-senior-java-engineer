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
            List<String> delivered = new ArrayList<>();
            Map<String, AlertService.Channel> channels = Map.of(
                    "email", m -> delivered.add("email: " + m),
                    "sms", m -> delivered.add("sms: " + m),
                    "pager", m -> delivered.add("pager: " + m));
            new AlertService(channels, Map.of("alerts.channel", "pager")).raise("replica lag 40 s on db-7");
            new AlertService(channels, Map.of("zone", "eu-1")).raise("nightly backup 25 min late");
            try {
                new AlertService(channels, Map.of("alerts.channel", "pagr")).raise("disk 98% on db-7");
            } catch (IllegalArgumentException refused) {
                System.out.println("deploy refused: " + refused.getMessage());
            }
            for (String line : delivered) System.out.println("delivered " + line);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
