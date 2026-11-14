package practice;

import java.time.Instant;
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
            Instant eight = Instant.parse("2026-05-04T08:00:00Z");
            JobQueue queue = new JobQueue();
            queue.submit(new JobQueue.Job("backup", eight.plusSeconds(60L * 45)));
            queue.submit(new JobQueue.Job("reindex", eight.plusSeconds(60L * 10)));
            queue.submit(new JobQueue.Job("report", eight.plusSeconds(60L * 25)));
            queue.submit(new JobQueue.Job("cleanup", eight.plusSeconds(60L * 5)));
            System.out.println("next up (2): " + queue.upcoming(2).stream().map(JobQueue.Job::name).toList());
            System.out.println("next up (9): " + queue.upcoming(9).stream().map(JobQueue.Job::name).toList());
            System.out.println("still queued: " + queue.size());
            System.out.println("worker takes: " + queue.next().map(JobQueue.Job::name).orElse("nothing"));
            System.out.println("empty queue: " + new JobQueue().upcoming(3));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
