package practice;

import java.time.Duration;
import java.util.LinkedHashMap;
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
            PayrollRun run = new PayrollRun(Duration.ofSeconds(45));
            Overrides.apply(run, ordered("currency", "SEK", "threads", 2, "timeout", Duration.ofMillis(800)));
            System.out.println("after override: " + run.currency + ", " + run.threads + " threads, " + run.timeout);
            try {
                Overrides.apply(run, ordered("currency", "NOK", "VERSION", "v9"));
            } catch (IllegalArgumentException refused) {
                System.out.println("refused: " + refused.getMessage());
            }
            System.out.println("currency untouched: " + run.currency);
            System.out.println("record component writable: " + Overrides.writable(Rate.class.getDeclaredFields()[0]));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    static final class PayrollRun {
        static final String VERSION = "v1";
        private String currency = "DKK";
        private final Duration timeout;
        private int threads = 8;
        PayrollRun(Duration timeout) { this.timeout = timeout; }
    }
    record Rate(String band, int percent) {}
    private static Map<String, Object> ordered(Object... pairs) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) map.put((String) pairs[i], pairs[i + 1]);
        return map;
    }
}
