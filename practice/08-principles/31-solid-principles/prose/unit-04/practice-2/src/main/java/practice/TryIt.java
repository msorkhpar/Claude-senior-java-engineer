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
            AuditTrails.LockedTrail trail = new AuditTrails.LockedTrail();
            trail.append("login u-912");
            trail.appendAll(List.of("debit acct-40 15000", "credit acct-63 15000"));
            System.out.println("locked trail: " + trail.entries());
            AuditTrails.AuditTrail team = new TeamTrail();
            team.appendAll(List.of("export e-7", "export e-8"));
            System.out.println("team trail: " + team.entries());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** A team's trail written against version 1: it only has append and entries. */
    static class TeamTrail implements AuditTrails.AuditTrail {
        private final List<String> lines = new java.util.ArrayList<>();
        @Override public void append(String entry) { lines.add("v1 " + entry); }
        @Override public List<String> entries() { return List.copyOf(lines); }
    }
}
