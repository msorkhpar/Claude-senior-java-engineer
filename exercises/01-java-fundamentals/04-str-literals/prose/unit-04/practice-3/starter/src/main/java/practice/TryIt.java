package practice;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
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
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            TagIndex index = new TagIndex();
            index.add("Kotlin");
            index.add("KOTLIN");
            index.add("TITLE");
            index.add("title");
            index.add("CAF\u00C9");
            index.add("cafe\u0301");
            System.out.println("tags on a Turkish server: " + index.counts());
            System.out.println("posts for kOtLiN: " + index.postsTagged("kOtLiN"));
            System.out.println("Tag(Kotlin) equals Tag(KOTLIN): " + new TagIndex.Tag("Kotlin").equals(new TagIndex.Tag("KOTLIN")));
            System.out.println("same hash code: " + (new TagIndex.Tag("Kotlin").hashCode() == new TagIndex.Tag("KOTLIN").hashCode()));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
