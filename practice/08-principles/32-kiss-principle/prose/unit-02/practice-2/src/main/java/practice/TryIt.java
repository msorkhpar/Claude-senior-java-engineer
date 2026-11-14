package practice;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Future;
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
            try (Wallets wallets = new Wallets(Map.of("kai", 40L, "lou", 0L, "mo", 15L), () -> { })) {
                Future<Boolean> first = wallets.transfer("kai", "lou", 25);
                Future<Boolean> second = wallets.transfer("kai", "mo", 25);
                Future<Boolean> third = wallets.transfer("lou", "mo", 20);
                System.out.println("kai -> lou 25: " + first.get());
                System.out.println("kai -> mo 25: " + second.get());
                System.out.println("lou -> mo 20: " + third.get());
                System.out.println("balances: " + new TreeMap<>(wallets.balances()));
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
