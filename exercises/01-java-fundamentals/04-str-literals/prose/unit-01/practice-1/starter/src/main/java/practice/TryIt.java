package practice;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
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
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
            FeedDecoder decoder = new FeedDecoder();
            byte[] danish = "Rugbrød 1 kg\nFlødeboller 8 stk\n".getBytes(Charset.forName("ISO-8859-1"));
            System.out.println("ISO-8859-1: " + decoder.names(new FeedDecoder.Upload("Odense Bakery", "ISO-8859-1", danish)));
            byte[] dutch = "Cadeaubon € 10\n".getBytes(Charset.forName("windows-1252"));
            System.out.println("windows-1252: " + decoder.names(new FeedDecoder.Upload("Utrecht Gifts", "windows-1252", dutch)));
            byte[] whole = "Gâteau au café\n".getBytes(Charset.forName("UTF-8"));
            byte[] cut = Arrays.copyOf(whole, whole.length - 2);
            try {
                System.out.println("cut-off UTF-8: " + decoder.names(new FeedDecoder.Upload("Lyon Roasters", "UTF-8", cut)));
            } catch (CharacterCodingException refused) {
                System.out.println("cut-off UTF-8 refused: " + refused);
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
