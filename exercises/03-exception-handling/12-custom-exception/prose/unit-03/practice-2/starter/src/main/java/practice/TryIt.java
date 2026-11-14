package practice;

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
            Playlist party = new Playlist();
            party.add("Stayin' Alive");
            party.add("Get Lucky");
            System.out.println("track 2: " + party.track(2));
            System.out.println("status for track 9: " + statusOf(() -> party.track(9)));
            party.lock();
            System.out.println("status for an add while locked: " + statusOf(() -> party.add("Ring My Bell")));
            party.unlock();
            Playlist screen = party.screenView();
            System.out.println("status for an add on the screen: " + statusOf(() -> screen.add("Ring My Bell")));
            try {
                party.track(5);
            } catch (Playlist.NoSuchTrackException missing) {
                System.out.println("the app caught position " + missing.position() + ": " + missing.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    private static int statusOf(Runnable call) {
        try {
            call.run();
            return 200;
        } catch (RuntimeException failed) {
            return Playlist.Api.status(failed);
        } catch (AssertionError unfinished) {
            throw new IllegalStateException(unfinished.getMessage());
        }
    }
}
