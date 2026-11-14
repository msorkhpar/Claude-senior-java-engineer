package practice;

import java.util.Arrays;
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
            System.out.println("RUG-0210: " + CATALOG.price("RUG-0210"));
            System.out.println("find VASE-0045: " + CATALOG.find("VASE-0045"));
            System.out.println("find VASE-9: " + CATALOG.find("VASE-9"));
            Catalog.UnknownSkuException fromWishlist = fromWishlist("CHAIR-0000");
            Catalog.UnknownSkuException fromCheckout = fromCheckout("CHAIR-0000");
            System.out.println("wishlist failure: " + fromWishlist.getMessage() + ", trace names fromWishlist: "
                    + names(fromWishlist, "fromWishlist") + ", fromCheckout: " + names(fromWishlist, "fromCheckout"));
            System.out.println("checkout failure: " + fromCheckout.getMessage() + ", trace names fromCheckout: "
                    + names(fromCheckout, "fromCheckout") + ", fromWishlist: " + names(fromCheckout, "fromWishlist"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    private static final Catalog CATALOG = new Catalog(Map.of("RUG-0210", 15_900L, "VASE-0045", 2_450L));

    private static Catalog.UnknownSkuException fromWishlist(String sku) {
        try {
            CATALOG.price(sku);
            return null;
        } catch (Catalog.UnknownSkuException e) {
            return e;
        }
    }

    private static Catalog.UnknownSkuException fromCheckout(String sku) {
        try {
            CATALOG.price(sku);
            return null;
        } catch (Catalog.UnknownSkuException e) {
            return e;
        }
    }

    private static boolean names(Throwable t, String method) {
        return Arrays.stream(t.getStackTrace()).anyMatch(f -> f.getMethodName().equals(method));
    }
}
