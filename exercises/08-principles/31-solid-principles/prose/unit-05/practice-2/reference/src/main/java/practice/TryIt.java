package practice;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
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
            Path jarRoot = Files.createTempDirectory("channels");
            Path listing = jarRoot.resolve("META-INF/services/" + Channels.Notifier.class.getName());
            Files.createDirectories(listing.getParent());
            Files.writeString(listing, Pager.class.getName() + "\n" + Slack.class.getName() + "\n");
            try (URLClassLoader plugins = new URLClassLoader(new URL[] {jarRoot.toUri().toURL()},
                    TryIt.class.getClassLoader())) {
                Channels.Notifier chat = Channels.forChannel("chat", plugins);
                System.out.println("chat notifier: " + chat.getClass().getSimpleName());
                System.out.println("chat delivers: " + chat.deliver("oncall", "queue depth 4800"));
                Channels.Notifier pager = Channels.forChannel("pager", plugins);
                System.out.println("pager delivers: " + pager.deliver("night-shift", "db replica lag"));
                try {
                    Channels.forChannel("chta", plugins);
                } catch (IllegalArgumentException refused) {
                    System.out.println("chta: " + refused.getMessage());
                }
            } finally {
                Files.delete(listing);
                Files.delete(listing.getParent());
                Files.delete(listing.getParent().getParent());
                Files.delete(jarRoot);
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** A paging channel, as a plug-in jar would ship it. */
    @Channels.Channel("pager")
    public static class Pager implements Channels.Notifier {
        public String deliver(String recipient, String text) { return "page " + recipient + ": " + text; }
    }

    /** A chat channel, as a plug-in jar would ship it. */
    @Channels.Channel("chat")
    public static class Slack implements Channels.Notifier {
        public String deliver(String recipient, String text) { return "chat #" + recipient + ": " + text; }
    }
}
