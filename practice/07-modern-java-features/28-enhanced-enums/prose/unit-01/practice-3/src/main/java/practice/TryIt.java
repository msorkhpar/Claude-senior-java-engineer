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
            TicketStore store = new TicketStore();
            store.save(4100, TicketStore.State.WAITING);
            store.save(4101, TicketStore.PreviousState.OPEN);
            store.importRow(4102, "ESCALATED");
            store.importRow(4103, null);
            store.save(4104, TicketStore.State.OPEN);
            System.out.println("ticket 4100: " + store.stateLabel(4100));
            System.out.println("ticket 4101: " + store.stateLabel(4101));
            System.out.println("ticket 4102: " + store.stateLabel(4102));
            System.out.println("ticket 4103: " + store.stateLabel(4103));
            System.out.println("open among all five: " + store.countIn(List.of(4100L, 4101L, 4102L, 4103L, 4104L), TicketStore.State.OPEN));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
