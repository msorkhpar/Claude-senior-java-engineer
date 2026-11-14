package practice;

import java.util.ArrayList;
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
            DeliveryRules.Store store = new DeliveryRules.Store();
            store.setMaxSizeKb("shop.example.com", 200);
            List<String> sent = new ArrayList<>();
            DeliveryRules.Sender sender = new DeliveryRules.Sender(store::current, m -> {
                sent.add(m.id());
                if (m.id().equals("n-2")) store.block("partner.example.com");
            });
            int count = sender.sendAll(List.of(
                    new DeliveryRules.Message("n-1", "shop.example.com", 150),
                    new DeliveryRules.Message("n-2", "partner.example.com", 20),
                    new DeliveryRules.Message("n-3", "partner.example.com", 20),
                    new DeliveryRules.Message("n-4", "shop.example.com", 450),
                    new DeliveryRules.Message("n-5", "example.net", 30)));
            System.out.println("sent count: " + count);
            System.out.println("sent in order: " + sent);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
