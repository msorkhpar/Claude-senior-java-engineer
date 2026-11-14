package practice;

import java.util.List;
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
            AlertRouter router = new AlertRouter();
            System.out.println("disk web-primary-1 at 93%: " + router.channelFor(new AlertRouter.DiskAlert("web-primary-1", 93)));
            System.out.println("disk web-primary-1 at 64%: " + router.channelFor(new AlertRouter.DiskAlert("web-primary-1", 64)));
            System.out.println("replica disk reports-replica-2 at 99%: "
                    + router.channelFor(new AlertRouter.ReplicaDiskAlert("reports-replica-2", 99)));
            System.out.println("latency billing 2500 ms: " + router.channelFor(new AlertRouter.LatencyAlert("billing", 2_500)));
            System.out.println("latency profile 310 ms: " + router.channelFor(new AlertRouter.LatencyAlert("profile", 310)));
            System.out.println("batch: " + router.routeAll(List.of(new AlertRouter.DiskAlert("queue-7", 91),
                    new AlertRouter.LatencyAlert("profile", 120), "cron job late")));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
