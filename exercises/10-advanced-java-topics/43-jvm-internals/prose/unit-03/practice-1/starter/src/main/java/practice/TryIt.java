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
            CompileLog log = new CompileLog(List.of(
                    "    150   21       3       cart.Price::net (44 bytes)",
                    "    160   25       4       cart.Price::net (44 bytes)",
                    "    161   21       3       cart.Price::net (44 bytes)   made not entrant",
                    "   7300   25       4       cart.Price::net (44 bytes)   made not entrant",
                    "   7310  402       3       cart.Price::net (44 bytes)",
                    "    170   30 %     4       cart.Price::loop @ 8 (90 bytes)",
                    "GC pause (young) 12ms"));
            System.out.println("compile lines read: " + log.lines().size());
            System.out.println("net tier now: " + log.currentTier("cart.Price::net"));
            System.out.println("net deoptimizations: " + log.deoptimizations("cart.Price::net"));
            System.out.println("loop tier for calls: " + log.currentTier("cart.Price::loop"));
            System.out.println("never compiled: " + log.currentTier("cart.Price::gross"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
