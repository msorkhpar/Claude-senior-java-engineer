package practice;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
            Map<String, String> handles = Map.of("id-1", "lena", "id-2", "omar");
            Prefetch prefetch = new Prefetch(id -> Optional.ofNullable(handles.get(id)));
            List<Prefetch.Playlist> home = List.of(
                    new Prefetch.Playlist("Jazz Nights", "id-2", List.of("s-1", "s-2", "s-3")),
                    new Prefetch.Playlist("Old Account Mix", "id-gone", List.of("s-4")),
                    new Prefetch.Playlist("Coffee", "id-1", List.of("s-5", "s-6")),
                    new Prefetch.Playlist("More Jazz", "id-2", List.of()));
            System.out.println("tracks: " + prefetch.trackCount(home));
            System.out.println("owners: " + prefetch.ownerHandles(home));
            System.out.println("banner: " + prefetch.banner(home));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
