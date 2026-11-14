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
            CatalogueLoader loader = new CatalogueLoader();
            List<CatalogueLoader.Product> products = loader.load(List.of(
                    "S100;Cloudberry jam;SEK;SE",
                    "S101;Rye crispbread;SEK;SE",
                    "S102;Fish soup;NOK;NO",
                    "S103;Pickled herring;SEK;SE"));
            System.out.println("first product: " + products.get(0));
            System.out.println("same SEK object in S100 and S103: " + (products.get(0).currency() == products.get(3).currency()));
            System.out.println("same SE object in S101 and S103: " + (products.get(1).country() == products.get(3).country()));
            System.out.println("table size: " + loader.tableSize());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
