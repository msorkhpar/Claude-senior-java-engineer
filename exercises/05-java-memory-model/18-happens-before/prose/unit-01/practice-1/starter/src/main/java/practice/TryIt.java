package practice;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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
            CountDownLatch stuck = new CountDownLatch(1);
            try {
                ShardReport.Shard quick = add -> {
                    add.accept(1_200);
                    add.accept(350);
                };
                ShardReport.Shard hanging = add -> {
                    add.accept(8_000);
                    stuck.await();
                };
                ShardReport.Shard small = add -> add.accept(75);
                long started = System.nanoTime();
                ShardReport.Report report = ShardReport.run(List.of(quick, hanging, small), Duration.ofSeconds(1));
                long tookMs = (System.nanoTime() - started) / 1_000_000;
                System.out.println("total cents: " + report.totalCents());
                System.out.println("unfinished shards: " + report.unfinished());
                System.out.println("ready within 2 s: " + (tookMs < 2_000));
            } finally {
                stuck.countDown();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
