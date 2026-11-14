package practice;

import java.security.SecureRandom;
import java.util.Arrays;
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
            PasswordHasher thisYear = new PasswordHasher(2_500, new SecureRandom());
            PasswordHasher.Stored ana = thisYear.hash("cobalt-meadow-demo".toCharArray());
            PasswordHasher.Stored ben = thisYear.hash("cobalt-meadow-demo".toCharArray());
            System.out.println("row: " + ana.salt().length + " byte salt, " + ana.iterations() + " iterations, " + ana.hash().length + " byte hash");
            System.out.println("same password, same hash: " + Arrays.equals(ana.hash(), ben.hash()));
            System.out.println("right password: " + thisYear.verify("cobalt-meadow-demo".toCharArray(), ana));
            System.out.println("wrong password: " + thisYear.verify("cobalt-meadow-dem0".toCharArray(), ana));
            PasswordHasher nextYear = new PasswordHasher(6_000, new SecureRandom());
            System.out.println("old row after the count was raised: " + nextYear.verify("cobalt-meadow-demo".toCharArray(), ana));
            byte[] login = PasswordHasher.derive("cobalt-meadow-demo".toCharArray(), ana.salt(), ana.iterations());
            System.out.println("login service's PBKDF2 matches: " + Arrays.equals(login, ana.hash()));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
