package practice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;
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
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            BodyDecoder decoder = new BodyDecoder();
            System.out.println("GZIP: " + decoder.codecFor("GZIP"));
            System.out.println("IDENTITY on a Turkish server: " + decoder.codecFor("IDENTITY"));
            System.out.println("no header: " + decoder.codecFor(null));
            ByteArrayOutputStream packed = new ByteArrayOutputStream();
            try (GZIPOutputStream gz = new GZIPOutputStream(packed)) {
                gz.write("{\"sensor\":\"silo-2\",\"temp\":18.5}".getBytes(StandardCharsets.UTF_8));
            }
            byte[] body = decoder.decode(new ByteArrayInputStream(packed.toByteArray()), "x-gzip").readAllBytes();
            System.out.println("decoded body: " + new String(body, StandardCharsets.UTF_8));
            try {
                System.out.println("br: " + decoder.codecFor("br"));
            } catch (IllegalArgumentException refused) {
                System.out.println("br refused: " + refused.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
