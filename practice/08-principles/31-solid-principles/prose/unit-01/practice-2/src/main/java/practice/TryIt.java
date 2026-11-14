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
            Fares.FarePolicy summer = new Fares.FarePolicy(10_000);
            Fares.FarePolicy winter = new Fares.FarePolicy(5_000);
            Fares.Trip couple = new Fares.Trip(2, 0, true, true);
            Fares.Trip scouts = new Fares.Trip(4, 6, false, false);
            System.out.println("couple with car, peak, summer: machine " + new Fares.TicketMachine(summer).charge(couple) + ", web " + new Fares.WebShop(summer).quote(couple));
            System.out.println("scout group, summer: machine " + new Fares.TicketMachine(summer).charge(scouts) + ", web " + new Fares.WebShop(summer).quote(scouts));
            System.out.println("couple with car, peak, winter: machine " + new Fares.TicketMachine(winter).charge(couple) + ", web " + new Fares.WebShop(winter).quote(couple));
            System.out.println("scout group, winter: machine " + new Fares.TicketMachine(winter).charge(scouts) + ", web " + new Fares.WebShop(winter).quote(scouts));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
