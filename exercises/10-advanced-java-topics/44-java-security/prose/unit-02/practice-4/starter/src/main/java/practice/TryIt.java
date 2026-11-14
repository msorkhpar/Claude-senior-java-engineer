package practice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) throws Exception {
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
            SERIALIZATION.setLevel(Level.FINE);
            CartCookie.Cart cart = new CartCookie.Cart("guest-77", new ArrayList<>(List.of(
                    new CartCookie.CartLine("SOAP-3", 4), new CartCookie.CartLine("TOWEL-1", 2))));
            System.out.println("cart back: " + CartCookie.read(CartCookie.write(cart)));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
                oos.writeObject(new CacheWarmer("catalogue"));
            }
            try {
                CartCookie.read(out.toByteArray());
            } catch (IOException refused) {
                System.out.println("forged cookie refused: " + refused.getClass().getSimpleName());
            }
            System.out.println("CacheWarmer code ran: " + warmed + " times");
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    private static final Logger SERIALIZATION = Logger.getLogger("java.io.serialization");
    static int warmed;
    static final class CacheWarmer implements Serializable {
        final String region;
        CacheWarmer(String region) { this.region = region; }
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            warmed++;
        }
    }
}
