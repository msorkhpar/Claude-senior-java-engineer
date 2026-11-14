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
            Escalation desk = new Escalation(new PrintingActions());
            System.out.println("severity 1:");
            desk.report("payments-error-rate", 1);
            System.out.println("severity 3:");
            desk.report("backup-lag", 3);
            try {
                desk.report("misconfigured-probe", 0);
            } catch (IllegalArgumentException refused) {
                System.out.println("severity 0 refused: " + refused.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** Prints each action as the desk takes it. */
    static class PrintingActions implements Escalation.Actions {
        @Override public void pageOnCall() { System.out.println("  page on-call"); }
        @Override public void postToChannel() { System.out.println("  post to channel"); }
        @Override public void openTicket() { System.out.println("  open ticket"); }
        @Override public void log() { System.out.println("  write log"); }
    }
}
