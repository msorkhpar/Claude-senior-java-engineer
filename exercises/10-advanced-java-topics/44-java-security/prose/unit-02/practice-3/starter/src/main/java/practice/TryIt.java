package practice;

import java.nio.file.Files;
import java.nio.file.Path;
import org.xml.sax.SAXException;
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
            Path dir = Files.createTempDirectory("tryit-orders");
            try {
                Files.writeString(dir.resolve("db.conf"), "db-secret-placeholder");
                Path plain = Files.writeString(dir.resolve("order-880.xml"),
                        "<order channel=\"api\"><item>BOLT-6</item><item>NUT-6</item><note>rush &amp; insure</note></order>");
                Path sneaky = Files.writeString(dir.resolve("order-881.xml"),
                        "<?xml version=\"1.0\"?>\n<!DOCTYPE order [ <!ENTITY leak SYSTEM \"db.conf\"> ]>\n"
                                + "<order channel=\"api\"><item>BOLT-6</item><note>&leak;</note></order>");
                OrderImport.Order order = new OrderImport().read(plain);
                System.out.println("plain order: " + order.channel() + " " + order.items() + " note: " + order.note());
                try {
                    System.out.println("sneaky order read as: " + new OrderImport().read(sneaky));
                } catch (SAXException refused) {
                    System.out.println("sneaky order refused: " + refused.getMessage());
                }
            } finally {
                try (var files = Files.walk(dir)) {
                    files.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
                }
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
