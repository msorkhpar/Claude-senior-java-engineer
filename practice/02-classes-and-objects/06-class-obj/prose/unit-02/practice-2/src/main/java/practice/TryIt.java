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
            DiskAlarm.Feed feed = new DiskAlarm.Feed();
            feed.publish("/data", 95);
            feed.publish("/logs", 30);
            DiskAlarm alarm = DiskAlarm.watch(feed, "/data", 85);
            System.out.println("raised at start: " + alarm.raised());
            feed.publish("/data", 70);
            feed.publish("/logs", 99);
            feed.publish("/data", 88);
            System.out.println("raised after three readings: " + alarm.raised());
            System.out.println("listeners: " + feed.listeners());
            try {
                DiskAlarm.watch(feed, "/logs", 100);
                System.out.println("limit 100: accepted");
            } catch (IllegalArgumentException refused) {
                System.out.println("limit 100: refused, " + refused.getMessage());
            }
            System.out.println("listeners after the refusal: " + feed.listeners());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
