package practice;

import java.time.Instant;
import java.util.Locale;
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
            System.out.println("values: " + Session.Attribute.values());
            System.out.println("valueOf(\"cartId\") is CART_ID: " + (Session.Attribute.valueOf("cartId") == Session.Attribute.CART_ID));
            Session session = new Session();
            session.put(Session.Attribute.USER_ID, 88_200_045L);
            session.put(Session.Attribute.CART_ID, "cart-77-x");
            session.put(Session.Attribute.LOCALE, Locale.GERMANY);
            session.put(Session.Attribute.LAST_SEEN, Instant.parse("2030-06-01T12:00:00Z"));
            Session restored = Session.restore(session.save());
            System.out.println("restored userId: " + restored.get(Session.Attribute.USER_ID));
            System.out.println("restored cartId: " + restored.get(Session.Attribute.CART_ID));
            System.out.println("restored locale: " + restored.get(Session.Attribute.LOCALE));
            System.out.println("restored lastSeen: " + restored.get(Session.Attribute.LAST_SEEN));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
