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
            Shipping.RateCard post = new Shipping.ParcelPost("SEK", Map.of("zone-a", 6_900L, "zone-b", 11_500L));
            Courier courier = new Courier("SEK", Map.of("zone-a", 24_000L));
            System.out.println("parcel post zone-a: " + post.rateCents("zone-a") + " " + post.currency());
            System.out.println("parcel post zone-b: " + post.rateCents("zone-b") + " " + post.currency());
            System.out.println("courier zone-a: " + courier.rateCents("zone-a") + " " + courier.currency());
            courier.rateCents("zone-a");
            courier.rateCents("zone-a");
            System.out.println("courier look-ups after three quotes: " + courier.lookUps);
            try {
                new Courier("sek", Map.of("zone-a", 24_000L));
                System.out.println("courier in \"sek\": created");
            } catch (IllegalArgumentException refused) {
                System.out.println("courier in \"sek\": refused, " + refused.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** A carrier added later, the way the freight team will add theirs. */
    static class Courier extends Shipping.CachedRateCard {
        private final Map<String, Long> table;
        int lookUps;

        Courier(String currency, Map<String, Long> table) {
            super(currency);
            this.table = table;
        }

        @Override protected long lookUp(String zone) {
            lookUps++;
            return table.get(zone);
        }
    }
}
