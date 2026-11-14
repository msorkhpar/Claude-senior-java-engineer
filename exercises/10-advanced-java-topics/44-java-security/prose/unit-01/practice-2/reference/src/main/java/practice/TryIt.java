package practice;

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
            char[][] handed = new char[1][];
            VaultUnlock.Vault vault = passphrase -> {
                handed[0] = passphrase;
                String seen = new String(passphrase);
                if (!seen.equals("amber-harbor-demo")) throw new IllegalArgumentException("wrong passphrase");
                return "session-" + seen.length();
            };
            char[] operator = "amber-harbor-demo\n".toCharArray();
            System.out.println("session: " + new VaultUnlock(vault).unlock(operator));
            System.out.println("operator array wiped: " + new String(operator).chars().allMatch(c -> c == 0));
            System.out.println("vault's array wiped: " + new String(handed[0]).chars().allMatch(c -> c == 0));
            char[] typo = "amber-harbour-demo\n".toCharArray();
            try {
                new VaultUnlock(vault).unlock(typo);
            } catch (IllegalArgumentException wrong) {
                System.out.println("typo: " + wrong.getMessage());
            }
            System.out.println("typo array wiped: " + new String(typo).chars().allMatch(c -> c == 0));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
