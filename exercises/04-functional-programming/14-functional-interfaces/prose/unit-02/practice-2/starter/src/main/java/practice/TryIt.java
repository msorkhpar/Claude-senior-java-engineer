package practice;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
            int[] described = {0};
            ShipmentChecks.Shipment boxed = new ShipmentChecks.Shipment("SH-900", "Parcel Co", List.of("a", "b"));
            ShipmentChecks.Shipment bare = new ShipmentChecks.Shipment("SH-901", null, List.of("c", "d", "e"));
            ShipmentChecks checks = new ShipmentChecks(Map.of(boxed.id(), boxed, bare.id(), bare),
                    s -> {
                        described[0]++;
                        return s.id() + " (" + s.parcels().size() + " parcels) has no carrier";
                    },
                    id -> new NoSuchElementException("no shipment " + id));
            for (int i = 0; i < 1000; i++) {
                checks.carrierOf(checks.find("SH-900"));
            }
            System.out.println("carrier of SH-900: " + checks.carrierOf(boxed));
            System.out.println("descriptions after 1000 good scans: " + described[0]);
            try {
                checks.carrierOf(bare);
            } catch (NullPointerException missing) {
                System.out.println("SH-901 failed: " + missing.getMessage());
            }
            try {
                checks.find("SH-404");
            } catch (NoSuchElementException unknown) {
                System.out.println("lookup failed: " + unknown.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
