package practice;

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
            Map<String, StartOptionsCheck.History> inventory = Map.of(
                    "UseLegacyHeapDump", new StartOptionsCheck.History(13, 17, 21),
                    "PrintStackSampling", new StartOptionsCheck.History(15, 21, 26),
                    "YoungBatchFactor", new StartOptionsCheck.History(18, null, null));
            List<String> script = List.of("-Xms1g", "-XX:+UseLegacyHeapDump", "-XX:-PrintStackSampling",
                    "-XX:YoungBatchFactor=8", "-XX:+UseZGC");
            for (int release : new int[] {11, 17, 21}) {
                StartOptionsCheck.Result result = StartOptionsCheck.check(script, inventory, release);
                System.out.println("Java " + release + ": starts " + result.starts() + ", deprecated " + result.deprecated()
                        + ", obsolete " + result.obsolete() + ", expired " + result.expired());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
