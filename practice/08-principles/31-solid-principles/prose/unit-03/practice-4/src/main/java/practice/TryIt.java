package practice;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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
            System.out.println("small bounded queue: " + TaskQueueContract.breaches(SmallBounded::new));
            System.out.println("queue that throws when full: " + TaskQueueContract.breaches(LoudWhenFull::new));
            System.out.println("queue that drops null quietly: " + TaskQueueContract.breaches(DropsNull::new));
            System.out.println("rejectsNull on the small bounded queue: " + TaskQueueContract.rejectsNull(new SmallBounded()));
            System.out.println("offerNeverThrows on the loud queue: " + TaskQueueContract.offerNeverThrows(new LoudWhenFull()));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** Holds 5 items and says false after that. */
    static final class SmallBounded implements TaskQueueContract.TaskQueue<String> {
        private final LinkedBlockingQueue<String> items = new LinkedBlockingQueue<>(5);
        public boolean offer(String item) { return items.offer(item); }
        public String poll() { return items.poll(); }
    }

    /** Holds 8 items and throws IllegalStateException after that. */
    static final class LoudWhenFull implements TaskQueueContract.TaskQueue<String> {
        private final ArrayBlockingQueue<String> items = new ArrayBlockingQueue<>(8);
        public boolean offer(String item) { return items.add(item); }
        public String poll() { return items.poll(); }
    }

    /** Answers false for null instead of throwing. */
    static final class DropsNull implements TaskQueueContract.TaskQueue<String> {
        private final LinkedBlockingQueue<String> items = new LinkedBlockingQueue<>();
        public boolean offer(String item) { return item != null && items.offer(item); }
        public String poll() { return items.poll(); }
    }
}
