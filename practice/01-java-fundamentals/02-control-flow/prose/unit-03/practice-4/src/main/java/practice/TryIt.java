package practice;

import java.util.function.IntPredicate;
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
            System.out.println(UploadLimit.hint(10_000, gateway(10_000, 2_048)));
            System.out.println(UploadLimit.hint(500, gateway(500, 0)));
            System.out.println(UploadLimit.hint(Integer.MAX_VALUE, gateway(Integer.MAX_VALUE, 1_234_567_890)));
            System.out.println(UploadLimit.hint(777, gateway(777, 777)));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** A gateway that accepts sizes up to limit and refuses probes outside 1..maxBytes or past 64. */
    private static IntPredicate gateway(int maxBytes, int limit) {
        int[] probes = new int[1];
        return size -> {
            if (size < 1 || size > maxBytes) throw new IllegalArgumentException("probed " + size + " bytes");
            if (++probes[0] > 64) throw new IllegalStateException("more than 64 probes");
            return size <= limit;
        };
    }
}
