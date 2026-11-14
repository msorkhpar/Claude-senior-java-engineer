package practice;

import java.util.ArrayList;
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
            List<String> pages = new ArrayList<>();
            TicketRouter router = new TicketRouter(pages);
            System.out.println("H-1 refund: " + router.route(new TicketRouter.Ticket("H-1", TicketRouter.Category.REFUND, 2)));
            System.out.println("H-2 outage sev 2: " + router.route(new TicketRouter.Ticket("H-2", TicketRouter.Category.OUTAGE, 2)));
            System.out.println("H-3 outage sev 4: " + router.route(new TicketRouter.Ticket("H-3", TicketRouter.Category.OUTAGE, 4)));
            System.out.println("H-4 security sev 3: " + router.route(new TicketRouter.Ticket("H-4", TicketRouter.Category.SECURITY, 3)));
            System.out.println("H-5 other: " + router.route(new TicketRouter.Ticket("H-5", TicketRouter.Category.OTHER, 1)));
            System.out.println("pages: " + pages);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
