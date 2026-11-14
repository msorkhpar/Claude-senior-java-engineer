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
            PageRequest asked = PageRequest.fromQuery(" 3", "50 ");
            System.out.println("query page 3, size 50: " + asked);
            System.out.println("saved search, page 9: " + new PageRequest(9));
            System.out.println("scrolled on: " + asked.next());
            PageRequest huge = PageRequest.fromQuery("1", "10000");
            System.out.println("size 10000 served as: " + huge);
            System.out.println("same request as size 200: " + huge.equals(new PageRequest(1, PageRequest.MAX_SIZE)));
            try {
                PageRequest.fromQuery("-1", "20");
                System.out.println("page -1: accepted");
            } catch (IllegalArgumentException refused) {
                System.out.println("page -1: refused, " + refused.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
