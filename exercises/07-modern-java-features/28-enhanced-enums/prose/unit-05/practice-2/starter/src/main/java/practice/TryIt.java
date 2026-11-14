package practice;

import java.util.Set;
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
            try {
                Ticket ticket = new Ticket();
                ticket.moveTo(Ticket.State.ASSIGNED);
                ticket.moveTo(Ticket.State.NEW);
                ticket.moveTo(Ticket.State.CLOSED);
                System.out.println("after assign, unassign, close as spam: " + ticket.state());
                try {
                    ticket.moveTo(Ticket.State.ASSIGNED);
                    System.out.println("reopen a closed ticket: allowed");
                } catch (IllegalStateException refused) {
                    System.out.println("reopen a closed ticket: refused, still " + ticket.state());
                }
                System.out.println("IN_PROGRESS may go to: " + Ticket.State.IN_PROGRESS.allowedNext());
                System.out.println("NEW can move to RESOLVED: " + Ticket.State.NEW.canMoveTo(Ticket.State.RESOLVED));
                Set<Ticket.State> buttons = Ticket.State.NEW.allowedNext();
                try {
                    buttons.add(Ticket.State.RESOLVED);
                } catch (UnsupportedOperationException refused) {
                    System.out.println("editing the buttons: refused");
                }
                System.out.println("NEW can move to RESOLVED afterwards: " + Ticket.State.NEW.canMoveTo(Ticket.State.RESOLVED));
            } catch (ExceptionInInitializerError failed) {
                throw (RuntimeException) failed.getCause();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
