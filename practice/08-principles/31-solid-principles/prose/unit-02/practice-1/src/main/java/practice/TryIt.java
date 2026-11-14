package practice;

import java.time.LocalDate;
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
            Shipping.Manifest manifest = new Shipping.Manifest();
            List<Shipping.Parcel> van = List.of(new Shipping.Letter(70), new Shipping.Box(4_200, 8_050), new Pallet(5_500, 120_000));
            System.out.println("letter of 70 g base: " + new Shipping.Letter(70).baseCents());
            System.out.println("box of 4,200 g base: " + new Shipping.Box(4_200, 8_050).baseCents());
            System.out.println("total postage: " + manifest.totalCents(van) + " cents");
            System.out.println("delivered by: " + manifest.deliveredBy(LocalDate.of(2027, 9, 6), van));
            System.out.println("letters only, delivered by: " + manifest.deliveredBy(LocalDate.of(2027, 9, 6), List.of(new Shipping.Letter(20))));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** A parcel kind written after Manifest: insured, eight days by rail. */
    record Pallet(long baseCents, long declaredValueCents) implements Shipping.Insured {
        public int transitDays() { return 8; }
    }
}
