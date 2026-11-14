package practice;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) throws Exception {
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
            SessionSweeper sessions = new SessionSweeper();
            sessions.touch("sess-k1", 5_400L);
            sessions.touch("sess-k2", 900L);
            sessions.touch("sess-k3", 7_100L);
            sessions.touch("sess-k4", 2_050L);
            Thread[] request = new Thread[1];
            List<String> gone = sessions.sweep((id, seen) -> {
                if (request[0] == null) {
                    request[0] = new Thread(() -> sessions.touch("sess-k9", 8_000L));
                    request[0].start();
                }
                return seen < 3_000L;
            });
            request[0].join(3_000);
            System.out.println("swept: " + gone.stream().sorted().toList());
            System.out.println("request during the sweep kept: " + sessions.lastSeen("sess-k9"));
            Map<String, Long> dump = sessions.snapshot();
            sessions.touch("sess-k1", 9_000L);
            sessions.touch("sess-k5", 9_100L);
            System.out.println("dump after later requests: " + new TreeMap<>(dump));
            System.out.println("sess-k1 now: " + sessions.lastSeen("sess-k1"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
