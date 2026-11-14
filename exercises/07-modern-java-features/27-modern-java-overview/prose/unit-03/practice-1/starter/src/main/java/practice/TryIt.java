package practice;

import java.io.InputStream;
import java.util.Map;
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
            byte[] thisClass;
            try (InputStream in = TryIt.class.getResourceAsStream("TryIt.class")) {
                thisClass = in.readAllBytes();
            }
            System.out.println("TryIt.class: " + ClassFileCheck.describe(thisClass));
            Map<String, byte[]> jar = Map.of(
                    "orders/Cart", header(61, 0),
                    "orders/Pricing", header(65, 0),
                    "orders/Matcher", header(66, 0xFFFF));
            System.out.println("orders/Matcher: " + ClassFileCheck.describe(jar.get("orders/Matcher")));
            System.out.println("refused by 21: " + ClassFileCheck.refused(jar, 21, false));
            System.out.println("refused by 22 with --enable-preview: " + ClassFileCheck.refused(jar, 22, true));
            System.out.println("refused by 24 with --enable-preview: " + ClassFileCheck.refused(jar, 24, true));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** The first bytes of a class file: magic, minor version, major version. */
    static byte[] header(int major, int minor) {
        return new byte[] {(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE,
                (byte) (minor >> 8), (byte) minor, (byte) (major >> 8), (byte) major};
    }
}
