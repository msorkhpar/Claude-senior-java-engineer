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
            GiftCard holiday = new GiftCard("GC-9101");
            holiday.load(20_000);
            GiftCard coffee = new GiftCard("GC-9102");
            coffee.load(1_500);
            holiday.transferTo(coffee, 6_000);
            System.out.println("after moving 6,000: holiday " + holiday.balance() + ", coffee " + coffee.balance());
            GiftCard full = new GiftCard("GC-9103");
            full.load(GiftCard.CAP - 500);
            try {
                holiday.transferTo(full, 2_000);
                System.out.println("2,000 onto a nearly full card: moved");
            } catch (GiftCard.RefusedException refused) {
                System.out.println("2,000 onto a nearly full card: refused, " + refused.getMessage());
            }
            System.out.println("balances: holiday " + holiday.balance() + ", full " + full.balance());
            coffee.cancel();
            try {
                holiday.transferTo(coffee, 1_000);
                System.out.println("1,000 onto a cancelled card: moved");
            } catch (GiftCard.RefusedException refused) {
                System.out.println("1,000 onto a cancelled card: refused, " + refused.getMessage());
            }
            System.out.println("balances: holiday " + holiday.balance() + ", coffee " + coffee.balance());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
