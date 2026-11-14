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
            Checkout.Rule nextDay = Checkout.nextDay();
            System.out.println("NL-2011, 3 kg, next day: " + nextDay.accepts(new Checkout.Parcel("NL", "NL-2011", 3_000)));
            System.out.println("NL-2011, 7 kg, next day: " + nextDay.accepts(new Checkout.Parcel("NL", "NL-2011", 7_000)));
            System.out.println("DE-10115, next day: " + nextDay.accepts(new Checkout.Parcel("DE", "DE-10115", 500)));
            System.out.println("no postcode, next day: " + nextDay.accepts(new Checkout.Parcel("NL", null, 500)));
            Checkout.Rule heavyOrGerman = Checkout.atMostGrams(1_000).or(parcel -> parcel.country().equals("DE"));
            System.out.println("light or German, DE 9 kg: " + heavyOrGerman.accepts(new Checkout.Parcel("DE", "DE-80331", 9_000)));
            System.out.println("price of 8000 cents at 25% off plus 450 fee: " + Checkout.price(25, 450).apply(8_000));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
