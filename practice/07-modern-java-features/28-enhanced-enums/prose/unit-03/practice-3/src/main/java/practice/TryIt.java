package practice;

import java.util.Map;
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
            Shelf[] shelves = {new Shelf("S1", "fruit"), new Shelf("S4", "bread"), new Shelf("S9", "dairy")};
            Map<String, Shelf> byCode = Rejections.index(shelves, Shelf::code);
            System.out.println("S4 holds: " + byCode.get("S4").holds());
            Shelf[] clash = {new Shelf("S1", "fruit"), new Shelf("S1", "frozen")};
            try {
                Rejections.index(clash, Shelf::code);
                System.out.println("duplicate code: accepted");
            } catch (IllegalArgumentException refused) {
                System.out.println("duplicate code refused: " + refused.getMessage());
            }
            System.out.println("AC04: " + Rejections.Reason.fromCode("AC04"));
            System.out.println("RR04: " + Rejections.Reason.fromCode("RR04"));
            System.out.println("XT99: " + Rejections.Reason.fromCode("XT99"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    record Shelf(String code, String holds) {}
}
