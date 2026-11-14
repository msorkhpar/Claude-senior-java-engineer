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
            Routing routing = new Routing(List.of(refuses("fragile"), servesPrefix("75", "hub-PAR"), servesPrefix("3", "hub-TLS")));
            System.out.println("parcel X-101 to 75011: " + routing.route("X-101", "75011"));
            System.out.println("parcel X-102 to 31000: " + routing.route("X-102", "31000"));
            Routing.NoRouteException miss = new Routing.NoRouteException("frozen", "X-103");
            System.out.println("a refusal: " + miss.getMessage() + ", stack frames: " + miss.getStackTrace().length);
            try {
                routing.route("X-104", "67000");
            } catch (RuntimeException unroutable) {
                System.out.println("no route: " + unroutable.getMessage() + ", stack frames: " + (unroutable.getStackTrace().length > 0));
                System.out.println("last refusal: " + unroutable.getCause().getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    private static Routing.Rule refuses(String name) {
        return (parcelId, postcode) -> {
            throw new Routing.NoRouteException(name, parcelId);
        };
    }

    private static Routing.Rule servesPrefix(String prefix, String hub) {
        return (parcelId, postcode) -> {
            if (postcode.startsWith(prefix)) {
                return hub;
            }
            throw new Routing.NoRouteException("prefix-" + prefix, parcelId);
        };
    }
}
