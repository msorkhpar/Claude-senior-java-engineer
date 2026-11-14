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
            long jan1 = 1_767_225_600_000L;
            long day = 86_400_000L;
            List<Sessions.Session> seen = List.of(
                    new Sessions.Session("s-a1", "u-1", jan1),
                    new Sessions.Session("s-b7", "u-2", jan1 + 150 * day),
                    new Sessions.Session("s-c3", "u-3", jan1 + 40 * day),
                    new Sessions.Session("s-d9", "u-4", jan1 + 150 * day),
                    new Sessions.Session("s-e2", "u-5", jan1 + 95 * day));
            for (Sessions.Session s : Sessions.forAdminPage(seen)) {
                System.out.println(s.id() + " last seen day " + (s.lastSeenMillis() - jan1) / day);
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
